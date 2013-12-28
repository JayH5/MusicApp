package za.jamie.soundstage.pablo;

import android.net.Uri;
import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jamie on 2013/12/28.
 */
public final class LastfmJsonParser {
    private LastfmJsonParser() {}

    public static Map<String, Uri> parseImages(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

        try {
            return readResponse(reader);
        } finally {
            reader.close();
        }
    }

    private static Map<String, Uri> readResponse(JsonReader reader) throws IOException {
        reader.beginObject(); // Root object/braces
        while (reader.hasNext()) {
            String name = reader.nextName();
            if ("artist".equals(name) || "album".equals(name)) { // Should be first and only object
                return readArtistOrAlbum(reader);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return null;
    }

    private static Map<String, Uri> readArtistOrAlbum(JsonReader reader) throws IOException {
        reader.beginObject(); // Album/artist
        while (reader.hasNext()) {
            if ("image".equals(reader.nextName())) {
                return readImages(reader);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return null;
    }

    private static Map<String, Uri> readImages(JsonReader reader) throws IOException {
        Map<String, Uri> images = new HashMap<String, Uri>();
        reader.beginArray();
        while (reader.hasNext()) { // Iterate through image array
            readImage(reader, images);
        }
        reader.endArray();
        return images;
    }

    private static void readImage(JsonReader reader, Map<String, Uri> images) throws IOException {
        String size = null;
        String uriString = null;

        reader.beginObject(); // Image entry
        while (reader.hasNext()) {
            String name = reader.nextName();
            if ("#text".equals(name)) { // Uri
                uriString = reader.nextString();
            } else if ("size".equals(name)) { // Size
                size = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        if (size != null && uriString != null) {
            images.put(size, Uri.parse(uriString));
        }
    }

}
