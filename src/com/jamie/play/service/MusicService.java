package com.jamie.play.service;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore;

import com.jamie.play.IMusicService;
import com.jamie.play.bitmapfun.ImageFetcher;
import com.jamie.play.utils.AppUtils;
import com.jamie.play.utils.ImageUtils;

/**
 * A backbround {@link Service} used to keep music playing between activities
 * and when the user moves Apollo into the background.
 */
public class MusicService extends Service {

	//private static final String TAG = "MusicService";
	
	// Intent actions for external control
	public static final String SERVICECMD = "com.jamie.play.musicservicecommand";
	public static final String ACTION_TOGGLE_PLAYBACK = "com.jamie.play.action.TOGGLE_PLAYBACK";
    public static final String ACTION_PLAY = "com.jamie.play.action.PLAY";
    public static final String ACTION_PAUSE = "com.jamie.play.action.PAUSE";
    public static final String ACTION_STOP = "com.jamie.play.action.STOP";
    public static final String ACTION_NEXT = "com.jamie.play.action.NEXT";
    public static final String ACTION_PREVIOUS = "com.jamie.play.action.PREVIOUS";
    public static final String ACTION_REPEAT = "com.jamie.play.action.REPEAT";
    public static final String ACTION_SHUFFLE = "com.jamie.play.action.SHUFFLE";
    
    public static final String KILL_FOREGROUND = "com.jamie.play.killforeground";
    public static final String REFRESH = "com.jamie.play.refresh";
    public static final String START_BACKGROUND = "com.jamie.play.startbackground";
    
    // Broadcast receiver command names
    public static final String CMD_NAME = "command";
    public static final String CMD_TOGGLE_PLAYBACK = "toggleplayback";
    public static final String CMD_STOP = "stop";
    public static final String CMD_PAUSE = "pause";
    public static final String CMD_PLAY = "play";
    public static final String CMD_PREVIOUS = "previous";
    public static final String CMD_NEXT = "next";
    public static final String CMD_NOTIF = "buttonId";
    
    // Player handler messages
    private static final int TRACK_ENDED = 1;
    private static final int TRACK_WENT_TO_NEXT = 2;
    private static final int RELEASE_WAKELOCK = 3;
    private static final int SERVER_DIED = 4;
    private static final int FOCUSCHANGE = 5;
    private static final int FADEDOWN = 6;
    private static final int FADEUP = 7;
    
    // Status change flags
 	public static final String PLAYSTATE_CHANGED = "com.jamie.play.playstatechanged";
    public static final String META_CHANGED = "com.jamie.play.metachanged";
    public static final String QUEUE_CHANGED = "com.jamie.play.queuechanged";
    public static final String REPEATMODE_CHANGED = "com.jamie.play.repeatmodechanged";
    public static final String SHUFFLEMODE_CHANGED = "com.jamie.play.shufflemodechanged";
    
    // Shuffle modes
    public static final int SHUFFLE_NONE = 0;
    public static final int SHUFFLE_NORMAL = 1;
    public static final int SHUFFLE_AUTO = 2;
    private int mShuffleMode = SHUFFLE_NONE;
    
    private static final Shuffler mShuffler = new Shuffler();
    private List<Track> mAutoShuffleList;

    // Repeat modes
    public static final int REPEAT_NONE = 0;
    public static final int REPEAT_CURRENT = 1;
    public static final int REPEAT_ALL = 2;
    private int mRepeatMode = REPEAT_NONE;
    
    // Different ways to add items to the queue
    public static final int NOW = 1;
    public static final int NEXT = 2;
    public static final int LAST = 3;
    
    // Service status information
    private boolean mServiceInUse = false;
    private boolean mIsSupposedToBePlaying = false;
    private boolean mQueueIsSaveable = true;
    private boolean mPausedByTransientLossOfFocus = false;
    
    // Queue of tracks to be played
    private List<Track> mPlayQueue;
    private int mPlayListLen = 0;
    private int mPlayPos = -1;
    private int mNextPlayPos = -1;
    
    private static final Uri BASE_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private static final String[] PROJECTION = new String[] {
    	MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST_ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ALBUM
    };
    
    private int mOpenFailedCounter = 0;
    
    // The queue is saved/loaded from a SharedPreferences instance and stored
    // as a String representation of a hex number as this is faster than storing
    // long id values.
    private SharedPreferences mPreferences;
    private int mCardId;
    
    // Keep track of previously played tracks
    private static final LinkedList<Integer> mHistory = new LinkedList<Integer>();
    private static final int MAX_HISTORY_SIZE = 100;

    // Audio playback objects
    private AudioManager mAudioManager;
    private MultiPlayer mPlayer;
    private MusicPlayerHandler mPlayerHandler;
    private DelayedHandler mDelayedStopHandler;
    
    private NotificationHelper mNotificationHelper; // Notifications 
    private boolean mBuildNotification = false;
    private RemoteControlClient mRemoteControlClient; // Lockscreen controls
    private ComponentName mMediaButtonReceiverComponent; // Media buttons
    
    private BroadcastReceiver mUnmountReceiver = null;
    private int mMediaMountedCount = 0;
    
    private final OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
		
