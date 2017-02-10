package udacity.kevin.podcastmaster.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import udacity.kevin.podcastmaster.PodcastMasterApplication;
import udacity.kevin.podcastmaster.R;
import udacity.kevin.podcastmaster.fragments.DownloadListFragment;
import udacity.kevin.podcastmaster.fragments.EpisodeDetailFragment;
import udacity.kevin.podcastmaster.fragments.EpisodeListFragment;
import udacity.kevin.podcastmaster.fragments.MyFeedsFragment;
import udacity.kevin.podcastmaster.listeners.DownloadRequestListener;
import udacity.kevin.podcastmaster.models.PMChannel;
import udacity.kevin.podcastmaster.models.PMEpisode;
import udacity.kevin.podcastmaster.services.MediaPlayerService;

public class MainActivity extends AppCompatActivity
	implements NavigationView.OnNavigationItemSelectedListener {

	private static final int STATE_PAUSED = 0;
	private static final int STATE_PLAYING = 1;
	private static final int STATE_STOPPED = 2;

	private Tracker mTracker;
	InterstitialAd mInterstitialAd;
	private final String LOG_TAG = "MainActivity";
	private final String SCREEN_NAME = "MainActivity";
	private final String SHOW_DETAIL_KEY = "SHOW_DETAIL_KEY";
	private static final long PROGRESS_UPDATE_INTERNAL = 1000;
	private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
	private EpisodeListFragment mEpisodeListFragment;
	private EpisodeDetailFragment mEpisodeDetailFragment;
	private DownloadRequestListener mDownloadRequestListener;
	private boolean masterDetailLayoutAvailable = false;
	private View detailFragmentContainer = null;
	private int mCurrentState = STATE_STOPPED;
	private MediaController mMediaController;
	private MediaBrowserCompat mMediaBrowserCompat;
	private MediaControllerCompat mMediaControllerCompat;
	private AppCompatSeekBar mSeekBar;
	private View mMediaPlayerView;
	private TextView mCurrentlyPlayingTextView;
	private TextView mCurrentDurationTextView;
	private TextView mTotalDurationTextView;
	private ImageButton mPlayControlButton;
	private boolean mCurrentDurationGreaterThanAnHour = false;
	private PlaybackStateCompat mLastPlaybackState;

	private MediaBrowserCompat.ConnectionCallback mMediaBrowserCompatConnectionCallback
		= new MediaBrowserCompat.ConnectionCallback() {

		@Override
		public void onConnected() {
			super.onConnected();
			try {
				mMediaControllerCompat = new MediaControllerCompat(MainActivity.this,
					mMediaBrowserCompat.getSessionToken());
				mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback);
				MediaControllerCompat.setMediaController(MainActivity.this, mMediaControllerCompat);
				buildTransportControls();

			} catch (RemoteException e) {
				Log.e(LOG_TAG, e.getMessage());
			}
		}
	};

	private MediaControllerCompat.Callback mMediaControllerCompatCallback =
		new MediaControllerCompat.Callback() {

			@Override
			public void onMetadataChanged(MediaMetadataCompat metadata) {
				super.onMetadataChanged(metadata);
				String feedTitle = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
				String episodeTitle = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
				mCurrentlyPlayingTextView.setText(feedTitle + ": " + episodeTitle);
				long duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
				mSeekBar.setMax((int)duration);
				mSeekBar.setProgress(0);
				mCurrentDurationGreaterThanAnHour = duration > 3600000;
				setTimerTextViewWithTimeMS(mCurrentDurationTextView, 0);
				setTimerTextViewWithTimeMS(mTotalDurationTextView, (int)duration);
			}

			@Override
			public void onSessionEvent(String event, Bundle extras) {
				super.onSessionEvent(event, extras);
			}

			@Override
			public void onPlaybackStateChanged(PlaybackStateCompat state) {
				super.onPlaybackStateChanged(state);
				if (state == null) {
					return;
				}
				mLastPlaybackState = state;
				switch (state.getState()) {
					case PlaybackStateCompat.STATE_PLAYING: {
						long duration = MediaControllerCompat.getMediaController(MainActivity.this).getMetadata()
							.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
						Log.d(LOG_TAG, "" + duration);
						if (mMediaPlayerView.getVisibility() != View.VISIBLE) {
							mMediaPlayerView.setVisibility(View.VISIBLE);
						}
						mCurrentState = STATE_PLAYING;
						scheduleSeekbarUpdate();
						mPlayControlButton.setImageDrawable(getDrawable(R.drawable.ic_pause_white_24dp));
						break;
					}
					case PlaybackStateCompat.STATE_PAUSED: {
						stopSeekbarUpdate();
						mCurrentState = STATE_PAUSED;
						mPlayControlButton.setImageDrawable(getDrawable(R.drawable.ic_play_arrow_white_24dp));
						break;
					}
					case PlaybackStateCompat.STATE_STOPPED: {
						stopSeekbarUpdate();
						mCurrentState = STATE_STOPPED;
						mPlayControlButton.setImageDrawable(getDrawable(R.drawable.ic_play_arrow_white_24dp));
						break;
					}
				}
			}
		};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mMediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this,
			MediaPlayerService.class), mMediaBrowserCompatConnectionCallback,
			null);

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
			this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		// Obtain the shared Tracker instance.
		PodcastMasterApplication application = (PodcastMasterApplication) getApplication();
		mTracker = application.getDefaultTracker();

		mInterstitialAd = new InterstitialAd(this);
		mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");

		requestNewInterstitial();
		mInterstitialAd.setAdListener(new AdListener() {
			@Override
			public void onAdClosed() {
				super.onAdClosed();
				if (mDownloadRequestListener != null) {
					mDownloadRequestListener.onBeginDownload();
					mDownloadRequestListener = null;
				}
				requestNewInterstitial();
			}
		});

		detailFragmentContainer = findViewById(R.id.fragment_container_detail);
		if (detailFragmentContainer != null) {
			masterDetailLayoutAvailable = true;
			if (savedInstanceState != null && savedInstanceState.getBoolean(SHOW_DETAIL_KEY, false)) {
				detailFragmentContainer.setVisibility(View.VISIBLE);
			}
		}


		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment fragmentCheck = masterDetailLayoutAvailable ?
			fragmentManager.findFragmentById(R.id.fragment_container_master) :
			fragmentManager.findFragmentById(R.id.fragment_container);

		if (fragmentCheck == null) {
			setTitle(getString(R.string.menu_drawer_my_feeds));
			MyFeedsFragment myFeedsFragment = new MyFeedsFragment();
			int containerIDToAddTo =
				masterDetailLayoutAvailable ? R.id.fragment_container_master : R.id.fragment_container;
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction
				.add(containerIDToAddTo, myFeedsFragment, MyFeedsFragment.FRAGMENT_TAG);
			fragmentTransaction.commit();
		}

		mSeekBar = (AppCompatSeekBar) findViewById(R.id.seek_bar_media);
		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					MediaControllerCompat.getMediaController(MainActivity.this)
						.getTransportControls().seekTo(progress);
				}
				setTimerTextViewWithTimeMS(mCurrentDurationTextView, progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				stopSeekbarUpdate();
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				scheduleSeekbarUpdate();
			}
		});

		mCurrentlyPlayingTextView = (TextView) findViewById(R.id.text_view_currently_playing);
		mCurrentDurationTextView = (TextView) findViewById(R.id.text_view_current_duration);
		mTotalDurationTextView = (TextView) findViewById(R.id.text_view_total_duration);
		mPlayControlButton = (ImageButton) findViewById(R.id.button_play_control);
		mMediaPlayerView = findViewById(R.id.view_media_player);
		mPlayControlButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCurrentState == STATE_PLAYING) {
					MediaControllerCompat.getMediaController(MainActivity.this)
						.getTransportControls().pause();
				} else {
					MediaControllerCompat.getMediaController(MainActivity.this)
						.getTransportControls().play();
				}
			}
		});
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!mMediaBrowserCompat.isConnected()) {
			mMediaBrowserCompat.connect();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (masterDetailLayoutAvailable) {
			if (findViewById(R.id.fragment_container_detail).getVisibility() == View.VISIBLE) {
				outState.putBoolean(SHOW_DETAIL_KEY, true);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Track the screen
		mTracker.setScreenName(SCREEN_NAME);
		mTracker.send(new HitBuilders.ScreenViewBuilder().build());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// (see "stay in sync with the MediaSession")
		if (MediaControllerCompat.getMediaController(MainActivity.this) != null) {
			MediaControllerCompat.getMediaController(MainActivity.this)
				.unregisterCallback(mMediaControllerCompatCallback);
		}
		mMediaBrowserCompat.disconnect();
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();

		if (id == R.id.nav_my_feeds || id == R.id.my_downloads) {
			layoutForMenuSelection(id);
		}

		// Debug stuff only
		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = null;
		if (id == R.id.nav_debug_copy_lore) {
			clip = ClipData.newPlainText("lore rss url", "http://lorepodcast.libsyn.com/rss");
		} else if (id == R.id.nav_debug_copy_tal) {
			clip = ClipData.newPlainText("tal rss url", "http://feed.thisamericanlife.org/talpodcast.xml");
		} else if (id == R.id.nav_debug_copy_iiwy) {
			clip = ClipData.newPlainText("iiwy rss url", "http://feeds.feedburner.com/soundcloud/mudw.xml");
		} else if (id == R.id.nav_debug_copy_serial) {
			clip = ClipData.newPlainText("serial rss url", "http://feeds.serialpodcast.org/serialpodcast.xml");
		} else if (id == R.id.nav_debug_copy_fresh_air) {
			clip = ClipData.newPlainText("fresh air rss url", "https://www.npr.org/rss/podcast.php?id=381444908");
		} else if (id == R.id.nav_debug_copy_99pi) {
			clip = ClipData.newPlainText("99pi rss url", "http://feeds.99percentinvisible.org/99percentinvisible.xml");
		} else if (id == R.id.nav_debug_copy_npr_hourly) {
			clip = ClipData.newPlainText("NPR Hourly rss url", "https://www.npr.org/rss/podcast.php?id=500005");
		}

		if (clip != null) {
			clipboard.setPrimaryClip(clip);
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	private void requestNewInterstitial() {
		AdRequest adRequest = new AdRequest.Builder()
			.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
			.build();

		mInterstitialAd.loadAd(adRequest);
	}

	public void channelSelected(PMChannel pmChannel) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		mEpisodeListFragment = new EpisodeListFragment();
		Bundle fragmentBundle = new Bundle();
		fragmentBundle.putParcelable(EpisodeListFragment.BUNDLE_KEY_CHANNEL_PARCELABLE, pmChannel);
		mEpisodeListFragment.setArguments(fragmentBundle);
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		if (masterDetailLayoutAvailable) {
			// Clear backstack if possible
			clearBackstack();
			detailFragmentContainer.setVisibility(View.VISIBLE);
			fragmentTransaction.replace(R.id.fragment_container_detail,
				mEpisodeListFragment, EpisodeListFragment.FRAGMENT_TAG);
		} else {
			fragmentTransaction.replace(R.id.fragment_container,
				mEpisodeListFragment, EpisodeListFragment.FRAGMENT_TAG);
			fragmentTransaction.addToBackStack(null);
		}
		fragmentTransaction.commit();
	}

	public void episodeSelected(PMEpisode pmEpisode, PMChannel pmChannel, boolean addToBackstack) {
		// Todo: Handle tablet layout
		FragmentManager fragmentManager = getSupportFragmentManager();
		mEpisodeDetailFragment = new EpisodeDetailFragment();
		Bundle fragmentBundle = new Bundle();
		fragmentBundle.putParcelable(EpisodeDetailFragment.BUNDLE_KEY_EPISODE_PARCELABLE, pmEpisode);
		fragmentBundle.putParcelable(EpisodeDetailFragment.BUNDLE_KEY_CHANNEL_PARCELABLE, pmChannel);
		mEpisodeDetailFragment.setArguments(fragmentBundle);
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		if (masterDetailLayoutAvailable) {
			fragmentTransaction.replace(R.id.fragment_container_detail,
				mEpisodeDetailFragment, EpisodeDetailFragment.FRAGMENT_TAG);
			detailFragmentContainer.setVisibility(View.VISIBLE);

			if (addToBackstack) {
				fragmentTransaction.addToBackStack(null);
			}
		} else {
			fragmentTransaction.replace(R.id.fragment_container,
				mEpisodeDetailFragment, EpisodeDetailFragment.FRAGMENT_TAG);
			fragmentTransaction.addToBackStack(null);
		}

		fragmentTransaction.commit();
	}

	public void layoutForMenuSelection(int id) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment showingFragment = fragmentManager.findFragmentById(masterDetailLayoutAvailable ?
			R.id.fragment_container_master : R.id.fragment_container);
		if (showingFragment instanceof MyFeedsFragment && id == R.id.nav_my_feeds ||
			showingFragment instanceof DownloadListFragment && id == R.id.my_downloads) {
			return;
		}

		Fragment fragmentToDisplay;
		String fragmentTag;
		clearBackstack();
		if (id == R.id.nav_my_feeds) {
			setTitle(getString(R.string.menu_drawer_my_feeds));
			fragmentToDisplay = new MyFeedsFragment();
			fragmentTag = MyFeedsFragment.FRAGMENT_TAG;
		} else {
			setTitle(getString(R.string.menu_drawer_my_downloads));
			fragmentToDisplay = new DownloadListFragment();
			fragmentTag = DownloadListFragment.FRAGMENT_TAG;
		}
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		if (masterDetailLayoutAvailable) {
			fragmentTransaction.replace(R.id.fragment_container_master, fragmentToDisplay, fragmentTag);
		} else {
			fragmentTransaction.replace(R.id.fragment_container, fragmentToDisplay, fragmentTag);
		}
		fragmentTransaction.commit();

		if (masterDetailLayoutAvailable) {
			detailFragmentContainer.setVisibility(View.GONE);
		}
	}

	public void playEpisode(PMChannel pmChannel, PMEpisode pmEpisode) {
		File file = new File(getFilesDir(), pmEpisode.getDownloadedMediaFilename());
		Uri uri = Uri.fromFile(file);
		Bundle bundle = new Bundle();
		bundle.putString(MediaMetadataCompat.METADATA_KEY_TITLE, pmEpisode.getTitle());
		bundle.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, pmChannel.getTitle());
		MediaControllerCompat.getMediaController(this).getTransportControls()
			.playFromUri(uri, bundle);
		MediaControllerCompat.getMediaController(this).getTransportControls().play();
	}

	public void showAd(DownloadRequestListener downloadRequestListener) {
		if (mInterstitialAd.isLoaded()) {
			mDownloadRequestListener = downloadRequestListener;
			mInterstitialAd.show();
		} else {
			downloadRequestListener.onBeginDownload();
		}
	}

	private void clearBackstack() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		if (fragmentManager.getBackStackEntryCount() > 0) {
			fragmentManager.popBackStack(fragmentManager.getBackStackEntryAt(0)
				.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
	}

	private void buildTransportControls() {
		mMediaControllerCompat = MediaControllerCompat.getMediaController(MainActivity.this);

		// Display the initial state
		MediaMetadataCompat metadata = mMediaControllerCompat.getMetadata();
		PlaybackStateCompat pbState = mMediaControllerCompat.getPlaybackState();

		// Register a Callback to stay in sync
		mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback);
	}

	private void setTimerTextViewWithTimeMS(TextView textView, int timeMS) {
		if (mCurrentDurationGreaterThanAnHour) {
			long hours = timeMS / 3600000;
			long minutes = ((timeMS % 3600000) / 60000);
			long seconds = ((timeMS % 3600000) % 60000 / 1000);
			textView
				.setText(String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds));
		} else {
			long minutes = timeMS / 60000;
			long seconds = (timeMS % 60000 / 1000);
			textView
				.setText(String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds));
		}
	}

	private void updateProgress() {
		if (MediaPlayerService.mMediaPlayer != null) {
			long currentPosition = MediaPlayerService.mMediaPlayer.getCurrentPosition();
			mSeekBar.setProgress((int) currentPosition);
			setTimerTextViewWithTimeMS(mCurrentDurationTextView, (int) currentPosition);
		}
	}

	private final Runnable mUpdateProgressTask = new Runnable() {
		@Override
		public void run() {
			updateProgress();
		}
	};

	private final ScheduledExecutorService mExecutorService =
		Executors.newSingleThreadScheduledExecutor();

	private ScheduledFuture<?> mScheduleFuture;
	private final Handler mHandler = new Handler();

	private void scheduleSeekbarUpdate() {
		stopSeekbarUpdate();
		if (!mExecutorService.isShutdown()) {
			mScheduleFuture = mExecutorService.scheduleAtFixedRate(
				new Runnable() {
					@Override
					public void run() {
						mHandler.post(mUpdateProgressTask);
					}
				}, PROGRESS_UPDATE_INITIAL_INTERVAL,
				PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
		}
	}

	private void stopSeekbarUpdate() {
		if (mScheduleFuture != null) {
			mScheduleFuture.cancel(false);
		}
	}
}
