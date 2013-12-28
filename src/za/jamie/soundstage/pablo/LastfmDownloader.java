package za.jamie.soundstage.pablo;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;

import com.squareup.picasso.OkHttpDownloader;

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

        // TODO: Intelligently pick size to download
        String method = uri.getQueryParameter("method");
        String size = "album.getinfo".equals(method) ? "mega" : "extralarge";
        Uri lastfmImage = getLastfmUri(lastfmQuery, size);

        // Then actually download the image.
        if (lastfmImage != null) {
		    return super.load(lastfmImage, localCacheOnly);
        }
        return null;
	}
	
	private static Uri getLastfmUri(Response lastfmQuery, String size) throws IOException {
		Uri uri = null;
        InputStream in = lastfmQuery.getInputStream();
        if (in != null) {
            Map<String, Uri> uris = LastfmJsonParser.parseImages(in);
            if (uris != null) {
                uri = uris.get(size);
            }
        }
		return uri;
	}

}
