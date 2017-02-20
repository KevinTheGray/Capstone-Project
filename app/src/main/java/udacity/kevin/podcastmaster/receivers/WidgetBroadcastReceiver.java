package udacity.kevin.podcastmaster.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import udacity.kevin.podcastmaster.activities.MainActivity;
import udacity.kevin.podcastmaster.services.MediaPlayerService;

public class WidgetBroadcastReceiver extends BroadcastReceiver {
	public static PlayControlWidgetListener mPlayControlWidgetListener;
  @Override
  public void onReceive(Context context, Intent intent) {
		if (MediaPlayerService.mMediaPlayer != null && MediaPlayerService.mPrepared) {
			if (mPlayControlWidgetListener != null) {
				mPlayControlWidgetListener.onWidgetIntentReceived(intent);
			}
		} else {
			Intent launchIntent = new Intent(context, MainActivity.class);
			launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			context.startActivity(launchIntent);
		}
  }

	public void setCallback(PlayControlWidgetListener playControlWidgetListener) {
		mPlayControlWidgetListener = playControlWidgetListener;
	}

	public interface PlayControlWidgetListener {
		void onWidgetIntentReceived(Intent intent);
	}
}
