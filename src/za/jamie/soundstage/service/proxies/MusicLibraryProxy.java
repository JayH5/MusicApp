package za.jamie.soundstage.service.proxies;

import java.lang.ref.WeakReference;
import java.util.List;

import android.os.RemoteException;

import za.jamie.soundstage.IMusicService;
import za.jamie.soundstage.models.Track;
import za.jamie.soundstage.service.connections.MusicLibraryConnection;

public class MusicLibraryProxy implements MusicLibraryConnection {

	private final WeakReference<IMusicService> mService;
	
	public MusicLibraryProxy(IMusicService service) {
		mService = new WeakReference<IMusicService>(service);
	}

	@Override
	public void open(List<Track> tracks, int position) {
		try {
			mService.get().open(tracks, position);
		} catch (RemoteException e) {
			
		}		
	}

	@Override
	public void shuffle(List<Track> tracks) {
		try {
			mService.get().shuffle(tracks);
		} catch (RemoteException e) {
			
		}
	}

	@Override
	public void enqueue(List<Track> tracks, int action) {
		try {
			mService.get().enqueue(tracks, action);
		} catch (RemoteException e) {
			
		}
	}

}
