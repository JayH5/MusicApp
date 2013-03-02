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
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

public class GaplessPlayer implements MediaPlayer.OnCompletionListener,
    	MediaPlayer.OnErrorListener {
    	
    private final Context mContext;

    private MediaPlayer mCurrentMediaPlayer = new MediaPlayer();
    private MediaPlayer mNextMediaPlayer;

    private PlayerEventListener mListener;

    private boolean mIsInitialized = false;

    public GaplessPlayer(Context context) {
    	mContext = context;
    	mCurrentMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
    }

    private boolean setDataSourceImpl(final MediaPlayer player, final Uri uri) {
        try {
            player.reset();
            Log.d("GaplessPlayer", "Attempting to open uri: " + uri);
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
    		
    		if (mListener != null) {
    			mListener.onTrackWentToNext();
    		}
    	} else {
    		if (mListener != null) {
    			mListener.onTrackEnded();
    		}
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
    		
    		if (mListener != null) {
    			mListener.onServerDied();
    		}
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
    
    public static class FadeHandler extends Handler {
    	private static final float VOLUME_DUCK = 0.2f;
    	private static final int FADEDOWN = 1;
    	private static final int FADEUP = 2;
    	
    	private final WeakReference<GaplessPlayer> mPlayer;
    	private float mCurrentVolume = 1.0f;
    	
    	public FadeHandler(GaplessPlayer player) {
    		mPlayer = new WeakReference<GaplessPlayer>(player);
    	}
    	
    	@Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
            // Fading for focus ducking
            case FADEDOWN:
                mCurrentVolume -= .05f;
                if (mCurrentVolume > VOLUME_DUCK) {
                    sendEmptyMessageDelayed(FADEDOWN, 10);
                } else {
                    mCurrentVolume = VOLUME_DUCK;
                }
                mPlayer.get().setVolume(mCurrentVolume);
                break;
            case FADEUP:
                mCurrentVolume += .01f;
                if (mCurrentVolume < 1.0f) {
                    sendEmptyMessageDelayed(FADEUP, 10);
                } else {
                    mCurrentVolume = 1.0f;
                }
                mPlayer.get().setVolume(mCurrentVolume);
                break;
            default:
            	break;
            }
    	}
    	
    	public void fadeUp() {
            removeMessages(FADEDOWN);
            sendEmptyMessage(FADEUP);
        }
        
        public void fadeDown() {
            removeMessages(FADEUP);
            sendEmptyMessage(FADEDOWN);
        }
        
        public void stopFadeUp() {
        	removeMessages(FADEUP);
        }
        
        public void mute() {
        	mCurrentVolume = 0.0f;
        	mPlayer.get().setVolume(mCurrentVolume);
        }
    }
}