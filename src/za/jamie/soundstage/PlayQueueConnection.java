package za.jamie.soundstage;

import java.lang.ref.WeakReference;

import android.os.RemoteException;

public class PlayQueueConnection implements MusicQueueWrapper {

	private final WeakReference<IMusicService> mService;
	
	public PlayQueueConnection(IMusicService service) {
		mService = new WeakReference<IMusicService>(service);
	}

	@Override
	public void setQueuePosition(int position) {
		if (mService.get() != null) {
			try {
				mService.get().setQueuePosition(position);
			} catch (RemoteException e) {
				
			}
		}
	}

	@Override
	public void moveQueueItem(int from, int to) {
		if (mService.get() != null) {
			try {
				mService.get().moveQueueItem(from, to);
			} catch (RemoteException e) {
				
			}
		}
	}

	@Override
	public void removeTrack(int position) {
		if (mService.get() != null) {
			try {
				mService.get().removeTrack(position);
			} catch (RemoteException e) {
				
			}
		}
	}

	@Override
	public void requestPlayQueue() {
		if (mService.get() != null) {
			try {
				mService.get().requestPlayQueue();
			} catch (RemoteException e) {
				
			}
		}
	}

	@Override
	public void registerPlayQueueCallback(IPlayQueueCallback callback) {
		if (mService.get() != null) {
			try {
				mService.get().registerPlayQueueCallback(callback);
			} catch (RemoteException e) {
				
			}
		}
	}

	@Override
	public void unregisterPlayQueueCallback(IPlayQueueCallback callback) {
		if (mService.get() != null) {
			try {
				mService.get().unregisterPlayQueueCallback(callback);
			} catch (RemoteException e) {
				
			}
		}
	}

}
