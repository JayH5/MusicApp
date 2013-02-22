package za.jamie.soundstage.bitmapfun;

import java.io.File;

import za.jamie.soundstage.models.Track;
import za.jamie.soundstage.utils.AppUtils;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;


public class ImageFetcher extends ImageWorker {
	
	private static final String TAG = "ImageFetcher";
	
	public static final String ARTIST_SUFFIX = "artist";
	private static final Uri ALBUM_ART_BASE_URI = 
			Uri.parse("content://media/external/audio/albumart");
	
	// Bundle keys
	public static final String KEY_ALBUM_ID = "album_id";
	public static final String KEY_ALBUM = "album";
	public static final String KEY_ARTIST = "artist";
	
	private ImageResizer mResizer;

	public ImageFetcher(Context context) {
		super(context);
		mResizer = new ImageResizer(context);
	}
	
	public static Uri getAlbumArtUri(long albumId) {
		Uri path = null;
		if (albumId > 0) {
			path = ContentUris.withAppendedId(ALBUM_ART_BASE_URI, albumId);
		}
		return path;
	}
	
	/**
     * {@inheritDoc}
     */
    @Override
    protected Bitmap processBitmap(Bundle data) {
        if (data == null || data.isEmpty()) {
        	return null;
        }
        
        Log.w(TAG, "Disk cache miss. Finding image from elsewhere...");
    	
    	Bitmap bitmap = null;
    	
    	// If this is an album, try get the image from the content resolver
    	if (data.containsKey(KEY_ALBUM_ID)) {
    		Uri albumArtUri = getAlbumArtUri(data.getLong(KEY_ALBUM_ID));
    		if (albumArtUri != null) {
    			bitmap = mResizer.getBitmapFromContentUri(albumArtUri);
    		}
        }
        
    	// See if a bitmap has been downloaded by the download service
    	if (bitmap == null) {
    		String key = data.getString(ImageWorker.KEY);
    		File downloadCacheFile = new File(AppUtils.getCacheDir(mContext, 
    				ImageDownloadService.CACHE_DIR), key);
    		
    		if (downloadCacheFile.exists()) {
    			bitmap = mResizer.getBitmapFromFile(downloadCacheFile);
    			downloadCacheFile.delete();
    		}
    		
    	}
    	
    	// If album art still not found or we're getting an artist image
    	// send an intent to the download service to go load the image into cache.
    	if (bitmap == null) {
    		Log.d(TAG, "Starting last fm fetch for image...");
    		
    		final Intent intent = new Intent(mContext, ImageDownloadService.class);
    		intent.putExtra(ImageDownloadService.KEY_BUNDLE, data);
    		mContext.startService(intent);
    	}
    	return bitmap;
    	
    }

    /**
     * Used to fetch album images.
     */
    public void loadAlbumImage(long albumId, String artist, String album,
            ImageView imageView) {
        
    	final Bundle data = new Bundle();
    	data.putLong(KEY_ALBUM_ID, albumId);
    	data.putString(KEY_ARTIST, artist);
    	data.putString(KEY_ALBUM, album);
    	
    	loadImage(String.valueOf(albumId), data, imageView);
    }
    
    public void loadAlbumImage(long albumId, ImageView imageView) {
    	final Bundle data = new Bundle();
    	data.putLong(KEY_ALBUM_ID, albumId);
    	
    	loadImage(String.valueOf(albumId), data, imageView);
    }
    
    public void loadArtistImage(long artistId, ImageView imageView) {
    	loadImage(artistId + ARTIST_SUFFIX, imageView);
    }
    
    /**
     * Used to fetch album images. Convenience method for MusicPlayerFragment.
     */
    public void loadAlbumImage(Track track, ImageView imageView) {
    	loadAlbumImage(track.getAlbumId(), track.getArtist(), track.getAlbum(), 
    			imageView);
    }

    /**
     * Used to fetch artist images.
     */
    public void loadArtistImage(long artistId, String artist, ImageView imageView) {
        final Bundle data = new Bundle();
        data.putString(KEY_ARTIST, artist);
    	
    	loadImage(String.valueOf(artistId) + ARTIST_SUFFIX, data, imageView);
    }
	
	

}
