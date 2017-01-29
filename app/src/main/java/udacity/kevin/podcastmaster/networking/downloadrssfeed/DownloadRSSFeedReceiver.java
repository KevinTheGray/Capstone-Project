package udacity.kevin.podcastmaster.networking.downloadrssfeed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DownloadRSSFeedReceiver extends BroadcastReceiver {
  private final String LOG_TAG = "DownloadRSSFeedReceiver";
  private DownloadRSSFeedReceiverCallback mCallback;

  @Override
  public void onReceive(Context context, Intent intent) {
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
