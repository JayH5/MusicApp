package za.jamie.soundstage.service;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import za.jamie.soundstage.IMusicService;
import za.jamie.soundstage.IMusicStatusCallback;
import za.jamie.soundstage.IQueueStatusCallback;
import za.jamie.soundstage.bitmapfun.ImageCache;
import za.jamie.soundstage.models.Track;
import za.jamie.soundstage.utils.AppUtils;
import za.jamie.soundstage.utils.HexUtils;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;

/**
 * A backbround {@link Service} used to keep music playing between activities
 * and when the user moves Apollo into the background.
 */
public class MusicService extends Service implements GaplessPlayer.PlayerEventListener, 
		AudioManager.OnAudioFocusChangeListener {

	private static final String TAG = "MusicService";
	
	// Intent actions for external control
	public static final String SERVICECMD = "za.jamie.soundstage.musicservicecommand";
	public static final String ACTION_TOGGLE_PLAYBACK = "za.jamie.soundstage.action.TOGGLE_PLAYBACK";
    public static final String ACTION_PLAY = "za.jamie.soundstage.action.PLAY";
    public static final String ACTION_PAUSE = "za.jamie.soundstage.action.PAUSE";
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
    public static final String REPEATMODE_CHANGED_MODE = "za.jamie.soundstage.repeatmodechanged.MODE";
    
    public static final String SHUFFLEMODE_CHANGED = "za.jamie.soundstage.shufflemodechanged";
    public static final String SHUFFLEMODE_CHANGED_MODE = "za.jamie.soundstage.shufflemodechanged.MODE";
    
    // Shared preference keys
    private static final String PREFERENCES = "Service";
    private static final String PREF_CARD_ID = "cardid";
    //private static final String PREF_QUEUE = "queue";
    private static final String PREF_CURRENT_POSITION = "curpos";
    private static final String PREF_SEEK_POSITION = "seekpos";
    private static final String PREF_REPEAT_MODE = "repeatmode";
    private static final String PREF_SHUFFLE_MODE = "shufflemode";
    private static final String PREF_HISTORY = "history";
    
    // Shuffle modes
    public static final int SHUFFLE_NONE = 0;
    public static final int SHUFFLE_NORMAL = 1;
    public static final int SHUFFLE_AUTO = 2;
    private int mShuffleMode = SHUFFLE_NONE;
    
    private final Shuffler mShuffler = new Shuffler();
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
    private PlayQueue mPlayQueue;
    
    private static final Uri BASE_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private static final String[] PROJECTION = new String[] {
    	MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST_ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.DURATION
    };
    private static final String IS_MUSIC_SELECTION = 
    		MediaStore.Audio.Media.IS_MUSIC + "=1";
    
    private int mOpenFailedCounter = 0;
    
    // The queue is saved/loaded from a SharedPreferences instance and stored
    // as a String representation of a hex number
    private SharedPreferences mPreferences;
    private int mCardId;
    
    // Keep track of previously played tracks
    private final List<Integer> mHistory = new LinkedList<Integer>();
    private static final int MAX_HISTORY_SIZE = 100;

    // Audio playback objects
    private AudioManager mAudioManager;
    private GaplessPlayer mPlayer;
    private DelayedHandler mDelayedStopHandler;
    
    private NotificationHelper mNotificationHelper; // Notifications 
    private boolean mBuildNotification = false;
    private RemoteControlClient mRemoteControlClient; // Lockscreen controls
    private ComponentName mMediaButtonReceiverComponent; // Media buttons
    
    private BroadcastReceiver mUnmountReceiver = null;
    //private int mMediaMountedCount = 0;
    
    // Delay before releasing the media player to ensure fast stop/start
    private static final int IDLE_DELAY = 60000;
	
	private final IBinder mBinder = new ServiceStub(this);
    
    private final RemoteCallbackList<IMusicStatusCallback> mMusicStatusCallbackList =
    		new RemoteCallbackList<IMusicStatusCallback>();
    
    private final RemoteCallbackList<IQueueStatusCallback> mQueueStatusCallbackList =
    		new RemoteCallbackList<IQueueStatusCallback>();
	
	private int mServiceStartId = -1;
	private WakeLock mWakeLock;

	private ImageCache mImageCache;
    
    @Override
    public IBinder onBind(final Intent intent) {
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mServiceInUse = true;
        final String intentAction = intent.getAction();
        if (IMusicService.class.getName().equals(intentAction)) {
        	Log.d(TAG, "Binding a music status interface.");
        	return mBinder;
        }
        
        Log.d(TAG, "Binding nothing...");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onUnbind(final Intent intent) {
        mServiceInUse = false;
        saveState();

        if (mIsSupposedToBePlaying || mPausedByTransientLossOfFocus) {
            // Something is currently playing, or will be playing once
            // an in-progress action requesting audio focus ends, so don't stop
            // the service now.
            return true;

            // If there is a playlist but playback is paused, then wait a while
            // before stopping the service, so that pause/resume isn't slow.
            // Also delay stopping the service if we're transitioning between
            // tracks.
        //} else if (!mPlayQueue.isEmpty() || mPlayerHandler.hasMessages(TRACK_ENDED)) {
        // TODO: Check is mPlayerHandler.hasMessages(TRACK_ENDED) is necessary for
        // gapless player.
        } else if (!mPlayQueue.isEmpty()) {
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

        mImageCache = ImageCache.getInstance(this);
        
        mPlayQueue = new PlayQueue(this);

        // Initialize the player and the fader
        mPlayer = new GaplessPlayer(this);
        mPlayer.setPlayerEventListener(this);
        
        mDelayedStopHandler = new DelayedHandler(this);
                
        // Initialize the audio manager and register any headset controls for
        // playback
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initRemoteControlClient();

        // Initialize the preferences
        mPreferences = getSharedPreferences(PREFERENCES, 0);
        mCardId = getCardId();

        registerExternalStorageListener();

        initBroadcastReceiver();

        // Initialize the wake lock
        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.setReferenceCounted(false);

        // Bring the queue back
        restoreState();

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
    	// Initialize the intent filter and each action
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_TOGGLE_PLAYBACK);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_STOP);
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_PREVIOUS);
        filter.addAction(ACTION_REPEAT);
        filter.addAction(ACTION_SHUFFLE);
        
        filter.addAction(KILL_FOREGROUND);
        filter.addAction(START_BACKGROUND);
        
        // Attach the broadcast listener
        registerReceiver(mIntentReceiver, filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // Let the equalisers know we're not going to be playing music
        final Intent audioEffectsIntent = new Intent(
        		AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, mPlayer.getAudioSessionId());
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(audioEffectsIntent);

        // Release the player
        mPlayer.stopFade();
        mPlayer.release();
        mPlayer = null;

        // Remove the audio focus listener and lock screen controls
        mAudioManager.abandonAudioFocus(this);
        mAudioManager.unregisterRemoteControlClient(mRemoteControlClient);
        mAudioManager.unregisterMediaButtonEventReceiver(mMediaButtonReceiverComponent);

        // Remove any callbacks from the handlers
        mDelayedStopHandler.removeCallbacksAndMessages(null);        
        
        // Close play queue database
        mPlayQueue.closeDb();
        
        // Remove music control callbacks
        mMusicStatusCallbackList.kill();
        mQueueStatusCallbackList.kill();

        // Unregister the mount listener
        unregisterReceiver(mIntentReceiver);
        if (mUnmountReceiver != null) {
            unregisterReceiver(mUnmountReceiver);
            mUnmountReceiver = null;
        }

        // Release the wake lock
        mWakeLock.release();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        mServiceStartId = startId;
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        if (intent != null) {
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
            } else if (KILL_FOREGROUND.equals(action)) {
                mBuildNotification = false;
                killNotification();
            } else if (START_BACKGROUND.equals(action)) {
                mBuildNotification = true;
                buildNotification();
            }
        }

        // Make sure the service will shut down on its own if it was
        // just started but not bound to and nothing is playing
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        final Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
        return START_STICKY;
    }

    /**
     * Creates/updates the notification if that has been requested or the app has gone to 
     * the background.
     */
    public void buildNotification() {
        if (mBuildNotification || AppUtils.isApplicationSentToBackground(this)) {
            final Track currentTrack = getCurrentTrack();
        	mNotificationHelper.buildNotification(currentTrack, getAlbumArt(currentTrack));
        }
    }

    /**
     * Removes the notification
     */
    public void killNotification() {
        stopForeground(true);
    }

    /**
     * @return A card ID (filesystem ID) used to save and restore playlists, i.e., the queue.
     */
    private int getCardId() {
        Cursor cursor = getContentResolver()
        		.query(Uri.parse("content://media/external/fs_id"), null, null, null, null);
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

    /**
     * Called when we receive a ACTION_MEDIA_EJECT notification.
     * 
     * @param storagePath The path to mount point for the removed media
     */
    public void closeExternalStorageFiles(final String storagePath) {
        stop(true);
        notifyQueueChanged();
        notifyMetaChanged();
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

                @Override
                public void onReceive(final Context context, final Intent intent) {
                    final String action = intent.getAction();
                    if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                        saveState();
                        mQueueIsSaveable = false;
                        closeExternalStorageFiles(intent.getData().getPath());
                    } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                        //mMediaMountedCount++;
                        mCardId = getCardId();
                        restoreState();
                        mQueueIsSaveable = true;
                        notifyQueueChanged();
                        notifyMetaChanged();
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
     * Changes the notification buttons to a paused state and begins the
     * countdown to calling {@code #stopForeground(true)}
     */
    private void gotoIdleState() {
        // Post a delayed message to stop service
    	mDelayedStopHandler.removeCallbacksAndMessages(null);
        final Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
        
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
            notifyPlayStateChanged();
        }
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

    /**
     * Called to open a new file as the current track and prepare the next for
     * playback
     */
    private void openCurrentAndNext() {    	
    	openCurrent();
        setNextTrack();
    }

    /**
     * We don't want to open the current and next track when the user is using
     * the {@code #prev()} method because they won't be able to travel back to
     * the previously listened track if they're shuffling.
     */
    private void openCurrent() {
    	if (mPlayQueue.isEmpty()) {
    		return;
    	}
    	stop(false);
            
    	// While the next track fails to open
    	while (!mPlayer.setDataSource(mPlayQueue.getCurrentUri())) {
    		if (mOpenFailedCounter++ < 10 && mPlayQueue.size() > 1) {
    			// Try to get the next play position
    			final int pos = getNextPosition(false);
    			// If that position is invalid, stop trying
    			if (pos < 0) {
    				gotoIdleState();
    				return;
    			}
    			mPlayQueue.setPlayPosition(pos);
    			stop(false);
    			// If we've tried to play too many times, give up
    		} else {
    			mOpenFailedCounter = 0;
    			gotoIdleState();
    			return;
    		}
    	}
    }

    /**
     * @param force True to force the player onto the track next, false
     *            otherwise.
     * @return The next position to play.
     */
    private int getNextPosition(final boolean force) {
    	int playPosition = mPlayQueue.getPlayPosition();
    	if (!force && mRepeatMode == REPEAT_CURRENT) {
    		if (playPosition < 0) {
    			return 0;
    		}
    		return playPosition;
    	} else if (mShuffleMode == SHUFFLE_NORMAL) {
    		if (playPosition >= 0) {
    			mHistory.add(playPosition);
    		}
    		if (mHistory.size() > MAX_HISTORY_SIZE) {
    			mHistory.remove(0);
    		}
    		final int numTracks = mPlayQueue.size();
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
    		return playPosition + 1;
    	} else {
    		if (playPosition >= mPlayQueue.size() - 1) {
    			if (mRepeatMode == REPEAT_NONE && !force) {
    				return -1;
    			} else if (mRepeatMode == REPEAT_ALL || force) {
    				return 0;
    			}
    			return -1;
    		} else {
    			return playPosition + 1;
    		}
    	}
    }

    /**
     * Sets the track track to be played
     */
    private void setNextTrack() {
    	final int nextPlayPosition = getNextPosition(false);
    	mPlayQueue.setNextPlayPosition(nextPlayPosition);
    	if (nextPlayPosition >= 0) {
    		mPlayer.setNextDataSource(mPlayQueue.getNextUri());
    	}
    }

    /**
     * Makes a list of shuffled list of tracks.
     * @return true if shuffle list was successfully created
     */
    private boolean makeAutoShuffleList() {
        boolean cursorValid = false;
    	Cursor cursor = getContentResolver().query(
            		BASE_URI, PROJECTION, IS_MUSIC_SELECTION, 
                    null, null);
            
        if (cursor != null) {
        	cursorValid = cursor.moveToFirst();
        	if (cursorValid) {
        		final List<Track> list = new ArrayList<Track>(cursor.getCount());
                	
            	do {
            		list.add(cursorToTrack(cursor));
                } while (cursor.moveToNext());
                	
                mAutoShuffleList = list;
            }
            cursor.close();
        }
        return cursorValid;
    }
    
    private Track cursorToTrack(Cursor cursor) {
    	return new Track(cursor.getLong(0), 
        		cursor.getString(1), 
        		cursor.getLong(2), 
        		cursor.getString(3), 
        		cursor.getLong(4), 
        		cursor.getString(5),
        		cursor.getLong(6));
    }

    /**
     * Creates the party shuffle playlist
     */
    private void doAutoShuffleUpdate() {
        boolean notify = false;
        final int playPosition = mPlayQueue.getPlayPosition();
        if (playPosition > 10) {
        	removeTracks(0, playPosition - 9);
        	notify = true;
        }
        final int toAdd = 7 - (mPlayQueue.size() - (playPosition < 0 ? -1 : playPosition));
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
        	notify = true;
        }
       	if (notify) {
       		notifyQueueChanged();
       	}
    }

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

    private void syncPosition() {
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
    }
    
    private void notifyMetaChanged() {
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
    	
    	syncPosition();
    	notifyQueuePositionChanged();
    	
    	updateRCCMetaData();
    }
    
    private void notifyQueuePositionChanged() {
    	int i = mQueueStatusCallbackList.beginBroadcast();
    	while (i > 0) {
    		i--;
    		try {
    			mQueueStatusCallbackList.getBroadcastItem(i)
    					.onQueuePositionChanged(getQueuePosition());
			} catch (RemoteException e) {
				Log.w(TAG, "Remote error while performing queue position changed callback.", e);
			}
    	}
    	mQueueStatusCallbackList.finishBroadcast();
    }
    
    private void notifyPlayStateChanged() {
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
    	
    	syncPosition();
    	
    	updateRCCPlayState();
    }
    
    private void notifyShuffleModeChanged() {
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
    	
    	saveState();
    }
    
    private void notifyRepeatModeChanged() {
    	int i = mMusicStatusCallbackList.beginBroadcast();
    	while (i > 0) {
    		i--;
    		try {
    			mMusicStatusCallbackList.getBroadcastItem(i)
    					.onShuffleModeChanged(getShuffleMode());
    		} catch (RemoteException e) {
    			Log.w(TAG, "notifyRepeatModeChanged()", e);
    		}
    	}
    	mMusicStatusCallbackList.finishBroadcast();
    	
    	saveState();
    }
    
    private void notifyQueueChanged() {
    	saveState();
    }
    
    private void deliverQueue() {
    	int i = mQueueStatusCallbackList.beginBroadcast();
    	while (i > 0) {
    		i--;
    		try {
    			mQueueStatusCallbackList.getBroadcastItem(i)
    					.onQueueChanged(getQueue());
			} catch (RemoteException e) {
				Log.w(TAG, "notifyQueueChanged()", e);
			}
    	}
    	mQueueStatusCallbackList.finishBroadcast();
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
				.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK,
						getAlbumArt(track)).apply();    		
    	}
    }
    
    /**
     * Saves the queue and state information such as queue position and seek position.
     * 
     */
    private void saveState() {
    	if (!mQueueIsSaveable) {
            return;
        }

        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(PREF_CARD_ID, mCardId);
        
        if (mShuffleMode != SHUFFLE_NONE) {
            editor.putString(PREF_HISTORY, HexUtils.intListToHexString(mHistory));
        }
        editor.putInt(PREF_CURRENT_POSITION, mPlayQueue.getPlayPosition());
        if (mPlayer.isInitialized()) {
        	editor.putLong(PREF_SEEK_POSITION, mPlayer.position());
        }
        editor.putInt(PREF_REPEAT_MODE, mRepeatMode);
        editor.putInt(PREF_SHUFFLE_MODE, mShuffleMode);
        editor.apply();
    }    

    /**
     * Reloads the queue as the user left it
     */
    private void restoreState() {
        String q = null;
        // Restore the card id
        int id = mCardId;
        if (mPreferences.contains(PREF_CARD_ID)) {
            id = mPreferences.getInt(PREF_CARD_ID, ~mCardId);
        }
        // If it matches our current card id then get the saved queue
        if (id == mCardId) {
            //q = mPreferences.getString(PREF_QUEUE, "");
        	// Restore the queue position
        	final int pos = mPreferences.getInt(PREF_CURRENT_POSITION, 0);
    		if (pos < 0 || pos >= mPlayQueue.size()) {
    			return;
    		}
    		mPlayQueue.setPlayPosition(pos);
    		
    		// Try to open the current queue position
            mOpenFailedCounter = 20;
            openCurrentAndNext();
            if (!mPlayer.isInitialized()) {
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
            mRepeatMode = repeatMode;

            // Get the saved shuffle mode
            int shuffleMode = mPreferences.getInt(PREF_SHUFFLE_MODE, SHUFFLE_NONE);
            // If it is invalid, switch shuffle off
            if (shuffleMode != SHUFFLE_AUTO && shuffleMode != SHUFFLE_NORMAL) {
            	shuffleMode = SHUFFLE_NONE;
            }
            
            // Restore the history if shuffle is on
            if (shuffleMode != SHUFFLE_NONE) {
                q = mPreferences.getString(PREF_HISTORY, "");
                if (q != null && !q.isEmpty()) {
                    mHistory.addAll(HexUtils.hexStringToIntList(q));
                }
            }
            if (shuffleMode == SHUFFLE_AUTO) {
                if (!makeAutoShuffleList()) {
                	shuffleMode = SHUFFLE_NONE;
                }
            }
            mShuffleMode = shuffleMode;
        }
    }    

	
	/**
     * Opens a list for playback
     * 
     * @param list The list of tracks to open
     * @param position The position to start playback at
     */
    public synchronized void open(final List<Track> list, final int position) {
    	if (mShuffleMode == SHUFFLE_AUTO) {
            mShuffleMode = SHUFFLE_NORMAL;
        }
    	// Check if list is the same as current list
    	if (list.size() == mPlayQueue.size()) {
    		long[] trackIds = mPlayQueue.getQueueIds();
    		int i = 0;
    		boolean match = true;
    		for (Track track : list) {
    			if (track.getId() != trackIds[i]) {
    				match = false;
    				break;
    			}
    		}
    		if (match) {
    			if (position == getQueuePosition()) {
    				play();
    			} else {
    				setQueuePosition(position);
    			}
    			return;
    		}
    	}
    	
    	long oldId = mPlayQueue.getCurrentId();
    	mPlayQueue.openList(list);
    	notifyQueueChanged(); 
    	
    	if (position >= 0 || position > list.size()) {
    		mPlayQueue.setPlayPosition(position);
    	} else {
    		mPlayQueue.setPlayPosition(mShuffler.nextInt(mPlayQueue.size()));
    	}
        
        mHistory.clear();
        openCurrentAndNext();
        play();
        if (oldId != mPlayQueue.getCurrentId()) {
            notifyMetaChanged();
        }
    }
    
    /**
     * Queues a new list for playback
     * 
     * @param list The list to queue
     * @param action The action to take
     */
    public synchronized void enqueue(final List<Track> list, final int action) {
    	int playPosition = -1;
   		playPosition = getQueuePosition();
   		if (action == NEXT && playPosition + 1 < mPlayQueue.size()) {
   			mPlayQueue.addAll(playPosition + 1, list);
   			notifyQueueChanged();
   			if (mPlayQueue.isEmpty()) {
   				notifyMetaChanged();
   			}
   		} else {
   			mPlayQueue.addAll(list);
   			notifyQueueChanged();
   			if (mPlayQueue.isEmpty()) {
   				notifyMetaChanged();
   			}
   			if (action == NOW) {
   				openAndPlay(mPlayQueue.size() - list.size());
   				return;
   			}
   		}

        if (playPosition < 0) {
            openAndPlay(0);
        }
    }

    /**
     * Sets the position of a track in the queue
     * 
     * @param index The position to place the track
     */
    public synchronized void setQueuePosition(final int index) {
        stop(false);
        openAndPlay(index);
        if (mShuffleMode == SHUFFLE_AUTO) {
            doAutoShuffleUpdate();
        }
    }

    /**
     * Moves an item in the queue from one position to another
     * 
     * @param from The position the item is currently at
     * @param to The position the item is being moved to
     */
    public synchronized void moveQueueItem(int from, int to) {
   		mPlayQueue.moveQueueItem(from, to);
        notifyQueueChanged();
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
    	/*int playPosition = -1;
   		playPosition = mPlayQueue.getPlayPosition();
    	
   		int numRemoved = mPlayQueue.removeTracks(first, last);
    	
    	if (first <= playPosition && playPosition <= last) {
        	goToNextInternal();
        }
    	
        if (numRemoved > 0) {
            notifyQueueChanged();
        }
        return numRemoved;*/
    	return 0;
    }
    
    public synchronized void removeTrack(int position) {
    	int playPosition = mPlayQueue.getPlayPosition();
    	mPlayQueue.removeTrack(position);
    	
    	if (playPosition == position) {
    		goToNextInternal();
    	}
    	notifyQueueChanged();
    }

    /**
     * Removes all instances of the track with the given ID from the playlist.
     * 
     * @param id The id to be removed
     * @return how many instances of the track were removed
     */
    public synchronized int removeTrackById(final long id) {
   		boolean goToNext = (id == mPlayQueue.getCurrentId());
    		
    	int numRemoved = mPlayQueue.removeTrack(id);
        	
    	// If one of the tracks removed was the one playing
    	if (goToNext) {
    		goToNextInternal();
    	}
        
    	if (numRemoved > 0) {
    		notifyQueueChanged();
        }
        return numRemoved;
    }    

    /**
     * Returns a copy of the List backing the play queue
     * 
     * @return The queue as a List<Track>
     */
    public synchronized List<Track> getQueue() {
    	return mPlayQueue.getQueue();
    }
    
    /**
     * Returns the ids of the tracks in the play queue
     * 
     * @return The queue as a List<Track>
     */
    public synchronized long[] getQueueIds() {
    	return mPlayQueue.getQueueIds();
    }

    /**
     * Returns the position in the queue
     * 
     * @return the current position in the queue
     */
    public synchronized int getQueuePosition() {
        return mPlayQueue.getPlayPosition();
    }

    /**
    * @return the Track that is currently playing/paused
    */
    public synchronized Track getCurrentTrack() {
    	return mPlayQueue.getCurrentTrack();
    }

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
    
/////////////////////////////////////////////////

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
     * Stops playback.
     */
    public synchronized void stop() {
    	pause();
        seek(0);
        killNotification();
        mBuildNotification = false;
    }

    /**
     * Resumes or starts playback.
     */
    public synchronized void play() {
        mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        // Check if player is ready
        boolean playerInitialized = mPlayer.isInitialized();
        if (playerInitialized) {
        	// Get the duration of the current track, if we're more than 2sec into
        	// the track and we're not going to repeat the current track, go to the
        	// next track
        	final long duration = mPlayer.duration();
        	if (mRepeatMode != REPEAT_CURRENT && duration > 2000
        			&& mPlayer.position() >= duration - 2000) {
        			
        		gotoNext(true);
        	}

        	mPlayer.start();
        	mPlayer.fadeUp();
        	
        	// Update the notification
            buildNotification();
            
            // Update the play state
            if (!mIsSupposedToBePlaying) {
                mIsSupposedToBePlaying = true;
                notifyPlayStateChanged();
            }        
        } else {
        	if (mPlayQueue.isEmpty()) {
        		setShuffleMode(SHUFFLE_AUTO);
        	}
        }
    }

    /**
     * Changes from the current track to the next track
     */
    private void gotoNext(final boolean force) {
        if (mPlayQueue.isEmpty()) {
            return;
        }
        final int pos = getNextPosition(force);
        if (pos < 0) {
            gotoIdleState();
            return;
        }
        mPlayQueue.setPlayPosition(pos);
        stop(false);
        openAndPlay(pos);
        notifyMetaChanged();
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
     * Changes from the current track to the next to be played
     */
    public void next() {
    	gotoNext(true);
    }

    /**
     * Changes from the current track to the previous played track
     */
    public synchronized void previous() {
    	if (position() < 2000) {
            gotoPrev();
        } else {
            seek(0);
            play();
            syncPosition();
        }
    }
    
    private void gotoPrev() {
    	if (mShuffleMode == SHUFFLE_NORMAL) {
            // Go to previously-played track and remove it from the history
            final int histsize = mHistory.size();
            if (histsize == 0) {
                return;
            }
            mPlayQueue.setPlayPosition(mHistory.remove(histsize - 1));
        } else {
            final int playPosition = mPlayQueue.getPlayPosition();
        	if (playPosition > 0) {
                mPlayQueue.setPlayPosition(playPosition - 1);
            } else {
            	mPlayQueue.setPlayPosition(mPlayQueue.size() - 1);
            }
        }
        stop(false);
        openCurrent();
        play();
        notifyMetaChanged();
    }

    /**
     * Cycles through the different repeat modes
     */
    public synchronized void cycleRepeat() {
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
     * Sets the repeat mode
     * 
     * @param repeatmode The repeat mode to use
     */
    private void setRepeatMode(final int repeatmode) {
        mRepeatMode = repeatmode;
        setNextTrack();
        saveState();
        notifyRepeatModeChanged();
    }

    /**
     * Cycles through the different shuffle modes
     */
    public synchronized void cycleShuffle() {
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
     * Sets the shuffle mode
     * 
     * @param shufflemode The shuffle mode to use
     */
    private void setShuffleMode(final int shufflemode) {
        if (mShuffleMode == shufflemode && !mPlayQueue.isEmpty()) {
            return;
        }
        mShuffleMode = shufflemode;
        if (mShuffleMode == SHUFFLE_AUTO) {
            if (makeAutoShuffleList()) {
                doAutoShuffleUpdate();
                openAndPlay(0);
                return;
            } else {
                mShuffleMode = SHUFFLE_NONE;
            }
        }
        saveState();
        notifyShuffleModeChanged();
    }
    /////////////////////////////////
    
    private void refreshMusicStatus() {
    	notifyMetaChanged();
    	notifyPlayStateChanged();
    	notifyShuffleModeChanged();
    	notifyRepeatModeChanged();
    }
    
    private void registerMusicStatusCallback(IMusicStatusCallback callback) {
    	mMusicStatusCallbackList.register(callback);
    }
    
    private void unregisterMusicStatusCallback(IMusicStatusCallback callback) {
    	mMusicStatusCallbackList.unregister(callback);
    }
    
    private void refreshQueueStatus() {
    	deliverQueue();
    	notifyMetaChanged();
    }
    
    private void registerQueueStatusCallback(IQueueStatusCallback callback) {
    	mQueueStatusCallbackList.register(callback);
    }
    
    private void unregisterQueueStatusCallback(IQueueStatusCallback callback) {
    	mQueueStatusCallbackList.unregister(callback);
    }

    /////////////////////////////////
    private void openAndPlay(int position) {
    	mPlayQueue.setPlayPosition(position);
    	openCurrentAndNext();
        play();
        notifyMetaChanged();
    }

    private void goToNextInternal() {
    	if (mPlayQueue.isEmpty()) {
            stop(true);
            mPlayQueue.setPlayPosition(-1);
        } else {
            if (mPlayQueue.getPlayPosition() >= mPlayQueue.size()) {
            	mPlayQueue.setPlayPosition(0);
            }
            final boolean wasPlaying = isPlaying();
            stop(false);
            openCurrentAndNext();
            if (wasPlaying) {
                play();
            }
        }
        notifyMetaChanged();
    }

    /**
     * @return The album art for the current album.
     */
    private Bitmap getAlbumArt(Track track) {
        // Return the cached artwork
        return mImageCache.getBitmapFromCache(String.valueOf(track.getAlbumId()));
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        /**
         * {@inheritDoc}
         */
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
            } else if (KILL_FOREGROUND.equals(action)) {
                mBuildNotification = false;
                killNotification();
            } else if (START_BACKGROUND.equals(action)) {
                mBuildNotification = true;
                buildNotification();
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
                    //TODO
            		/*|| mService.get().mPlayerHandler.hasMessages(TRACK_ENDED)*/) {
                return;
            }
            mService.get().saveState();
            mService.get().stopSelf(mService.get().mServiceStartId);
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

	@Override
	public void onTrackWentToNext() {
		mPlayQueue.goToNext();
		notifyMetaChanged();
		buildNotification();
		setNextTrack();		
	}

	@Override
	public void onTrackEnded() {
		if (mRepeatMode == REPEAT_CURRENT) {
			seek(0);
			play();
		} else {
			gotoNext(false);
		}	
	}

	@Override
	public void onServerDied() {
		if (mIsSupposedToBePlaying) {
			gotoNext(true);
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
		public void removeTrackById(long id) throws RemoteException {
			mService.get().removeTrackById(id);
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
		public void open(List<Track> tracks, int position) throws RemoteException {
			mService.get().open(tracks, position);
		}

		@Override
		public void enqueue(List<Track> tracks, int action) throws RemoteException {
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
		
	}
    
}
