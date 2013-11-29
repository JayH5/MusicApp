package za.jamie.soundstage.activities;

import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.RetainFragment;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class AudioPreviewActivity extends Activity implements OnPreparedListener, 
		OnErrorListener, OnCompletionListener, LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final String TAG = "AudioPreviewActivity";
	
	private PreviewPlayer mPlayer;
	
	private AudioManager mAudioManager;

	private ObjectAnimator mSeekBarAnimator;
	
	private ProgressBar mProgressBar;
	private TextView mLoadingText;
	
	private ViewGroup mTitleAndButtons;	
	private TextView mTextLine1;
	private TextView mTextLine2;	
	private SeekBar mSeekBar;
	private ImageButton mPlayPauseButton;
	
	private Uri mUri;
	
	private boolean mPausedByTransientLossOfFocus = false;	
	private boolean mSeeking = false;
	
	private RetainFragment mRetainFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        mUri = intent.getData();
        if (mUri == null) {
            finish();
            return;
        }
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_audio_preview);
        initViews();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        
        if (mPlayer == null) {
			mRetainFragment = RetainFragment.findOrCreateRetainFragment(getFragmentManager());
	        mPlayer = (PreviewPlayer) mRetainFragment.getObject();
	        if (mPlayer == null) {
	        	mPlayer = createMediaPlayer();
	        	mRetainFragment.setObject(mPlayer);
	        } else {
	        	mPlayer.setActivity(this);
	        	if (mPlayer.isPrepared()) {
	        		showPostPrepareUI();
	        	}
	        }
		}
        
        getLoaderManager().initLoader(0, null, this);
	}
	
	private void initViews() {
		mTextLine1 = (TextView) findViewById(R.id.line1);
		mTextLine1.setText(mUri.getLastPathSegment());
		mTextLine2 = (TextView) findViewById(R.id.line2);
		mProgressBar = (ProgressBar) findViewById(R.id.spinner);
		mLoadingText = (TextView) findViewById(R.id.loading);
		mTitleAndButtons = (ViewGroup) findViewById(R.id.titleandbuttons);
		
		mPlayPauseButton = (ImageButton) findViewById(R.id.playpause);
		mPlayPauseButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (mPlayer == null) {
					return;
				}
				togglePlayback();
			}
		});
		
		mSeekBar = (SeekBar) findViewById(R.id.progress);
		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				endSeek();				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				beginSeek();
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (mPlayer == null || !fromUser) {
					return;
				}
				final int seekPosition = (int) (mPlayer.getDuration() * ((float) progress / 1000));
				seek(seekPosition);
			}
		});
		mSeekBarAnimator = ObjectAnimator.ofInt(mSeekBar, "progress", 0, 1000);
	}
	
	private void beginSeek() {
		mSeeking = true;
		mSeekBarAnimator.cancel();
	}
	
	private void seek(int position) {
		mPlayer.seekTo(position);
	}
	
	private void endSeek() {
		mSeeking = false;
		updateSeekBar(mPlayer.getCurrentPosition(), mPlayer.isPlaying());
	}
	
	private PreviewPlayer createMediaPlayer() {
		PreviewPlayer mp = new PreviewPlayer();
		mp.setActivity(this);
		try {
			mp.setDataSource(this, mUri);
		} catch (Exception e) {
			// catch generic Exception, since we may be called with a media
            // content URI, another content provider's URI, a file URI,
            // an http URI, and there are different exceptions associated
            // with failure to open each of those.
            Log.d(TAG, "Failed to open file: ", e);
            Toast.makeText(this, "Playback failed", Toast.LENGTH_SHORT).show();
            finish();
            return null;
		}
		mp.prepareAsync();
		return mp;
	}

	@Override
    public void onDestroy() {
        super.onDestroy();
        // Release player resources
        if (mPlayer != null) {
        	mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
            mAudioManager.abandonAudioFocus(mAudioFocusListener);
        }
    }
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		// Mark player as saved
		mPlayer = null;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mPlayer == null) {
			mPlayer = (PreviewPlayer) mRetainFragment.getObject();
		}
		updateUI(mPlayer.getCurrentPosition(), mPlayer.isPlaying());
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		mSeekBar.setProgress(mSeekBar.getMax());
        pause();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Toast.makeText(this, "Playback failed", Toast.LENGTH_SHORT).show();
        finish();
        return true;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
        showPostPrepareUI();
        play();
	}
	
	private void showPostPrepareUI() {
        mProgressBar.setVisibility(View.GONE);
        final int duration = mPlayer.getDuration();
        if (duration > 0) {
            mSeekBarAnimator.setDuration(duration);
            mSeekBar.setVisibility(View.VISIBLE);
        }
        
        mLoadingText.setVisibility(View.GONE);
        mTitleAndButtons.setVisibility(View.VISIBLE);
    }
	
	private void togglePlayback() {
		if (mPlayer.isPlaying()) {
			pause();
		} else {
			play();
		}
	}

	private void play() {
        if (!mPlayer.isPlaying()) {
        	mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        	mPlayer.start();
            updateUI(mPlayer.getCurrentPosition(), true);
        }
    }
	
	private void pause() {
		if (mPlayer.isPlaying()) {
			mPlayer.pause();
	        mAudioManager.abandonAudioFocus(mAudioFocusListener);
	        updateUI(mPlayer.getCurrentPosition(), false);
		}
	}
	
	private void updateUI(long position, boolean isPlaying) {
		updatePlayPauseButton(isPlaying);
        updateSeekBar(position, isPlaying);
	}
	
	private void updatePlayPauseButton(boolean isPlaying) {
		if (isPlaying) {
			mPlayPauseButton.setImageResource(R.drawable.btn_playback_pause);
		} else {
			mPlayPauseButton.setImageResource(R.drawable.btn_playback_play);
		}
	}
	
	private void updateSeekBar(long position, boolean isPlaying) {
		if (isPlaying) {
			if (!mSeeking) {
				mSeekBarAnimator.start();
			}
		} else {
			mSeekBarAnimator.cancel();
		}
		mSeekBarAnimator.setCurrentPlayTime(position);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final String scheme = mUri.getScheme();
		if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            if (mUri.getAuthority().equals(MediaStore.AUTHORITY)) {
            	// try to get title and artist from the media content provider
            	return new CursorLoader(this, mUri, new String [] {
                        	MediaStore.Audio.Media.TITLE, 
                        	MediaStore.Audio.Media.ARTIST }, 
                        null, null, null);
            } else {
                // Try to get the display name from another content provider.
                // Don't specifically ask for the display name though, since the
                // provider might not actually support that column.
            	return new CursorLoader(this, mUri, null, null, null, null);
            }
        } else if (scheme.equals("file")) {
            // check if this file is in the media database (clicking on a download
            // in the download manager might follow this path
            String path = mUri.getPath();
            return new CursorLoader(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            		new String [] {
            			MediaStore.Audio.Media._ID,
            			MediaStore.Audio.Media.TITLE, 
            			MediaStore.Audio.Media.ARTIST },
            		MediaStore.Audio.Media.DATA + "=?", new String [] { path }, null);
        }
		
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (cursor != null && cursor.moveToFirst()) {
            int titleIdx = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistIdx = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int displaynameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

            if (titleIdx >= 0) {
                String title = cursor.getString(titleIdx);
                mTextLine1.setText(title);
                if (artistIdx >= 0) {
                    String artist = cursor.getString(artistIdx);
                    mTextLine2.setText(artist);
                    mTextLine2.setVisibility(View.VISIBLE);
                }
            } else if (displaynameIdx >= 0) {
                String name = cursor.getString(displaynameIdx);
                mTextLine1.setText(name);
                mTextLine2.setVisibility(View.GONE);
            } else {
                // Couldn't find anything to display, what to do now?
                Log.w(TAG, "Cursor had no names for us");
            }
        } else {
            Log.w(TAG, "empty cursor");
        }
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Nothing to do		
	}
    
    private final OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (mPlayer == null) {
                // this activity has handed its MediaPlayer off to the next activity
                // (e.g. portrait/landscape switch) and should abandon its focus
                mAudioManager.abandonAudioFocus(this);
                return;
            }
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    mPausedByTransientLossOfFocus = false;
                    pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (mPlayer.isPlaying()) {
                        mPausedByTransientLossOfFocus = true;
                        pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (mPausedByTransientLossOfFocus) {
                        mPausedByTransientLossOfFocus = false;
                        play();
                    }
                    break;
            }
        }
    };
    
    private static class PreviewPlayer extends MediaPlayer implements OnPreparedListener {
		private boolean mIsPrepared = false;
		
		private AudioPreviewActivity mActivity;
    	
    	@Override
		public void onPrepared(MediaPlayer mp) {
			mIsPrepared = true;
			if (mActivity != null) {
				mActivity.onPrepared(mp);
			}
		}
    	
    	public void setActivity(AudioPreviewActivity activity) {
    		mActivity = activity;
    		setOnPreparedListener(this);
    		setOnErrorListener(activity);
    		setOnCompletionListener(activity);
    	}
    	
    	public boolean isPrepared() {
    		return mIsPrepared;
    	}
    	
    }

}
