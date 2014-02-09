package za.jamie.soundstage.appwidgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.LibraryActivity;
import za.jamie.soundstage.activities.MusicActivity;
import za.jamie.soundstage.service.MusicService;

public class AppWidget extends AppWidgetProvider {

    private static final String TAG = "AppWidget";
	
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
        
        
        if (isBigWidget(views)) {
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
        int minWidth = -1;
		int minHeight = -1;
		if (options != null) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
			int minHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
            Log.d(TAG, "Notification min width= " + minWidthDp + ", min height= " + minHeightDp);

            int maxWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
            int maxHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
            Log.d(TAG, "Notification max width= " + maxWidthDp + ", max height= " + maxHeightDp);

            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            minWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, maxWidthDp, dm);
            minHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, maxHeightDp, dm);
		}
        return createRemoteViews(context, minWidth, minHeight);
    }
    
    private static RemoteViews createRemoteViews(Context context, int minWidth, int minHeight) {
		Resources res = context.getResources();
        final int layout;
		if (minHeight >= res.getDimensionPixelOffset(R.dimen.widget_5x5_height)
                && minWidth >= res.getDimensionPixelOffset(R.dimen.widget_5x5_image)) {
        	layout = R.layout.app_widget_5x5;
        } else if (minHeight >= res.getDimensionPixelOffset(R.dimen.widget_4x4_height)) {
        	layout = R.layout.app_widget_4x4;
        } else if (minHeight >= res.getDimensionPixelOffset(R.dimen.widget_4x3_height)) {
        	layout = R.layout.app_widget_4x3;
        } else if (minHeight >= res.getDimensionPixelOffset(R.dimen.widget_4x2_height)) {
        	layout = R.layout.app_widget_4x2;
        } else if (minHeight >= res.getDimensionPixelOffset(R.dimen.widget_4x1_height)) {
            layout = R.layout.app_widget_4x1;
        } else {
        	layout = R.layout.app_widget_4x2;
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), layout);
		linkButtons(context, views);
		
		return views;
	}

    public static boolean isBigWidget(RemoteViews views) {
        final int layoutId = views.getLayoutId();
        return layoutId == R.layout.app_widget_4x3
                || layoutId == R.layout.app_widget_4x4
                || layoutId == R.layout.app_widget_5x5;
    }
    
    private static void requestUpdate(Context context, int appWidgetId) {
    	final Intent updateIntent = new Intent(context, MusicService.class);
        updateIntent.setAction(MusicService.ACTION_UPDATE_WIDGETS);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        updateIntent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.startService(updateIntent);
    }

}
