package za.jamie.soundstage.fragments;

import za.jamie.soundstage.R;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
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
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class AudioPreviewFragment extends Fragment implements OnPreparedListener, 
		OnErrorListener, OnCompletionListener, LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final String TAG = "AudioPreviewFragment";
	private static final String EXTRA_URI = "extra_uri";
	
	private MediaPlayer mPlayer;
	
	private AudioManager mAudioManager;
	private Handler mProgressRefresher;
	
	private ProgressBar mProgressBar;
	private TextView mLoadingText;
	
	private ViewGroup mTitleAndButtons;	
	private TextView mTextLine1;
	private TextView mTextLine2;	
	private SeekBar mSeekBar;
	//private TextView mElapsedTime;
	//private TextView mTotalTime;
	private ImageButton mPlayPauseButton;
	
	private long mMediaId;
	private Uri mUri;
	private int mDuration;
	private boolean mSeeking;
	private boolean mPausedByTransientLossOfFocus;
	
	public static AudioPreviewFragment newInstance(Uri uri) {
		Bundle args = new Bundle();
		args.putParcelable(EXTRA_URI, uri);
		
		AudioPreviewFragment frag = new AudioPreviewFragment();
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setRetainInstance(true);
		
		mProgressRefresher = new Handler();
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		
		mPlayer = new MediaPlayer();
		mPlayer.setOnPreparedListener(this);
		mPlayer.setOnErrorListener(this);
		mPlayer.setOnCompletionListener(this);
		
		mUri = getArguments().getParcelable(EXTRA_URI);
		try {
			mPlayer.setDataSource(getActivity(), mUri);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mPlayer.prepareAsync();
		
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, 
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.fragment_audio_preview, parent, false);
		
		mTextLine1 = (TextView) v.findViewById(R.id.line1);
		mTextLine2 = (TextView) v.findViewById(R.id.line2);
		mProgressBar = (ProgressBar) v.findViewById(R.id.spinner);
		mLoadingText = (TextView) v.findViewById(R.id.loading);
		mTitleAndButtons = (ViewGroup) v.findViewById(R.id.titleandbuttons);
		
		mPlayPauseButton = (ImageButton) v.findViewById(R.id.playpause);
		mPlayPauseButton.setOnClickListener(mPlayPauseButtonListener);
		
		mSeekBar = (SeekBar) v.findViewById(R.id.progress);
		
		return v;
	}
	
	@Override
    public void onDestroy() {
        stopPlayback();
        super.onDestroy();
    }

	@Override
	public void onCompletion(MediaPlayer mp) {
		mSeekBar.setProgress(mDuration);
        updatePlayPause();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Toast.makeText(getActivity(), "Playback failed", Toast.LENGTH_SHORT).show();
        getActivity().finish();
        return true;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
        mPlayer = mp;
        setNames();
        mPlayer.start();
        showPostPrepareUI();		
	}
	
	private void showPostPrepareUI() {
        mProgressBar.setVisibility(View.GONE);
        mDuration = mPlayer.getDuration();
        if (mDuration != 0) {
            mSeekBar.setMax(mDuration);
            mSeekBar.setVisibility(View.VISIBLE);
        }
        mSeekBar.setOnSeekBarChangeListener(mSeekListener);
        mLoadingText.setVisibility(View.GONE);
        mTitleAndButtons.setVisibility(View.VISIBLE);
        mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        mProgressRefresher.postDelayed(new ProgressRefresher(), 200);
        updatePlayPause();
    }
	
	private void updatePlayPause() {
        if (mPlayer.isPlaying()) {
            mPlayPauseButton.setImageResource(R.drawable.btn_playback_pause);
        } else {
        	mPlayPauseButton.setImageResource(R.drawable.btn_playback_play);
            mProgressRefresher.removeCallbacksAndMessages(null);
        }
    }
	
	private void start() {
        mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        mPlayer.start();
        mProgressRefresher.postDelayed(new ProgressRefresher(), 200);
    }
	
	private void stopPlayback() {
        if (mProgressRefresher != null) {
            mProgressRefresher.removeCallbacksAndMessages(null);
        }
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
            mAudioManager.abandonAudioFocus(mAudioFocusListener);
        }
    }
    
    public void setNames() {
        if (TextUtils.isEmpty(mTextLine1.getText())) {
            mTextLine1.setText(mUri.getLastPathSegment());
        }
        if (TextUtils.isEmpty(mTextLine2.getText())) {
            mTextLine2.setVisibility(View.GONE);
        } else {
            mTextLine2.setVisibility(View.VISIBLE);
        }
    }

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final String scheme = mUri.getScheme();
		if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            if (mUri.getAuthority() == MediaStore.AUTHORITY) {
            	// try to get title and artist from the media content provider
            	return new CursorLoader(getActivity(), mUri, new String [] {
                        	MediaStore.Audio.Media.TITLE, 
                        	MediaStore.Audio.Media.ARTIST }, 
                        null, null, null);
            } else {
                // Try to get the display name from another content provider.
                // Don't specifically ask for the display name though, since the
                // provider might not actually support that column.
            	return new CursorLoader(getActivity(), mUri, null, null, null, null);
            }
        } else if (scheme.equals("file")) {
            // check if this file is in the media database (clicking on a download
            // in the download manager might follow this path
            String path = mUri.getPath();
            return new CursorLoader(getActivity(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
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
            int idIdx = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int displaynameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

            if (idIdx >=0) {
                mMediaId = cursor.getLong(idIdx);
            }
            
            if (titleIdx >= 0) {
                String title = cursor.getString(titleIdx);
                mTextLine1.setText(title);
                if (artistIdx >= 0) {
                    String artist = cursor.getString(artistIdx);
                    mTextLine2.setText(artist);
                }
            } else if (displaynameIdx >= 0) {
                String name = cursor.getString(displaynameIdx);
                mTextLine1.setText(name);
            } else {
                // Couldn't find anything to display, what to do now?
                Log.w(TAG, "Cursor had no names for us");
            }
        } else {
            Log.w(TAG, "empty cursor");
        }

        if (cursor != null) {
            cursor.close();
        }
        setNames();
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Nothing to do		
	}
	
	private View.OnClickListener mPlayPauseButtonListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// Protection for case of simultaneously tapping on play/pause and exit
	        if (mPlayer == null) {
	            return;
	        }
	        if (mPlayer.isPlaying()) {
	            mPlayer.pause();
	        } else {
	            start();
	        }
	        updatePlayPause();			
		}
	};
	
	private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            mSeeking = true;
        }
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                return;
            }
            // Protection for case of simultaneously tapping on seek bar and exit
            if (mPlayer == null) {
                return;
            }
            mPlayer.seekTo(progress);
        }
        public void onStopTrackingTouch(SeekBar bar) {
            mSeeking = false;
        }
    };
    
    private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
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
                    mPlayer.pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (mPlayer.isPlaying()) {
                        mPausedByTransientLossOfFocus = true;
                        mPlayer.pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (mPausedByTransientLossOfFocus) {
                        mPausedByTransientLossOfFocus = false;
                        start();
                    }
                    break;
            }
            updatePlayPause();
        }
    };
    
    class ProgressRefresher implements Runnable {

        public void run() {
            if (mPlayer != null && !mSeeking && mDuration != 0) {
                mSeekBar.setProgress(mPlayer.getCurrentPosition());
            }
            mProgressRefresher.removeCallbacksAndMessages(null);
            mProgressRefresher.postDelayed(new ProgressRefresher(), 200);
        }
    }    

}
