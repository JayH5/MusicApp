package za.jamie.soundstage.fragments.musicplayer;

import za.jamie.soundstage.IMusicStatusCallback;
import za.jamie.soundstage.R;
import za.jamie.soundstage.bitmapfun.ImageFetcher;
import za.jamie.soundstage.fragments.MusicFragment;
import za.jamie.soundstage.models.Track;
import za.jamie.soundstage.service.MusicService;
import za.jamie.soundstage.utils.ImageUtils;
import za.jamie.soundstage.widgets.DurationTextView;
import za.jamie.soundstage.widgets.RepeatingImageButton;
import android.animation.ObjectAnimator;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class MusicPlayerFragment extends MusicFragment {

	// Rate at which the repeat listeners for the seek buttons refresh in ms
	private static final int REPEAT_INTERVAL = 260;
	
	// Handle elapsed time text updates
    private final Handler mHandler = new Handler();
    private boolean mSeeking = false;
	
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
	private SeekBar mSeekBar;
	private DurationTextView mElapsedTime;
	private DurationTextView mTotalTime;
	private long mDuration;
	
	private DurationTextView mBigElapsedTime;
	private DurationTextView mBigTotalTime;
	private LinearLayout mSeekInfoHolder;
	
	// Seek bar
	private long mSeekStartPosition = 0;
    private ObjectAnimator mSeekBarAnimator;
    
    private boolean mIsPlaying = false;
	
	private long mTimeSync;
	private long mTimeSyncStamp;
	
	public MusicPlayerFragment() {}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MusicService service = getMusicService();
		if (service != null) {
			service.registerMusicStatusCallback(mCallback);
		}
		
		mImageWorker = ImageUtils.getBigImageFetcher(getActivity());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateTime(calculatePosition());
	}
	
	@Override
	public void onStop() {
		super.onStop();		
		mHandler.removeCallbacksAndMessages(null);
	}
	
	@Override
	public void onDestroy() {
        super.onDestroy();
        getMusicService().unregisterMusicStatusCallback(mCallback);
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
        
        // YAAAAAAYYYY Android. I just love typing.
        // Playback controls
        mPlayPauseButton = (ImageButton) v.findViewById(R.id.action_button_play);
        mPreviousButton = (RepeatingImageButton) v.findViewById(R.id.action_button_previous);
        mNextButton = (RepeatingImageButton) v.findViewById(R.id.action_button_next);
        
        mPlayPauseButton.setOnClickListener(mButtonListener);        
        mPreviousButton.setOnClickListener(mButtonListener);
        mPreviousButton.setRepeatListener(mSeekButtonListener, REPEAT_INTERVAL);
        mNextButton.setOnClickListener(mButtonListener);
        mNextButton.setRepeatListener(mSeekButtonListener, REPEAT_INTERVAL);
        
        // Shuffle/repeat
        mShuffleButton = (ImageButton) v.findViewById(R.id.action_button_shuffle);
        mRepeatButton = (ImageButton) v.findViewById(R.id.action_button_repeat);
        
        mShuffleButton.setOnClickListener(mButtonListener);
        mRepeatButton.setOnClickListener(mButtonListener);
        
        // Metadata info
        mTrackText = (TextView) v.findViewById(R.id.music_player_track_name);
        mAlbumText = (TextView) v.findViewById(R.id.music_player_album_name);
        mAlbumText.setOnClickListener(mMetaListener);
        mArtistText = (TextView) v.findViewById(R.id.music_player_artist_name);
        mArtistText.setOnClickListener(mMetaListener);
        mAlbumArt = (ImageView) v.findViewById(R.id.music_player_album_art);
        
        mSeekBar = (SeekBar) v.findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(mSeekListener);
        mSeekBarAnimator = ObjectAnimator.ofInt(mSeekBar, "progress", 0, 1000);        
        
        mElapsedTime = (DurationTextView) v.findViewById(R.id.elapsedTime);
        mTotalTime = (DurationTextView) v.findViewById(R.id.totalTime);
        
        // UI that appears when seeking with seek bar
        mSeekInfoHolder = (LinearLayout) v.findViewById(R.id.seek_info_holder);
        mBigElapsedTime = (DurationTextView) v.findViewById(R.id.big_elapsed_time);
        mBigTotalTime = (DurationTextView) v.findViewById(R.id.big_total_time);

        return v;
    }
	
	/*
	 * Listener for long presses on ffwd/rewind buttons. Skip ahead/back
	 */
	private final RepeatingImageButton.RepeatListener mSeekButtonListener =
            new RepeatingImageButton.RepeatListener() {

        @Override
        public void onRepeat(View view, long duration, int repeatCount) {
            if (view == mPreviousButton) {
                scan(duration, repeatCount, true);
            } else if (view == mNextButton) {
                scan(duration, repeatCount, false);
            }
        }
    };

    /*
     * Listener for seek bar input. Displays extra UI when seeking
     */
    private final SeekBar.OnSeekBarChangeListener mSeekListener = 
            new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            beginSeek();
            mBigElapsedTime.setDuration(calculatePosition());
            mBigTotalTime.setDuration(mDuration);
            ObjectAnimator.ofFloat(mSeekInfoHolder, "alpha", 0.0f, 1.0f)
            	.setDuration(150)
            	.start();
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                return;
            }
            final long seekPosition = (long) (mDuration * ((float) progress / 1000));
            mBigElapsedTime.setDuration(seekPosition);
            seek(seekPosition);
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            endSeek();
            ObjectAnimator.ofFloat(mSeekInfoHolder, "alpha", 1.0f, 0.0f)
					.setDuration(150)
					.start();
        }
    };
	
	/**
     * Used to scan backwards in time through the current track
     * 
     * @param repeatCount The repeat count
     * @param delta The long press duration
     */
    private void scan(long duration, int repeatCount, boolean backward) {
        if (repeatCount == 0) {
        	mSeekStartPosition = calculatePosition();
            beginSeek();
        } else {
            if (duration < 5000) {
                // seek at 10x speed for the first 5 seconds
            	duration = duration * 10;
            } else {
                // seek at 40x after that
            	duration = 50000 + (duration - 5000) * 40;
            }
            duration = backward ? -duration : duration;
            final long seekPosition = mSeekStartPosition + duration;
            if (seekPosition < 0) {
                getMusicService().previous();
            } else if (seekPosition > mDuration) {
                getMusicService().next();
            } else {
            	seek(seekPosition);
            }

            if (repeatCount < 0) {
                endSeek();
            }
        }
    }
    
    /*
     * When a new track comes in from the server all the UI must get updated
     */
    private void updateTrack(Track track) {
    	if (track != null) {
			mTrackText.setText(track.getTitle());
			
			mAlbumText.setText(track.getAlbum());
			mAlbumText.setTag(track.getAlbumId()); // bit sneaky
			
			mArtistText.setText(track.getArtist());
			mArtistText.setTag(track.getArtistId());
			
			mImageWorker.loadAlbumImage(track, mAlbumArt);
			
			updateDuration(track.getDuration());
		}
    }
    
    private void updateDuration(long duration) {
    	mDuration = duration;
    	mTotalTime.setDuration(duration);
    	mSeekBarAnimator.setDuration(duration);
    }
    
    private void updatePlayState(boolean isPlaying) {
    	mIsPlaying = isPlaying;
    	if (isPlaying) {
			mPlayPauseButton.setImageResource(R.drawable.btn_playback_pause);
			mElapsedTime.clearAnimation();
            updateTime(calculatePosition());
		} else {
			mPlayPauseButton.setImageResource(R.drawable.btn_playback_play);
			if (isAdded()) { // Sometimes this is called before fragment is attached, causing NPE
				mElapsedTime.startAnimation(AnimationUtils.loadAnimation(getActivity(), 
						R.anim.fade_in_out));
			}
            stopTime();
		}
    }
    
    private void updateTime(long position, long timeStamp) {
    	syncTime(position, timeStamp);
    	updateTime(position);
    }
    
    private long calculatePosition() {
   		return mIsPlaying ? mTimeSync + (System.currentTimeMillis() - mTimeSyncStamp) : mTimeSync;    	
    }
    
    private void seek(long position) {
        getMusicService().seek(position);
        updateTime(position, System.currentTimeMillis());
    }

    private void syncTime(long position, long timeStamp) {
    	mTimeSync = position;
    	mTimeSyncStamp = timeStamp;
    }
    
    private void updateTime(long position) {
    	updateSeekBar(position);
    	updateElapsedTime(position);    	
    }
    
    private void stopTime() {
    	mSeekBarAnimator.cancel();
    	mHandler.removeCallbacks(mTimeRefresh);
    }
    
    private void updateSeekBar(long position) {
    	if (mIsPlaying && !mSeeking && !mSeekBarAnimator.isStarted()) {
    		mSeekBarAnimator.start();
    	}
    	mSeekBarAnimator.setCurrentPlayTime(position);
    }
    
    private void beginSeek() {
    	mSeeking = true;
    	updateTime(calculatePosition());
    	if (mIsPlaying) {
    		stopTime();
    	}
    }
    
    private void endSeek() {
    	mSeeking = false;
    	updateTime(calculatePosition());
    }
    
    private void updateElapsedTime(long position) {
    	if (position < 0) {
    		mElapsedTime.setDuration(0);
    	} else if (position > mDuration) {
    		mElapsedTime.setDuration(mDuration);
    	} else {
    		mElapsedTime.setDuration(position);
    		if (!mSeeking && mIsPlaying) {
    			mHandler.postDelayed(mTimeRefresh, 1000 - (position % 1000)); // Update again on the next second
    		}
    	}
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
    
    private final View.OnClickListener mButtonListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (v == mPlayPauseButton) {
				getMusicService().togglePlayback();
			} else if (v == mNextButton) {
				getMusicService().next();
			} else if (v == mPreviousButton) {
				getMusicService().previous();
			} else if (v == mShuffleButton) {
				getMusicService().toggleShuffle();
			} else if (v == mRepeatButton) {
				getMusicService().cycleRepeat();
			}
			
		}
	};
	
	private final View.OnClickListener mMetaListener = new View.OnClickListener() {		
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

    private final Runnable mTimeRefresh = new Runnable() {
        @Override
        public void run() {
            if (!mSeeking && mDuration != 0) {
                updateElapsedTime(calculatePosition());
            }
        }
    };
	
	private final IMusicStatusCallback mCallback = new IMusicStatusCallback.Stub() {
		
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
