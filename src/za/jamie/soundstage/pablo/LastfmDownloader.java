package za.jamie.soundstage.pablo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.squareup.picasso.Downloader;

public class LastfmDownloader implements Downloader {
	
	private static final String TAG = "LastfmDownloader";
	
	private static final Uri ALBUM_ART_BASE_URI = 
			Uri.parse("content://media/external/audio/albumart");
	
	private static final int DEFAULT_READ_TIMEOUT = 20 * 1000; // 20s
	private static final int DEFAULT_CONNECT_TIMEOUT = 15 * 1000; // 15s

	private final DiskCache mCache;
	private final Context mContext;
	
	public LastfmDownloader(Context context) {
		mContext = context;
		mCache = new DiskCache(context);
	}
	
	protected HttpURLConnection openConnection(Uri uri) throws IOException {
	    HttpURLConnection connection = (HttpURLConnection) new URL(uri.toString()).openConnection();
	    connection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
	    connection.setReadTimeout(DEFAULT_READ_TIMEOUT);
	    return connection;
	}

	@Override
	public Response load(Uri uri, boolean localCacheOnly) throws IOException {
		InputStream in = null;
		final String id = uri.getQueryParameter("_id");
		if (id != null) {
			Uri albumArtUri = ContentUris.withAppendedId(ALBUM_ART_BASE_URI, Long.parseLong(id));
			
			try {
				in = mContext.getContentResolver().openInputStream(albumArtUri);
			} catch (FileNotFoundException ignored) { }
			
			if (in != null) {
				return new Response(in, true);
			}
		}
		
		final String key = uri.toString();
		in = mCache.get(key);
		if (in != null) {
			return new Response(in, true);
		}
		
		if (!localCacheOnly) {
			downloadImageToCache(key, getLastfmUri(uri));
			in = mCache.get(key);
			if (in != null) {
				return new Response(in, false);
			}
		}		
		
		return null;
	}
	
	private void downloadImageToCache(String key, Uri uri) throws IOException {
		HttpURLConnection connection = openConnection(uri);
		int responseCode = connection.getResponseCode();
	    if (responseCode < 300) {
	    	InputStream in = connection.getInputStream();
	    	if (in != null) {
	    		Log.d(TAG, "Opened connection for image");
	    		mCache.put(key, connection.getInputStream());
	    	}
	    }
	}
	
	private Uri getLastfmUri(Uri request) throws IOException {

		Map<String, Uri> uris = null;
		HttpURLConnection connection = openConnection(request);
		int responseCode = connection.getResponseCode();
	    if (responseCode < 300) {
			InputStream in = connection.getInputStream();
			if (in != null) {
				try {			
					uris = LastfmXmlParser.parseImages(in);
				} catch (XmlPullParserException e) {
					Log.w(TAG, "Error parsing xml!", e);
				}
			}
	    }
	    
	    Uri imageUri = null;
	    if (uris != null) {
			String method = request.getQueryParameter("method");
			if ("artist.getinfo".equals(method)) {
				imageUri = uris.get("extralarge");
			} else if ("album.getinfo".equals(method)) {
				imageUri = uris.get("mega");
			}
		}
	    Log.d(TAG, "Image uri from lastfm= " + imageUri);
		return imageUri;
	}

}
