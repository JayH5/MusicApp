package za.jamie.soundstage.service;

import java.util.ArrayList;
import java.util.List;

import za.jamie.soundstage.IMusicService;
import za.jamie.soundstage.IMusicStatusCallback;
import za.jamie.soundstage.IPlayQueueCallback;
import za.jamie.soundstage.models.Track;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * Class to encapsulate the interface between the MusicService process
 * and the user-side app process. All the calls to the MusicService process
 * fail silently (with logging) if something goes wrong and return true if a
 * call was successful.
 */
public class MusicConnection implements ServiceConnection {

	private static final String TAG = "MusicServiceConnection";
	
	private IMusicService mService;
	private final List<ConnectionCallbacks> mCallbacks =
			new ArrayList<ConnectionCallbacks>();

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		mService = IMusicService.Stub.asInterface(service);
		for (ConnectionCallbacks callback : mCallbacks) {
			callback.onConnected();
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mService = null;
		for (ConnectionCallbacks callback : mCallbacks) {
			callback.onDisconnected();
		}
	}
	
	/**
	 * Modelled similarly to a Loader callback, this method registers a
	 * callback that receives calls when the service is connected or disconnected
	 * or if the service is already connected.
	 * @param callback
	 */
	public void requestConnectionCallbacks(ConnectionCallbacks callback) {
		mCallbacks.add(callback);
		if (mService != null) {
			callback.onConnected();
		}
	}
	
	public void releaseConnectionCallbacks(ConnectionCallbacks callback) {
		mCallbacks.remove(callback);
	}
	
	public boolean setQueuePosition(int position) {
		if (mService != null) {
			try {
				mService.setQueuePosition(position);
				return true;
			} catch (RemoteException e) {
				Log.w(TAG, "setQueuePosition(int)", e);
			}
		}
		return false;
	}
	
	public boolean moveQueueItem(int from, int to) {
		if (mService != null) {
			try {
				mService.moveQueueItem(from, to);
				return true;
			} catch (RemoteException e) {
				Log.w(TAG, "moveQueueItem(int, int)", e);
			}
		}
		return false;
	}
	
	public boolean removeTrack(int position) {
		if (mService != null) {
			try {
				mService.removeTrack(position);
				return true;
			} catch (RemoteException e) {
				Log.w(TAG, "removeTrack(int)", e);
			}
		}
		return false;
	}
	
	public boolean registerPlayQueueCallback(IPlayQueueCallback callback) {
		if (mService != null) {
			try {
				mService.registerPlayQueueCallback(callback);
				return true;
			} catch (RemoteException e) {
				Log.w(TAG, "registerPlayQueueCallback(IPlayQueueCallback)", e);
			}
		}
		return false;
	}
	
	public boolean unregisterPlayQueueCallback(IPlayQueueCallback callback) {
		if (mService != null) {
			try {
				mService.unregisterPlayQueueCallback(callback);
				return true;
			} catch (RemoteException e) {
				Log.w(TAG, "unregisterPlayQueueCallback(IPlayQueueCallback)", e);
			}
		}
		return false;
	}
	
	public boolean togglePlayback() {
		if (mService != null) {
			try {
				mService.togglePlayback();
				return true;
			} catch (RemoteException e) {
				Log.w(TAG, "togglePlayback()", e);
			}
		}
		return false;
	}
	
	public boolean next() {
		if (mService != null) {
			try {
				mService.next();
				return true;
			} catch (RemoteException e) {
				Log.w(TAG, "next()", e);
			}
		}
		return false;
	}
	
	public boolean previous() {
		if (mService != null) {
			try {
				mService.previous();
				return true;
			} catch (RemoteException e) {
				Log.w(TAG, "previous()", e);
			}
		}
		return false;
	}
	
	public boolean seek(long position) {
		if (mService != null) {
			try {
				mService.seek(position);
				return true;
			} catch (RemoteException e) {
				Log.w(TAG, "seek(long)", e);
			}
		}
		return false;
	}
	
	public boolean toggleShuffle() {
		if (mService != null) {
			try {
				mService.toggleShuffle();
				return true;
			} catch (RemoteException e) {
				Log.w(TAG, "toggleShuffle()", e);
			}
		}
		return false;
	}
	
	public boolean cycleRepeatMode() {
		if (mService != null) {
			try {
				mService.cycleRepeatMode();
				return true;
			} catch (RemoteException e) {
				Log.w(TAG, "cycleRepeatMode()", e);
			}
		}
		return false;
	}
	
	public boolean registerMusicStatusCallback(IMusicStatusCallback callback) {
		if (mService != null) {
			try {
				mService.registerMusicStatusCallback(callback);
				return true;
			} catch (RemoteException e) {
				Log.w(TAG, "registerMusicStatusCallback(IMusicStatusCallback)", e);
			}
		}
		return false;
	}
	
	public boolean unregisterMusicStatusCallback(IMusicStatusCallback callback) {
		if (mService != null) {
			try {
				mService.unregisterMusicStatusCallback(callback);
				return true;
			} catch (RemoteException e) {
				Log.w(TAG, "unregisterMusicStatusCallback(IMusicStatusCallback)", e);
			}
		}
		return false;
	}
	
	public boolean open(List<Track> tracks, int position) {
		if (mService != null) {
			try {
				mService.open(tracks, position);
				return true;
			} catch (RemoteException e) {
				Log.w(TAG, "open(List<Track>, int)", e);
			}
		}
		return false;
	}
	
	public boolean shuffle(List<Track> tracks) {
		if (mService != null) {
			try {
				mService.shuffle(tracks);
				return true;
			} catch (RemoteException e) {
				Log.w(TAG, "shuffle(List<Track>)", e);
			}
		}
		return false;
	}
	
	public boolean enqueue(List<Track> tracks, int action) {
		if (mService != null) {
			try {
				mService.enqueue(tracks, action);
				return true;
			} catch (RemoteException e) {
				Log.w(TAG, "enqueue(List<Track>, int)", e);
			}
		}
		return false;
	}
	
	public boolean showNotification(PendingIntent intent) {
		if (mService != null) {
			try {
				mService.showNotification(intent);
				return true;
			} catch (RemoteException e) {
				Log.w(TAG, "showNotification(PendingIntent)", e);
			}
		}
		return false;
	}
	
	public boolean hideNotification() {
		if (mService != null) {
			try {
				mService.hideNotification();
				return true;
			} catch (RemoteException e) {
				Log.w(TAG, "hideNotification()", e);
			}
		}
		return false;
	}
	
	/**
	 * A simple callback interface used to monitor the state of the connection to the Service.
	 */
	public interface ConnectionCallbacks {
		/**
		 * Corresponds to {@link ServiceConnection#onServiceConnected(ComponentName, IBinder)}
		 */
		void onConnected();
		
		/**
		 * Corresponds to {@link ServiceConnection#onServiceDisconnected(ComponentName)}
		 */
		void onDisconnected();
	}

}
