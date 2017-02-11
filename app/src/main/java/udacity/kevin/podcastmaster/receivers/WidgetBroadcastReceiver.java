package udacity.kevin.podcastmaster.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WidgetBroadcastReceiver extends BroadcastReceiver {
	public static PlayControlWidgetListener mPlayControlWidgetListener;
  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d("YEAH!", intent.getAction());
		if (mPlayControlWidgetListener != null) {
			mPlayControlWidgetListener.onWidgetIntentReceived(intent);
		}
  }

	public void setCallback(PlayControlWidgetListener playControlWidgetListener) {
		mPlayControlWidgetListener = playControlWidgetListener;
	}

	public interface PlayControlWidgetListener {
		void onWidgetIntentReceived(Intent intent);
	}
}
