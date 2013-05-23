package za.jamie.soundstage.fragments.musicplayer;

import java.lang.ref.WeakReference;

import za.jamie.soundstage.IMusicStatusCallback;
import za.jamie.soundstage.MusicPlaybackWrapper;
import za.jamie.soundstage.R;
import za.jamie.soundstage.bitmapfun.ImageFetcher;
import za.jamie.soundstage.bitmapfun.SingleBitmapCache;
import za.jamie.soundstage.models.Track;
import za.jamie.soundstage.service.MusicService;
import za.jamie.soundstage.utils.ImageUtils;
import za.jamie.soundstage.utils.TextUtils;
import za.jamie.soundstage.widgets.RepeatingImageButton;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MusicPlayerFragment extends Fragment implements 
		SeekBar.OnSeekBarChangeListener {

	// Rate at which the repeat listeners for the seek buttons refresh in ms
	private static final int REPEAT_INTERVAL = 260;
	
	// Handler messages
	private static final int MSG_UPDATE_TIME = 1;
	
	// Handle UI updates
    private Handler mHandler;
    
    //private static final String TAG = "MusicPlayerFragment";
	
	private ImageFetcher mImageWorker;
	
	// Play control buttons
	private ImageButton mPlayPauseButton;
	private RepeatingImageButton mPreviousButton;
	private RepeatingImageButton mNextButton;
	
	// Shuffle/repeat buttons
	private ImageButton mShuffleButton;
	private ImageButton mRepeatButton;
	
	// UI elements for metadata
	private TextView mTrackText;
	private TextView mAlbumText;
	private TextView mArtistText;
	private ImageView mAlbumArt;
	
	// UI elements for seeking
	private SeekBar mProgress;
	private TextView mElapsedTime;
	private TextView mTotalTime;
	private long mDuration;
	
	// Seek bar
	private long mStartSeekPos = 0;
    private long mLastSeekEventTime;
    private long mPosOverride = -1;
    private boolean mFromTouch = false;
    
    private boolean mIsPlaying = false;
	
	private long mTimeSync;
	private long mTimeSyncStamp;
	
	private MusicPlaybackWrapper mService;
	
	public MusicPlayerFragment() {}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mImageWorker = ImageUtils.getBigImageFetcher(getActivity());
		
		mHandler = new TimeHandler(this);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		mService = (MusicPlaybackWrapper) activity;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		mService = null;
		mHandler.removeCallbacksAndMessages(null);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		updateTime();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		mHandler.removeCallbacksAndMessages(null);
	}
	
	@Override
	public void onDestroy() {
        super.onDestroy();
        
        mService.unregisterMusicStatusCallback(mCallback);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
		
		// create ContextThemeWrapper from the original Activity Context with the custom theme
		final Context themedContext = new ContextThemeWrapper(getActivity(), 
				android.R.style.Theme_Holo);
		// clone the inflater using the ContextThemeWrapper
		LayoutInflater localInflater = inflater.cloneInContext(themedContext);
		// inflate using the cloned inflater, not the passed in default	
        final View v = localInflater.inflate(R.layout.fragment_music_player, container, false);
        
        // Playback controls
        mPlayPauseButton = (ImageButton) v.findViewById(R.id.action_button_play);
        mPreviousButton = (RepeatingImageButton) v.findViewById(R.id.action_button_previous);
        mNextButton = (RepeatingImageButton) v.findViewById(R.id.action_button_next);
        
        mPlayPauseButton.setOnClickListener(mButtonListener);
        
        mPreviousButton.setOnClickListener(mButtonListener);
        mPreviousButton.setRepeatListener(mRewListener, REPEAT_INTERVAL);
        
        mNextButton.setOnClickListener(mButtonListener);
        mNextButton.setRepeatListener(mFfwdListener, REPEAT_INTERVAL);
        
        // Shuffle/repeat
        mShuffleButton = (ImageButton) v.findViewById(R.id.action_button_shuffle);
        mRepeatButton = (ImageButton) v.findViewById(R.id.action_button_repeat);
        
        mShuffleButton.setOnClickListener(mButtonListener);
        mRepeatButton.setOnClickListener(mButtonListener);
        
        mTrackText = (TextView) v.findViewById(R.id.music_player_track_name);
        mAlbumText = (TextView) v.findViewById(R.id.music_player_album_name);
        mAlbumText.setOnClickListener(mMetaListener);
        mArtistText = (TextView) v.findViewById(R.id.music_player_artist_name);
        mArtistText.setOnClickListener(mMetaListener);
        mAlbumArt = (ImageView) v.findViewById(R.id.music_player_album_art);
        ensureSquareImageView();
        
        mProgress = (SeekBar) v.findViewById(R.id.seek_bar);
        mProgress.setOnSeekBarChangeListener(this);
        
        mElapsedTime = (TextView) v.findViewById(R.id.elapsedTime);
        mTotalTime = (TextView) v.findViewById(R.id.totalTime);
        
        return v;
    }
	
	private void ensureSquareImageView() {
		int slidingMenuOffset = getResources()
				.getDimensionPixelSize(R.dimen.slidingmenu_offset);
		DisplayMetrics dm = ImageUtils.getDisplayMetrics(getActivity());
		int imageWidth = dm.widthPixels - slidingMenuOffset;
		
		mAlbumArt.getLayoutParams().height = imageWidth;
	}
	
	private RepeatingImageButton.RepeatListener mRewListener =
	        new RepeatingImageButton.RepeatListener() {
		
		@Override    
		public void onRepeat(View v, long howlong, int repeatCount) {
	        scanBackward(repeatCount, howlong);
	    }
	};
	    
	private RepeatingImageButton.RepeatListener mFfwdListener =
	        new RepeatingImageButton.RepeatListener() {
	    
		@Override
		public void onRepeat(View v, long howlong, int repeatCount) {
	        scanForward(repeatCount, howlong);
	    }
	};
	
	/**
     * Used to scan backwards in time through the current track
     * 
     * @param repeatCount The repeat count
     * @param delta The long press duration
     */
    private void scanBackward(final int repeatCount, long delta) {
        if (repeatCount == 0) {
            mStartSeekPos = calculatePosition();
            mLastSeekEventTime = 0;
        } else {
            if (delta < 5000) {
                // seek at 10x speed for the first 5 seconds
                delta = delta * 10;
            } else {
                // seek at 40x after that
                delta = 50000 + (delta - 5000) * 40;
            }
            long newpos = mStartSeekPos - delta;
            if (newpos < 0) {
                // move to previous track
            	mService.previous();
                mStartSeekPos += mDuration;
                newpos += mDuration;
            }
            if (delta - mLastSeekEventTime > 250 || repeatCount < 0) {
            	mService.seek(newpos);
            	syncTime(newpos, System.currentTimeMillis());
                mLastSeekEventTime = delta;
            }
            if (repeatCount >= 0) {
                mPosOverride = newpos;
            } else {
                mPosOverride = -1;
            }
            refreshCurrentTime();
        }
    }

    /**
     * Used to scan forwards in time through the current track
     * 
     * @param repeatCount The repeat count
     * @param delta The long press duration
     */
    private void scanForward(final int repeatCount, long delta) {
        if (repeatCount == 0) {
            mStartSeekPos = calculatePosition();
            mLastSeekEventTime = 0;
        } else {
            if (delta < 5000) {
                // seek at 10x speed for the first 5 seconds
                delta = delta * 10;
            } else {
                // seek at 40x after that
                delta = 50000 + (delta - 5000) * 40;
            }
            long newpos = mStartSeekPos + delta;
            if (newpos >= mDuration) {
                // move to next track
            	mService.next();
                mStartSeekPos -= mDuration; // is OK to go negative
                newpos -= mDuration;
            }
            if (delta - mLastSeekEventTime > 250 || repeatCount < 0) {
                mService.seek(newpos);
                syncTime(newpos, System.currentTimeMillis());
                mLastSeekEventTime = delta;
            }
            if (repeatCount >= 0) {
                mPosOverride = newpos;
            } else {
                mPosOverride = -1;
            }
            refreshCurrentTime();
        }
    }
    
    private long refreshCurrentTime() {
    	final long pos = mPosOverride < 0 ? calculatePosition() : mPosOverride;
    	if (pos >= 0 && mDuration > 0) {
            mElapsedTime.setText(TextUtils.getTrackDurationText(getResources(), pos));
            final int progress = (int)(1000 * pos / mDuration);
            mProgress.setProgress(progress);
            
        } else {
            mProgress.setProgress(0);
        }
        // calculate the number of milliseconds until the next full second,
        // so the counter can be updated at just the right time
        final long remaining = 1000 - pos % 1000;
        // approximate how often we would need to refresh the slider to
        // move it smoothly
        int width = mProgress.getWidth();
        if (width == 0) {
            width = 320;
        }
        final long smoothrefreshtime = mDuration / width;
        if (smoothrefreshtime > remaining) {
            return remaining;
        }
        if (smoothrefreshtime < 20) {
            return 20;
        }
        return smoothrefreshtime;
    }
    
    private long calculatePosition() {
   		if (mIsPlaying) {
   			return mTimeSync + (System.currentTimeMillis() - mTimeSyncStamp);
   		} else {
   			return mTimeSync;
   		}    	
    }
    
    private void syncTime(long position, long timeStamp) {
    	mTimeSync = position;
    	mTimeSyncStamp = timeStamp;
    	updateTime();
    }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (!fromUser) {
            return;
        }
        final long now = SystemClock.elapsedRealtime();
        if (now - mLastSeekEventTime > 250) {
            mLastSeekEventTime = now;
            mPosOverride = mDuration * progress / 1000;
            mService.seek(mPosOverride);
            syncTime(mPosOverride, System.currentTimeMillis());
            if (!mFromTouch) {
                // refreshCurrentTime();
                mPosOverride = -1;
            }
        }
		
	}

	@Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mLastSeekEventTime = 0;
        mFromTouch = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mPosOverride = -1;
        mFromTouch = false;
    }
    
    private void queueNextRefresh(final long delay) {
        if (mIsPlaying) {
            final Message message = mHandler.obtainMessage(MSG_UPDATE_TIME);
            mHandler.removeMessages(MSG_UPDATE_TIME);
            mHandler.sendMessageDelayed(message, delay);
        }
    }
    
    private void updateTrack(Track track) {
    	if (track != null && mService != null) {
			mTrackText.setText(track.getTitle());
			
			mAlbumText.setText(track.getAlbum());
			mAlbumText.setTag(track.getAlbumId()); // bit sneaky
			
			mArtistText.setText(track.getArtist());
			mArtistText.setTag(track.getArtistId());
			
			mImageWorker.setLoadingImage( // also bit sneaky
					((SingleBitmapCache) mImageWorker.getMemoryCache()).get());
			mImageWorker.loadAlbumImage(track, mAlbumArt);
			
			mDuration = track.getDuration();
			mTotalTime.setText(TextUtils.getTrackDurationText(getResources(), mDuration));
		}
    }
    
    private void updatePlayState(boolean isPlaying) {
    	mIsPlaying = isPlaying;
    	if (isPlaying) {
			mPlayPauseButton.setImageResource(R.drawable.btn_playback_pause);
			mElapsedTime.clearAnimation();
			queueNextRefresh(1);
		} else {
			mPlayPauseButton.setImageResource(R.drawable.btn_playback_play);
			refreshCurrentTime();
			mElapsedTime.startAnimation(AnimationUtils.loadAnimation(getActivity(), 
					R.anim.fade_in_out));
			mHandler.removeMessages(MSG_UPDATE_TIME);
		}
    }
    
    private void updateTime(long position, long timeStamp) {
    	syncTime(position, timeStamp);
    	updateTime();
    }
    
    private void updateTime() {
    	final long next = refreshCurrentTime();
    	queueNextRefresh(next);
    }
    
    private void updateShuffleState(boolean shuffleEnabled) {
    	mShuffleButton.setImageResource(shuffleEnabled ? 
    			R.drawable.btn_playback_shuffle_all : R.drawable.btn_shuffle);
    }
    
    private void updateRepeatMode(int repeatMode) {
    	switch (repeatMode) {
    	case MusicService.REPEAT_NONE:
    		mRepeatButton.setImageResource(R.drawable.btn_repeat);
    		break;
    	case MusicService.REPEAT_ALL:
    		mRepeatButton.setImageResource(R.drawable.btn_playback_repeat_all);
    		break;
    	case MusicService.REPEAT_CURRENT:
    		mRepeatButton.setImageResource(R.drawable.btn_playback_repeat_one);
    		break;
    	}
    }
    
    private View.OnClickListener mButtonListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (v == mPlayPauseButton) {
				mService.togglePlayback();
			} else if (v == mNextButton) {
				mService.next();
			} else if (v == mPreviousButton) {
				mService.previous();
			} else if (v == mShuffleButton) {
				mService.toggleShuffle();
			} else if (v == mRepeatButton) {
				mService.cycleRepeatMode();
			}
			
		}
	};
	
	private View.OnClickListener mMetaListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (v == mArtistText) {
				final Uri data = ContentUris.withAppendedId(
						MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, 
						(Long) mArtistText.getTag());
				startActivity(new Intent(Intent.ACTION_VIEW)
						.setDataAndType(data, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE));
			} else if (v == mAlbumText) {
				final Uri data = ContentUris.withAppendedId(
						MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, 
						(Long) mAlbumText.getTag());
				startActivity(new Intent(Intent.ACTION_VIEW)
						.setDataAndType(data, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE));
			}			
		}

	};
	
	public boolean isPlaying() {
		return mIsPlaying;
	}
    
    /**
     * Used to update the seek bar and elapsed time counter
     */
    private static final class TimeHandler extends Handler {

        private final WeakReference<MusicPlayerFragment> mPlayer;

        public TimeHandler(MusicPlayerFragment player) {
            mPlayer = new WeakReference<MusicPlayerFragment>(player);
        }
        
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
    			case MSG_UPDATE_TIME:
    				mPlayer.get().updateTime();             
    				break;
                default:
                	super.handleMessage(msg);
                    break;
            }
        }
    }
    
    public void onServiceConnected() {
    	mService.registerMusicStatusCallback(mCallback);
    	mService.requestMusicStatus();
    }
	
	public final IMusicStatusCallback mCallback = new IMusicStatusCallback.Stub() {
		
		@Override
		public void onTrackChanged(final Track track) throws RemoteException {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					updateTrack(track);
					
				}
				
			});
		}
		
		@Override
		public void onPositionSync(final long position, final long timeStamp)
				throws RemoteException {
			
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					updateTime(position, timeStamp);
					
				}
				
			});
		}
		
		@Override
		public void onPlayStateChanged(final boolean isPlaying) throws RemoteException {			
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					updatePlayState(isPlaying);
					
				}
				
			});
			
		}

		@Override
		public void onShuffleStateChanged(final boolean shuffleEnabled)
				throws RemoteException {
			
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					updateShuffleState(shuffleEnabled);
					
				}
				
			});
		}

		@Override
		public void onRepeatModeChanged(final int repeatMode) throws RemoteException {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					updateRepeatMode(repeatMode);
					
				}
				
			});
			
		}
	};

}
