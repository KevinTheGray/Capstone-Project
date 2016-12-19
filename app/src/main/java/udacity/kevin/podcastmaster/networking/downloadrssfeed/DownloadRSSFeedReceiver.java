package udacity.kevin.podcastmaster.networking.downloadrssfeed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DownloadRSSFeedReceiver extends BroadcastReceiver {
  private final String LOG_TAG = "DownloadRSSFeedReceiver";
  private DownloadRSSFeedReceiverCallback mCallback;

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d(LOG_TAG, intent.getAction());
    if (this.mCallback != null) {
      this.mCallback.onDownloadRSSFeedIntentReceived(context, intent);
    }
  }

  public interface DownloadRSSFeedReceiverCallback {
    void onDownloadRSSFeedIntentReceived(Context context, Intent intent);
  }

  public void setCallback(DownloadRSSFeedReceiverCallback mCallback) {
    this.mCallback = mCallback;
  }

}
