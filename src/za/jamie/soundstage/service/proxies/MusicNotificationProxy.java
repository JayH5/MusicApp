package za.jamie.soundstage.service.proxies;

import za.jamie.soundstage.IMusicService;
import za.jamie.soundstage.service.connections.MusicNotificationConnection;
import android.content.ComponentName;
import android.net.Uri;
import android.os.RemoteException;

public class MusicNotificationProxy implements MusicNotificationConnection {

	private final IMusicService mService;
	
	public MusicNotificationProxy(IMusicService service) {
		mService = service;
	}
	
	@Override
	public void showNotification(ComponentName component, Uri uri) {
		try {
			mService.showNotification(component, uri);
		} catch (RemoteException e) {
			
		}
	}

	@Override
	public void hideNotification() {
		try {
			mService.hideNotification();
		} catch (RemoteException e) {
			
		}
	}

}
