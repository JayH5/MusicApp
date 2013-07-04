package za.jamie.soundstage.service.connections;

import android.content.ComponentName;
import android.net.Uri;

public interface MusicNotificationConnection {

	/**
	 * Show the notification. When clicked, the notification will launch the component
	 * specified with the uri specified.
	 * @param component
	 * @param uri
	 */
	void showNotification(ComponentName component, Uri uri);
	
	/**
	 * Hide the notification.
	 */
	void hideNotification();
}
