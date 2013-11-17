package za.jamie.soundstage.service;

import za.jamie.soundstage.R;
import za.jamie.soundstage.models.Track;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

public class MusicNotificationHelper implements Target {
	
	public static final int NOTIFICATION_ID = 1;
	
	private Notification mNotification;
	private final NotificationManager mManager;
	
	private RemoteViews mBaseView;
	private RemoteViews mExpandedView;
	
	public MusicNotificationHelper(Context context) {
		mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		final String packageName = context.getPackageName();
		mBaseView = new RemoteViews(packageName, R.layout.notification_base);
		mExpandedView = new RemoteViews(packageName, R.layout.notification_expanded);
		initPlaybackActions(context);
		initExpandedPlaybackActions(context);		
    }
	
	private Notification buildNotification(Context context, PendingIntent intent) {
		Notification notification = new Notification.Builder(context)
				.setSmallIcon(R.drawable.stat_notify_music)
				.setContentIntent(intent)
				.setPriority(Notification.PRIORITY_DEFAULT)
				.setContent(mBaseView)
				.build();

		notification.bigContentView = mExpandedView;
		return notification;
	}
	
	public void updateMetaData(Track track) {
		initCollapsedLayout(track.getTitle(), track.getArtist());
		initExpandedLayout(track.getTitle(), track.getArtist(), track.getAlbum());
		mManager.notify(NOTIFICATION_ID, mNotification);
	}
	
	public void updateAlbumArt(Bitmap albumArt) {
		mBaseView.setImageViewBitmap(R.id.notification_base_image, albumArt);
		mExpandedView.setImageViewBitmap(R.id.notification_expanded_image, albumArt);
		mManager.notify(NOTIFICATION_ID, mNotification);
	}
	
	public void updatePlayState(boolean isPlaying) {
		int playButtonDrawable = isPlaying ?
				R.drawable.btn_playback_pause : R.drawable.btn_playback_play;
	    mBaseView.setImageViewResource(R.id.notification_base_play, playButtonDrawable);
	    mExpandedView.setImageViewResource(R.id.notification_expanded_play, playButtonDrawable);
	    mManager.notify(NOTIFICATION_ID, mNotification);
	}
	
	public void startForeground(Service service, PendingIntent intent) {
		mNotification = buildNotification(service, intent);
		service.startForeground(NOTIFICATION_ID, mNotification);
	}
	
	private void initCollapsedLayout(String track, String artist) {
	    mBaseView.setTextViewText(R.id.notification_base_line_one, track);
	    mBaseView.setTextViewText(R.id.notification_base_line_two, artist);
	}
	
	private void initExpandedLayout(String track, String artist, String album) {
        mExpandedView.setTextViewText(R.id.notification_expanded_base_line_one, track);
        mExpandedView.setTextViewText(R.id.notification_expanded_base_line_two, album);
        mExpandedView.setTextViewText(R.id.notification_expanded_base_line_three, artist);
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

	@Override
	public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
		updateAlbumArt(bitmap);		
	}

	@Override
	public void onBitmapFailed(Drawable errorDrawable) {
		updateAlbumArt(null);
	}

	@Override
	public void onPrepareLoad(Drawable placeholder) {
		updateAlbumArt(null);		
	}	
}
