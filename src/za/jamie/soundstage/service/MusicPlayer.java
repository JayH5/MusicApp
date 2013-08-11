package za.jamie.soundstage.service;

import java.io.IOException;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

public class MusicPlayer implements MediaPlayer.OnCompletionListener,
    	MediaPlayer.OnErrorListener {
    	
    private static final String TAG = "GaplessPlayer";
	
	private final Context mContext;

    private MediaPlayer mCurrentMediaPlayer = new MediaPlayer();
    private MediaPlayer mNextMediaPlayer;

    private PlayerEventListener mListener;
    
    private final PlayerHandler mHandler;

    private boolean mIsInitialized = false;

    public MusicPlayer(Context context) {
    	mContext = context;
    	mCurrentMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
    	
    	final HandlerThread thread = new HandlerThread("MusicPlayerHandler",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mHandler = new PlayerHandler(thread.getLooper(), this);
    }

    private boolean setDataSourceImpl(MediaPlayer player, Uri uri) {
        try {
            player.reset();
            player.setDataSource(mContext, uri);
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
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, mContext.getPackageName());
        mContext.sendBroadcast(intent);
            
        return true;
    }
    	
    	
    public boolean setDataSource(Uri uri) {
        mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, uri);
        if (mIsInitialized) {
          	setNextDataSource(null);
        }
        return mIsInitialized;
    }
    	
    public void setNextDataSource(final Uri uri) {
    	Log.d(TAG, "Setting next data source: " + uri);
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
        mNextMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
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
        
    public void setPlayerEventListener(PlayerEventListener listener) {
    	mListener = listener;
    }

    public boolean isInitialized() {
    	return mIsInitialized;
    }

    public void start() {
    	mCurrentMediaPlayer.start();
    }

    public void stop() {
    	mCurrentMediaPlayer.reset();
    	Log.d("GaplessPlayer", "Player stopped");
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

    public long seek(long whereto) {
    	mCurrentMediaPlayer.seekTo((int)whereto);
    	return whereto;
    }
    
    public void setLooping(boolean looping) {
    	mCurrentMediaPlayer.setLooping(looping);
    }
    
    public boolean isLooping() {
    	return mCurrentMediaPlayer.isLooping();
    }

    public void setVolume(float volume) {
    	mCurrentMediaPlayer.setVolume(volume, volume);
    }

    public void setAudioSessionId(int sessionId) {
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
    		mHandler.sendEmptyMessage(PlayerHandler.TRACK_WENT_TO_NEXT);    		
    	} else {
    		mHandler.sendEmptyMessage(PlayerHandler.TRACK_ENDED);
    	}

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
    	switch (what) {
    	case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
    		mIsInitialized = false;
    		mCurrentMediaPlayer.release();
    		mCurrentMediaPlayer = new MediaPlayer();
    		mCurrentMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);    		
    		mHandler.sendEmptyMessage(PlayerHandler.SERVER_DIED);
    		return true;
    	default:
    		break;
    	}
    	return false;
    }

    public interface PlayerEventListener {
    	public void onTrackWentToNext();
    	public void onTrackEnded();
    	public void onServerDied();
    }
    
    public void fadeUp() {
        stopFadeDown();
        mHandler.sendEmptyMessage(PlayerHandler.FADEUP);
    }
    
    public void fadeDown() {
        stopFadeUp();
        mHandler.sendEmptyMessage(PlayerHandler.FADEDOWN);
    }
    
    public void stopFadeUp() {
    	mHandler.removeMessages(PlayerHandler.FADEUP);
    }
    
    public void stopFadeDown() {
    	mHandler.removeMessages(PlayerHandler.FADEDOWN);
    }
    
    public void stopFade() {
    	mHandler.removeCallbacksAndMessages(null);
    }
    
    public void mute() {
    	mHandler.mCurrentVolume = 0.0f;
    	setVolume(0.0f);
    }
    
    private void notifyTrackWentToNext() {
    	if (mListener != null) {
    		mListener.onTrackWentToNext();
    	}
    }
    
    private void notifyTrackEnded() {
    	if (mListener != null) {
    		mListener.onTrackEnded();
    	}
    }
    
    private void notifyServerDied() {
    	if (mListener != null) {
    		mListener.onServerDied();
    	}
    }
    
    private static class PlayerHandler extends Handler {
    	private static final float VOLUME_DUCK = 0.2f;
    	
    	private static final int FADEDOWN = 1;
    	private static final int FADEUP = 2;
    	private static final int TRACK_WENT_TO_NEXT = 3;
    	private static final int TRACK_ENDED = 4;
    	private static final int SERVER_DIED = 5;    	
    	
    	private float mCurrentVolume = 1.0f;
    	
    	private final WeakReference<MusicPlayer> mPlayer;
    	
    	private PlayerHandler(Looper looper, MusicPlayer player) {
    		super(looper);
    		mPlayer = new WeakReference<MusicPlayer>(player);
    	}
    	
    	@Override
        public void handleMessage(final Message msg) {
            final MusicPlayer player = mPlayer.get();
            if (player == null) {
            	return;
            }
    		
    		switch (msg.what) {
            // Fading for focus ducking
            case FADEDOWN:
                mCurrentVolume -= .05f;
                if (mCurrentVolume > VOLUME_DUCK) {
                    sendEmptyMessageDelayed(FADEDOWN, 10);
                } else {
                    mCurrentVolume = VOLUME_DUCK;
                }
                player.setVolume(mCurrentVolume);
                break;
            case FADEUP:
                mCurrentVolume += .01f;
                if (mCurrentVolume < 1.0f) {
                    sendEmptyMessageDelayed(FADEUP, 10);
                } else {
                    mCurrentVolume = 1.0f;
                }
                player.setVolume(mCurrentVolume);
                break;
            case TRACK_WENT_TO_NEXT:
            	player.notifyTrackWentToNext();
            	break;
            case TRACK_ENDED:
            	player.notifyTrackEnded();
            	break;
            case SERVER_DIED:
            	player.notifyServerDied();
            	break;            
            default:
            	break;
            }
    	}
    }
}