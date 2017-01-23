package udacity.kevin.podcastmaster.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import udacity.kevin.podcastmaster.PodcastMasterApplication;
import udacity.kevin.podcastmaster.R;
import udacity.kevin.podcastmaster.fragments.MyFeedsFragment;
import udacity.kevin.podcastmaster.models.PMChannel;

public class MainActivity extends AppCompatActivity
  implements NavigationView.OnNavigationItemSelectedListener {

  private Tracker mTracker;
  InterstitialAd mInterstitialAd;
  private final String LOG_TAG = "MainActivity";
  private final String SCREEN_NAME = "MainActivity";
  private MyFeedsFragment mMyFeedsFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

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

    mInterstitialAd.setAdListener(new AdListener() {
      @Override
      public void onAdClosed() {
        super.onAdClosed();
        requestNewInterstitial();
      }
    });

    requestNewInterstitial();

    // Todo: Handle tablet layout
    FragmentManager fragmentManager = getSupportFragmentManager();
    mMyFeedsFragment = (MyFeedsFragment) fragmentManager.findFragmentByTag(MyFeedsFragment.FRAGMENT_TAG);
    if (mMyFeedsFragment == null) {
      FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
      MyFeedsFragment myFeedsFragment = new MyFeedsFragment();
      fragmentTransaction
        .add(R.id.fragment_container, myFeedsFragment, MyFeedsFragment.FRAGMENT_TAG);
      fragmentTransaction.commit();
    }

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
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
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

  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();

    if (id == R.id.nav_my_feeds) {
    } else if (id == R.id.my_downloads) {
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
    Log.d(LOG_TAG, pmChannel.getTitle() + " selected");
  }
}
