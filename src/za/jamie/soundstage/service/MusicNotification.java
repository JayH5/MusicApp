package za.jamie.soundstage.service;

import za.jamie.soundstage.R;
import za.jamie.soundstage.models.Track;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

public class MusicNotification {
	
	private Notification mNotification;
	
	private RemoteViews mBaseView;
	private RemoteViews mExpandedView;
	
	public MusicNotification(Context context) {
		mBaseView = new RemoteViews(context.getPackageName(),
                R.layout.notification_base);
		initPlaybackActions(context);
		
		mExpandedView = new RemoteViews(context.getPackageName(),
        		R.layout.notification_expanded);
		initExpandedPlaybackActions(context);
		
		
    }
	
	private void initNotification(Context context, PendingIntent intent) {
		mNotification = new Notification.Builder(context)
				.setSmallIcon(R.drawable.stat_notify_music)
				.setContentIntent(intent)
				.setPriority(Notification.PRIORITY_DEFAULT)
				.setContent(mBaseView)
				.build();

		mNotification.bigContentView = mExpandedView;
	}
	
	public void updateMeta(Track track, Bitmap albumArt) {		
		if (mBaseView != null) {
			initCollapsedLayout(track.getTitle(), track.getArtist(), albumArt);
		}
		if (mExpandedView != null) {
			initExpandedLayout(track.getTitle(), track.getArtist(), track.getAlbum(), albumArt);
		}
	}
	
	public void updatePlayState(boolean isPlaying) {
		if (mBaseView != null) {
	        mBaseView.setImageViewResource(R.id.notification_base_play,
	                isPlaying ? R.drawable.btn_playback_pause : R.drawable.btn_playback_play);
	    }
	    if (mExpandedView != null) {
	        mExpandedView.setImageViewResource(R.id.notification_expanded_play,
	                isPlaying ? R.drawable.btn_playback_pause : R.drawable.btn_playback_play);
	    }
	}
	
	/*public PendingIntent getPendingIntent(Context context, ComponentName componentName, 
			Uri uri) {
		
		Class<?> launchClass = null;
		try {
			launchClass = Class.forName(componentName.getClassName());
		} catch (ClassNotFoundException e) {
			Log.e("MusicNotification", "Couldn't find class for PendingIntent", e);
		}
		
		if (launchClass != null) {
			Intent launchIntent = new Intent(context, launchClass);
			launchIntent = new Intent();
			launchIntent.setData(uri);
			launchIntent.putExtra(MusicActivity.EXTRA_OPEN_DRAWER, true);
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
			stackBuilder.addParentStack(launchClass);
			stackBuilder.addNextIntent(launchIntent);
			return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
			
		}
		return null;
	}*/
	
	public Notification newNotification(Context context, PendingIntent intent) {
		initNotification(context, intent);
		
		return mNotification;
	}
	
	public Notification getNotification() {
		return mNotification;
	}
	
	/*private PendingIntent getPendingIntent(Context context) {
		Intent intent = new Intent(context, LibraryActivity.class);
		//intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		//intent.setAction(Intent.ACTION_MAIN);
		intent.putExtra(MusicActivity.EXTRA_OPEN_DRAWER, true);
		
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(LibraryActivity.class);
		stackBuilder.addNextIntent(intent);
		PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		
		return pendingIntent;
    }*/
	
	private void initCollapsedLayout(String track, String artist, Bitmap albumArt) {
	    mBaseView.setTextViewText(R.id.notification_base_line_one, track);
	    mBaseView.setTextViewText(R.id.notification_base_line_two, artist);
	    mBaseView.setImageViewBitmap(R.id.notification_base_image, albumArt);
	}
	
	private void initExpandedLayout(String track, String artist, String album, 
			Bitmap albumArt) {
        mExpandedView.setTextViewText(R.id.notification_expanded_base_line_one, track);
        mExpandedView.setTextViewText(R.id.notification_expanded_base_line_two, album);
        mExpandedView.setTextViewText(R.id.notification_expanded_base_line_three, artist);
        mExpandedView.setImageViewBitmap(R.id.notification_expanded_image, albumArt);
    }
	 
	private void initPlaybackActions(Context context) {
	    // Play and pause
		mBaseView.setOnClickPendingIntent(R.id.notification_base_play,
				retreivePlaybackActions(context, 1));

	    // Skip tracks
	    mBaseView.setOnClickPendingIntent(R.id.notification_base_next,
	            retreivePlaybackActions(context, 2));

	    // Previous tracks
	    mBaseView.setOnClickPendingIntent(R.id.notification_base_previous,
	            retreivePlaybackActions(context, 3));

	    // Stop and collapse the notification
	    mBaseView.setOnClickPendingIntent(R.id.notification_base_collapse,
	            retreivePlaybackActions(context, 4));

	    // Update the play button image
	    mBaseView.setImageViewResource(R.id.notification_base_play,
	            R.drawable.btn_playback_pause);
	}
	
	private void initExpandedPlaybackActions(Context context) {
		// Play and pause
        mExpandedView.setOnClickPendingIntent(R.id.notification_expanded_play,
                retreivePlaybackActions(context, 1));

        // Skip tracks
        mExpandedView.setOnClickPendingIntent(R.id.notification_expanded_next,
                retreivePlaybackActions(context, 2));

        // Previous tracks
        mExpandedView.setOnClickPendingIntent(R.id.notification_expanded_previous,
                retreivePlaybackActions(context, 3));

        // Stop and collapse the notification
        mExpandedView.setOnClickPendingIntent(R.id.notification_expanded_remove,
                retreivePlaybackActions(context, 4));

        // Update the play button image
        mExpandedView.setImageViewResource(R.id.notification_expanded_play,
                R.drawable.btn_playback_pause);
    }
	 
	private final PendingIntent retreivePlaybackActions(Context context, int which) {
		Intent action;
	    PendingIntent pendingIntent;
	    final ComponentName serviceName = new ComponentName(context, MusicService.class);
	    switch (which) {
	    	case 1:
	    		// Play and pause
	            action = new Intent(MusicService.ACTION_TOGGLE_PLAYBACK);
	            action.setComponent(serviceName);
	            pendingIntent = PendingIntent.getService(context, 1, action, 0);
	            return pendingIntent;
	        case 2:
	            // Skip tracks
	            action = new Intent(MusicService.ACTION_NEXT);
	            action.setComponent(serviceName);
	            pendingIntent = PendingIntent.getService(context, 2, action, 0);
	            return pendingIntent;
	        case 3:
	            // Previous tracks
	            action = new Intent(MusicService.ACTION_PREVIOUS);
	            action.setComponent(serviceName);
	            pendingIntent = PendingIntent.getService(context, 3, action, 0);
	            return pendingIntent;
	        case 4:
	            // Stop and collapse the notification
	            action = new Intent(MusicService.ACTION_STOP);
	            action.setComponent(serviceName);
	            pendingIntent = PendingIntent.getService(context, 4, action, 0);
	            return pendingIntent;
	        default:
	            break;
	    }
	    return null;
	}	
}
