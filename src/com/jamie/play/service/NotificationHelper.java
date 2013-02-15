package com.jamie.play.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import com.jamie.play.R;

public class NotificationHelper {
	private final int NOTIFICATION_ID = 1;
	
	private final NotificationManager mNotificationManager;
	private final Service mService;
	
	private Notification mNotification;
	private RemoteViews mBaseView;
	private RemoteViews mExpandedView;
	
	public NotificationHelper(Service service) {
		mService = service;
		mNotificationManager = (NotificationManager) service
				.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	public void buildNotification(Track track, Bitmap albumArt) {
		final String title = track.getTitle();
		final String artist = track.getArtist();
		final String album = track.getAlbum();
		
		mBaseView = new RemoteViews(mService.getPackageName(),
                R.layout.notification_base);
		initCollapsedLayout(title, album, albumArt);
		initPlaybackActions();
		
		mNotification = new Notification.Builder(mService)
				.setSmallIcon(R.drawable.stat_notify_music)
				.setContentIntent(getPendingIntent())
				.setPriority(Notification.PRIORITY_DEFAULT)
				.setContent(mBaseView)
				.build();
		
		mExpandedView = new RemoteViews(mService.getPackageName(),
        		R.layout.notification_expanded);
        mNotification.bigContentView = mExpandedView;
        initExpandedLayout(title, artist, album, albumArt);
        initExpandedPlaybackActions();
        
        mService.startForeground(NOTIFICATION_ID, mNotification);
		
	}
	
	public void updateNotification(String track, String artist, String album,
			Bitmap albumArt) {
		
		initCollapsedLayout(track, artist, albumArt);
		initExpandedLayout(track, artist, album, albumArt);
	}
	
	private PendingIntent getPendingIntent() {
        return PendingIntent.getActivity(mService, 0, 
        		new Intent("com.jamie.MUSIC_PLAYER")
        				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);
    }
	
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
	 
	private void initPlaybackActions() {
	    // Play and pause
		mBaseView.setOnClickPendingIntent(R.id.notification_base_play,
				retreivePlaybackActions(1));

	    // Skip tracks
	    mBaseView.setOnClickPendingIntent(R.id.notification_base_next,
	            retreivePlaybackActions(2));

	    // Previous tracks
	    mBaseView.setOnClickPendingIntent(R.id.notification_base_previous,
	            retreivePlaybackActions(3));

	    // Stop and collapse the notification
	    mBaseView.setOnClickPendingIntent(R.id.notification_base_collapse,
	            retreivePlaybackActions(4));

	    // Update the play button image
	    mBaseView.setImageViewResource(R.id.notification_base_play,
	            R.drawable.btn_playback_pause);
	}
	
	private void initExpandedPlaybackActions() {
		// Play and pause
        mExpandedView.setOnClickPendingIntent(R.id.notification_expanded_play,
                retreivePlaybackActions(1));

        // Skip tracks
        mExpandedView.setOnClickPendingIntent(R.id.notification_expanded_next,
                retreivePlaybackActions(2));

        // Previous tracks
        mExpandedView.setOnClickPendingIntent(R.id.notification_expanded_previous,
                retreivePlaybackActions(3));

        // Stop and collapse the notification
        mExpandedView.setOnClickPendingIntent(R.id.notification_expanded_remove,
                retreivePlaybackActions(4));

        // Update the play button image
        mExpandedView.setImageViewResource(R.id.notification_expanded_play,
                R.drawable.btn_playback_pause);
    }
	 
	private final PendingIntent retreivePlaybackActions(final int which) {
		Intent action;
	    PendingIntent pendingIntent;
	    final ComponentName serviceName = new ComponentName(mService, MusicService.class);
	    switch (which) {
	    	case 1:
	    		// Play and pause
	            action = new Intent(MusicService.ACTION_TOGGLE_PLAYBACK);
	            action.setComponent(serviceName);
	            pendingIntent = PendingIntent.getService(mService, 1, action, 0);
	            return pendingIntent;
	        case 2:
	            // Skip tracks
	            action = new Intent(MusicService.ACTION_NEXT);
	            action.setComponent(serviceName);
	            pendingIntent = PendingIntent.getService(mService, 2, action, 0);
	            return pendingIntent;
	        case 3:
	            // Previous tracks
	            action = new Intent(MusicService.ACTION_PREVIOUS);
	            action.setComponent(serviceName);
	            pendingIntent = PendingIntent.getService(mService, 3, action, 0);
	            return pendingIntent;
	        case 4:
	            // Stop and collapse the notification
	            action = new Intent(MusicService.ACTION_STOP);
	            action.setComponent(serviceName);
	            pendingIntent = PendingIntent.getService(mService, 4, action, 0);
	            return pendingIntent;
	        default:
	            break;
	    }
	    return null;
	}

    public void goToIdleState(final boolean isPlaying) {
    	if (mNotification == null || mNotificationManager == null) {
    		return;
	    }
	    if (mBaseView != null) {
	        mBaseView.setImageViewResource(R.id.notification_base_play,
	                isPlaying ? R.drawable.btn_playback_play : R.drawable.btn_playback_pause);
	    }

	    if (mExpandedView != null) {
	        mExpandedView.setImageViewResource(R.id.notification_expanded_play,
	                isPlaying ? R.drawable.btn_playback_play : R.drawable.btn_playback_pause);
	    }
	    mNotificationManager.notify(NOTIFICATION_ID, mNotification);
	    
    }
	
}
