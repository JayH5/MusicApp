package za.jamie.soundstage.pablo;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;

import com.squareup.picasso.OkHttpDownloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import za.jamie.soundstage.utils.UriUtils;

public class LastfmDownloader extends OkHttpDownloader {

	private static final String TAG = "LastfmDownloader";

	private static final Uri ALBUM_ART_BASE_URI =
			Uri.parse("content://media/external/audio/albumart");

    private final ContentResolver mContentResolver;

	public LastfmDownloader(Context context) {
        super(context);
		mContentResolver = context.getContentResolver();
	}

    public LastfmDownloader(Context context, File cacheDir) {
        super(cacheDir);
        mContentResolver = context.getContentResolver();
    }

	@Override
	public Response load(Uri uri, boolean localCacheOnly) throws IOException {
		// First check local album art
        String type = UriUtils.getFirstPathSegment(uri);
        if ("album".equals(type)) {
            String id = uri.getLastPathSegment();
            if (id != null && id.matches("\\d+")) {
                Uri albumArtUri = ContentUris.withAppendedId(ALBUM_ART_BASE_URI, Long.parseLong(id));

                AssetFileDescriptor afd = null;
                try {
                    afd = mContentResolver.openAssetFileDescriptor(albumArtUri, "r");
                } catch (FileNotFoundException ignored) { }

                if (afd != null) {
                    InputStream in = new AssetFileDescriptor.AutoCloseInputStream(afd);
                    return new Response(in, true, afd.getLength());
                }
            }
        }

        // Now check lastfm. First query the service.
        Uri lastfmQueryUri = LastfmUtils.queryFromSoundstageUri(uri);
		Response lastfmQueryResponse = super.load(lastfmQueryUri, localCacheOnly);

        Map<String, Uri> lastfmImageUris = getLastfmImageUris(lastfmQueryResponse);

        // Then actually download the image.
        if (lastfmImageUris != null) {
            int width = UriUtils.getIntegerQueryParameter(uri, "width", -1);
            int height = UriUtils.getIntegerQueryParameter(uri, "height", -1);
            Uri imageUri = LastfmUtils.pickBestImageUri(lastfmImageUris, width, height);
		    return super.load(imageUri, localCacheOnly);
        }
        return null;
	}

	private static Map<String, Uri> getLastfmImageUris(Response lastfmQueryResponse)
            throws IOException {
		Map<String, Uri> uris = null;
        InputStream in = lastfmQueryResponse.getInputStream();
        if (in != null) {
            uris = LastfmJsonParser.parseImages(in);
        }
		return uris;
	}

}
