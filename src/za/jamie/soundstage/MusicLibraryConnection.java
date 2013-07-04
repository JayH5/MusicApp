package za.jamie.soundstage;

import java.lang.ref.WeakReference;
import java.util.List;

import android.os.RemoteException;

import za.jamie.soundstage.models.Track;

public class MusicLibraryConnection implements MusicLibraryWrapper {

	private final WeakReference<IMusicService> mService;
	
	public MusicLibraryConnection(IMusicService service) {
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
