package za.jamie.soundstage.service.proxies;

import java.lang.ref.WeakReference;

import za.jamie.soundstage.IMusicService;
import za.jamie.soundstage.IPlayQueueCallback;
import za.jamie.soundstage.service.connections.MusicQueueConnection;

import android.os.RemoteException;

public class MusicQueueProxy implements MusicQueueConnection {

	private final WeakReference<IMusicService> mService;
	
	public MusicQueueProxy(IMusicService service) {
		mService = new WeakReference<IMusicService>(service);
	}

	@Override
	public void setQueuePosition(int position) {
		try {
			mService.get().setQueuePosition(position);
		} catch (RemoteException e) {
			
		}
	}

	@Override
	public void moveQueueItem(int from, int to) {
		try {
			mService.get().moveQueueItem(from, to);
		} catch (RemoteException e) {
			
		}
	}

	@Override
	public void removeTrack(int position) {
		try {
			mService.get().removeTrack(position);
		} catch (RemoteException e) {
			
		}
	}

	@Override
	public void requestPlayQueue() {
		try {
			mService.get().requestPlayQueue();
		} catch (RemoteException e) {
			
		}
	}

	@Override
	public void registerPlayQueueCallback(IPlayQueueCallback callback) {
		try {
			mService.get().registerPlayQueueCallback(callback);
		} catch (RemoteException e) {
			
		}
	}

	@Override
	public void unregisterPlayQueueCallback(IPlayQueueCallback callback) {
		try {
			mService.get().unregisterPlayQueueCallback(callback);
		} catch (RemoteException e) {
			
		}
	}

}
