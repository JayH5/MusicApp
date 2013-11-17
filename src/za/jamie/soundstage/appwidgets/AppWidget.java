package za.jamie.soundstage.appwidgets;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.LibraryActivity;
import za.jamie.soundstage.activities.MusicActivity;
import za.jamie.soundstage.service.MusicService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

public class AppWidget extends AppWidgetProvider {
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		for (int appWidgetId : appWidgetIds) {
			final Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
			onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, options);
		}
	}
	
	@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);        
        final RemoteViews views = getRemoteViews(context, appWidgetManager, appWidgetId); 
        appWidgetManager.updateAppWidget(appWidgetId, views);
        requestUpdate(context, appWidgetId);
    }
    
    private static void linkButtons(Context context, RemoteViews views) {
		PendingIntent activityIntent = buildActivityIntent(context);
    	views.setOnClickPendingIntent(R.id.app_widget_info_container, activityIntent);
    	int layoutId = views.getLayoutId();
    	if (layoutId != R.layout.app_widget_4x1) {
    		views.setOnClickPendingIntent(R.id.app_widget_album_art, activityIntent);
    	}
        
    	views.setOnClickPendingIntent(R.id.app_widget_previous,
        		buildActionIntent(context, MusicService.ACTION_PREVIOUS));
        views.setOnClickPendingIntent(R.id.app_widget_play,
        		buildActionIntent(context, MusicService.ACTION_TOGGLE_PLAYBACK));
        views.setOnClickPendingIntent(R.id.app_widget_next,
        		buildActionIntent(context, MusicService.ACTION_NEXT));
        
        
        if (layoutId == R.layout.app_widget_4x3 || layoutId == R.layout.app_widget_4x4) {
        	views.setOnClickPendingIntent(R.id.app_widget_shuffle,
        			buildActionIntent(context, MusicService.ACTION_SHUFFLE));
        	views.setOnClickPendingIntent(R.id.app_widget_repeat,
        			buildActionIntent(context, MusicService.ACTION_REPEAT));
        }
    }
    
    private static PendingIntent buildActivityIntent(Context context) {
    	Intent intent = new Intent(context, LibraryActivity.class);
		intent.setAction(MusicActivity.ACTION_SHOW_PLAYER)
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
			.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return PendingIntent.getActivity(context, 0, intent, 0);
    }
    
    private static PendingIntent buildActionIntent(Context context, String action) {
        Intent intent = new Intent(action);
        intent.setComponent(new ComponentName(context, MusicService.class));
        return PendingIntent.getService(context, 0, intent, 0);
    }
    
    
    public static RemoteViews getRemoteViews(Context context, AppWidgetManager appWidgetManager,
    		int appWidgetId) {
		Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
		int numRows = -1;
		if (options != null) {
			int minHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        	numRows = (minHeightDp + 30) / 70;
		}
		return createRemoteViews(context, numRows, appWidgetId);
    }
    
    private static RemoteViews createRemoteViews(Context context, int numRows, int appWidgetId) {
		final RemoteViews views;
		if (numRows == 1) {
        	views = new RemoteViews(context.getPackageName(), R.layout.app_widget_4x1);
        } else if (numRows == 2) {
        	views = new RemoteViews(context.getPackageName(), R.layout.app_widget_4x2);
        } else if (numRows == 3) {
        	views = new RemoteViews(context.getPackageName(), R.layout.app_widget_4x3);
        } else if (numRows == 4) {
        	views = new RemoteViews(context.getPackageName(), R.layout.app_widget_4x4);
        } else {
        	views = new RemoteViews(context.getPackageName(), R.layout.app_widget_4x2);
        }
		linkButtons(context, views);
		
		return views;
	}
    
    private static void requestUpdate(Context context, int appWidgetId) {
    	final Intent updateIntent = new Intent(context, MusicService.class);
        updateIntent.setAction(MusicService.ACTION_UPDATE_WIDGETS);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        updateIntent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.startService(updateIntent);
    }

}
