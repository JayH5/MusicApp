package za.jamie.soundstage.service;

import za.jamie.soundstage.models.Track;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.os.Looper;

import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

public class MusicRCC extends RemoteControlClient implements Target {

	public MusicRCC(PendingIntent mediaButtonIntent) {
		super(mediaButtonIntent);
	}

	public MusicRCC(PendingIntent mediaButtonIntent, Looper looper) {
		super(mediaButtonIntent, looper);
	}
	
	/**
     * Initializes the remote control client with a number of flags and a PendingIntent
     * to the mediaButtonReceiver.
     */
	public static MusicRCC createRemoteControlClient(
			Context context, ComponentName mediaButtonReceiver) {
		
		Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
    	mediaButtonIntent.setComponent(mediaButtonReceiver);
    	PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(context,
    			0, mediaButtonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		MusicRCC rcc = new MusicRCC(mediaPendingIntent);
    	final int flags = FLAG_KEY_MEDIA_PREVIOUS
        		| FLAG_KEY_MEDIA_NEXT
                | FLAG_KEY_MEDIA_PLAY
                | FLAG_KEY_MEDIA_PAUSE
                | FLAG_KEY_MEDIA_PLAY_PAUSE
                | FLAG_KEY_MEDIA_STOP;
        rcc.setTransportControlFlags(flags);
        return rcc;
	}

	@Override
	public void onBitmapFailed() {
		editMetadata(false).putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, null).apply();		
	}

	@Override
	public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
		if (bitmap != null) {
			// RemoteControlClient wants to recycle the bitmaps thrown at it, so we need
			// to make sure not to hand out our cache copy
			Bitmap.Config config = bitmap.getConfig();
			if (config == null) {
				config = Bitmap.Config.ARGB_8888;
			}
			bitmap = bitmap.copy(config, false);
		}
		editMetadata(false).putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, bitmap).apply();		
	}
	
	public void updateTrack(Track track) {
		editMetadata(true)
			.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, track.getTitle())
			.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, track.getArtist())
			.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, track.getAlbum())			
			.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, track.getDuration())
			.apply();
	}
	
	public void updatePlayState(boolean isPlaying) {
		setPlaybackState(isPlaying ? PLAYSTATE_PLAYING : PLAYSTATE_PAUSED);
	}

}