		@Override
		public void onAudioFocusChange(int focusChange) {
			mPlayerHandler.obtainMessage(FOCUSCHANGE, focusChange, 0);
			
		}
	};
    
    // Delay before releasing the media player to ensure fast stop/start
    private static final int IDLE_DELAY = 60000;
	
	private final IBinder mBinder = new ServiceStub(this);
	private int mServiceStartId = -1;
	private WakeLock mWakeLock;
	
	//private ImageFetcher mImageWorker;
    
    @Override
    public IBinder onBind(final Intent intent) {
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mServiceInUse = true;
        return mBinder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onUnbind(final Intent intent) {
        mServiceInUse = false;
        saveQueue(true);

        if (mIsSupposedToBePlaying || mPausedByTransientLossOfFocus) {
            // Something is currently playing, or will be playing once
            // an in-progress action requesting audio focus ends, so don't stop
            // the service now.
            return true;

            // If there is a playlist but playback is paused, then wait a while
            // before stopping the service, so that pause/resume isn't slow.
            // Also delay stopping the service if we're transitioning between
            // tracks.
        } else if (mPlayListLen > 0 || mPlayerHandler.hasMessages(TRACK_ENDED)) {
            final Message msg = mDelayedStopHandler.obtainMessage();
            mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
            return true;
        }
        stopSelf(mServiceStartId);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRebind(final Intent intent) {
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mServiceInUse = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // TODO: Recents record
        // Initialize the favorites and recents databases
        //mRecentsCache = RecentStore.getInstance(this);
        //mFavoritesCache = FavoritesStore.getInstance(this);

        // Initialze the notification helper
        mNotificationHelper = new NotificationHelper(this);

        // TODO: Create way to give Music Service access to imagecache
        // without it having its own instance
        // Initialize the image worker
        //mImageWorker = ImageUtils.getImageFetcher(this);

        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work will not disrupt the UI.
        final HandlerThread thread = new HandlerThread("MusicPlayerHandler",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Initialize the handlers
        mPlayerHandler = new MusicPlayerHandler(this, thread.getLooper());
        mDelayedStopHandler = new DelayedHandler(this);

        // Initialze the audio manager and register any headset controls for
        // playback
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        initRemoteControlClient();

        // Initialize the preferences
        mPreferences = getSharedPreferences("Service", 0);
        mCardId = getCardId();

        registerExternalStorageListener();

        // Initialze the media player
        mPlayer = new MultiPlayer(this);
        mPlayer.setHandler(mPlayerHandler);

        initBroadcastReceiver();

        // Initialize the wake lock
        final PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.setReferenceCounted(false);

        // Bring the queue back
        reloadQueue();
        notifyChange(QUEUE_CHANGED);
        notifyChange(META_CHANGED);

        // Listen for the idle state
        final Message message = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(message, IDLE_DELAY);
    }

    /**
     * Initializes the remote control client
     */
    private void initRemoteControlClient() {
        mMediaButtonReceiverComponent = new ComponentName(this, MusicIntentReceiver.class);
        mAudioManager.registerMediaButtonEventReceiver(mMediaButtonReceiverComponent);
        
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(mMediaButtonReceiverComponent);
        PendingIntent mediaPendingIntent = PendingIntent
        		.getBroadcast(getApplicationContext(), 0, mediaButtonIntent, 0);
        
        mRemoteControlClient = new RemoteControlClient(mediaPendingIntent);
        mAudioManager.registerRemoteControlClient(mRemoteControlClient);
		
		// Flags for the media transport control that this client supports.
        final int flags = RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
        		| RemoteControlClient.FLAG_KEY_MEDIA_NEXT
                | RemoteControlClient.FLAG_KEY_MEDIA_PLAY
                | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
                | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
                | RemoteControlClient.FLAG_KEY_MEDIA_STOP;
        mRemoteControlClient.setTransportControlFlags(flags);
    }
    
    private void initBroadcastReceiver() {
    	// Initialze the intent filter and each action
        final IntentFilter filter = new IntentFilter();
        filter.addAction(SERVICECMD);
        filter.addAction(ACTION_TOGGLE_PLAYBACK);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_STOP);
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_PREVIOUS);
        filter.addAction(ACTION_REPEAT);
        filter.addAction(ACTION_SHUFFLE);
        filter.addAction(KILL_FOREGROUND);
        filter.addAction(START_BACKGROUND);
        //filter.addAction(UPDATE_LOCKSCREEN);
        // Attach the broadcast listener
        registerReceiver(mIntentReceiver, filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // Remove any sound effects
        final Intent audioEffectsIntent = new Intent(
        		AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(audioEffectsIntent);

        // Release the player
        mPlayer.release();
        mPlayer = null;

        // Remove the audio focus listener and lock screen controls
        mAudioManager.abandonAudioFocus(mAudioFocusListener);
        mAudioManager.unregisterRemoteControlClient(mRemoteControlClient);
        mAudioManager.unregisterMediaButtonEventReceiver(mMediaButtonReceiverComponent);

        // Remove any callbacks from the handlers
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mPlayerHandler.removeCallbacksAndMessages(null);

        // Unregister the mount listener
        unregisterReceiver(mIntentReceiver);
        if (mUnmountReceiver != null) {
            unregisterReceiver(mUnmountReceiver);
            mUnmountReceiver = null;
        }

        // Release the wake lock
        mWakeLock.release();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        mServiceStartId = startId;
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        if (intent != null) {
            final String action = intent.getAction();
            final String command = intent.getStringExtra("command");
            if (CMD_NEXT.equals(command) || ACTION_NEXT.equals(action)) {
                gotoNext(true);
            } else if (CMD_PREVIOUS.equals(command) || ACTION_PREVIOUS.equals(action)) {
                if (position() < 2000) {
                    prev();
                } else {
                    seek(0);
                    play();
                }
            } else if (CMD_TOGGLE_PLAYBACK.equals(command) || ACTION_TOGGLE_PLAYBACK.equals(action)) {
                if (mIsSupposedToBePlaying) {
                    pause();
                    mPausedByTransientLossOfFocus = false;
                } else {
                    play();
                }
            } else if (CMD_PAUSE.equals(command) || ACTION_PAUSE.equals(action)) {
                pause();
                mPausedByTransientLossOfFocus = false;
            } else if (CMD_PLAY.equals(command)) {
                play();
            } else if (CMD_STOP.equals(command) || ACTION_STOP.equals(action)) {
                pause();
                mPausedByTransientLossOfFocus = false;
                seek(0);
                killNotification();
                mBuildNotification = false;
            } else if (ACTION_REPEAT.equals(action)) {
                cycleRepeat();
            } else if (ACTION_SHUFFLE.equals(action)) {
                cycleShuffle();
            } else if (KILL_FOREGROUND.equals(action)) {
                mBuildNotification = false;
                killNotification();
            } else if (START_BACKGROUND.equals(action)) {
                mBuildNotification = true;
                buildNotification();
            } /*else if (UPDATE_LOCKSCREEN.equals(action)) {
                mEnableLockscreenControls = intent.getBooleanExtra(UPDATE_LOCKSCREEN, true);
                if (mEnableLockscreenControls) {
                    setUpRemoteControlClient();
                    // Update the controls according to the current playback
                    notifyChange(PLAYSTATE_CHANGED);
                    notifyChange(META_CHANGED);
                } else {
                    // Remove then unregister the conrols
                    mRemoteControlClientCompat
                            .setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
                    RemoteControlHelper.unregisterRemoteControlClient(mAudioManager,
                            mRemoteControlClientCompat);
                }
            }*/
        }

        // Make sure the service will shut down on its own if it was
        // just started but not bound to and nothing is playing
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        final Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
        return START_STICKY;
    }

    public void buildNotification() {
        if (mBuildNotification || AppUtils.isApplicationSentToBackground(this)) {
            try {
                mNotificationHelper.buildNotification(getTrackName(), getArtistName(),
                        getAlbumName(), getAlbumArt());
            } catch (final IllegalStateException parcelBitmap) {
                parcelBitmap.printStackTrace();
            }
        }
    }

    /**
     * Removes the foreground notification
     */
    public void killNotification() {
        stopForeground(true);
    }

    /**
     * @return A card ID used to save and restore playlists, i.e., the queue.
     */
    private int getCardId() {
        final ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(Uri.parse("content://media/external/fs_id"), null, null,
                null, null);
        int mCardId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            mCardId = cursor.getInt(0);
            cursor.close();
            cursor = null;
        }
        return mCardId;
    }

    /**
     * Called when we receive a ACTION_MEDIA_EJECT notification.
     * 
     * @param storagePath The path to mount point for the removed media
     */
    public void closeExternalStorageFiles(final String storagePath) {
        stop(true);
        notifyChange(QUEUE_CHANGED);
        notifyChange(META_CHANGED);
    }

    /**
     * Registers an intent to listen for ACTION_MEDIA_EJECT notifications. The
     * intent will call closeExternalStorageFiles() if the external media is
     * going to be ejected, so applications can clean up any files they have
     * open.
     */
    public void registerExternalStorageListener() {
        if (mUnmountReceiver == null) {
            mUnmountReceiver = new BroadcastReceiver() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onReceive(final Context context, final Intent intent) {
                    final String action = intent.getAction();
                    if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                        saveQueue(true);
                        mQueueIsSaveable = false;
                        closeExternalStorageFiles(intent.getData().getPath());
                    } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                        mMediaMountedCount++;
                        mCardId = getCardId();
                        reloadQueue();
                        mQueueIsSaveable = true;
                        notifyChange(QUEUE_CHANGED);
                        notifyChange(META_CHANGED);
                    }
                }
            };
            final IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MEDIA_EJECT);
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addDataScheme("file");
            registerReceiver(mUnmountReceiver, filter);
        }
    }

    /**
     * Changes the notification buttons to a paused state and beging the
     * countdown to calling {@code #stopForeground(true)}
     */
    private void gotoIdleState() {
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        final Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
        if (mBuildNotification) {
            mNotificationHelper.goToIdleState(mIsSupposedToBePlaying);
        }
        mDelayedStopHandler.postDelayed(new Runnable() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void run() {
                killNotification();
            }
        }, IDLE_DELAY);
    }

    /**
     * Stops playback
     * 
     * @param remove_status_icon True to go to the idle state, false otherwise
     */
    private void stop(final boolean removeStatusIcon) {
        if (mPlayer.isInitialized()) {
            mPlayer.stop();
        }
        if (removeStatusIcon) {
            gotoIdleState();
        } else {
            stopForeground(false);
        }
        if (removeStatusIcon) {
            mIsSupposedToBePlaying = false;
        }
    }

    /**
     * Adds a list to the playlist
     * 
     * @param list The list to add
     * @param position The position to place the tracks
     */
    private void addToPlayList(final List<Track> list, int position) {
        // If position < 0 it signals that a new queue should be created
        if (position < 0) {
            mPlayListLen = 0;
            position = 0;
            mPlayQueue.clear();
        } else if (position > mPlayListLen) {
            position = mPlayListLen;
        }
        
        mPlayQueue.addAll(position, list);
        
        mPlayListLen = mPlayQueue.size();
        if (mPlayListLen == 0) {
            notifyChange(META_CHANGED);
        }
    }

    /**
     * Called to open a new file as the current track and prepare the next for
     * playback
     */
    private void openCurrentAndNext() {
        openCurrentAndMaybeNext(true);
    }

    /**
     * Called to open a new file as the current track and prepare the next for
     * playback
     * 
     * @param openNext True to prepare the next track for playback, false
     *            otherwise.
     */
    private void openCurrentAndMaybeNext(final boolean openNext) {
        synchronized (this) {
            if (mPlayListLen == 0) {
                return;
            }
            stop(false);
            
            // While the next track fails to open
            while (!openUri(mPlayQueue.get(mPlayPos).getUri())) {
                if (mOpenFailedCounter++ < 10 && mPlayListLen > 1) {
                    // Try to get the next play position
                	final int pos = getNextPosition(false);
                    // If that position is invalid, stop trying
                	if (pos < 0) {
                        gotoIdleState();
                        if (mIsSupposedToBePlaying) {
                            mIsSupposedToBePlaying = false;
                            notifyChange(PLAYSTATE_CHANGED);
                        }
                        return;
                    }
                    mPlayPos = pos;
                    stop(false);
                // If we've tried to play too many times, give up
                } else {
                    mOpenFailedCounter = 0;
                    gotoIdleState();
                    if (mIsSupposedToBePlaying) {
                        mIsSupposedToBePlaying = false;
                        notifyChange(PLAYSTATE_CHANGED);
                    }
                    return;
                }
            }
            // Prepare the track if requested
            if (openNext) {
                setNextTrack();
            }
        }
    }

    /**
     * @param force True to force the player onto the track next, false
     *            otherwise.
     * @return The next position to play.
     */
    private int getNextPosition(final boolean force) {
        if (!force && mRepeatMode == REPEAT_CURRENT) {
            if (mPlayPos < 0) {
                return 0;
            }
            return mPlayPos;
        } else if (mShuffleMode == SHUFFLE_NORMAL) {
            if (mPlayPos >= 0) {
                mHistory.add(mPlayPos);
            }
            if (mHistory.size() > MAX_HISTORY_SIZE) {
                mHistory.remove(0);
            }
            final int numTracks = mPlayListLen;
            // Create ascending array of play positions
            final int[] tracks = new int[numTracks];
            for (int i = 0; i < numTracks; i++) {
                tracks[i] = i;
            }

            int numUnplayed = numTracks;
            // Mark off all the tracks that have been played
            for (int idx : mHistory) {
            	if (idx < numTracks && tracks[idx] >= 0) {
                    numUnplayed--;
                    tracks[idx] = -1;
                }
            }
            
            // If they've all been played...
            if (numUnplayed <= 0) {
                // If repeat is on, mark them all as unplayed again
            	if (mRepeatMode == REPEAT_ALL || force) {
                    numUnplayed = numTracks;
                    for (int i = 0; i < numTracks; i++) {
                        tracks[i] = i;
                    }
                // Otherwise stop playing
            	} else {
                    return -1;
                }
            }
            
            // Skip to a position using the shuffler
            int skip = mShuffler.nextInt(numUnplayed);
            int position = 0;
            do {
            	// Find first unplayed track
            	while (tracks[position] < 0) {
                    position++;
                }
                skip--;
            } while (skip >= 0);            
            return position;
        } else if (mShuffleMode == SHUFFLE_AUTO) {
            doAutoShuffleUpdate();
            return mPlayPos + 1;
        } else {
            if (mPlayPos >= mPlayListLen - 1) {
                if (mRepeatMode == REPEAT_NONE && !force) {
                    return -1;
                } else if (mRepeatMode == REPEAT_ALL || force) {
                    return 0;
                }
                return -1;
            } else {
                return mPlayPos + 1;
            }
        }
    }

    /**
     * Sets the track track to be played
     */
    private void setNextTrack() {
        mNextPlayPos = getNextPosition(false);
        if (mNextPlayPos >= 0 && mPlayQueue != null) {
            final Track track = mPlayQueue.get(mNextPlayPos);
            mPlayer.setNextDataSource(track.getUri());
        }
    }

    /**
     * Creates a shuffled playlist used for party mode
     */
    private boolean makeAutoShuffleList() {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
            		BASE_URI, PROJECTION, MediaStore.Audio.Media.IS_MUSIC + "=1", 
                    null, null);
            if (cursor == null || cursor.getCount() == 0) {
                // It's ok if the cursor is not null here since it will still 
            	// be closed in the finally clause
            	return false;
            }

            final List<Track> list = new ArrayList<Track>(cursor.getCount());
            if (cursor.moveToFirst()) {
            	do {
            		list.add(cursorToTrack(cursor));
            	} while (cursor.moveToNext());
            }
            mAutoShuffleList = list;
            return true;
        } catch (final RuntimeException e) {
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return false;
    }
    
    private Track cursorToTrack(Cursor cursor) {
    	return new Track(cursor.getLong(0), 
        		cursor.getString(1), 
        		cursor.getLong(2), 
        		cursor.getString(3), 
        		cursor.getLong(4), 
        		cursor.getString(5));
    }

    /**
     * Creates the party shuffle playlist
     */
    private void doAutoShuffleUpdate() {
        boolean notify = false;
        if (mPlayPos > 10) {
            removeTracks(0, mPlayPos - 9);
            notify = true;
        }
        final int toAdd = 7 - (mPlayListLen - (mPlayPos < 0 ? -1 : mPlayPos));
        for (int i = 0; i < toAdd; i++) {
            int lookback = mHistory.size();
            int idx = -1;
            while (true) {
                idx = mShuffler.nextInt(mAutoShuffleList.size());
                if (!wasRecentlyUsed(idx, lookback)) {
                    break;
                }
                lookback /= 2;
            }
            mHistory.add(idx);
            if (mHistory.size() > MAX_HISTORY_SIZE) {
                mHistory.remove(0);
            }
            mPlayQueue.add(mAutoShuffleList.get(idx));
            mPlayListLen++;
            notify = true;
        }
        if (notify) {
            notifyChange(QUEUE_CHANGED);
        }
    }

    /**/
    private boolean wasRecentlyUsed(final int idx, int lookbacksize) {
        if (lookbacksize == 0) {
            return false;
        }
        final int histsize = mHistory.size();
        if (histsize < lookbacksize) {
            lookbacksize = histsize;
        }
        final int maxidx = histsize - 1;
        for (int i = 0; i < lookbacksize; i++) {
            final long entry = mHistory.get(maxidx - i);
            if (entry == idx) {
                return true;
            }
        }
        return false;
    }

    /**
     * Notify the change-receivers that something has changed.
     */
    private void notifyChange(final String what) {
        final Intent intent = new Intent(what);
        sendStickyBroadcast(intent);

        // Update the lockscreen controls
        updateRemoteControlClient(what);

        if (what.equals(META_CHANGED)) {
            // Increase the play count for favorite songs.
            /*if (mFavoritesCache.getSongId(getAudioId()) != null) {
                mFavoritesCache.addSongId(getAudioId(), getTrackName(), getAlbumName(),
                        getArtistName());
            }
            // Add the track to the recently played list.
            mRecentsCache.addAlbumId(getAlbumId(), getAlbumName(), getArtistName(),
                    MusicUtils.getSongCountForAlbum(this, getAlbumName()),
                    MusicUtils.getReleaseDateForAlbum(this, getAlbumName()));*/
        } else if (what.equals(QUEUE_CHANGED)) {
            saveQueue(true);
        } else {
            saveQueue(false);
        }

        // Update the app-widgets
        /*mAppWidgetSmall.notifyChange(this, what);
        mAppWidgetLarge.notifyChange(this, what);
        mAppWidgetLargeAlternate.notifyChange(this, what);
        if (ApolloUtils.hasHoneycomb()) {
            mRecentWidgetProvider.notifyChange(this, what);
        }*/
    }

    /**
     * Updates the lockscreen controls, if enabled.
     * 
     * @param what The broadcast
     */
    private void updateRemoteControlClient(final String what) {
    	if (what.equals(PLAYSTATE_CHANGED)) {
    		// If the playstate changes notify the lock screen
            // controls
            mRemoteControlClient
            		.setPlaybackState(mIsSupposedToBePlaying ? RemoteControlClient.PLAYSTATE_PLAYING
            				: RemoteControlClient.PLAYSTATE_PAUSED);
        } else if (what.equals(META_CHANGED)) {
        	// Update the lockscreen controls
            mRemoteControlClient
            		.editMetadata(true)
                    .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, getArtistName())
                    .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, getAlbumName())
                    .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, getTrackName())
                    .putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, duration())
                    .putBitmap(
                           RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK,
                           getAlbumArt()).apply();
        }
    }    	
    
    /**
     * Saves the queue
     * 
     * @param full True if the queue is full
     */
    private void saveQueue(final boolean full) {
        if (!mQueueIsSaveable) {
            return;
        }

        final SharedPreferences.Editor editor = mPreferences.edit();
        if (full) {
        	if (mPlayQueue != null) {
        		editor.putString("queue", queueToHexString2());
        	}
            
            editor.putInt("cardid", mCardId);
            if (mShuffleMode != SHUFFLE_NONE) {
                editor.putString("history", historyToHexString2());
            }
        }
        editor.putInt("curpos", mPlayPos);
        if (mPlayer.isInitialized()) {
            editor.putLong("seekpos", mPlayer.position());
        }
        editor.putInt("repeatmode", mRepeatMode);
        editor.putInt("shufflemode", mShuffleMode);
        editor.apply();
    }
    
    
    
    private String queueToHexString2() {
    	final StringBuilder builder = new StringBuilder();
    	for (Track track : mPlayQueue) {
    		builder.append(Long.toHexString(track.getId()));
    		builder.append(';');
    	}
    	return builder.toString();
    }
    
    private String historyToHexString2() {
    	final StringBuilder builder = new StringBuilder();
    	for (int id : mHistory) {
    		builder.append(Integer.toHexString(id));
    		builder.append(';');
    	}
    	return builder.toString();
    }
    
    /*private String historyToHexString() {
    	final StringBuilder q = new StringBuilder();
    	for (long n : mHistory) {
    		if (n == 0) {
                q.append("0;");
            } else {
                while (n != 0) {
                    final int digit = (int) n & 0xf;
                    n >>>= 4;
                    q.append(HEX_DIGITS[digit]);
                }
                q.append(";");
            }
    	}
    	return q.toString();
    }
    
    private String queueToHexString() {
    	final StringBuilder q = new StringBuilder();
        for (Track track : mPlayQueue) {
            long n = track.getId();
            if (n < 0) {
                continue;
            } else if (n == 0) {
                q.append("0;");
            } else {
                while (n != 0) {
                    final int digit = (int)(n & 0xf);
                    n >>>= 4;
                    q.append(HEX_DIGITS[digit]);
                }
                q.append(";");
            }
        }
        return q.toString();
    }*/

    /**
     * Reloads the queue as the user left it
     */
    private void reloadQueue() {
        String q = null;
        int id = mCardId;
        if (mPreferences.contains("cardid")) {
            id = mPreferences.getInt("cardid", ~mCardId);
        }
        if (id == mCardId) {
            q = mPreferences.getString("queue", "");
        }
        if (q != null && !q.isEmpty()) {
        	//List<Long> trackIds = hexStringToTrackIds(q);        	
            //mPlayListLen = trackIds.size();
            
        	long[] trackIds = hexStringToTrackIds2(q);
        	mPlayListLen = trackIds.length;
        	
            final int pos = mPreferences.getInt("curpos", 0);
            if (pos < 0 || pos >= mPlayListLen) {
                mPlayListLen = 0;
                return;
            }
            mPlayPos = pos;
            mPlayQueue = getTracksForIds(trackIds);
            if (mPlayQueue == null || mPlayQueue.size() == 0) {
                SystemClock.sleep(3000);
                mPlayQueue = getTracksForIds(trackIds);
            }
            mOpenFailedCounter = 20;
            openCurrentAndNext();
            if (!mPlayer.isInitialized()) {
                mPlayListLen = 0;
                return;
            }

            // Get the saved seek position
            long seekPos = mPreferences.getLong("seekpos", 0);
            // If it is invalid, just start from the beginning
            if (seekPos < 0 || seekPos > duration()) {
            	seekPos = 0;
            }
            seek(seekPos);

            // Get the saved repeat mode
            int repmode = mPreferences.getInt("repeatmode", REPEAT_NONE);
            // If it is invalid, switch repeat off
            if (repmode != REPEAT_ALL && repmode != REPEAT_CURRENT) {
                repmode = REPEAT_NONE;
            }
            mRepeatMode = repmode;

            // Get the saved shuffle mode
            int shufmode = mPreferences.getInt("shufflemode", SHUFFLE_NONE);
            // If it is invalid, switch shuffle off
            if (shufmode != SHUFFLE_AUTO && shufmode != SHUFFLE_NORMAL) {
                shufmode = SHUFFLE_NONE;
            }
            
            if (shufmode != SHUFFLE_NONE) {
                q = mPreferences.getString("history", "");
                if (q != null && !q.isEmpty()) {
                    hexStringToHistory2(q);
                }
            }
            if (shufmode == SHUFFLE_AUTO) {
                if (!makeAutoShuffleList()) {
                    shufmode = SHUFFLE_NONE;
                }
            }
            mShuffleMode = shufmode;
        }
    }
    
    private long[] hexStringToTrackIds2(String hexString) {
    	final String[] hexes = hexString.split(";");
    	final int length = hexes.length;
    	long[] trackIds = new long[length];
    	for (int i = 0; i < length; i++) {
    		trackIds[i] = Long.parseLong(hexes[i], 16);
    	}
    	return trackIds;
    }
    
    private void hexStringToHistory2(String hexString) {
    	final String[] hexes = hexString.split(";");
    	mHistory.clear();
    	for (String hex : hexes) {
    		mHistory.add(Integer.parseInt(hex, 16));
    	}
    }
    
    /*private List<Long> hexStringToTrackIds(String q) {
    	List<Long> trackIds = new ArrayList<Long>();
        int n = 0;
        int shift = 0;
        char[] charArray = q.toCharArray();
        for (char c : charArray) {
            if (c == ';') {
            	trackIds.add((long) n);
                n = 0;
                shift = 0;
            } else {
                if (c >= '0' && c <= '9') {
                    n += c - '0' << shift;
                } else if (c >= 'a' && c <= 'f') {
                    n += 10 + c - 'a' << shift;
                } else {
                    break;
                }
                shift += 4;
            }
        }
    	return trackIds;
    }
    
    private void hexStringToHistory(String q) {
    	mHistory.clear();
        int n = 0;
        int shift = 0;        
        
        char[] charArray = q.toCharArray();
        for (char c : charArray) {
        	if (c == ';') {
                if (n >= mPlayListLen) {
                    mHistory.clear();
                    break;
                }
                mHistory.add(n);
                n = 0;
                shift = 0;
            } else {
                if (c >= '0' && c <= '9') {
                    n += c - '0' << shift;
                } else if (c >= 'a' && c <= 'f') {
                    n += 10 + c - 'a' << shift;
                } else {
                    mHistory.clear();
                    break;
                }
                shift += 4;
            }
        }
    }
    
    private ArrayList<Track> getTracksForIds(List<Long> ids) {
    	final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Media._ID + " IN (");
        final int len = ids.size();
        for (int i = 0; i < len; i++) {
            selection.append(ids.get(i));
            if (i < len - 1) {
                selection.append(",");
            }
        }
        selection.append(")");
        
        Cursor cursor = getContentResolver().query(
        		MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
        		PROJECTION, selection.toString(), null, null);
        
        ArrayList<Track> tracks = null;
        if (cursor != null) {
        	// Load ids from cursor for fast sorting
        	final int length = cursor.getCount();
        	long[] unsortedIds = new long[length];
        	if (cursor.moveToFirst()) {
				for (int i = 0; i < length; i++) {
					unsortedIds[i] = cursor.getLong(0);
					cursor.moveToNext();
				}
			}
        	
        	tracks = new ArrayList<Track>(length);
        	
        	for (long id : ids) {
        		int position = Arrays.binarySearch(unsortedIds, id);
        		if (cursor.moveToPosition(position)) {
        			tracks.add(cursorToTrack(cursor));
        		}
        	}
        	cursor.close();
        	cursor = null;
        }
        return tracks;
    }*/
    
    private List<Track> getTracksForIds(long[] trackIds) {
    	final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Media._ID + " IN (");
        final int len = mPlayListLen;
        for (int i = 0; i < len; i++) {
            selection.append(trackIds[i]);
            if (i < len - 1) {
                selection.append(",");
            }
        }
        selection.append(")");
        
        Cursor cursor = getContentResolver().query(
        		MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
        		PROJECTION, selection.toString(), null, null);
        
        ArrayList<Track> tracks = null;
        if (cursor != null) {
        	// Load ids from cursor for fast sorting
        	final int length = cursor.getCount();
        	long[] unsortedIds = new long[length];
        	if (cursor.moveToFirst()) {
				for (int i = 0; i < length; i++) {
					unsortedIds[i] = cursor.getLong(0);
					cursor.moveToNext();
				}
			}
        	
        	tracks = new ArrayList<Track>(length);
        	
        	for (long id : trackIds) {
        		int position = Arrays.binarySearch(unsortedIds, id);
        		if (cursor.moveToPosition(position)) {
        			tracks.add(cursorToTrack(cursor));
        		}
        	}
        	cursor.close();
        	cursor = null;
        }
        return tracks;
    }
    
    /**
     * Opens a file and prepares it for playback
     * 
     * @param path The path of the file to open
     */
    public boolean openFile(final String path) {
        synchronized (this) {
            /*if (path == null) {
                return false;
            }

            // If mCursor is null, try to associate path with a database cursor
            if (mCursor == null) {
                final ContentResolver resolver = getContentResolver();
                Uri uri;
                String where;
                String selectionArgs[];
                if (path.startsWith("content://media/")) {
                    uri = Uri.parse(path);
                    where = null;
                    selectionArgs = null;
                } else {
                    uri = MediaStore.Audio.Media.getContentUriForPath(path);
                    where = MediaStore.Audio.Media.DATA + "=?";
                    selectionArgs = new String[] {
                        path
                    };
                }
                try {
                    mCursor = resolver.query(uri, PROJECTION, where, selectionArgs, null);
                    if (mCursor != null) {
                        if (mCursor.getCount() == 0) {
                            mCursor.close();
                            mCursor = null;
                        } else {
                            mCursor.moveToNext();
                            ensurePlayListCapacity(1);
                            mPlayListLen = 1;
                            mPlayList[0] = mCursor.getLong(IDCOLIDX);
                            mPlayPos = 0;
                        }
                    }
                } catch (final UnsupportedOperationException ex) {
                }
            }
            mFileToPlay = path;
            mPlayer.setDataSource(mFileToPlay);
            if (mPlayer.isInitialized()) {
                mOpenFailedCounter = 0;
                return true;
            }
            stop(true);
            return false;*/
        }
        return false;
    }
    
    public boolean openUri(Uri uri) {
    	mPlayer.setDataSource(uri);
    	if (mPlayer.isInitialized()) {
    		return true;
    	}
    	return false;
    }

    /**
     * Returns the audio session ID
     * 
     * @return The current media player audio session ID
     */
    public int getAudioSessionId() {
        synchronized (this) {
            return mPlayer.getAudioSessionId();
        }
    }

    /**
     * Sets the audio session ID.
     * 
     * @param sessionId: the audio session ID.
     */
    public void setAudioSessionId(final int sessionId) {
        synchronized (this) {
            mPlayer.setAudioSessionId(sessionId);
        }
    }

    /**
     * Indicates if the media storeage device has been mounted or not
     * 
     * @return 1 if Intent.ACTION_MEDIA_MOUNTED is called, 0 otherwise
     */
    public int getMediaMountedCount() {
        return mMediaMountedCount;
    }

    /**
     * Returns the shuffle mode
     * 
     * @return The current shuffle mode (all, party, none)
     */
    public int getShuffleMode() {
        return mShuffleMode;
    }

    /**
     * Returns the repeat mode
     * 
     * @return The current repeat mode (all, one, none)
     */
    public int getRepeatMode() {
        return mRepeatMode;
    }

    /**
     * Removes all instances of the track with the given ID from the playlist.
     * 
     * @param id The id to be removed
     * @return how many instances of the track were removed
     */
    public int removeTrack(final long id) {
    	int numRemoved = 0;
    	synchronized (this) {        	
        	boolean goToNext = id == mPlayQueue.get(mPlayPos).getId();
    		
        	// Iterate through play queue to find matching tracks
        	for (int i = 0; i < mPlayQueue.size(); i++) {
        		if (mPlayQueue.get(i).getId() == id) {
        			mPlayQueue.remove(i);
        			numRemoved++;
        			if (i < mPlayPos) {
        				mPlayPos--;
        			}
        		}
        	}
        	
        	mPlayListLen -= numRemoved;
        	
        	// If one of the tracks removed was the one playing
        	if (goToNext) {
                goToNextInternal();
            }
        }
        if (numRemoved > 0) {
            notifyChange(QUEUE_CHANGED);
        }
        return numRemoved;
    }

    /**
     * Removes the range of tracks specified from the play list. If a file
     * within the range is the file currently being played, playback will move
     * to the next file after the range.
     * 
     * @param first The first file to be removed
     * @param last The last file to be removed
     * @return the number of tracks deleted
     */
    public int removeTracks(int first, int last) {
    	if (last < first) {
            return 0;
        } else if (first < 0) {
            first = 0;
        } else if (last >= mPlayListLen) {
            last = mPlayListLen - 1;
        }

    	final int numRemoved = last - first + 1;
    	
        boolean goToNext = false;
        if (first <= mPlayPos && mPlayPos <= last) {
            mPlayPos = first;
            goToNext = true;
        } else if (mPlayPos > last) {
            mPlayPos -= numRemoved;
        }
        
        mPlayQueue.subList(first, last).clear();        
        mPlayListLen -= numRemoved;

        if (goToNext) {
            goToNextInternal();
        }
    	
        if (numRemoved > 0) {
            notifyChange(QUEUE_CHANGED);
        }
        return numRemoved;
    }
    
    private void goToNextInternal() {
    	if (mPlayListLen == 0) {
            stop(true);
            mPlayPos = -1;
        } else {
            if (mPlayPos >= mPlayListLen) {
                mPlayPos = 0;
            }
            final boolean wasPlaying = isPlaying();
            stop(false);
            openCurrentAndNext();
            if (wasPlaying) {
                play();
            }
        }
        notifyChange(META_CHANGED);
    }

    /**
     * Returns the position in the queue
     * 
     * @return the current position in the queue
     */
    public int getQueuePosition() {
        synchronized (this) {
            return mPlayPos;
        }
    }

    /**
     * Returns the path to current song
     * 
     * @return The path to the current song
     */
    public Uri getUri() {
        synchronized (this) {
            if (mPlayQueue == null) {
                return null;
            }
            return mPlayQueue.get(mPlayPos).getUri();
        }
    }

    /**
     * Returns the album name
     * 
     * @return The current song album Name
     */
    public String getAlbumName() {
        synchronized (this) {
            if (mPlayQueue == null) {
                return null;
            }
            return mPlayQueue.get(mPlayPos).getAlbum();
        }
    }

    /**
     * Returns the song name
     * 
     * @return The current song name
     */
    public String getTrackName() {
        synchronized (this) {
            if (mPlayQueue == null) {
                return null;
            }
            return mPlayQueue.get(mPlayPos).getTitle();
        }
    }

    /**
     * Returns the artist name
     * 
     * @return The current song artist name
     */
    public String getArtistName() {
        synchronized (this) {
            if (mPlayQueue == null) {
                return null;
            }
            return mPlayQueue.get(mPlayPos).getArtist();
        }
    }

    /**
     * Returns the album ID
     * 
     * @return The current song album ID
     */
    public long getAlbumId() {
        synchronized (this) {
            if (mPlayQueue == null) {
                return -1;
            }
            return mPlayQueue.get(mPlayPos).getAlbumId();
        }
    }

    /**
     * Returns the artist ID
     * 
     * @return The current song artist ID
     */
    public long getArtistId() {
        synchronized (this) {
            if (mPlayQueue == null) {
                return -1;
            }
            return mPlayQueue.get(mPlayPos).getArtistId();
        }
    }

    /**
     * Returns the current audio ID
     * 
     * @return The current track ID
     */
    public long getAudioId() {
        synchronized (this) {
            if (mPlayPos >= 0 && mPlayer.isInitialized()) {
                return mPlayQueue.get(mPlayPos).getId();
            }
        }
        return -1;
    }

    /**
     * Seeks the current track to a specific time
     * 
     * @param position The time to seek to
     * @return The time to play the track at
     */
    public long seek(long position) {
        if (mPlayer.isInitialized()) {
            if (position < 0) {
                position = 0;
            } else if (position > mPlayer.duration()) {
                position = mPlayer.duration();
            }
            return mPlayer.seek(position);
        }
        return -1;
    }

    /**
     * Returns the current position in time of the currenttrack
     * 
     * @return The current playback position in miliseconds
     */
    public long position() {
        if (mPlayer.isInitialized()) {
            return mPlayer.position();
        }
        return -1;
    }

    /**
     * Returns the full duration of the current track
     * 
     * @return The duration of the current track in miliseconds
     */
    public long duration() {
        if (mPlayer.isInitialized()) {
            return mPlayer.duration();
        }
        return -1;
    }

    /**
     * Returns the queue
     * 
     * @return The queue as a long[]
     */
    public List<Track> getQueue() {
        synchronized (this) {
            List<Track> copy = new ArrayList<Track>(mPlayQueue);
        	return copy;
        }
    }

    /**
     * @return True if music is playing, false otherwise
     */
    public boolean isPlaying() {
        return mIsSupposedToBePlaying;
    }

    /**
     * True if the current track is a "favorite", false otherwise
     */
    public boolean isFavorite() {
        /*if (mFavoritesCache != null) {
            synchronized (this) {
                final Long id = mFavoritesCache.getSongId(getAudioId());
                return id != null ? true : false;
            }
        }*/
        return false;
    }

    /**
     * Opens a list for playback
     * 
     * @param list The list of tracks to open
     * @param position The position to start playback at
     */
    public void open(final List<Track> list, final int position) {
        synchronized (this) {
            if (mShuffleMode == SHUFFLE_AUTO) {
                mShuffleMode = SHUFFLE_NORMAL;
            }
            final long oldId = getAudioId();
            if (mPlayQueue == null) {
            	mPlayQueue = new ArrayList<Track>(list.size());
            }
            if (!mPlayQueue.equals(list)) {
                addToPlayList(list, -1);
                notifyChange(QUEUE_CHANGED);
            }
            if (position >= 0) {
                mPlayPos = position;
            } else {
                mPlayPos = mShuffler.nextInt(mPlayListLen);
            }
            mHistory.clear();
            openCurrentAndNext();
            if (oldId != getAudioId()) {
                notifyChange(META_CHANGED);
            }
        }
    }

    /**
     * Stops playback.
     */
    public void stop() {
        stop(true);
    }

    /**
     * Resumes or starts playback.
     */
    public void play() {
        mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        //mAudioManager.registerMediaButtonEventReceiver(new ComponentName(getPackageName(),
        //        MediaButtonIntentReceiver.class.getName()));

        if (mPlayer.isInitialized()) {
            final long duration = mPlayer.duration();
            if (mRepeatMode != REPEAT_CURRENT && duration > 2000
                    && mPlayer.position() >= duration - 2000) {
                gotoNext(true);
            }

            mPlayer.start();
            mPlayerHandler.removeMessages(FADEDOWN);
            mPlayerHandler.sendEmptyMessage(FADEUP);

            // Update the notification
            buildNotification();
            if (!mIsSupposedToBePlaying) {
                mIsSupposedToBePlaying = true;
                notifyChange(PLAYSTATE_CHANGED);
            }

        } else if (mPlayListLen <= 0) {
            setShuffleMode(SHUFFLE_AUTO);
        }
    }

    /**
     * Temporarily pauses playback.
     */
    public void pause() {
        synchronized (this) {
            mPlayerHandler.removeMessages(FADEUP);
            if (mIsSupposedToBePlaying) {
                mPlayer.pause();
                gotoIdleState();
                mIsSupposedToBePlaying = false;
                notifyChange(PLAYSTATE_CHANGED);
            }
        }
    }

    /**
     * Changes from the current track to the next track
     */
    public void gotoNext(final boolean force) {
        synchronized (this) {
            if (mPlayListLen <= 0) {
                return;
            }
            final int pos = getNextPosition(force);
            if (pos < 0) {
                gotoIdleState();
                if (mIsSupposedToBePlaying) {
                    mIsSupposedToBePlaying = false;
                    notifyChange(PLAYSTATE_CHANGED);
                }
                return;
            }
            mPlayPos = pos;
            stop(false);
            mPlayPos = pos;
            openCurrentAndNext();
            play();
            notifyChange(META_CHANGED);
        }
    }

    /**
     * Changes from the current track to the previous played track
     */
    public void prev() {
        synchronized (this) {
            if (mShuffleMode == SHUFFLE_NORMAL) {
                // Go to previously-played track and remove it from the history
                final int histsize = mHistory.size();
                if (histsize == 0) {
                    return;
                }
                final Integer pos = mHistory.remove(histsize - 1);
                mPlayPos = pos.intValue();
            } else {
                if (mPlayPos > 0) {
                    mPlayPos--;
                } else {
                    mPlayPos = mPlayListLen - 1;
                }
            }
            stop(false);
            openCurrent();
            play();
            notifyChange(META_CHANGED);
        }
    }

    /**
     * We don't want to open the current and next track when the user is using
     * the {@code #prev()} method because they won't be able to travel back to
     * the previously listened track if they're shuffling.
     */
    private void openCurrent() {
        openCurrentAndMaybeNext(false);
    }

    /**
     * Toggles the current song as a favorite.
     */
    public void toggleFavorite() {
        /*if (mFavoritesCache != null) {
            synchronized (this) {
                mFavoritesCache.toggleSong(getAudioId(), getTrackName(), getAlbumName(),
                        getArtistName());
            }
        }*/
    }

    /**
     * Moves an item in the queue from one position to another
     * 
     * @param from The position the item is currently at
     * @param to The position the item is being moved to
     */
    public void moveQueueItem(int index1, int index2) {
        synchronized (this) {
            if (index1 >= mPlayListLen) {
                index1 = mPlayListLen - 1;
            }
            if (index2 >= mPlayListLen) {
                index2 = mPlayListLen - 1;
            }
            if (index1 < index2) {
            	mPlayQueue.add(index1, mPlayQueue.remove(index2));
                if (mPlayPos == index1) {
                    mPlayPos = index2;
                } else if (mPlayPos >= index1 && mPlayPos <= index2) {
                    mPlayPos--;
                }
            } else if (index2 < index1) {
            	mPlayQueue.add(index2, mPlayQueue.remove(index1));
                if (mPlayPos == index1) {
                    mPlayPos = index2;
                } else if (mPlayPos >= index2 && mPlayPos <= index1) {
                    mPlayPos++;
                }
            }
            notifyChange(QUEUE_CHANGED);
        }
    }

    /**
     * Sets the repeat mode
     * 
     * @param repeatmode The repeat mode to use
     */
    public void setRepeatMode(final int repeatmode) {
        synchronized (this) {
            mRepeatMode = repeatmode;
            setNextTrack();
            saveQueue(false);
            notifyChange(REPEATMODE_CHANGED);
        }
    }

    /**
     * Sets the shuffle mode
     * 
     * @param shufflemode The shuffle mode to use
     */
    public void setShuffleMode(final int shufflemode) {
        synchronized (this) {
            if (mShuffleMode == shufflemode && mPlayListLen > 0) {
                return;
            }
            mShuffleMode = shufflemode;
            if (mShuffleMode == SHUFFLE_AUTO) {
                if (makeAutoShuffleList()) {
                    mPlayListLen = 0;
                    doAutoShuffleUpdate();
                    mPlayPos = 0;
                    openCurrentAndNext();
                    play();
                    notifyChange(META_CHANGED);
                    return;
                } else {
                    mShuffleMode = SHUFFLE_NONE;
                }
            }
            saveQueue(false);
            notifyChange(SHUFFLEMODE_CHANGED);
        }
    }

    /**
     * Sets the position of a track in the queue
     * 
     * @param index The position to place the track
     */
    public void setQueuePosition(final int index) {
        synchronized (this) {
            stop(false);
            mPlayPos = index;
            openCurrentAndNext();
            play();
            notifyChange(META_CHANGED);
            if (mShuffleMode == SHUFFLE_AUTO) {
                doAutoShuffleUpdate();
            }
        }
    }

    /**
     * Queues a new list for playback
     * 
     * @param list The list to queue
     * @param action The action to take
     */
    public void enqueue(final List<Track> list, final int action) {
        synchronized (this) {
            if (action == NEXT && mPlayPos + 1 < mPlayListLen) {
                addToPlayList(list, mPlayPos + 1);
                notifyChange(QUEUE_CHANGED);
            } else {
                addToPlayList(list, Integer.MAX_VALUE);
                notifyChange(QUEUE_CHANGED);
                if (action == NOW) {
                    mPlayPos = mPlayListLen - list.size();
                    openCurrentAndNext();
                    play();
                    notifyChange(META_CHANGED);
                    return;
                }
            }
            if (mPlayPos < 0) {
                mPlayPos = 0;
                openCurrentAndNext();
                play();
                notifyChange(META_CHANGED);
            }
        }
    }

    /**
     * Cycles through the different repeat modes
     */
    private void cycleRepeat() {
        if (mRepeatMode == REPEAT_NONE) {
            setRepeatMode(REPEAT_ALL);
        } else if (mRepeatMode == REPEAT_ALL) {
            setRepeatMode(REPEAT_CURRENT);
            if (mShuffleMode != SHUFFLE_NONE) {
                setShuffleMode(SHUFFLE_NONE);
            }
        } else {
            setRepeatMode(REPEAT_NONE);
        }
    }

    /**
     * Cycles through the different shuffle modes
     */
    private void cycleShuffle() {
        if (mShuffleMode == SHUFFLE_NONE) {
            setShuffleMode(SHUFFLE_NORMAL);
            if (mRepeatMode == REPEAT_CURRENT) {
                setRepeatMode(REPEAT_ALL);
            }
        } else if (mShuffleMode == SHUFFLE_NORMAL || mShuffleMode == SHUFFLE_AUTO) {
            setShuffleMode(SHUFFLE_NONE);
        }
    }

    /**
     * @return The album art for the current album.
     */
    public Bitmap getAlbumArt() {
        // Return the cached artwork
        //return mImageWorker.getCachedImage(String.valueOf(getAlbumId()));
    	return null;
    }

    /**
     * Called when one of the lists should refresh or requery.
     */
    public void refresh() {
        notifyChange(REFRESH);
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            final String command = intent.getStringExtra("command");
            if (CMD_NEXT.equals(command) || ACTION_NEXT.equals(action)) {
                gotoNext(true);
            } else if (CMD_PREVIOUS.equals(command) || ACTION_PREVIOUS.equals(action)) {
                if (position() < 2000) {
                    prev();
                } else {
                    seek(0);
                    play();
                }
            } else if (CMD_TOGGLE_PLAYBACK.equals(command) || ACTION_TOGGLE_PLAYBACK.equals(action)) {
                if (mIsSupposedToBePlaying) {
                    pause();
                    mPausedByTransientLossOfFocus = false;
                } else {
                    play();
                }
            } else if (CMD_PAUSE.equals(command) || ACTION_PAUSE.equals(action)) {
                pause();
                mPausedByTransientLossOfFocus = false;
            } else if (CMD_PLAY.equals(command)) {
                play();
            } else if (CMD_STOP.equals(command) || ACTION_STOP.equals(action)) {
                pause();
                mPausedByTransientLossOfFocus = false;
                seek(0);
                killNotification();
                mBuildNotification = false;
            } else if (ACTION_REPEAT.equals(action)) {
                cycleRepeat();
            } else if (ACTION_SHUFFLE.equals(action)) {
                cycleShuffle();
            } else if (KILL_FOREGROUND.equals(action)) {
                mBuildNotification = false;
                killNotification();
            } else if (START_BACKGROUND.equals(action)) {
                mBuildNotification = true;
                buildNotification();
            /*} else if (UPDATE_LOCKSCREEN.equals(action)) {
                mEnableLockscreenControls = intent.getBooleanExtra(UPDATE_LOCKSCREEN, true);
                if (mEnableLockscreenControls) {
                    setUpRemoteControlClient();
                    // Update the controls according to the current playback
                    notifyChange(PLAYSTATE_CHANGED);
                    notifyChange(META_CHANGED);
                } else {
                    // Remove then unregister the conrols
                    mRemoteControlClientCompat
                            .setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
                    RemoteControlHelper.unregisterRemoteControlClient(mAudioManager,
                            mRemoteControlClientCompat);
                }*/
            /*} else if (AppWidgetSmall.CMDAPPWIDGETUPDATE.equals(command)) {
                final int[] small = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                mAppWidgetSmall.performUpdate(MusicPlaybackService.this, small);
            } else if (AppWidgetLarge.CMDAPPWIDGETUPDATE.equals(command)) {
                final int[] large = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                mAppWidgetLarge.performUpdate(MusicPlaybackService.this, large);
            } else if (AppWidgetLargeAlternate.CMDAPPWIDGETUPDATE.equals(command)) {
                final int[] largeAlt = intent
                        .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                mAppWidgetLargeAlternate.performUpdate(MusicPlaybackService.this, largeAlt);
            } else if (RecentWidgetProvider.CMDAPPWIDGETUPDATE.equals(command)) {
                final int[] recent = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                mRecentWidgetProvider.performUpdate(MusicPlaybackService.this, recent);*/
            }
        }
    };

    private static final class DelayedHandler extends Handler {

        private final WeakReference<MusicService> mService;

        /**
         * Constructor of <code>DelayedHandler</code>
         * 
         * @param service The service to use.
         */
        public DelayedHandler(final MusicService service) {
            mService = new WeakReference<MusicService>(service);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void handleMessage(final Message msg) {
            if (mService.get().isPlaying() || mService.get().mPausedByTransientLossOfFocus
                    || mService.get().mServiceInUse
                    || mService.get().mPlayerHandler.hasMessages(TRACK_ENDED)) {
                return;
            }
            mService.get().saveQueue(true);
            mService.get().stopSelf(mService.get().mServiceStartId);
        }
    }

    private static final class MusicPlayerHandler extends Handler {

        private final WeakReference<MusicService> mService;

        private float mCurrentVolume = 1.0f;

        /**
         * Constructor of <code>MusicPlayerHandler</code>
         * 
         * @param service The service to use.
         * @param looper The thread to run on.
         */
        public MusicPlayerHandler(final MusicService service, final Looper looper) {
            super(looper);
            mService = new WeakReference<MusicService>(service);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case FADEDOWN:
                    mCurrentVolume -= .05f;
                    if (mCurrentVolume > .2f) {
                        sendEmptyMessageDelayed(FADEDOWN, 10);
                    } else {
                        mCurrentVolume = .2f;
                    }
                    mService.get().mPlayer.setVolume(mCurrentVolume);
                    break;
                case FADEUP:
                    mCurrentVolume += .01f;
                    if (mCurrentVolume < 1.0f) {
                        sendEmptyMessageDelayed(FADEUP, 10);
                    } else {
                        mCurrentVolume = 1.0f;
                    }
                    mService.get().mPlayer.setVolume(mCurrentVolume);
                    break;
                case SERVER_DIED:
                    if (mService.get().mIsSupposedToBePlaying) {
                        mService.get().gotoNext(true);
                    } else {
                        mService.get().openCurrentAndNext();
                    }
                    break;
                case TRACK_WENT_TO_NEXT:
                    mService.get().mPlayPos = mService.get().mNextPlayPos;
                    mService.get().notifyChange(META_CHANGED);
                    mService.get().buildNotification();
                    mService.get().setNextTrack();
                    break;
                case TRACK_ENDED:
                    if (mService.get().mRepeatMode == REPEAT_CURRENT) {
                        mService.get().seek(0);
                        mService.get().play();
                    } else {
                        mService.get().gotoNext(false);
                    }
                    break;
                case RELEASE_WAKELOCK:
                    mService.get().mWakeLock.release();
                    break;
                case FOCUSCHANGE:
                    switch (msg.arg1) {
                        case AudioManager.AUDIOFOCUS_LOSS:
                            if (mService.get().isPlaying()) {
                                mService.get().mPausedByTransientLossOfFocus = false;
                            }
                            mService.get().pause();
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            removeMessages(FADEUP);
                            sendEmptyMessage(FADEDOWN);
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            if (mService.get().isPlaying()) {
                                mService.get().mPausedByTransientLossOfFocus = true;
                            }
                            mService.get().pause();
                            break;
                        case AudioManager.AUDIOFOCUS_GAIN:
                            if (!mService.get().isPlaying()
                                    && mService.get().mPausedByTransientLossOfFocus) {
                                mService.get().mPausedByTransientLossOfFocus = false;
                                mCurrentVolume = 0f;
                                mService.get().mPlayer.setVolume(mCurrentVolume);
                                mService.get().play();
                            } else {
                                removeMessages(FADEDOWN);
                                sendEmptyMessage(FADEUP);
                            }
                            break;
                        default:
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static final class Shuffler {

        private final LinkedList<Integer> mHistoryOfNumbers = new LinkedList<Integer>();

        private final TreeSet<Integer> mPreviousNumbers = new TreeSet<Integer>();

        private final Random mRandom = new Random();

        private int mPrevious;

        /**
         * Constructor of <code>Shuffler</code>
         */
        public Shuffler() {
            super();
        }

        /**
         * @param interval The length the queue
         * @return The position of the next track to play
         */
        public int nextInt(final int interval) {
            int next;
            do {
                next = mRandom.nextInt(interval);
            } while (next == mPrevious && interval > 1
                    && !mPreviousNumbers.contains(Integer.valueOf(next)));
            mPrevious = next;
            mHistoryOfNumbers.add(mPrevious);
            mPreviousNumbers.add(mPrevious);
            cleanUpHistory();
            return next;
        }

        /**
         * Removes old tracks and cleans up the history preparing for new tracks
         * to be added to the mapping
         */
        private void cleanUpHistory() {
            if (!mHistoryOfNumbers.isEmpty() && mHistoryOfNumbers.size() >= MAX_HISTORY_SIZE) {
                for (int i = 0; i < Math.max(1, MAX_HISTORY_SIZE / 2); i++) {
                    mPreviousNumbers.remove(mHistoryOfNumbers.removeFirst());
                }
            }
        }
    };

    private static final class MultiPlayer implements MediaPlayer.OnErrorListener,
			MediaPlayer.OnCompletionListener {

    	private final WeakReference<MusicService> mService;

    	private MediaPlayer mCurrentMediaPlayer = new MediaPlayer();
    	private MediaPlayer mNextMediaPlayer;

    	private Handler mHandler;

    	private boolean mIsInitialized = false;

    	public MultiPlayer(MusicService service) {
    		mService = new WeakReference<MusicService>(service);
    		mCurrentMediaPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);
    	}

    	private boolean setDataSourceImpl(final MediaPlayer player, final Uri uri) {
            try {
                player.reset();
                player.setOnPreparedListener(null);
                player.setDataSource(mService.get(), uri);
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.prepare();
            } catch (final IOException todo) {
                // TODO: notify the user why the file couldn't be opened
                return false;
            } catch (final IllegalArgumentException todo) {
                // TODO: notify the user why the file couldn't be opened
                return false;
            }
            player.setOnCompletionListener(this);
            player.setOnErrorListener(this);
            final Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
            intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
            intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, mService.get().getPackageName());
            mService.get().sendBroadcast(intent);
            return true;
        }
    	
    	public void setDataSource(final Uri uri) {
            mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, uri);
            if (mIsInitialized) {
                setNextDataSource(null);
            }
        }
    	
    	public void setNextDataSource(final Uri uri) {
            // Clear the next media player attached to the current one
        	mCurrentMediaPlayer.setNextMediaPlayer(null);
            // If the next media player exists, clear that too
        	if (mNextMediaPlayer != null) {
                mNextMediaPlayer.release();
                mNextMediaPlayer = null;
            }
        	// If uri is null, i.e. we're setting the current data source, exit
            if (uri == null) {
                return;
            }
            
            // Set up a new media player to be the next to play
            mNextMediaPlayer = new MediaPlayer();
            mNextMediaPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);
            mNextMediaPlayer.setAudioSessionId(getAudioSessionId());
            
            // If we set the data source successfully then set this to be next to play
            if (setDataSourceImpl(mNextMediaPlayer, uri)) {
                mCurrentMediaPlayer.setNextMediaPlayer(mNextMediaPlayer);
            } else {
            	// Otherwise release the new player
                if (mNextMediaPlayer != null) {
                    mNextMediaPlayer.release();
                    mNextMediaPlayer = null;
                }
            }
        }

    	public void setHandler(final Handler handler) {
    		mHandler = handler;
    	}

    	public boolean isInitialized() {
    		return mIsInitialized;
    	}

    	public void start() {
    		mCurrentMediaPlayer.start();
    	}

    	public void stop() {
    		mCurrentMediaPlayer.reset();
    		mIsInitialized = false;
    	}

    	public void release() {
    		stop();
    		mCurrentMediaPlayer.release();
    	}

    	public void pause() {
    		mCurrentMediaPlayer.pause();
    	}

    	public long duration() {
    		return mCurrentMediaPlayer.getDuration();
    	}

    	public long position() {
    		return mCurrentMediaPlayer.getCurrentPosition();
    	}

    	public long seek(final long whereto) {
    		mCurrentMediaPlayer.seekTo((int)whereto);
    		return whereto;
    	}

    	public void setVolume(final float vol) {
    		mCurrentMediaPlayer.setVolume(vol, vol);
    	}

    	public void setAudioSessionId(final int sessionId) {
    		mCurrentMediaPlayer.setAudioSessionId(sessionId);
    	}

    	public int getAudioSessionId() {
    		return mCurrentMediaPlayer.getAudioSessionId();
    	}

    	@Override
    	public void onCompletion(MediaPlayer mp) {
    		if (mp == mCurrentMediaPlayer && mNextMediaPlayer != null) {
    			mCurrentMediaPlayer.release();
    			mCurrentMediaPlayer = mNextMediaPlayer;
    			mNextMediaPlayer = null;
    			mHandler.sendEmptyMessage(TRACK_WENT_TO_NEXT);
    		} else {
    			//mService.get().mWakeLock.acquire(30000);
    			mHandler.sendEmptyMessage(TRACK_ENDED);
    			mHandler.sendEmptyMessage(RELEASE_WAKELOCK);
    		}
	
    	}

    	@Override
    	public boolean onError(MediaPlayer mp, int what, int extra) {
    		switch (what) {
    		case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
    			mIsInitialized = false;
    			mCurrentMediaPlayer.release();
    			mCurrentMediaPlayer = new MediaPlayer();
    			mCurrentMediaPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);
    			mHandler.sendMessageDelayed(mHandler.obtainMessage(SERVER_DIED), 2000);
    			return true;
    		default:
    			break;
    		}
    		return false;
    	}

    }

    private static final class ServiceStub extends IMusicService.Stub {

        private final WeakReference<MusicService> mService;

        private ServiceStub(final MusicService service) {
            mService = new WeakReference<MusicService>(service);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void openFile(final String path) throws RemoteException {
            mService.get().openFile(path);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void open(final List<Track> list, final int position) 
        		throws RemoteException {
            mService.get().open(list, position);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void stop() throws RemoteException {
            mService.get().stop();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void pause() throws RemoteException {
            mService.get().pause();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void play() throws RemoteException {
            mService.get().play();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void prev() throws RemoteException {
            mService.get().prev();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void next() throws RemoteException {
            mService.get().gotoNext(true);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void enqueue(final List<Track> list, final int action) throws RemoteException {
            mService.get().enqueue(list, action);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setQueuePosition(final int index) throws RemoteException {
            mService.get().setQueuePosition(index);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setShuffleMode(final int shufflemode) throws RemoteException {
            mService.get().setShuffleMode(shufflemode);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setRepeatMode(final int repeatmode) throws RemoteException {
            mService.get().setRepeatMode(repeatmode);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void moveQueueItem(final int from, final int to) throws RemoteException {
            mService.get().moveQueueItem(from, to);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void refresh() throws RemoteException {
            mService.get().refresh();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isPlaying() throws RemoteException {
            return mService.get().isPlaying();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<Track> getQueue() throws RemoteException {
            return mService.get().getQueue();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long duration() throws RemoteException {
            return mService.get().duration();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long position() throws RemoteException {
            return mService.get().position();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long seek(final long position) throws RemoteException {
            return mService.get().seek(position);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getAudioId() throws RemoteException {
            return mService.get().getAudioId();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getArtistId() throws RemoteException {
            return mService.get().getArtistId();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getAlbumId() throws RemoteException {
            return mService.get().getAlbumId();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getArtistName() throws RemoteException {
            return mService.get().getArtistName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getTrackName() throws RemoteException {
            return mService.get().getTrackName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getAlbumName() throws RemoteException {
            return mService.get().getAlbumName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Uri getUri() throws RemoteException {
            return mService.get().getUri();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getQueuePosition() throws RemoteException {
            return mService.get().getQueuePosition();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getShuffleMode() throws RemoteException {
            return mService.get().getShuffleMode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getRepeatMode() throws RemoteException {
            return mService.get().getRepeatMode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int removeTracks(final int first, final int last) throws RemoteException {
            return mService.get().removeTracks(first, last);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int removeTrack(final long id) throws RemoteException {
            return mService.get().removeTrack(id);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getMediaMountedCount() throws RemoteException {
            return mService.get().getMediaMountedCount();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getAudioSessionId() throws RemoteException {
            return mService.get().getAudioSessionId();
        }

    }
}
