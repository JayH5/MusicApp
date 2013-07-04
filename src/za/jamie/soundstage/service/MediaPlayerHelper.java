package za.jamie.soundstage.service;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.PowerManager;
import android.util.Log;

public class MediaPlayerHelper implements MediaPlayer.OnCompletionListener, 
		MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener {

	private static final String TAG = "MediaPlayerHelper";
	
	private final Context mContext;
	
	private OnPlayerEventListener mListener;
	
	public MediaPlayerHelper(Context context) {
		mContext = context;
	}
	
	public void setListeners(MediaPlayer mp) {
		mp.setOnCompletionListener(this);
		mp.setOnErrorListener(this);
		mp.setOnSeekCompleteListener(this);
	}
	
	public boolean setDataSource(GaplessPlayer2 player, Uri uri) {
        if (setDataSourceImpl(player, uri)) {
          	setNextDataSource(player, null);
          	return true;
        }
        return false;
    }
	
	public void setNextDataSource(GaplessPlayer2 currentPlayer, Uri uri) {
    	Log.d(TAG, "Setting next data source: " + uri);
        // Clear the next media player attached to the current one
    	currentPlayer.setNextMediaPlayer(null);
    	
      	// If uri is null, i.e. we're setting the current data source, exit
        if (uri == null) {
            return;
        }
            
        // Set up a new media player to be the next to play
        GaplessPlayer2 nextPlayer = new GaplessPlayer2();
        nextPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
        nextPlayer.setAudioSessionId(currentPlayer.getAudioSessionId());
            
        // If we set the data source successfully then set this to be next to play
        if (setDataSourceImpl(nextPlayer, uri)) {
            currentPlayer.setNextMediaPlayer(nextPlayer);
        } else {
           	nextPlayer.release();
        }
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
            
        openAudioEffectControlSession(player);
            
        return true;
    }

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		switch (what) {
    	case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
    		mp.release();
    		
    		GaplessPlayer2 newPlayer = new GaplessPlayer2();
    		newPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
    		
    		if (mListener != null) {
    			mListener.onError(newPlayer);
    		}
    		return true;
    	default:
    		break;
    	}
    	return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		GaplessPlayer2 currentPlayer = (GaplessPlayer2) mp;
		GaplessPlayer2 nextPlayer = currentPlayer.getNextMediaPlayer();
		if (nextPlayer != null) {
    		currentPlayer.release();
    		
    		if (mListener != null) {
    			mListener.onTrackWentToNext(nextPlayer);
    		}
    	} else {
    		if (mListener != null) {
    			mListener.onTrackEnded();
    		}
    	}		
	}
	
	public void openAudioEffectControlSession(MediaPlayer mp) {
		final Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
		intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, mp.getAudioSessionId());
		intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, mContext.getPackageName());
		intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
		mContext.sendBroadcast(intent);
	}
	
	public void closeAudioEffectControlSession(MediaPlayer mp) {
		final Intent intent = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
		intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, mp.getAudioSessionId());
		intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, mContext.getPackageName());
		mContext.sendBroadcast(intent);
	}
	
	public void setOnPlayerEventListener(OnPlayerEventListener listener) {
		mListener = listener;
	}
	
	public interface OnPlayerEventListener {
		void onTrackWentToNext(GaplessPlayer2 newPlayer);
		void onTrackEnded();
		void onError(GaplessPlayer2 newPlayer);
	}

}
