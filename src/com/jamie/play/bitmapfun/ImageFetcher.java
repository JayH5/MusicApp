package com.jamie.play.bitmapfun;

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
	
	private static final String ARTIST_SUFFIX = "artist";
	private static final Uri ALBUM_ART_BASE_URI = Uri.parse("content://media/external/audio/albumart");
	
	// Bundle keys
	public static final String KEY_ALBUM_ID = "album_id";
	public static final String KEY_ALBUM = "album";
	public static final String KEY_ARTIST = "artist";
	
	private ImageResizer mResizer;
	//private ImageDownloader mDownloader;

	public ImageFetcher(Context context) {
		super(context);
		mResizer = new ImageResizer(context);
		//mDownloader = new ImageDownloader(context, mResizer);
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
    	
    	Bitmap bitmap = null;
    	
    	// If this is an album, try get the image from the content resolver
    	if (data.containsKey(KEY_ALBUM_ID)) {
    		Uri albumArtUri = getAlbumArtUri(data.getLong(KEY_ALBUM_ID));
    		if (albumArtUri != null) {
    			bitmap = mResizer.getBitmapFromContentUri(albumArtUri);
    		}
        }
        
    	// If album art still not found or we're getting an artist image
    	// send an intent to the download service to go load the image into cache.
    	if (bitmap == null) {
    		Log.d(TAG, "Starting last fm fetch for image...");
    		
    		final Intent intent = new Intent(mContext, ImageDownloadService.class);
    		intent.putExtra(ImageDownloadService.KEY_BUNDLE, data);
    		mContext.startService(intent);
    		
    		//bitmap = mDownloader.downloadBitmap(data);
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

    /**
     * Used to fetch artist images.
     */
    public void loadArtistImage(long artistId, String artist, ImageView imageView) {
        final Bundle data = new Bundle();
        data.putString(KEY_ARTIST, artist);
    	
    	loadImage(String.valueOf(artistId) + ARTIST_SUFFIX, data, imageView);
    }
	
	

}
