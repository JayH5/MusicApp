package za.jamie.soundstage.service.proxies;

import java.lang.ref.WeakReference;

import za.jamie.soundstage.IMusicService;
import za.jamie.soundstage.service.connections.MusicNotificationConnection;
import android.content.ComponentName;
import android.net.Uri;
import android.os.RemoteException;

public class MusicNotificationProxy implements MusicNotificationConnection {

	private final WeakReference<IMusicService> mService;
	
	public MusicNotificationProxy(IMusicService service) {
		mService = new WeakReference<IMusicService>(service);
	}
	
	@Override
	public void showNotification(ComponentName component, Uri uri) {
		try {
			mService.get().showNotification(component, uri);
		} catch (RemoteException e) {
			
		}
	}

	@Override
	public void hideNotification() {
		try {
			mService.get().hideNotification();
		} catch (RemoteException e) {
			
		}
	}

}
