package udacity.kevin.podcastmaster.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import udacity.kevin.podcastmaster.R;

public class PlayControlWidgetProvider extends AppWidgetProvider {
  static final String PLAY_CONTROL_ACTION = "udacity.kevin.podcastmaster.PLAY_CONTROL_ACTION";
  static final String SEEK_FORWARD_ACTION = "udacity.kevin.podcastmaster.SEEK_FORWARD_ACTION";
  static final String SEEK_BACKWARD_ACTION = "udacity.kevin.podcastmaster.SEEK_BACKWARD_ACTION";
  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    super.onUpdate(context, appWidgetManager, appWidgetIds);
    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_play_control);
    // Controls for playing
    Intent playControlIntent = new Intent(PLAY_CONTROL_ACTION);
    PendingIntent playControlPendingIntent = PendingIntent.
      getBroadcast(context, 0, playControlIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    // Seek forward
    Intent seekForwardIntent = new Intent(SEEK_FORWARD_ACTION);
    PendingIntent seekForwardPendingIntent = PendingIntent.
      getBroadcast(context, 0, seekForwardIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    // Seek back
    Intent seekBackwardIntent = new Intent(SEEK_BACKWARD_ACTION);
    PendingIntent seekBackwardPendingIntent = PendingIntent.
      getBroadcast(context, 0, seekBackwardIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    remoteViews.setOnClickPendingIntent(R.id.button_play_control, playControlPendingIntent);
    remoteViews.setOnClickPendingIntent(R.id.button_next_control, seekForwardPendingIntent);
    remoteViews.setOnClickPendingIntent(R.id.button_previous_control, seekBackwardPendingIntent);
  }
}
