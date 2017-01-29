package udacity.kevin.podcastmaster.networking.downloadcontent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DownloadEpisodeReceiver extends BroadcastReceiver {
  private final String LOG_TAG = "DownloadEpisodeReceiver";
  private DownloadEpisodeReceiverCallback mCallback;

  @Override
  public void onReceive(Context context, Intent intent) {
    if (this.mCallback != null) {
      this.mCallback.onDownloadEpisodeIntentReceived(context, intent);
    }
  }

  public interface DownloadEpisodeReceiverCallback {
    void onDownloadEpisodeIntentReceived(Context context, Intent intent);
  }

  public void setCallback(DownloadEpisodeReceiverCallback callback) {
    this.mCallback = callback;
  }
}
