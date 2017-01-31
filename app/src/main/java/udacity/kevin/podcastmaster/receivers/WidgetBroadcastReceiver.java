package udacity.kevin.podcastmaster.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import udacity.kevin.podcastmaster.services.MediaPlayerService;

public class WidgetBroadcastReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d("YEAH!", intent.getAction());
    Intent playIntent = new Intent(context, MediaPlayerService.class);
    playIntent.setAction(MediaPlayerService.ACTION_PLAY);
    context.startService(playIntent);
  }
}
