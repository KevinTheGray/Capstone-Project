package udacity.kevin.podcastmaster.services;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import udacity.kevin.podcastmaster.R;

public class MediaPlayerService extends MediaBrowserServiceCompat implements
	MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener,
	MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener {

	private static String LOG_TAG = "MediaPlayerService";
	public static MediaPlayer mMediaPlayer;
	private MediaSessionCompat mMediaSessionCompat;

	private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if( mMediaPlayer != null && mMediaPlayer.isPlaying() ) {
				mMediaSessionCallback.onPause();
			}
		}
	};

	private MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {

		@Override
		public void onPlay() {
			super.onPlay();

			if( !successfullyRetrievedAudioFocus() ) {
				return;
			}

			mMediaSessionCompat.setActive(true);
			setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
			showPlayingNotification();
			mMediaPlayer.start();
		}

		@Override
		public void onPause() {
			super.onPause();

			if( mMediaPlayer.isPlaying() ) {
				mMediaPlayer.pause();
				setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
				showPausedNotification();
			}
		}

		@Override
		public void onStop() {
			super.onStop();
			setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
		}

		@Override
		public void onPlayFromUri(Uri uri, Bundle extras) {
			super.onPlayFromUri(uri, extras);

			try {
				mMediaPlayer.stop();
				mMediaPlayer.release();
				mMediaPlayer = null;
				initMediaPlayer();

				mMediaPlayer.setDataSource(MediaPlayerService.this, uri);
				mMediaPlayer.prepare();
				initMediaSessionMetadata(extras);
			} catch (IOException ioException) {
				Log.e(LOG_TAG, ioException.getMessage());
			}

		}

		@Override
		public void onSeekTo(long pos) {
			mMediaPlayer.seekTo((int)pos);
		}

	};

	public final static String ACTION_PLAY = "udacity.kevin.podcastmaster.ACTION_PLAY";
	public final static String INTENT_EXTRA_KEY_FILENAME = "udacity.kevin.podcastmaster.KEY_FILENAME";

	public final static String ACTION_PAUSE = "udacity.kevin.podcastmaster.ACTION_PAUSE";
	public final static String ACTION_SEEK = "udacity.kevin.podcastmaster.ACTION_SEEK";

	@Override
	public void onCreate() {
		super.onCreate();

		initMediaPlayer();
		initMediaSession();
		initNoisyReceiver();
	}

	private void initMediaPlayer() {
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mMediaPlayer.setVolume(1.0f, 1.0f);
		mMediaPlayer.setOnErrorListener(this);
		mMediaPlayer.setOnSeekCompleteListener(this);
		mMediaPlayer.setOnCompletionListener(this);
	}

	private void initMediaSession() {
		ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
		mMediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "Tag", mediaButtonReceiver, null);

		mMediaSessionCompat.setCallback(mMediaSessionCallback);
		mMediaSessionCompat.setFlags( MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS );

		Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
		mMediaSessionCompat.setMediaButtonReceiver(pendingIntent);

		setSessionToken(mMediaSessionCompat.getSessionToken());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.abandonAudioFocus(this);
		unregisterReceiver(mNoisyReceiver);
		mMediaSessionCompat.release();
		NotificationManagerCompat.from(this).cancel(1);
	}

	private boolean successfullyRetrievedAudioFocus() {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		int result = audioManager.requestAudioFocus(this,
			AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

		return result == AudioManager.AUDIOFOCUS_GAIN;
	}

	@Nullable
	@Override
	public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
		if(TextUtils.equals(clientPackageName, getPackageName())) {
			return new BrowserRoot(getString(R.string.app_name), null);
		}

		return null;
	}

	private void initMediaSessionMetadata(Bundle extras) {
		MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
		//Notification icon in card
		metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
		metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

		//lock screen icon for pre lollipop
		metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
		metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM,
			extras.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
		metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE,
			extras.getString(MediaMetadataCompat.METADATA_KEY_TITLE));

		metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
			mMediaPlayer.getDuration());


		mMediaSessionCompat.setMetadata(metadataBuilder.build());
	}


	private void initNoisyReceiver() {
		//Handles headphones coming unplugged. cannot be done through a manifest receiver
		IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		registerReceiver(mNoisyReceiver, filter);
	}

	private void setMediaPlaybackState(int state) {
		PlaybackStateCompat.Builder playbackstateBuilder = new PlaybackStateCompat.Builder();
		if( state == PlaybackStateCompat.STATE_PLAYING ) {
			playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE);
		} else {
			playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY);
		}
		playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
		mMediaSessionCompat.setPlaybackState(playbackstateBuilder.build());
	}

	private void showPlayingNotification() {
		NotificationCompat.Builder builder = MediaStyleHelper.from(MediaPlayerService.this, mMediaSessionCompat);
		if( builder == null ) {
			return;
		}

		builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
		builder.setStyle(new android.support.v7.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mMediaSessionCompat.getSessionToken()));
		builder.setSmallIcon(R.mipmap.ic_launcher);
		NotificationManagerCompat.from(MediaPlayerService.this).notify(1, builder.build());
	}

	private void showPausedNotification() {
		NotificationCompat.Builder builder = MediaStyleHelper.from(this, mMediaSessionCompat);
		if( builder == null ) {
			return;
		}

		builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
		builder.setStyle(new android.support.v7.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mMediaSessionCompat.getSessionToken()));
		builder.setSmallIcon(R.mipmap.ic_launcher);
		NotificationManagerCompat.from(this).notify(1, builder.build());
	}

	@Override
	public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
		result.sendResult(null);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		mMediaSessionCompat.getController().getTransportControls().stop();
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		switch( focusChange ) {
			case AudioManager.AUDIOFOCUS_LOSS: {
				if( mMediaPlayer.isPlaying() ) {
					mMediaPlayer.stop();
				}
				break;
			}
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
				mMediaPlayer.pause();
				break;
			}
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
				if( mMediaPlayer != null ) {
					mMediaPlayer.setVolume(0.3f, 0.3f);
				}
				break;
			}
			case AudioManager.AUDIOFOCUS_GAIN: {
				if( mMediaPlayer != null ) {
					if( !mMediaPlayer.isPlaying() ) {
						mMediaPlayer.start();
					}
					mMediaPlayer.setVolume(1.0f, 1.0f);
				}
				break;
			}
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		return false;
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		Log.d(LOG_TAG, "" + mp.isPlaying());
	}
}
