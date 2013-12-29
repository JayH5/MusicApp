package za.jamie.soundstage.service;

import java.lang.ref.WeakReference;
import java.util.List;

import za.jamie.soundstage.IMusicService;
import za.jamie.soundstage.IMusicStatusCallback;
import za.jamie.soundstage.IPlayQueueCallback;
import za.jamie.soundstage.models.MusicItem;
import za.jamie.soundstage.models.Track;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.os.RemoteException;

public class MusicServiceStub extends IMusicService.Stub {

	private final WeakReference<MusicService> mService;
	
	public MusicServiceStub(MusicService service) {
		mService = new WeakReference<MusicService>(service);
	}

	@Override
	public void setQueuePosition(int position) throws RemoteException {
		mService.get().setQueuePosition(position);
	}

	@Override
	public void moveQueueItem(int from, int to) throws RemoteException {
		mService.get().moveQueueItem(from, to);
	}

	@Override
	public void removeTrack(int position) throws RemoteException {
		mService.get().removeTrack(position);			
	}
	
	@Override
	public void savePlayQueueAsPlaylist(String playlistName) throws RemoteException {
		mService.get().savePlayQueueAsPlaylist(playlistName);
	}

	@Override
	public void registerMusicStatusCallback(IMusicStatusCallback callback)
			throws RemoteException {
		
		mService.get().registerMusicStatusCallback(callback);
	}

	@Override
	public void unregisterMusicStatusCallback(IMusicStatusCallback callback)
			throws RemoteException {
		
		mService.get().unregisterMusicStatusCallback(callback);
	}

	@Override
	public void open(List<Track> tracks, int position) 
			throws RemoteException {

		mService.get().open(tracks, position);
	}
	
	@Override
	public void shuffle(List<Track> tracks) throws RemoteException {
		mService.get().shuffle(tracks);
	}

	@Override
	public void enqueue(MusicItem item, int action) 
			throws RemoteException {

		mService.get().enqueue(item, action);
	}

	@Override
	public void registerPlayQueueCallback(IPlayQueueCallback callback)
			throws RemoteException {
		
		mService.get().registerPlayQueueCallback(callback);
	}

	@Override
	public void unregisterPlayQueueCallback(IPlayQueueCallback callback)
			throws RemoteException {
		
		mService.get().unregisterPlayQueueCallback(callback);
	}

	@Override
	public void togglePlayback() throws RemoteException {
		mService.get().togglePlayback();			
	}

	@Override
	public void next() throws RemoteException {
		mService.get().next();			
	}

	@Override
	public void previous() throws RemoteException {
		mService.get().previous();			
	}

	@Override
	public void seek(long position) throws RemoteException {
		mService.get().seek(position);			
	}

	@Override
	public void toggleShuffle() throws RemoteException {
		mService.get().toggleShuffle();			
	}

	@Override
	public void cycleRepeatMode() throws RemoteException {
		mService.get().cycleRepeat();			
	}

    @Override
    public void registerActivityStart(ComponentName activity) throws RemoteException {
        mService.get().registerActivityStart(activity);
    }

    @Override
    public void registerActivityStop(ComponentName activity) throws RemoteException {
        mService.get().registerActivityStop(activity);
    }

}
