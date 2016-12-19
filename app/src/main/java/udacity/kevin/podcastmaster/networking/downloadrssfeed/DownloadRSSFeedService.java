package udacity.kevin.podcastmaster.networking.downloadrssfeed;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class DownloadRSSFeedService extends IntentService {

  public final String LOG_TAG = "DownloadRSSFeedService";

  public final static String BROADCAST_UPDATE_ACTION =
    "udacity.kevin.podcastmaster.downloadrssfeedservice.UPDATE";
  public final static String BROADCAST_FINISHED_ACTION =
    "udacity.kevin.podcastmaster.downloadrssfeedservice.FINISHED";

  public DownloadRSSFeedService() {
    super("DownloadRSSFeedService");
  }
  @Override
  protected void onHandleIntent(Intent intent) {
    Intent finishedIntent = new Intent(BROADCAST_FINISHED_ACTION);
    Intent updateIntent = new Intent(BROADCAST_UPDATE_ACTION);
    // Broadcasts the Intent to receivers in this app.
    try {
      Thread.sleep(3000);
    } catch (Exception e) {

    }
    LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);

    try {
      Thread.sleep(3000);
    } catch (Exception e) {

    }
    LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);

    try {
      Thread.sleep(3000);
    } catch (Exception e) {

    }
    LocalBroadcastManager.getInstance(this).sendBroadcast(finishedIntent);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d(LOG_TAG, "Destroyed!!!");
  }
}
