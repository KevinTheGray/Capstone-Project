package udacity.kevin.podcastmaster.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import udacity.kevin.podcastmaster.R;

import static android.app.PendingIntent.getBroadcast;

public class PlayControlWidgetProvider extends AppWidgetProvider {
  static final String PLAY_CONTROL_ACTION = "udacity.kevin.podcastmaster.PLAY_CONTROL_ACTION";
  static final String SEEK_FORWARD_ACTION = "udacity.kevin.podcastmaster.SEEK_FORWARD_ACTION";
  static final String SEEK_BACKWARD_ACTION = "udacity.kevin.podcastmaster.SEEK_BACKWARD_ACTION";

  @Override
  public void onEnabled(Context context) {
    super.onEnabled(context);
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    // Perform this loop procedure for each App Widget that belongs to this provider
    for (int appWidgetId : appWidgetIds) {

          // Controls for playing
    Intent playControlIntent = new Intent(PLAY_CONTROL_ACTION);
    PendingIntent playControlPendingIntent =
      getBroadcast(context, 0, playControlIntent, 0);

    // Seek forward
    Intent seekForwardIntent = new Intent(SEEK_FORWARD_ACTION);
    PendingIntent seekForwardPendingIntent =
      getBroadcast(context, 0, seekForwardIntent, 0);

    // Seek back
    Intent seekBackwardIntent = new Intent(SEEK_BACKWARD_ACTION);
    PendingIntent seekBackwardPendingIntent =
      PendingIntent.getBroadcast(context, 0, seekBackwardIntent, 0);

      // Get the layout for the App Widget and attach an on-click listener
      // to the button
      RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_play_control);
      remoteViews.setOnClickPendingIntent(R.id.button_play_control, playControlPendingIntent);
      remoteViews.setOnClickPendingIntent(R.id.button_next_control, seekForwardPendingIntent);
      remoteViews.setOnClickPendingIntent(R.id.button_previous_control, seekBackwardPendingIntent);

      // Tell the AppWidgetManager to perform an update on the current app widget
      appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }
  }
}
