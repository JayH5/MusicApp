package za.jamie.soundstage.service.proxies;

import java.lang.ref.WeakReference;

import za.jamie.soundstage.IMusicService;
import za.jamie.soundstage.IMusicStatusCallback;
import za.jamie.soundstage.service.connections.MusicPlaybackConnection;

import android.os.RemoteException;

public class MusicPlaybackProxy implements MusicPlaybackConnection {

	private final WeakReference<IMusicService> mService;
	
	public MusicPlaybackProxy(IMusicService service) {
		mService = new WeakReference<IMusicService>(service);
	}

	@Override
	public void togglePlayback() {
		try {
			mService.get().togglePlayback();
		} catch (RemoteException e) {
			
		}		
	}

	@Override
	public void next() {
		try {
			mService.get().next();
		} catch (RemoteException e) {

		}		
	}

	@Override
	public void previous() {
		try {
			mService.get().previous();
		} catch (RemoteException e) {

		}		
	}

	@Override
	public void seek(long position) {
		try {
			mService.get().seek(position);
		} catch (RemoteException e) {

		}		
	}

	@Override
	public void toggleShuffle() {
		try {
			mService.get().toggleShuffle();
		} catch (RemoteException e) {

		}		
	}

	@Override
	public void cycleRepeatMode() {
		try {
			mService.get().cycleRepeatMode();
		} catch (RemoteException e) {

		}		
	}

	@Override
	public void requestMusicStatus() {
		try {
			mService.get().requestMusicStatus();
		} catch (RemoteException e) {

		}
		
	}

	@Override
	public void registerMusicStatusCallback(IMusicStatusCallback callback) {
		try {
			mService.get().registerMusicStatusCallback(callback);
		} catch (RemoteException e) {

		}		
	}

	@Override
	public void unregisterMusicStatusCallback(IMusicStatusCallback callback) {
		try {
			mService.get().unregisterMusicStatusCallback(callback);
		} catch (RemoteException e) {

		}		
	}

}
