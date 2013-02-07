package com.jamie.play.bitmapfun;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.jamie.play.lastfm.Album;
import com.jamie.play.lastfm.Artist;
import com.jamie.play.lastfm.Image;
import com.jamie.play.lastfm.ImageSize;
import com.jamie.play.lastfm.PaginatedResult;
import com.jamie.play.utils.AppUtils;


public class ImageDownloader {
	
	private static final String TAG = "ImageDownloader";
	private static final int IO_BUFFER_SIZE_BYTES = 1024;
	private static final String LAST_FM_API_KEY = "0bec3f7ec1f914d7c960c12a916c8fb3";
	private static final String DEFAULT_CACHE_DIR = "DownloadCache";
	
	private final ImageResizer mResizer;
	private final Context mContext;
	
	public ImageDownloader(Context context, ImageResizer resizer) {
		mResizer = resizer;
		mContext = context;
		//initHttpCache(context);
	}
	
	private void initHttpCache(Context context) {
		try {
			File httpCacheDir = AppUtils.getCacheDir(context, "http");
	        long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
	        HttpResponseCache.install(httpCacheDir, httpCacheSize);
		} catch (IOException e) {
	        Log.w(TAG, "HTTP response cache installation failed:" + e);
	    }
	}
	
	public Bitmap downloadBitmap(Bundle args) {
		String url = null;
		if (args.containsKey(ImageFetcher.KEY_ALBUM_ID)) {
			url = processAlbumArtUrl(args.getString(ImageFetcher.KEY_ARTIST), 
					args.getString(ImageFetcher.KEY_ALBUM));
		} else {
			url = processArtistImageUrl(args.getString(ImageFetcher.KEY_ARTIST));
		}
		
		Bitmap bitmap = null;
		if (url != null) {
			// Try download the image
			File imageFile = downloadFile(mContext, url, 
					AppUtils.getCacheDir(mContext, DEFAULT_CACHE_DIR));
			
			if (imageFile != null) {
				// Resize the image
				bitmap = mResizer.getBitmapFromFile(imageFile);
    			// Delete the downloaded file
				imageFile.delete();
			}
		}
		return bitmap;
	}
	
	private String processArtistImageUrl(String artistName) {
    	if (!TextUtils.isEmpty(artistName)) {
        	final PaginatedResult<Image> paginatedResult = Artist.getImages(
        			artistName, LAST_FM_API_KEY);
            if (paginatedResult != null) {                
            	// Search through images for an extra large one
            	for (Image image : paginatedResult) {
            		final String url = image.getImageURL(ImageSize.EXTRALARGE);
            		if (url != null) {
            			return url;
            		}
            	}
            }
        }
    	return null;
    }
    
    private String processAlbumArtUrl(String artistName, String albumName) {
    	String url = null;
    	if (!TextUtils.isEmpty(artistName) && !TextUtils.isEmpty(albumName)) {
            // Get a correction for the artist's name
    		final Artist correction = Artist.getCorrection(artistName, 
            		LAST_FM_API_KEY);
            
            if (correction != null) {
                final Album album = Album.getInfo(correction.getName(),albumName, 
                		LAST_FM_API_KEY);
                
                if (album != null) {
                    url = album.getImageURL(ImageSize.LARGE);
                }
            }
        }
    	return url;
    }
    
    /**
     * Download a bitmap from a URL, write it to a disk and return the File pointer. This
     * implementation uses a simple disk cache.
     *
     * @param context The context to use
     * @param urlString The URL to fetch
     * @return A File pointing to the fetched file
     */
    public synchronized static File downloadFile(Context context, String urlString, File cacheDir) {
    	if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }

        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        
        Log.d(TAG, "Attempting to download from url: " + urlString);

        try {
        	//$NON-NLS-1$
            final File tempFile = File.createTempFile("bitmap", null, cacheDir);

            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection)url.openConnection();
            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }
            final InputStream in = new BufferedInputStream(urlConnection.getInputStream(), 
            		IO_BUFFER_SIZE_BYTES);
            out = new BufferedOutputStream(new FileOutputStream(tempFile), 
            		IO_BUFFER_SIZE_BYTES);

            int oneByte;
            while ((oneByte = in.read()) != -1) {
                out.write(oneByte);
            }
            Log.d(TAG, "Image successfully downloaded from url: " + urlString);
            return tempFile;
        } catch (final IOException ioe) {
        	Log.w(TAG, "IOException during image download.", ioe);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException ignored) {
                }
            }
        }
        return null;
    }
	
}
