package za.jamie.soundstage.service;

import java.lang.ref.WeakReference;
import java.util.List;

import za.jamie.soundstage.IMusicService;
import za.jamie.soundstage.IMusicStatusCallback;
import za.jamie.soundstage.IQueueStatusCallback;
import za.jamie.soundstage.models.Track;
import za.jamie.soundstage.utils.AppUtils;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;


public class MusicService extends Service implements AudioManager.OnAudioFocusChangeListener,
		GaplessPlayer.PlayerEventListener {

	private static final String TAG = "MusicService";
	
	// Intent actions for external control
	public static final String ACTION_TOGGLE_PLAYBACK = 
			"za.jamie.soundstage.action.TOGGLE_PLAYBACK";
    public static final String ACTION_PLAY = "za.jamie.soundstage.action.PLAY";
    public static final String ACTION_PAUSE = 
    		"za.jamie.soundstage.action.PAUSE";
    public static final String ACTION_STOP = "za.jamie.soundstage.action.STOP";
    public static final String ACTION_NEXT = "za.jamie.soundstage.action.NEXT";
    public static final String ACTION_PREVIOUS = "za.jamie.soundstage.action.PREVIOUS";
    public static final String ACTION_REPEAT = "za.jamie.soundstage.action.REPEAT";
    public static final String ACTION_SHUFFLE = "za.jamie.soundstage.action.SHUFFLE";
    
    public static final String KILL_FOREGROUND = "za.jamie.soundstage.killforeground";
    public static final String START_BACKGROUND = "za.jamie.soundstage.startbackground";
    
    // Status change flags
 	public static final String PLAYSTATE_CHANGED = "za.jamie.soundstage.playstatechanged"; 	
 	public static final String META_CHANGED = "za.jamie.soundstage.metachanged"; 	
    public static final String QUEUE_CHANGED = "za.jamie.soundstage.queuechanged";
    public static final String REPEATMODE_CHANGED = "za.jamie.soundstage.repeatmodechanged";
    public static final String SHUFFLEMODE_CHANGED = "za.jamie.soundstage.shufflemodechanged";

    // Shuffle modes
    public static final int SHUFFLE_NONE = 0;
    public static final int SHUFFLE_NORMAL = 1;
    public static final int SHUFFLE_AUTO = 2;
    private int mShuffleMode = SHUFFLE_NONE;

    // Repeat modes
    public static final int REPEAT_NONE = 0;
    public static final int REPEAT_CURRENT = 1;
    public static final int REPEAT_ALL = 2;
    private int mRepeatMode = REPEAT_NONE;
    
    // Different ways to add items to the queue
    public static final int NOW = 1;
    public static final int NEXT = 2;
    public static final int LAST = 3;
    
    // Shared preference keys
    private static final String PREFERENCES = "Service";
    private static final String PREF_CARD_ID = "cardid";
    private static final String PREF_QUEUE_POSITION = "queuepos";
    private static final String PREF_SEEK_POSITION = "seekpos";
    private static final String PREF_REPEAT_MODE = "repeatmode";
    private static final String PREF_SHUFFLE_MODE = "shufflemode";

    // State information
    private boolean mIsBound = false;
    private boolean mMediaMounted = true;
    private boolean mIsSupposedToBePlaying = false;
    private boolean mPausedByTransientLossOfFocus = false;
    private boolean mBuildNotification = false;
    
    private int mCardId;
    private int mServiceStartId;
    private SharedPreferences mPreferences;

    // Audio playback objects
    private AudioManager mAudioManager;
    private GaplessPlayer mPlayer;
    
    // Handlers
    private final Handler mDelayedStopHandler = new DelayedStopHandler(this);
    // Delay before releasing the media player to ensure fast stop/start
    private static final int IDLE_DELAY = 60000;

    // Broadcast receivers
    private final BroadcastReceiver mIntentReceiver = new IntentReceiver();
    private final BroadcastReceiver mUnmountReceiver = new UnmountReceiver();

    // Queue of tracks to be played
    private PlayQueue mPlayQueue;
    private PlayQueueDatabase mPlayQueueDatabase;

    private NotificationHelper mNotificationHelper; // Notifications 
    private RemoteControlClient mRemoteControlClient; // Lockscreen controls
    private ComponentName mMediaButtonReceiverComponent; // Media buttons

    private final IBinder mBinder = new ServiceStub(this);

    // Callback lists for remote listeners
    private final RemoteCallbackList<IMusicStatusCallback> mMusicStatusCallbackList =
    		new RemoteCallbackList<IMusicStatusCallback>();
    
    private final RemoteCallbackList<IQueueStatusCallback> mQueueStatusCallbackList =
    		new RemoteCallbackList<IQueueStatusCallback>();

    @Override
    public IBinder onBind(final Intent intent) {
        mDelayedStopHandler.removeCallbacksAndMessages(null);

        mIsBound = true;
        return mBinder;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
    	mIsBound = false;

    	if (!mIsSupposedToBePlaying && !mPausedByTransientLossOfFocus) {
    		stopSelf(mServiceStartId);
    	} else if (!mPlayQueue.isEmpty()) {
    		sendDelayedStopMessage(false);
    	}

    	return true;
    }

    @Override
    public void onRebind(final Intent intent) {
        mDelayedStopHandler.removeCallbacksAndMessages(null);

        mIsBound = true;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationHelper = new NotificationHelper(this);

        // Initialize the player and the fader
        mPlayer = new GaplessPlayer(this);
        mPlayer.setPlayerEventListener(this);
        
        // Initialize the audio manager and register any headset controls for
        // playback... set up the lockscreen controls
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initRemoteControlClient();

        // Register the broadcast receivers
        registerIntentReceiver();
        registerUnmountReceiver();
        
        // Restore previous state from shared preferences
        mPreferences = getSharedPreferences(PREFERENCES, 0);
        mCardId = getCardId();
        mPlayQueueDatabase = new PlayQueueDatabase(this);
        restoreState();

        sendDelayedStopMessage(false);
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

    private void registerIntentReceiver() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_TOGGLE_PLAYBACK);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_STOP);
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_PREVIOUS);
        filter.addAction(ACTION_REPEAT);
        filter.addAction(ACTION_SHUFFLE);
        registerReceiver(mIntentReceiver, filter);
    }

    private void registerUnmountReceiver() {
    	final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addDataScheme("file");
        registerReceiver(mUnmountReceiver, filter);
    }

    /**
     *
     * @return A card ID (filesystem ID) used to save and restore playlists.
     */
    private int getCardId() {
        Cursor cursor = getContentResolver()
        		.query(Uri.parse("content://media/external/fs_id"), null, null, 
        				null, null);
        int cardId = -1;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
            	cardId = cursor.getInt(0);
            }
            cursor.close();
            cursor = null;
        }
        return cardId;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mPlayQueueDatabase.close();
        
        setAudioEffectsEnabled(false);

        // Release the player
        mPlayer.stopFade();
        mPlayer.release();

        // Remove the audio focus listener and lock screen controls
        mAudioManager.abandonAudioFocus(this);
        mAudioManager.unregisterRemoteControlClient(mRemoteControlClient);
        mAudioManager.unregisterMediaButtonEventReceiver(mMediaButtonReceiverComponent);

        // Remove music control callbacks
        mMusicStatusCallbackList.kill();
        mQueueStatusCallbackList.kill();

        // Unregister the broadcast receivers
        unregisterReceiver(mIntentReceiver);
        unregisterReceiver(mUnmountReceiver);

        // Remove any callbacks from the handlers
        mDelayedStopHandler.removeCallbacksAndMessages(null); 
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        mServiceStartId = startId;
        if (intent != null) {
            mIntentReceiver.onReceive(this, intent);
        }

        return START_STICKY;
    }

    private void setAudioEffectsEnabled(boolean enable) {
        final Intent intent = new Intent();
        if (enable) {
        	intent.setAction(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        } else {
        	intent.setAction(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        }
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, mPlayer.getAudioSessionId());
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(intent);
    }

    private void sendDelayedStopMessage(boolean removeMessages) {
    	if (removeMessages) {
    		mDelayedStopHandler.removeCallbacksAndMessages(null);
    	}
    	final Message message = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(message, IDLE_DELAY);
    }

    private void restoreState() {
    	int id = mCardId;
        if (mPreferences.contains(PREF_CARD_ID)) {
            id = mPreferences.getInt(PREF_CARD_ID, ~mCardId);
        }
        if (id == mCardId) {
        	// Bring back the queue
        	List<Track> trackList = mPlayQueueDatabase.getTrackList();
        	List<Integer> shuffleMap = mPlayQueueDatabase.getShuffleMap();
        	mPlayQueue = new PlayQueue(trackList, shuffleMap);
        	
        	final int pos = mPreferences.getInt(PREF_QUEUE_POSITION, 0);
    		if (!mPlayQueue.moveToPosition(pos)) {
    			return;
    		}

    		// Try load the tracks
    		openCurrentAndNext();
            if (!mPlayer.isInitialized()) {  // If player didn't manage to load, give up
            	Log.w(TAG, "Player not initialized while restoring state.");
                return;
            }

            // Get the saved seek position
            long seekPosition = mPreferences.getLong(PREF_SEEK_POSITION, 0);
            // If it is invalid, just start from the beginning
            if (seekPosition < 0 || seekPosition > mPlayer.duration()) {
            	seekPosition = 0;
            }
            seek(seekPosition);

            // Get the saved repeat mode
            int repeatMode = mPreferences.getInt(PREF_REPEAT_MODE, REPEAT_NONE);
            // If it is invalid, switch repeat off
            if (repeatMode != REPEAT_ALL && repeatMode != REPEAT_CURRENT) {
            	repeatMode = REPEAT_NONE;
            }
            setRepeatMode(repeatMode);

            // Get the saved shuffle mode
            int shuffleMode = mPreferences.getInt(PREF_SHUFFLE_MODE, SHUFFLE_NONE);
            // If it is invalid, switch shuffle off
            if (shuffleMode != SHUFFLE_AUTO && shuffleMode != SHUFFLE_NORMAL) {
            	shuffleMode = SHUFFLE_NONE;
            }
            setShuffleMode(shuffleMode);
        } else {
        	mPlayQueue = new PlayQueue();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Change foreground/background states

    /**
     * Changes the notification buttons to a paused state and begins the
     * countdown to calling {@code #stopForeground(true)}
     */
    private void gotoIdleState() {
        // Post a delayed message to stop service
    	sendDelayedStopMessage(true);
        
        // Put the notification in idle state and remove it after a bit
        if (mBuildNotification) {
            mNotificationHelper.goToIdleState(mIsSupposedToBePlaying);
        }
        mDelayedStopHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                killNotification();
            }
        }, IDLE_DELAY);
        
        // Update play state
        if (mIsSupposedToBePlaying) {
            mIsSupposedToBePlaying = false;
            onPlayStateChanged();
        }
    }

    /**
     * Removes the notification
     */
    public void killNotification() {
        stopForeground(true);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Open and load tracks

    /**
     * Moves the play queue to the next track depending on the repeat mode
     * 
     * @return True if changing to the next track was successful
     */
    private boolean gotoNextInternal() {
    	if (mRepeatMode == REPEAT_ALL && mPlayQueue.isLast()) {
    		return mPlayQueue.moveToFirst();
    	} else if (mRepeatMode == REPEAT_CURRENT) {
    		return true;
    	} else {
    		return mPlayQueue.moveToNext();
    	}
    }

    private boolean gotoPreviousInternal() {
    	if (mRepeatMode == REPEAT_ALL && mPlayQueue.isFirst()) {
    		return mPlayQueue.moveToLast();
    	} else if (mRepeatMode == REPEAT_CURRENT) {
    		return true;
    	} else {
    		return mPlayQueue.moveToPrevious();
    	}
    }

    private void openAndPlay() {
    	// Stop current playback
    	stop(false);
    	
    	// Open the current track and initialize the next
    	openCurrentAndNext();
    	
    	// Play
    	play();
    	
    	// Notify track changed
    	onMetaChanged();
    }

    /**
     * Called to open a new file as the current track and prepare the next for
     * playback
     */
    private void openCurrentAndNext() {    	
    	openCurrent();
        setNextTrack();
    }

    private void openCurrent() {
    	if (mPlayQueue.isEmpty()) {
    		return;
    	}

    	stop(false);
        // Try a maximum of 10 times to load a track
        int tries = 0;
        while (!mPlayer.setDataSource(getCurrentTrack().getUri())) {
        	if (tries++ < 10) {
        		if (!gotoNextInternal()) {
        			gotoIdleState();
        			break;
        		}
        	} else {
        		gotoIdleState();
        		break;
        	}
        }
    }

    /**
     * Sets the track track to be played after the current (enables gapless playback)
     */
    private void setNextTrack() {
    	if (mRepeatMode != REPEAT_CURRENT) {
    		Track nextTrack;
    		if (mRepeatMode == REPEAT_ALL && mPlayQueue.isLast()) {
        		nextTrack = mPlayQueue.peekFirst();
        	} else {
        		nextTrack = mPlayQueue.peekNext();
        	}
    		
    		if (nextTrack != null) {
        		mPlayer.setNextDataSource(nextTrack.getUri());
        	}
    	}
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Notify listeners and store state upon change

    private void syncSeekPosition() {
    	int i = mMusicStatusCallbackList.beginBroadcast();
    	while (i > 0) {
    		i--;
    		try {
    			mMusicStatusCallbackList.getBroadcastItem(i)
    					.onPositionSync(position(), System.currentTimeMillis());
    		} catch (RemoteException e) {
    			Log.w(TAG, "syncPosition()", e);
    		}
    	}
    	mMusicStatusCallbackList.finishBroadcast();
    	
    	mPreferences.edit()
    			.putLong(PREF_SEEK_POSITION, position())
    			.apply();
    }

    private void deliverQueue() {
    	int i = mQueueStatusCallbackList.beginBroadcast();
    	while (i > 0) {
    		i--;
    		try {
    			mQueueStatusCallbackList.getBroadcastItem(i)
    					.onQueueChanged(mPlayQueue.getTrackList());
			} catch (RemoteException e) {
				Log.w(TAG, "notifyQueueChanged()", e);
			}
    	}
    	mQueueStatusCallbackList.finishBroadcast();
    }
    
    private void onMetaChanged() {
    	int i = mMusicStatusCallbackList.beginBroadcast();
    	while (i > 0) {
    		i--;
    		try {
    			mMusicStatusCallbackList.getBroadcastItem(i)
    					.onTrackChanged(getCurrentTrack());
			} catch (RemoteException e) {
				Log.w(TAG, "Remote error while performing track changed callback.", e);
			}
    	}
    	mMusicStatusCallbackList.finishBroadcast();
    	
    	syncSeekPosition();
    	onQueuePositionChanged();
    	
    	updateRCCMetaData();
    }
    
    private void onQueueChanged() {
    	// Next track may have become invalid
    	setNextTrack();
    	
    	// Queue position may have changed
    	onQueuePositionChanged();
    }
    
    private void onQueuePositionChanged() {
    	final int queuePosition = mPlayQueue.getPosition();
    	int i = mQueueStatusCallbackList.beginBroadcast();
    	while (i > 0) {
    		i--;
    		try {
    			mQueueStatusCallbackList.getBroadcastItem(i)
    					.onQueuePositionChanged(queuePosition);
			} catch (RemoteException e) {
				Log.w(TAG, "Remote error while performing queue position changed callback.", e);
			}
    	}
    	mQueueStatusCallbackList.finishBroadcast();

    	mPreferences.edit()
    			.putInt(PREF_QUEUE_POSITION, mPlayQueue.getPosition())
    			.apply();
    }
    
    private void onPlayStateChanged() {
    	int i = mMusicStatusCallbackList.beginBroadcast();
    	while (i > 0) {
    		i--;
    		try {
    			mMusicStatusCallbackList.getBroadcastItem(i)
    					.onPlayStateChanged(isPlaying());
			} catch (RemoteException e) {
				Log.w(TAG, "Remote error while performing track changed callback.", e);
			}
    	}
    	mMusicStatusCallbackList.finishBroadcast();
    	
    	syncSeekPosition();
    	
    	updateRCCPlayState();
    }
    
    private void onShuffleModeChanged() {
    	int i = mMusicStatusCallbackList.beginBroadcast();
    	while (i > 0) {
    		i--;
    		try {
    			mMusicStatusCallbackList.getBroadcastItem(i)
    					.onShuffleModeChanged(getShuffleMode());
    		} catch (RemoteException e) {
    			Log.w(TAG, "Remote error during shuffle mode change.", e);
    		}
    	}
    	mMusicStatusCallbackList.finishBroadcast();

    	mPreferences.edit()
    			.putInt(PREF_SHUFFLE_MODE, mShuffleMode)
    			.apply();
    }
    
    private void onRepeatModeChanged() {
    	int i = mMusicStatusCallbackList.beginBroadcast();
    	while (i > 0) {
    		i--;
    		try {
    			mMusicStatusCallbackList.getBroadcastItem(i)
    					.onRepeatModeChanged(getRepeatMode());
    		} catch (RemoteException e) {
    			Log.w(TAG, "notifyRepeatModeChanged()", e);
    		}
    	}
    	mMusicStatusCallbackList.finishBroadcast();

    	mPreferences.edit()
    			.putInt(PREF_REPEAT_MODE, mRepeatMode)
    			.apply();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Playback controls

    /**
    * Toggles playback between playing/paused
    */
    public synchronized void togglePlayback() {
    	if (mIsSupposedToBePlaying) {
            pause();
        } else {
            play();
        }
    }

    /**
     * Resumes or starts playback.
     */
    public synchronized void play() {
        if (mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
        	return;
        }

        // Check if player is ready
        if (mPlayer.isInitialized()) {
        	// Get the duration of the current track, if we're more than 2sec into
        	// the track and we're not going to repeat the current track, go to the
        	// next track
        	final long duration = mPlayer.duration();
        	if (mRepeatMode != REPEAT_CURRENT && duration > 2000
        			&& mPlayer.position() >= duration - 2000) {
        			
        		gotoNext();
        	}

        	mPlayer.start();
        	mPlayer.fadeUp();
            
            // Update the play state
            if (!mIsSupposedToBePlaying) {
                mIsSupposedToBePlaying = true;
                onPlayStateChanged();
            }        
        }
    }

    /**
     * Temporarily pauses playback.
     */
    public synchronized void pause() {
    	pause(false);
    }
    
    private void pause(boolean transientLossOfFocus) {
    	mPausedByTransientLossOfFocus = transientLossOfFocus;
        mPlayer.stopFadeUp();
        if (mIsSupposedToBePlaying) {
            mPlayer.pause();
            gotoIdleState();
        }
    }

    /**
     * Stops playback.
     */
    public synchronized void stop() {
    	pause();
        seek(0);
        killNotification();
        mBuildNotification = false;
    }

    /**
     * Stops playback
     * 
     * @param removeStatusIcon True to go to the idle state, false otherwise
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
    }

    public synchronized void next() {
    	gotoNext();
    }

    /**
     * Changes from the current track to the next track
     */
    private void gotoNext() {
        if (gotoNextInternal()) {
	        openAndPlay();	
        } else {
        	seek(mPlayer.duration());
        }
    }

    /**
     * Changes from the current track to the previous played track
     */
    public synchronized void previous() {
    	if (position() < 2000) {
    		gotoPrevious();
        } else {
        	seek(0);
            play();
            syncSeekPosition();
        }
    }
    
    private void gotoPrevious() {
        if (gotoPreviousInternal()) {
        	openAndPlay();
        } else {
        	seek(0);
        	pause();
        	gotoIdleState();
        }      
    }

    /**
     * Seeks the current track to a specific time
     * 
     * @param position The time to seek to
     * @return The time to play the track at
     */
    public synchronized long seek(long position) {
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Change shuffle/repeat modes

    /**
     * Cycles through the different repeat modes
     */
    public synchronized void cycleRepeat() {
        switch(mRepeatMode) {
        case REPEAT_NONE:
        	setRepeatMode(REPEAT_ALL);
        	break;
        case REPEAT_ALL:
        	setRepeatMode(REPEAT_CURRENT);
        	break;
        case REPEAT_CURRENT:
        	setRepeatMode(REPEAT_NONE);
        	break;
        }
    }

    /**
     * Sets the repeat mode
     * 
     * @param repeatmode The repeat mode to use
     */
    private void setRepeatMode(final int repeatmode) {
        if (mRepeatMode == repeatmode && !mPlayQueue.isEmpty()) {
            return;
        }

        mRepeatMode = repeatmode;
        if (mRepeatMode == REPEAT_CURRENT) {
        	mPlayer.setLooping(true);
        } else {
        	mPlayer.setLooping(false);
        	setNextTrack();
        }        
        onRepeatModeChanged();
    }

    /**
     * Cycles through the different shuffle modes
     */
    public synchronized void cycleShuffle() {
        switch(mShuffleMode) {
        case SHUFFLE_NONE:
        	setShuffleMode(SHUFFLE_NORMAL);
        	break;
        case SHUFFLE_NORMAL:
        	setShuffleMode(SHUFFLE_NONE);
        	break;
        case SHUFFLE_AUTO:
        	setShuffleMode(SHUFFLE_NONE);
        	break;
        }
    }

    /**
     * Sets the shuffle mode
     * 
     * @param shufflemode The shuffle mode to use
     */
    private void setShuffleMode(final int shufflemode) {
        if (mShuffleMode == shufflemode && !mPlayQueue.isEmpty()) {
            return;
        }
        mShuffleMode = shufflemode;
        switch(mShuffleMode) {
        case SHUFFLE_NONE:
        	mPlayQueue.unShuffle();
        	break;
        case SHUFFLE_NORMAL:
        	mPlayQueue.shuffle();
        	if (mRepeatMode == REPEAT_CURRENT) {
                setRepeatMode(REPEAT_ALL);
            }
        	break;
        case SHUFFLE_AUTO:
        	// TODO: auto shuffle
        	mShuffleMode = SHUFFLE_NONE;
        	break;
        }
        setNextTrack();
        onShuffleModeChanged();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // State accessors

    /**
     * @return True if music is playing, false otherwise
     */
    public boolean isPlaying() {
        return mIsSupposedToBePlaying;
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
     * Returns the current position in time of the currenttrack
     * 
     * @return The current playback position in miliseconds
     */
    public synchronized long position() {
    	if (mPlayer.isInitialized()) {
    		return mPlayer.position();
    	}
        return -1;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Metadata accessors

    /**
    * @return the Track that is currently playing/paused
    */
    private Track getCurrentTrack() {
    	if (!mPlayQueue.isEmpty()) {
    		return mPlayQueue.current();
    	}
    	return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Queue manipulators

    /**
     * Opens a list of tracks for playback
     * 
     * @param list The list of tracks to open
     * @param position The position to start playback at
     */
    public synchronized void open(List<Track> list, int position) {
    	if (mShuffleMode == SHUFFLE_AUTO) {
            setShuffleMode(SHUFFLE_NORMAL);
        }
    	
    	long oldId = -1;
    	Track currentTrack = mPlayQueue.current();
    	if (currentTrack != null) {
    		oldId = currentTrack.getId();
    	}
    	
    	if (mPlayQueue.open(list, position, mShuffleMode == SHUFFLE_NORMAL)) { // if queue changed
    		onQueueChanged();
    		mPlayQueueDatabase.open(list, mPlayQueue.getShuffleMap());
    	}
    	
        openCurrentAndNext();
        play();
        if (oldId != mPlayQueue.current().getId()) {
            onMetaChanged();
        }
    }
    
    public synchronized void shuffle(List<Track> tracks) {
    	setShuffleMode(SHUFFLE_NORMAL);
    	open(tracks, -1);
    }
    
    /**
     * Queues a new list for playback
     * 
     * @param list The list to queue
     * @param action The action to take
     */
    public synchronized void enqueue(final List<Track> list, final int action) {
    	if (list == null || list.isEmpty()) {
    		return;
    	}
    	
    	boolean playQueueWasEmpty = mPlayQueue.isEmpty();
    	switch(action) {
    	case NEXT:
    		final int playPosition = mPlayQueue.getPosition();
    		if (mShuffleMode == SHUFFLE_NONE && playPosition + 1 < mPlayQueue.size()) {
    			mPlayQueue.addAll(playPosition + 1, list);
    			mPlayQueueDatabase.addAll(playPosition + 1, list);
    		} else {
    			int end = mPlayQueue.size() - 1;
    			mPlayQueue.addAll(list);
    			mPlayQueueDatabase.addAll(end, list);
    		}
    		break;
    	case NOW:
    		int end = mPlayQueue.size() - 1;
    		mPlayQueue.addAll(list);
    		mPlayQueueDatabase.addAll(end, list);
    		mPlayQueue.moveToNext();
    		openCurrentAndNext();
    		play();
    		break;
    	case LAST:
    		end = mPlayQueue.size() - 1;
    		mPlayQueue.addAll(list);
    		mPlayQueueDatabase.addAll(end, list);
    		break;
    	}
   		
   		onQueueChanged();
    	if (playQueueWasEmpty) {
			onMetaChanged();
		}
    }

     /**
     * Sets the position of a track in the queue
     * 
     * @param index The position to place the track
     */
    public synchronized void setQueuePosition(final int index) {
        if (mPlayQueue.moveToPosition(index)) {
        	openAndPlay();
        }
    }

    /**
     * Removes the track in the specified position in the play queue
     * 
     * @param position
     */
    public synchronized void removeTrack(int position) {
    	int playPosition = mPlayQueue.getPosition();
    	mPlayQueue.remove(position);
    	mPlayQueueDatabase.remove(position);
    	
    	if (playPosition == position) {
    		openAndPlay();
    	}
    	onQueueChanged();
    }

    /**
     * Moves an item in the queue from one position to another
     * 
     * @param from The position the item is currently at
     * @param to The position the item is being moved to
     */
    public synchronized void moveQueueItem(int from, int to) {
   		if (mPlayQueue.moveItem(from, to)) {
   			mPlayQueueDatabase.move(from, to);
   			onQueuePositionChanged();
   			setNextTrack();
   		}
        onQueueChanged();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Update notification/lock screen controls

    /**
     * Creates/updates the notification if that has been requested or the app has gone to 
     * the background.
     */
    private void buildNotification() {
        if (mBuildNotification || AppUtils.isApplicationSentToBackground(this)) {
            final Track currentTrack = getCurrentTrack();
        	mNotificationHelper.buildNotification(currentTrack, null);
        }
    }

    private void updateRCCPlayState() {
    	mRemoteControlClient
				.setPlaybackState(mIsSupposedToBePlaying ? RemoteControlClient.PLAYSTATE_PLAYING
						: RemoteControlClient.PLAYSTATE_PAUSED);
    }
    
    private void updateRCCMetaData() {
    	final Track track = getCurrentTrack();
    	if (track != null) {
    		mRemoteControlClient.editMetadata(true)
				.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, track.getArtist())
				.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, track.getAlbum())
				.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, track.getTitle())
				.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, track.getDuration())
				/*.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK,
						getAlbumArt(track))*/.apply();    		
    	}
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Internal callbacks
    
    private void refreshMusicStatus() {
    	onMetaChanged();
    	onPlayStateChanged();
    	onShuffleModeChanged();
    	onRepeatModeChanged();
    }

    private void registerMusicStatusCallback(IMusicStatusCallback callback) {
    	mMusicStatusCallbackList.register(callback);
    }
    
    private void unregisterMusicStatusCallback(IMusicStatusCallback callback) {
    	mMusicStatusCallbackList.unregister(callback);
    }

    private void refreshQueueStatus() {
    	deliverQueue();
    	onMetaChanged();
    }
    
    private void registerQueueStatusCallback(IQueueStatusCallback callback) {
    	mQueueStatusCallbackList.register(callback);
    }
    
    private void unregisterQueueStatusCallback(IQueueStatusCallback callback) {
    	mQueueStatusCallbackList.unregister(callback);
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Audio events
    
    @Override
	public void onTrackWentToNext() {
		Log.d(TAG, "Track went to next");
		if (gotoNextInternal()) {
			onMetaChanged();
			buildNotification();
			setNextTrack();
		} else {
			gotoIdleState();
		}
	}

	/**
	 * This callback will only ever be called if there is no next track set
	 */
	@Override
	public void onTrackEnded() {
		Log.d(TAG, "Track ended");
		gotoIdleState();
	}

	@Override
	public void onServerDied() {
		if (mIsSupposedToBePlaying) {
			gotoNext();
		} else {
			openCurrentAndNext();
		}		
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		switch (focusChange) {
        case AudioManager.AUDIOFOCUS_LOSS:
            boolean focusLoss = isPlaying() ? false : mPausedByTransientLossOfFocus;
            pause(focusLoss);            
            break;
        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
            mPlayer.fadeDown();
            break;
        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            pause(isPlaying());
            break;
        case AudioManager.AUDIOFOCUS_GAIN:
            if (!isPlaying() && mPausedByTransientLossOfFocus) {
                mPausedByTransientLossOfFocus = false;
                mPlayer.mute();
                play();
            } else {
            	mPlayer.fadeUp();
            }
            break;
        default:
        	break;
        }		
	}

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Broadcast receivers

    private class IntentReceiver extends BroadcastReceiver {

    	@Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (ACTION_NEXT.equals(action)) {
                next();
            } else if (ACTION_PREVIOUS.equals(action)) {
                previous();
            } else if (ACTION_TOGGLE_PLAYBACK.equals(action)) {
                togglePlayback();
            } else if (ACTION_PAUSE.equals(action)) {
                pause();
            } else if (ACTION_PLAY.equals(action)) {
                play();
            } else if (ACTION_STOP.equals(action)) {
                stop();
            } else if (ACTION_REPEAT.equals(action)) {
                cycleRepeat();
            } else if (ACTION_SHUFFLE.equals(action)) {
                cycleShuffle();
            }
        }
    }

    private class UnmountReceiver extends BroadcastReceiver {
    	@Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                mMediaMounted = false;
                stop(true);
		        onMetaChanged();
            } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                mCardId = getCardId();
                restoreState();
                mMediaMounted = true;
                onMetaChanged();
            }
        }
    }

    private static class DelayedStopHandler extends Handler {

        private final WeakReference<MusicService> mService;
        
        public DelayedStopHandler(MusicService service) {
        	mService = new WeakReference<MusicService>(service);
        }
    	
    	@Override
        public void handleMessage(final Message msg) {
            if (!mService.get().isPlaying() && !mService.get().mPausedByTransientLossOfFocus 
            		&& !mService.get().mIsBound) {
                
                mService.get().stopSelf(mService.get().mServiceStartId);
            }
        }
    }

    private static final class ServiceStub extends IMusicService.Stub {

		private final WeakReference<MusicService> mService;
		
		public ServiceStub(MusicService service) {
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
		public void requestMusicStatusRefresh() throws RemoteException {
			mService.get().refreshMusicStatus();
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
		public void enqueue(List<Track> tracks, int action) 
				throws RemoteException {

			mService.get().enqueue(tracks, action);
		}

		@Override
		public void requestQueueStatusRefresh() throws RemoteException {
			mService.get().refreshQueueStatus();			
		}

		@Override
		public void registerQueueStatusCallback(IQueueStatusCallback callback)
				throws RemoteException {
			
			mService.get().registerQueueStatusCallback(callback);
		}

		@Override
		public void unregisterQueueStatusCallback(IQueueStatusCallback callback)
				throws RemoteException {
			
			mService.get().unregisterQueueStatusCallback(callback);
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
		public void cycleShuffleMode() throws RemoteException {
			mService.get().cycleShuffle();
			
		}

		@Override
		public void cycleRepeatMode() throws RemoteException {
			mService.get().cycleRepeat();
			
		}
		
	}
	
}
