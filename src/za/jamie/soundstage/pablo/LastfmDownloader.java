package za.jamie.soundstage.pablo;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.squareup.picasso.OkHttpDownloader;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class LastfmDownloader extends OkHttpDownloader {
	
	private static final String TAG = "LastfmDownloader";
	
	private static final Uri ALBUM_ART_BASE_URI = 
			Uri.parse("content://media/external/audio/albumart");

    private final ContentResolver mContentResolver;

	public LastfmDownloader(Context context) {
        super(context);
		mContentResolver = context.getContentResolver();
	}

	@Override
	public Response load(Uri uri, boolean localCacheOnly) throws IOException {
		// First check local album art
        final String id = uri.getQueryParameter("_id");
		if (id != null) {
			Uri albumArtUri = ContentUris.withAppendedId(ALBUM_ART_BASE_URI, Long.parseLong(id));

            InputStream in = null;
			try {
				in = mContentResolver.openInputStream(albumArtUri);
			} catch (FileNotFoundException ignored) { }
			
			if (in != null) {
				return new Response(in, true);
			}
		}

        // Now check lastfm. First query the service.
		Response lastfmQuery = super.load(uri, localCacheOnly);
        Uri lastfmImage = getLastfmUri(lastfmQuery, uri);

        // Then actually download the image.
        if (lastfmImage != null) {
		    return super.load(lastfmImage, localCacheOnly);
        }
        return null;
	}
	
	private Uri getLastfmUri(Response lastfmQuery, Uri uri) throws IOException {
		Map<String, Uri> uris = null;
        InputStream in = lastfmQuery.getInputStream();
        if (in != null) {
            try {
                uris = LastfmJsonParser.parseImages(in);
            } catch (JSONException e) {
                Log.w(TAG, "Error parsing JSON.", e);
            }
        }
	    
	    Uri imageUri = null;
	    if (uris != null) {
			String method = uri.getQueryParameter("method");
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
