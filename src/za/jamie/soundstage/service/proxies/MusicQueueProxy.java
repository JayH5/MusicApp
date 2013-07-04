package za.jamie.soundstage.service.proxies;

import za.jamie.soundstage.IMusicService;
import za.jamie.soundstage.IPlayQueueCallback;
import za.jamie.soundstage.service.connections.MusicQueueConnection;
import android.os.RemoteException;

public class MusicQueueProxy implements MusicQueueConnection {

	private final IMusicService mService;
	
	public MusicQueueProxy(IMusicService service) {
		mService = service;
	}

	@Override
	public void setQueuePosition(int position) {
		try {
			mService.setQueuePosition(position);
		} catch (RemoteException e) {
			
		}
	}

	@Override
	public void moveQueueItem(int from, int to) {
		try {
			mService.moveQueueItem(from, to);
		} catch (RemoteException e) {
			
		}
	}

	@Override
	public void removeTrack(int position) {
		try {
			mService.removeTrack(position);
		} catch (RemoteException e) {
			
		}
	}

	@Override
	public void requestPlayQueue() {
		try {
			mService.requestPlayQueue();
		} catch (RemoteException e) {
			
		}
	}

	@Override
	public void registerPlayQueueCallback(IPlayQueueCallback callback) {
		try {
			mService.registerPlayQueueCallback(callback);
		} catch (RemoteException e) {
			
		}
	}

	@Override
	public void unregisterPlayQueueCallback(IPlayQueueCallback callback) {
		try {
			mService.unregisterPlayQueueCallback(callback);
		} catch (RemoteException e) {
			
		}
	}

}
