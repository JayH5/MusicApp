package za.jamie.soundstage.bitmapfun;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import za.jamie.soundstage.lastfm.Album;
import za.jamie.soundstage.lastfm.Artist;
import za.jamie.soundstage.lastfm.Image;
import za.jamie.soundstage.lastfm.ImageSize;
import za.jamie.soundstage.lastfm.PaginatedResult;
import za.jamie.soundstage.utils.AppUtils;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;


public class ImageDownloadService extends IntentService {

	private static final String TAG = "ImageDownloadService";
	public static final String CACHE_DIR = "DownloadCache";
	private static final int IO_BUFFER_SIZE_BYTES = 1024;
	
	// TODO: Get own key.
	private static final String LAST_FM_API_KEY = "5221fc4823ffde4ec61f8aef509d247c";
	
	public static final String KEY_BUNDLE = "za.jamie.soundstage.bitmapfun.ImageDownloadService";
	
	private Set<String> mDownloadedSet = new HashSet<String>();
	
	public ImageDownloadService() {
		super(TAG);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "Handling intent to download image...");
		// Get the bundle with the image info
		Bundle args = intent.getBundleExtra(KEY_BUNDLE);
		String key = args.getString(ImageWorker.KEY);
		
		if (!mDownloadedSet.contains(key)) {
			// Try get a url for an image
			String url = null;
			if (args.containsKey(ImageFetcher.KEY_ALBUM_ID)) {
				url = processAlbumArtUrl(args.getString(ImageFetcher.KEY_ARTIST), 
						args.getString(ImageFetcher.KEY_ALBUM));
			} else {
				url = processArtistImageUrl(args.getString(ImageFetcher.KEY_ARTIST));
			}
			
			if (url != null) {
				// Try download the image
				File imageFile = downloadFile(this, url, key,
						AppUtils.getCacheDir(this, CACHE_DIR));
				
				if (imageFile != null) {
	    			mDownloadedSet.add(key);
	    			Log.d(TAG, "Image successfully downloaded for key: " + key);
				}
			}
		}	
		
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
    public static File downloadFile(Context context, String urlString, String filename, 
    		File cacheDir) {
    	
    	if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }

        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        File file = null;
        
        Log.d(TAG, "Attempting to download from url: " + urlString);

        try {
        	//$NON-NLS-1$
        	file = new File(cacheDir, filename);
        	if (file.createNewFile()) {
        		final URL url = new URL(urlString);
        		urlConnection = (HttpURLConnection)url.openConnection();
        		if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
        			return null;
        		}
        		final InputStream in = new BufferedInputStream(urlConnection.getInputStream(), 
        				IO_BUFFER_SIZE_BYTES);
        		out = new BufferedOutputStream(new FileOutputStream(file), 
        				IO_BUFFER_SIZE_BYTES);

        		int oneByte;
        		while ((oneByte = in.read()) != -1) {
        			out.write(oneByte);
        		}
        		Log.d(TAG, "Image successfully downloaded from url: " + urlString);
        		return file;
        	}
        } catch (final IOException ioe) {
        	Log.w(TAG, "IOException during image download.", ioe);
        	file.delete();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException ioe) {
                	Log.w(TAG, "Failed to close output stream.", ioe);
                }
            }
        }
        return null;
    }

}
