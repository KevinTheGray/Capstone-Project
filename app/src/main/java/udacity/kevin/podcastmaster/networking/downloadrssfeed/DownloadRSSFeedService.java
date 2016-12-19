package udacity.kevin.podcastmaster.networking.downloadrssfeed;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class DownloadRSSFeedService extends IntentService {

  public final String LOG_TAG = "DownloadRSSFeedService";
  public DownloadRSSFeedService() {
    super("DownloadRSSFeedService");
  }
  @Override
  protected void onHandleIntent(Intent intent) {
    Log.d(LOG_TAG, "HANDLING THE INTENT, OH YEAH!");
  }
}
