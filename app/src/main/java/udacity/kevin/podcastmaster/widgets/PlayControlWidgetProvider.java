package udacity.kevin.podcastmaster.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import udacity.kevin.podcastmaster.R;
import udacity.kevin.podcastmaster.activities.MainActivity;

public class PlayControlWidgetProvider extends AppWidgetProvider {
  static final String PLAY_CONTROL_ACTION = "udacity.kevin.podcastmaster.PLAY_CONTROL_ACTION";
  static final String SEEK_FORWARD_ACTION = "udacity.kevin.podcastmaster.SEEK_FORWARD_ACTION";
  static final String SEEK_BACKWARD_ACTION = "udacity.kevin.podcastmaster.SEEK_BACKWARD_ACTION";
  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
//    super.onUpdate(context, appWidgetManager, appWidgetIds);
//    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_play_control);
//    // Controls for playing
//    //Intent playControlIntent = new Intent(PLAY_CONTROL_ACTION);
//    Intent intent = new Intent(context, MainActivity.class);
//    PendingIntent playControlPendingIntent =
//      PendingIntent.getActivity(context, 0, intent, 0);
//
//    // Seek forward
//    Intent seekForwardIntent = new Intent(SEEK_FORWARD_ACTION);
//    PendingIntent seekForwardPendingIntent = PendingIntent.
//      getBroadcast(context, 0, seekForwardIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//    // Seek back
//    Intent seekBackwardIntent = new Intent(SEEK_BACKWARD_ACTION);
//    PendingIntent seekBackwardPendingIntent = PendingIntent.
//      getBroadcast(context, 0, seekBackwardIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//    remoteViews.setOnClickPendingIntent(R.id.button_play_control, playControlPendingIntent);
//    remoteViews.setOnClickPendingIntent(R.id.button_next_control, seekForwardPendingIntent);
//    remoteViews.setOnClickPendingIntent(R.id.button_previous_control, seekBackwardPendingIntent);
//    appWidgetManager.updateAppWidget(R.id.button_play_control, remoteViews);
    // Perform this loop procedure for each App Widget that belongs to this provider
    final int N = appWidgetIds.length;
    for (int i=0; i<N; i++) {
      int appWidgetId = appWidgetIds[i];

      // Create an Intent to launch ExampleActivity
      Intent intent = new Intent(context, MainActivity.class);
      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

      // Get the layout for the App Widget and attach an on-click listener
      // to the button
      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_play_control);
      views.setOnClickPendingIntent(R.id.button_play_control, pendingIntent);

      // Tell the AppWidgetManager to perform an update on the current app widget
      appWidgetManager.updateAppWidget(appWidgetId, views);
    }
  }
}
