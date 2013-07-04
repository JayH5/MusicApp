package za.jamie.soundstage.service.proxies;

import za.jamie.soundstage.IMusicService;
import za.jamie.soundstage.IMusicStatusCallback;
import za.jamie.soundstage.service.connections.MusicPlaybackConnection;
import android.os.RemoteException;

public class MusicPlaybackProxy implements MusicPlaybackConnection {

	private final IMusicService mService;
	
	public MusicPlaybackProxy(IMusicService service) {
		mService = service;
	}

	@Override
	public void togglePlayback() {
		try {
			mService.togglePlayback();
		} catch (RemoteException e) {
			
		}		
	}

	@Override
	public void next() {
		try {
			mService.next();
		} catch (RemoteException e) {

		}		
	}

	@Override
	public void previous() {
		try {
			mService.previous();
		} catch (RemoteException e) {

		}		
	}

	@Override
	public void seek(long position) {
		try {
			mService.seek(position);
		} catch (RemoteException e) {

		}		
	}

	@Override
	public void toggleShuffle() {
		try {
			mService.toggleShuffle();
		} catch (RemoteException e) {

		}		
	}

	@Override
	public void cycleRepeatMode() {
		try {
			mService.cycleRepeatMode();
		} catch (RemoteException e) {

		}		
	}

	@Override
	public void requestMusicStatus() {
		try {
			mService.requestMusicStatus();
		} catch (RemoteException e) {

		}
		
	}

	@Override
	public void registerMusicStatusCallback(IMusicStatusCallback callback) {
		try {
			mService.registerMusicStatusCallback(callback);
		} catch (RemoteException e) {

		}		
	}

	@Override
	public void unregisterMusicStatusCallback(IMusicStatusCallback callback) {
		try {
			mService.unregisterMusicStatusCallback(callback);
		} catch (RemoteException e) {

		}		
	}

}
