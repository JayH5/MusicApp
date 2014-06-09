package za.jamie.soundstage.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.database.Observable;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import za.jamie.soundstage.models.MusicItem;
import za.jamie.soundstage.utils.MessengerUtils;

/**
 * Created by jamie on 2014/06/06.
 */
public class MusicConnection implements ServiceConnection {

    private static final String TAG = "MusicConnection";

    public static final int MSG_TOGGLE_PLAYBACK = 1;
    public static final int MSG_NEXT = 2;
    public static final int MSG_PREVIOUS = 3;
    public static final int MSG_SEEK = 4;
    public static final int MSG_TOGGLE_SHUFFLE = 5;
    public static final int MSG_CYCLE_REPEAT = 6;

    public static final int MSG_MOVE_QUEUE_ITEM = 7;
    public static final int MSG_REMOVE_QUEUE_ITEM = 8;
    public static final int MSG_SET_QUEUE_POSITION = 9;

    public static final int MSG_OPEN = 10;
    public static final int MSG_ENQUEUE = 11;
    public static final int MSG_SHUFFLE = 12;
    public static final int MSG_PLAY_ALL = 13;

    public static final int MSG_REQUEST_PLAYER_UPDATE = 14;
    public static final int MSG_REGISTER_PLAYER_CLIENT = 15;
    public static final int MSG_UNREGISTER_PLAYER_CLIENT = 16;

    public static final int MSG_REQUEST_QUEUE_UPDATE = 17;
    public static final int MSG_REGISTER_QUEUE_CLIENT = 18;
    public static final int MSG_UNREGISTER_QUEUE_CLIENT = 19;

    public static final int MSG_REGISTER_ACTIVITY_START = 20;
    public static final int MSG_REGISTER_ACTIVITY_STOP = 21;

    public static final int MSG_GET_AUDIO_SESSION_ID = 22;

    private Messenger mService;

    private final ConnectionObservable mObservable = new ConnectionObservable();

    public MusicConnection() {
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mService = new Messenger(service);
        mObservable.notifyConnected();
        Log.d(TAG, "Connected");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
        mObservable.notifyDisconnected();
        Log.d(TAG, "Disconnected");
    }

    public void registerConnectionObserver(ConnectionObserver observer) {
        mObservable.registerObserver(observer);
    }

    public void unregisterConnectionObserver(ConnectionObserver observer) {
        mObservable.unregisterObserver(observer);
    }

    public boolean requestPlayerUpdate(Messenger client) {
        if (mService != null) {
            try {
                MessengerUtils.send(mService, MSG_REQUEST_PLAYER_UPDATE, client);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    public boolean registerPlayerClient(Messenger client) {
        if (mService != null) {
            try {
                MessengerUtils.send(mService, MSG_REGISTER_PLAYER_CLIENT, client);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    public boolean unregisterPlayerClient(Messenger client) {
        if (mService != null) {
            try {
                MessengerUtils.send(mService, MSG_UNREGISTER_PLAYER_CLIENT, client);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    public boolean requestQueueUpdate(Messenger client) {
        if (mService != null) {
            try {
                MessengerUtils.send(mService, MSG_REQUEST_QUEUE_UPDATE, client);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    public boolean registerQueueClient(Messenger client) {
        if (mService != null) {
            try {
                MessengerUtils.send(mService, MSG_REGISTER_QUEUE_CLIENT, client);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    public boolean unregisterQueueClient(Messenger client) {
        if (mService != null) {
            try {
                MessengerUtils.send(mService, MSG_UNREGISTER_QUEUE_CLIENT, client);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    public boolean togglePlayback() {
        if (mService != null) {
            try {
                MessengerUtils.send(mService, MSG_TOGGLE_PLAYBACK);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    public boolean next() {
        if (mService != null) {
            try {
                MessengerUtils.send(mService, MSG_NEXT);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    public boolean previous() {
        if (mService != null) {
            try {
                MessengerUtils.send(mService, MSG_PREVIOUS);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    public boolean seek(int position) {
        if (mService != null) {
            try {
                MessengerUtils.send(mService, MSG_SEEK, position);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    public boolean toggleShuffle() {
        if (mService != null) {
            try {
                MessengerUtils.send(mService, MSG_TOGGLE_SHUFFLE);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    public boolean cycleRepeat() {
        if (mService != null) {
            try {
                MessengerUtils.send(mService, MSG_CYCLE_REPEAT);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    public boolean moveQueueItem(int from, int to) {
        if (mService != null) {
            try {
                MessengerUtils.send(mService, MSG_MOVE_QUEUE_ITEM, from, to);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    public boolean removeQueueItem(int position) {
        if (mService != null) {
            try {
                MessengerUtils.send(mService, MSG_REMOVE_QUEUE_ITEM, position);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    public boolean setQueuePosition(int position) {
        if (mService != null) {
            try {
                MessengerUtils.send(mService, MSG_SET_QUEUE_POSITION, position);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    public boolean open(MusicItem item, int position) {
        if (mService != null) {
            Message msg = Message.obtain(null, MSG_OPEN);
            msg.getData().putParcelable("item", item);
            msg.arg1 = position;
            try {
                mService.send(msg);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    public boolean enqueue(MusicItem item, int action) {
        if (mService != null) {
            Message msg = Message.obtain(null, MSG_ENQUEUE);
            msg.getData().putParcelable("item", item);
            msg.arg1 = action;
            try {
                mService.send(msg);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    public boolean shuffle(MusicItem item) {
        if (mService != null) {
            Message msg = Message.obtain(null, MSG_SHUFFLE);
            msg.getData().putParcelable("item", item);
            try {
                mService.send(msg);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    public boolean playAll(int position) {
        if (mService != null) {
            try {
                MessengerUtils.send(mService, MSG_PLAY_ALL, position);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    public boolean registerActivityStart(ComponentName activity) {
        if (mService != null) {
            try {
                MessengerUtils.send(mService, MSG_REGISTER_ACTIVITY_START, activity);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    public boolean registerActivityStop(ComponentName activity) {
        if (mService != null) {
            try {
                MessengerUtils.send(mService, MSG_REGISTER_ACTIVITY_STOP, activity);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    public boolean getAudioSessionId(Messenger replyTo) {
        if (mService != null) {
            try {
                MessengerUtils.send(mService, MSG_GET_AUDIO_SESSION_ID, replyTo);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Service object died", e);
            }
        }
        return false;
    }

    private class ConnectionObservable extends Observable<ConnectionObserver> {
        @Override
        public void registerObserver(ConnectionObserver observer) {
            super.registerObserver(observer);
            if (mService != null) {
                observer.onConnected();
            }
        }

        public void notifyConnected() {
            synchronized (mObservers) {
                for (int i = mObservers.size() - 1; i>= 0; i--) {
                    mObservers.get(i).onConnected();
                }
            }
        }

        public void notifyDisconnected() {
            synchronized (mObservers) {
                for (int i = mObservers.size() - 1; i>= 0; i--) {
                    mObservers.get(i).onDisconnected();
                }
            }
        }
    }

    /**
     * A simple callback object used to monitor the state of the connection to the Service.
     */
    public static class ConnectionObserver {
        /**
         * Corresponds to {@link ServiceConnection#onServiceConnected(ComponentName, IBinder)}
         */
        public void onConnected() {
        }

        /**
         * Corresponds to {@link ServiceConnection#onServiceDisconnected(ComponentName)}
         */
        public void onDisconnected() {
        }
    }

}
