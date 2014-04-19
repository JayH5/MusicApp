package za.jamie.soundstage.appwidgets;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

import za.jamie.soundstage.R;
import za.jamie.soundstage.models.Track;
import za.jamie.soundstage.service.MusicService;

public class AppWidgetHelper implements Target {

	private final Context mContext;
	private final AppWidgetManager mAppWidgetManager;
	private final ComponentName mProvider;
	
	public AppWidgetHelper(Context context) {
		mContext = context.getApplicationContext();
		mAppWidgetManager = AppWidgetManager.getInstance(mContext);
		mProvider = new ComponentName(mContext, AppWidget.class);
	}
	
	private int[] getAppWidgetIds() {
		return mAppWidgetManager.getAppWidgetIds(mProvider);
	}
	
	public void updateTrack(Track track) {
        if (track == null) {
            return;
        }
    	for (int appWidgetId : getAppWidgetIds()) {
    		final RemoteViews views = AppWidget.getRemoteViews(mContext, mAppWidgetManager, appWidgetId);
	    	views.setTextViewText(R.id.app_widget_track, track.getTitle());
	    	if (views.getLayoutId() != R.layout.app_widget_4x1) {
	    		views.setTextViewText(R.id.app_widget_album, track.getAlbum());
	    	}
			views.setTextViewText(R.id.app_widget_artist, track.getArtist());
			mAppWidgetManager.updateAppWidget(appWidgetId, views);
		}
    }
    
    public void updatePlayState(boolean isPlaying) {
    	final int resource = isPlaying ? R.drawable.btn_playback_pause : R.drawable.btn_playback_play;    	
    	for (int appWidgetId : getAppWidgetIds()) {
    		final RemoteViews views = AppWidget.getRemoteViews(mContext, mAppWidgetManager, appWidgetId);
    		views.setImageViewResource(R.id.app_widget_play, resource);
    		mAppWidgetManager.updateAppWidget(appWidgetId, views);
    	}
    }
    
    public void updateShuffleState(boolean shuffled) {
    	final int resource = shuffled ? R.drawable.btn_playback_shuffle_all : R.drawable.btn_shuffle;
    	for (int appWidgetId : getAppWidgetIds()) {
    		final RemoteViews views = AppWidget.getRemoteViews(mContext, mAppWidgetManager, appWidgetId);
            if (AppWidget.isBigWidget(views)) {
	    		views.setImageViewResource(R.id.app_widget_shuffle, resource);
	    		mAppWidgetManager.updateAppWidget(appWidgetId, views);
    		}
    	}
    }
    
    public void updateRepeatMode(int repeatMode) {
    	final int resource;
    	if (repeatMode == MusicService.REPEAT_ALL) {
    		resource = R.drawable.btn_playback_repeat_all;
    	} else if (repeatMode == MusicService.REPEAT_CURRENT) {
    		resource = R.drawable.btn_playback_repeat_one;
    	} else {
    		resource = R.drawable.btn_repeat;
    	}
    	for (int appWidgetId : getAppWidgetIds()) {
    		final RemoteViews views = AppWidget.getRemoteViews(mContext, mAppWidgetManager, appWidgetId);
    		if (AppWidget.isBigWidget(views)) {
	    		views.setImageViewResource(R.id.app_widget_repeat, resource);
	    		mAppWidgetManager.updateAppWidget(appWidgetId, views);
    		}
    	}
    }
    
    public void updateAlbumArt(Bitmap albumArt) {
    	for (int appWidgetId : getAppWidgetIds()) {
    		final RemoteViews views = AppWidget.getRemoteViews(mContext, mAppWidgetManager, appWidgetId);
    		if (views.getLayoutId() != R.layout.app_widget_4x1) {
	    		views.setImageViewBitmap(R.id.app_widget_album_art, albumArt);
	    		mAppWidgetManager.updateAppWidget(appWidgetId, views);
    		}
    	}
    }
    
    public void update(Track track, boolean isPlaying, boolean shuffled, int repeatMode,
                       int appWidgetId) {
    	RemoteViews views = AppWidget.getRemoteViews(mContext, mAppWidgetManager, appWidgetId);
    	
    	// Update metadata
    	if (track != null) {
            views.setTextViewText(R.id.app_widget_track, track.getTitle());
            if (views.getLayoutId() != R.layout.app_widget_4x1) {
                views.setTextViewText(R.id.app_widget_album, track.getAlbum());
            }
            views.setTextViewText(R.id.app_widget_artist, track.getArtist());
        }
		
		// Update play/pause button
		int resource = isPlaying ? R.drawable.btn_playback_pause : R.drawable.btn_playback_play; 
		views.setImageViewResource(R.id.app_widget_play, resource);
		
		// Shuffle and repeat
		final int layoutId = views.getLayoutId();
		if (AppWidget.isBigWidget(views)) {
			resource = shuffled ? R.drawable.btn_playback_shuffle_all : R.drawable.btn_shuffle;
    		views.setImageViewResource(R.id.app_widget_shuffle, resource);
    		
    		resource = R.drawable.btn_repeat;
        	if (repeatMode == MusicService.REPEAT_ALL) {
        		resource = R.drawable.btn_playback_repeat_all;
        	} else if (repeatMode == MusicService.REPEAT_CURRENT) {
        		resource = R.drawable.btn_playback_repeat_one;
        	}
    		views.setImageViewResource(R.id.app_widget_repeat, resource);
		}
		
		mAppWidgetManager.updateAppWidget(appWidgetId, views);
    }
    

	@Override
	public void onBitmapFailed(Drawable errorDrawable) {
		updateAlbumArt(null);
	}

	@Override
	public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
		updateAlbumArt(bitmap);
	}

	@Override
	public void onPrepareLoad(Drawable loadingDrawable) {
		// Nothing to do...		
	}
}
