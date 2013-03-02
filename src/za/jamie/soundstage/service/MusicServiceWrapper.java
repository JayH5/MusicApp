/*
 * Copyright (C) 2012 Andrew Neal Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package za.jamie.soundstage.service;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import za.jamie.soundstage.IMusicService;
import za.jamie.soundstage.models.Track;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

public final class MusicServiceWrapper {

    public static IMusicService mService = null;

    private static final WeakHashMap<Context, ServiceBinder> mConnectionMap;

    private static final List<Track> sEmptyList;

    //private static ContentValues[] mContentValuesCache = null;

    static {
        mConnectionMap = new WeakHashMap<Context, ServiceBinder>();
        sEmptyList = new ArrayList<Track>(0);
    }

    /* This class is never instantiated */
    public MusicServiceWrapper() {
    }

    /**
     * @param context The {@link Context} to use
     * @param callback The {@link ServiceConnection} to use
     * @return The new instance of {@link ServiceToken}
     */
    public static final ServiceToken bindToService(final Context context,
            final ServiceConnection callback) {
        Activity realActivity = ((Activity)context).getParent();
        if (realActivity == null) {
            realActivity = (Activity)context;
        }
        final ContextWrapper contextWrapper = new ContextWrapper(realActivity);
        contextWrapper.startService(new Intent(contextWrapper, MusicService.class));
        final ServiceBinder binder = new ServiceBinder(callback);
        if (contextWrapper.bindService(
                new Intent().setClass(contextWrapper, MusicService.class), binder, 0)) {
            mConnectionMap.put(contextWrapper, binder);
            return new ServiceToken(contextWrapper);
        }
        return null;
    }

    /**
     * @param token The {@link ServiceToken} to unbind from
     */
    public static void unbindFromService(final ServiceToken token) {
        if (token == null) {
            return;
        }
        final ContextWrapper mContextWrapper = token.mWrappedContext;
        final ServiceBinder mBinder = mConnectionMap.remove(mContextWrapper);
        if (mBinder == null) {
            return;
        }
        mContextWrapper.unbindService(mBinder);
        if (mConnectionMap.isEmpty()) {
            mService = null;
        }
    }

    public static final class ServiceBinder implements ServiceConnection {
        private final ServiceConnection mCallback;

        /**
         * Constructor of <code>ServiceBinder</code>
         * 
         * @param context The {@link ServiceConnection} to use
         */
        public ServiceBinder(final ServiceConnection callback) {
            mCallback = callback;
        }

        @Override
        public void onServiceConnected(final ComponentName className, final IBinder service) {
            mService = IMusicService.Stub.asInterface(service);
            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }
            mService = null;
        }
    }

    public static final class ServiceToken {
        public ContextWrapper mWrappedContext;

        /**
         * Constructor of <code>ServiceToken</code>
         * 
         * @param context The {@link ContextWrapper} to use
         */
        public ServiceToken(final ContextWrapper context) {
            mWrappedContext = context;
        }
    }

    /**
     * Changes to the next track
     */
    public static void next(Context context) {
        context.startService(new Intent(MusicService.ACTION_NEXT));
    }

    /**
     * Changes to the previous track.
     */
    public static void previous(Context context) {
    	context.startService(new Intent(MusicService.ACTION_PREVIOUS));
    }

    /**
     * Plays or pauses the music.
     */
    public static void togglePlayback(Context context) {
    	context.startService(new Intent(MusicService.ACTION_TOGGLE_PLAYBACK));
    }
    
    public static void play(Context context) {
    	context.startService(new Intent(MusicService.ACTION_PLAY));
    }
    
    public static void pause(Context context) {
    	context.startService(new Intent(MusicService.ACTION_PAUSE));
    }

    /**
     * Cycles through the repeat options.
     */
    public static void cycleRepeat(Context context) {
    	context.startService(new Intent(MusicService.ACTION_REPEAT));
    }

    /**
     * Cycles through the shuffle options.
     */
    public static void cycleShuffle(Context context) {
    	context.startService(new Intent(MusicService.ACTION_SHUFFLE));
    }

    /**
     * @return True if we're playing music, false otherwise.
     */
    public static final boolean isPlaying() {
        if (mService != null) {
            try {
                return mService.isPlaying();
            } catch (final RemoteException ignored) {
            }
        }
        return false;
    }

    /**
     * @return The current shuffle mode.
     */
    public static final int getShuffleMode() {
        if (mService != null) {
            try {
                return mService.getShuffleMode();
            } catch (final RemoteException ignored) {
            }
        }
        return 0;
    }

    /**
     * @return The current repeat mode.
     */
    public static final int getRepeatMode() {
        if (mService != null) {
            try {
                return mService.getRepeatMode();
            } catch (final RemoteException ignored) {
            }
        }
        return 0;
    }

    /**
     * @return The current track
     */
    public static final Track getCurrentTrack() {
    	if (mService != null) {
    		try {
    			return mService.getCurrentTrack();
    		} catch (final RemoteException ignored) {
    		}
    	}
    	return null;
    }

    /**
     * @return The queue.
     */
    public static final List<Track> getQueue() {
        try {
            if (mService != null) {
                return mService.getQueue();
            } else {
            }
        } catch (final RemoteException ignored) {
        }
        return sEmptyList;
    }

    /**
     * @param id The ID of the track to remove.
     * @return removes track from a playlist or the queue.
     */
    public static final int removeTrack(final long id) {
        try {
            if (mService != null) {
                return mService.removeTrack(id);
            }
        } catch (final RemoteException ingored) {
        }
        return 0;
    }

    /**
     * @return The position of the current track in the queue.
     */
    public static final int getQueuePosition() {
        try {
            if (mService != null) {
                return mService.getQueuePosition();
            }
        } catch (final RemoteException ignored) {
        }
        return 0;
    }

    /**
     * @param context The {@link Context} to use.
     * @param list The list of songs to play.
     * @param position Specify where to start.
     */
    public static void playAll(final Context context, final List<Track> list, int position) {
        if (list.size() == 0 || mService == null) {
            return;
        }
        try {
            if (position != -1 && list.equals(getQueue())) {
                if (position == getQueuePosition()) {
                    play(context);
                    return;
                } else {
                	mService.setQueuePosition(position);
                }
            }
            if (position < 0) {
                position = 0;
            }
            mService.open(list, position);
            play(context);
        } catch (final RemoteException ignored) {
        }
    }

    /**
     * @param list The list to enqueue.
     */
    public static void playNext(final List<Track> list) {
        if (mService == null) {
            return;
        }
        try {
            mService.enqueue(list, MusicService.NEXT);
        } catch (final RemoteException ignored) {
        }
    }

    /**
     * @param context The {@link Context} to use.
     */
    /*public static void shuffleAll(final Context context) {
        Cursor cursor = SongLoader.makeSongCursor(context);
        final long[] mTrackList = getSongListForCursor(cursor);
        final int position = 0;
        if (mTrackList.length == 0 || mService == null) {
            return;
        }
        try {
            mService.setShuffleMode(MusicService.SHUFFLE_NORMAL);
            final long mCurrentId = mService.getAudioId();
            final int mCurrentQueuePosition = getQueuePosition();
            if (position != -1 && mCurrentQueuePosition == position
                    && mCurrentId == mTrackList[position]) {
                final long[] mPlaylist = getQueue();
                if (Arrays.equals(mTrackList, mPlaylist)) {
                    mService.play();
                    return;
                }
            }
            mService.open(mTrackList, -1);
            mService.play();
            cursor.close();
            cursor = null;
        } catch (final RemoteException ignored) {
        }
    }*/
    
    /**
     * @param context The {@link Context} to use.
     * @param list The list to enqueue.
     */
    public static void addToQueue(final Context context, final List<Track> list) {
        if (mService == null) {
            return;
        }
        try {
            mService.enqueue(list, MusicService.LAST);
            // TODO: Toast or something to notify user tracks have been added to queue
        } catch (final RemoteException ignored) {
        }
    }

    /**
     * @param from The index the item is currently at.
     * @param to The index the item is moving to.
     */
    public static void moveQueueItem(final int from, final int to) {
        try {
            if (mService != null) {
                mService.moveQueueItem(from, to);
            } else {
            }
        } catch (final RemoteException ignored) {
        }
    }

    /**
     * Seeks the current track to a desired position
     * 
     * @param position The position to seek to
     */
    public static void seek(final long position) {
        if (mService != null) {
            try {
                mService.seek(position);
            } catch (final RemoteException ignored) {
            }
        }
    }

    /**
     * @return The current position time of the track
     */
    public static final long position() {
        if (mService != null) {
            try {
                return mService.position();
            } catch (final RemoteException ignored) {
            }
        }
        return 0;
    }

    /**
     * @param position The position to move the queue to
     */
    public static void setQueuePosition(final int position) {
        if (mService != null) {
            try {
                mService.setQueuePosition(position);
            } catch (final RemoteException ignored) {
            }
        }
    }

    /**
     * Clears the qeueue
     */
    public static void clearQueue() {
        try {
            mService.removeTracks(0, Integer.MAX_VALUE);
        } catch (final RemoteException ignored) {
        }
    }

    /**
     * Used to build and show a notification when Apollo is sent into the
     * background
     * 
     * @param context The {@link Context} to use.
     */
    public static void startBackgroundService(final Context context) {
        final Intent startBackground = new Intent(context, MusicService.class);
        startBackground.setAction(MusicService.START_BACKGROUND);
        context.startService(startBackground);
    }

    /**
     * Used to kill the current foreground notification
     * 
     * @param context The {@link Cotext} to use.
     */
    public static void killForegroundService(final Context context) {
        final Intent killForeground = new Intent(context, MusicService.class);
        killForeground.setAction(MusicService.KILL_FOREGROUND);
        context.startService(killForeground);
    }
    
}