package com.jamie.play.service;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

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
import android.provider.MediaStore;
import android.util.Log;

import com.jamie.play.IMusicService;
import com.jamie.play.bitmapfun.ImageCache;
import com.jamie.play.models.Track;
import com.jamie.play.utils.AppUtils;
import com.jamie.play.utils.HexUtils;
import com.jamie.play.utils.ImageUtils;

/**
 * A backbround {@link Service} used to keep music playing between activities
 * and when the user moves Apollo into the background.
 */
public class MusicService extends Service {

	private static final String TAG = "MusicService";
	
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
    
    // Shared preference keys
    private static final String PREFERENCES = "Service";
    private static final String PREF_CARD_ID = "cardid";
    private static final String PREF_QUEUE = "queue";
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
    private PlayerHandler mPlayerHandler;
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

	private ImageCache mImageCache;
    
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
        saveState(true);

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

        mImageCache = ImageUtils.getImageCache(this);
        
        mPlayQueue = new PlayQueue(this);
        
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work will not disrupt the UI.
        final HandlerThread playerThread = new HandlerThread("MusicPlayerHandler",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        playerThread.start();

        // Initialize the handlers
        mPlayerHandler = new PlayerHandler(this, playerThread.getLooper());
        mDelayedStopHandler = new DelayedHandler(this);
        
        // Initialize the media player
        mPlayer = new GaplessPlayer(this);
        mPlayer.setHandler(mPlayerHandler);
        
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
    	// Initialize the intent filter and each action
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
        
        // Attach the broadcast listener
        registerReceiver(mIntentReceiver, filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // Let the equalizers know we're not going to be playing music
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
        mIntentReceiver.onReceive(this, intent);
        /*if (intent != null) {
            final String action = intent.getAction();
            final String command = intent.getStringExtra("command");
            if (CMD_NEXT.equals(command) || ACTION_NEXT.equals(action)) {
                gotoNext(true);
            } else if (CMD_PREVIOUS.equals(command) || ACTION_PREVIOUS.equals(action)) {
                prev();
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
            }
        }*/

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

                @Override
                public void onReceive(final Context context, final Intent intent) {
                    final String action = intent.getAction();
                    if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                        saveState(true);
                        mQueueIsSaveable = false;
                        closeExternalStorageFiles(intent.getData().getPath());
                    } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                        mMediaMountedCount++;
                        mCardId = getCardId();
                        restoreState();
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
            notifyChange(PLAYSTATE_CHANGED);
        }
    }

