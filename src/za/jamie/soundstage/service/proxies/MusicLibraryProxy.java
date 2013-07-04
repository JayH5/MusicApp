package za.jamie.soundstage.service.proxies;

import java.util.List;

import za.jamie.soundstage.IMusicService;
import za.jamie.soundstage.models.Track;
import za.jamie.soundstage.service.connections.MusicLibraryConnection;
import android.os.RemoteException;

public class MusicLibraryProxy implements MusicLibraryConnection {

	private final IMusicService mService;
	
	public MusicLibraryProxy(IMusicService service) {
		mService = service;
	}

	@Override
	public void open(List<Track> tracks, int position) {
		try {
			mService.open(tracks, position);
		} catch (RemoteException e) {
			
		}		
	}

	@Override
	public void shuffle(List<Track> tracks) {
		try {
			mService.shuffle(tracks);
		} catch (RemoteException e) {
			
		}
	}

	@Override
	public void enqueue(List<Track> tracks, int action) {
		try {
			mService.enqueue(tracks, action);
		} catch (RemoteException e) {
			
		}
	}

}