    /**
     * Stops playback
     * 
     * @param removeStatusIcon True to go to the idle state, false otherwise
     */
    private void stop(final boolean removeStatusIcon) {
        synchronized (mPlayer) {
        	if (mPlayer.isInitialized()) {
        		mPlayer.stop();
        	}
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
    	synchronized (mPlayQueue) {
    		if (mPlayQueue.isEmpty()) {
    			return;
    		}
    		synchronized (mPlayer) {
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
    	}
    }

    /**
     * @param force True to force the player onto the track next, false
     *            otherwise.
     * @return The next position to play.
     */
    private int getNextPosition(final boolean force) {
    	synchronized (mPlayQueue) {
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
    }

    /**
     * Sets the track track to be played
     */
    private void setNextTrack() {
    	synchronized (mPlayQueue) {
    		final int nextPlayPosition = getNextPosition(false);
    		mPlayQueue.setNextPlayPosition(nextPlayPosition);
    		if (nextPlayPosition >= 0) {
    			synchronized (mPlayer) {
    				mPlayer.setNextDataSource(mPlayQueue.getNextUri());
    			}
    		}
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
        synchronized (mPlayQueue) {
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
        }
       	if (notify) {
       		notifyChange(QUEUE_CHANGED);
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

    /**
     * Notify the change-receivers that something has changed.
     */
    private void notifyChange(final String what) {
        sendStickyBroadcast(new Intent(what));

        if (what.equals(META_CHANGED)) {
        	// Update the lockscreen controls
        	updateRCCMetaData();    	
        } else if (what.equals(PLAYSTATE_CHANGED)) {
        	// Update the lockscreen controls
        	updateRCCPlayState();
    	} else if (what.equals(QUEUE_CHANGED)) {
            saveState(true);
        } else {
            saveState(false);
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
				.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK,
						getAlbumArt(track)).apply();    		
    	}
    }
    
    /**
     * Saves the queue and state information such as queue position and seek position.
     * 
     * @param saveQueue True if the queue should be saved
     */
    private void saveState(boolean saveQueue) {
    	if (!mQueueIsSaveable) {
            return;
        }

        final SharedPreferences.Editor editor = mPreferences.edit();
        if (saveQueue) {
        	/*synchronized (mPlayQueue) {
        		if (!mPlayQueue.isEmpty()) {
        			editor.putString(PREF_QUEUE, mPlayQueue.toHexString());
        		}
        	}*/
            
            editor.putInt(PREF_CARD_ID, mCardId);
            
            if (mShuffleMode != SHUFFLE_NONE) {
                editor.putString(PREF_HISTORY, HexUtils.intListToHexString(mHistory));
            }
        }
        editor.putInt(PREF_CURRENT_POSITION, mPlayQueue.getPlayPosition());
        synchronized (mPlayer) {
        	if (mPlayer.isInitialized()) {
        		editor.putLong(PREF_SEEK_POSITION, mPlayer.position());
        	}
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
            q = mPreferences.getString(PREF_QUEUE, "");
        }
        if (q != null && !q.isEmpty()) {
        	synchronized (mPlayQueue) {
        		// Restore the queue
        		//mPlayQueue.open(this, q);
        	
        		// Restore the queue position
        		final int pos = mPreferences.getInt(PREF_CURRENT_POSITION, 0);
        		if (pos < 0 || pos >= mPlayQueue.size()) {
        			return;
        		}
        		mPlayQueue.setPlayPosition(pos);
        	}
            
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
        } else {
        	Log.w(TAG, "Couldn't restore queue from shared preferences");
        }
    }    

    /**
     * Returns the audio session ID
     * 
     * @return The current media player audio session ID
     */
    public int getAudioSessionId() {
    	synchronized (mPlayer) {
    		return mPlayer.getAudioSessionId();
    	}
    }

    /**
     * Sets the audio session ID.
     * 
     * @param sessionId: the audio session ID.
     */
    public void setAudioSessionId(final int sessionId) {
    	synchronized (mPlayer) {
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
    	boolean goToNext = false;
    	int numRemoved = 0;
    	synchronized (mPlayQueue) {
    		goToNext = (id == mPlayQueue.getCurrentId());
    		
    		numRemoved = mPlayQueue.removeTrack(id);
    	}
        	
    	// If one of the tracks removed was the one playing
    	if (goToNext) {
    		goToNextInternal();
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
    	int numRemoved = 0;
    	int playPosition = -1;
    	synchronized (mPlayQueue) {
    		playPosition = mPlayQueue.getPlayPosition();
    	
    		numRemoved = mPlayQueue.removeTracks(first, last);
    	}
    	
    	if (first <= playPosition && playPosition <= last) {
        	goToNextInternal();
        }
    	
        if (numRemoved > 0) {
            notifyChange(QUEUE_CHANGED);
        }
        return numRemoved;
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
        notifyChange(META_CHANGED);
    }

    /**
     * Returns the position in the queue
     * 
     * @return the current position in the queue
     */
    public int getQueuePosition() {
        synchronized (mPlayQueue) {
        	return mPlayQueue.getPlayPosition();
        }
    }


    /**
     * Seeks the current track to a specific time
     * 
     * @param position The time to seek to
     * @return The time to play the track at
     */
    public long seek(long position) {
    	synchronized (mPlayer) {
    		if (mPlayer.isInitialized()) {
    			if (position < 0) {
    				position = 0;
    			} else if (position > mPlayer.duration()) {
    				position = mPlayer.duration();
    			}
    			return mPlayer.seek(position);
    		}	
        }
        return -1;
    }

    /**
     * Returns the current position in time of the currenttrack
     * 
     * @return The current playback position in miliseconds
     */
    public long position() {
    	synchronized (mPlayer) {
    		if (mPlayer.isInitialized()) {
    			return mPlayer.position();
    		}
    	}
        return -1;
    }

    /**
     * Returns a shallow copy of the List backing the play queue
     * 
     * @return The queue as a List<Track>
     */
    public List<Track> getQueue() {
    	synchronized (mPlayQueue) {
    		return mPlayQueue.getQueue();
    	}
    }

    /**
     * @return True if music is playing, false otherwise
     */
    public boolean isPlaying() {
        return mIsSupposedToBePlaying;
    }

    /**
     * Opens a list for playback
     * 
     * @param list The list of tracks to open
     * @param position The position to start playback at
     */
    public void open(final List<Track> list, final int position) {
    	if (mShuffleMode == SHUFFLE_AUTO) {
            mShuffleMode = SHUFFLE_NORMAL;
        }
    	long oldId = -1;
    	synchronized (mPlayQueue) {
    		oldId = getCurrentTrackId();
    		if (mPlayQueue.openList(list)) {
    			notifyChange(QUEUE_CHANGED);
    		} 
    		if (position >= 0 || position > list.size()) {
    			mPlayQueue.setPlayPosition(position);
    		} else {
    			mPlayQueue.setPlayPosition(mShuffler.nextInt(mPlayQueue.size()));
    		}
    	}
        
        mHistory.clear();
        openCurrentAndNext();
        if (oldId != getCurrentTrackId()) {
            notifyChange(META_CHANGED);
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

        boolean playerInitialized = false;
        synchronized (mPlayer) {
        	// Check if player is ready
        	playerInitialized = mPlayer.isInitialized();
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
        		mPlayerHandler.removeMessages(FADEDOWN);
        		mPlayerHandler.sendEmptyMessage(FADEUP);
        	}
        }
        if (playerInitialized) {
            // Update the notification
            buildNotification();
            
            // Update the play state
            if (!mIsSupposedToBePlaying) {
                mIsSupposedToBePlaying = true;
                notifyChange(PLAYSTATE_CHANGED);
            }

        } else {
        	synchronized (mPlayQueue) {
        		if (mPlayQueue.isEmpty()) {
        			setShuffleMode(SHUFFLE_AUTO);
        		}
        	}
        }
    }

    /**
     * Temporarily pauses playback.
     */
    public synchronized void pause() {
        mPlayerHandler.removeMessages(FADEUP);
        if (mIsSupposedToBePlaying) {
            mPlayer.pause();
            gotoIdleState();
        }
    }

    /**
     * Changes from the current track to the next track
     */
    public synchronized void gotoNext(final boolean force) {
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
        notifyChange(META_CHANGED);
    }

    /**
     * Changes from the current track to the previous played track
     */
    public synchronized void prev() {
    	if (position() < 2000) {
            gotoPrev();
        } else {
            seek(0);
            play();
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
        notifyChange(META_CHANGED);
    }

    /**
     * Moves an item in the queue from one position to another
     * 
     * @param from The position the item is currently at
     * @param to The position the item is being moved to
     */
    public void moveQueueItem(int from, int to) {
    	synchronized (mPlayQueue) {
    		mPlayQueue.moveQueueItem(from, to);
    	}
        notifyChange(QUEUE_CHANGED);
    }

    /**
     * Sets the repeat mode
     * 
     * @param repeatmode The repeat mode to use
     */
    public void setRepeatMode(final int repeatmode) {
        mRepeatMode = repeatmode;
        setNextTrack();
        saveState(false);
        notifyChange(REPEATMODE_CHANGED);
    }

    /**
     * Sets the shuffle mode
     * 
     * @param shufflemode The shuffle mode to use
     */
    public synchronized void setShuffleMode(final int shufflemode) {
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
        saveState(false);
        notifyChange(SHUFFLEMODE_CHANGED);
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
     * Queues a new list for playback
     * 
     * @param list The list to queue
     * @param action The action to take
     */
    public void enqueue(final List<Track> list, final int action) {
    	int playPosition = -1;
    	synchronized (mPlayQueue) {
    		playPosition = getQueuePosition();
    		if (action == NEXT && playPosition + 1 < mPlayQueue.size()) {
    			mPlayQueue.addToQueue(list, playPosition + 1);
    			notifyChange(QUEUE_CHANGED);
    			if (mPlayQueue.isEmpty()) {
    				notifyChange(META_CHANGED);
    			}
    		} else {
    			mPlayQueue.addToQueue(list);
    			notifyChange(QUEUE_CHANGED);
    			if (mPlayQueue.isEmpty()) {
    				notifyChange(META_CHANGED);
    			}
    			if (action == NOW) {
    				openAndPlay(mPlayQueue.size() - list.size());
    				return;
    			}
    		}
        }
        if (playPosition < 0) {
            openAndPlay(0);
        }
    }
    
    public Track getCurrentTrack() {
    	synchronized (mPlayQueue) {
    		return mPlayQueue.getCurrentTrack();
    	}
    }
    
    public long getCurrentTrackId() {
    	synchronized (mPlayQueue) {
    		return mPlayQueue.getCurrentId();
    	}
    }
    
    private void openAndPlay(int position) {
    	mPlayQueue.setPlayPosition(position);
    	openCurrentAndNext();
        play();
        notifyChange(META_CHANGED);
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
    public Bitmap getAlbumArt(Track track) {
        // Return the cached artwork
        return mImageCache.getBitmapFromCache(String.valueOf(track.getAlbumId()));
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
                prev();
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
            mService.get().saveState(true);
            mService.get().stopSelf(mService.get().mServiceStartId);
        }
    }

    private static final class PlayerHandler extends Handler {

        private final WeakReference<MusicService> mService;

        private float mCurrentVolume = 1.0f;

        /**
         * Constructor of <code>MusicPlayerHandler</code>
         * 
         * @param service The service to use.
         * @param looper The thread to run on.
         */
        public PlayerHandler(final MusicService service, final Looper looper) {
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
                    mService.get().mPlayQueue.goToNext();
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
    
    private static class GaplessPlayer implements MediaPlayer.OnCompletionListener,
    		MediaPlayer.OnErrorListener {
    	private final WeakReference<Context> mContext;

    	private MediaPlayer mCurrentMediaPlayer = new MediaPlayer();
    	private MediaPlayer mNextMediaPlayer;

    	private Handler mHandler;

    	private boolean mIsInitialized = false;

    	public GaplessPlayer(Context context) {
    		mContext = new WeakReference<Context>(context);
    		mCurrentMediaPlayer.setWakeMode(mContext.get(), PowerManager.PARTIAL_WAKE_LOCK);
    	}

    	private boolean setDataSourceImpl(final MediaPlayer player, final Uri uri) {
            try {
                player.reset();
                Log.d("GaplessPlayer", "Attempting to open uri: " + uri);
                player.setDataSource(mContext.get(), uri);
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
            
            // Notify any equalizers/audio effects that we're going to play music.
            final Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
            intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
            intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, mContext.get().getPackageName());
            mContext.get().sendBroadcast(intent);
            
            return true;
        }
    	
    	
    	public boolean setDataSource(final Uri uri) {
            mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, uri);
            if (mIsInitialized) {
            	setNextDataSource(null);
            }
            return mIsInitialized;
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
            mNextMediaPlayer.setWakeMode(mContext.get(), PowerManager.PARTIAL_WAKE_LOCK);
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
    			mCurrentMediaPlayer.setWakeMode(mContext.get(), PowerManager.PARTIAL_WAKE_LOCK);
    			mHandler.sendEmptyMessage(SERVER_DIED);
    			return true;
    		default:
    			break;
    		}
    		return false;
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

    private static final class ServiceStub extends IMusicService.Stub {

        private final WeakReference<MusicService> mService;

        private ServiceStub(final MusicService service) {
            mService = new WeakReference<MusicService>(service);
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
        public Track getCurrentTrack() throws RemoteException {
        	return mService.get().getCurrentTrack();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override 
        public long getCurrentTrackId() throws RemoteException {
        	return mService.get().getCurrentTrackId();
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
