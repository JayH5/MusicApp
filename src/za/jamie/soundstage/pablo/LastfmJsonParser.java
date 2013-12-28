package za.jamie.soundstage.pablo;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
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

    public static Map<String, Uri> parseImages(InputStream in) throws IOException, JSONException {
        JSONObject root = new JSONObject(streamToString(in));
        JSONArray images = null;
        if (root.has("album")) {
            images = root.getJSONObject("album").getJSONArray("image");
        } else if (root.has("artist")) {
            images = root.getJSONObject("artist").getJSONArray("image");
        }

        Map<String, Uri> imagesMap = null;
        if (images != null) {
            final int len = images.length();
            imagesMap = new HashMap<String, Uri>(len);
            for (int i = 0; i < len; i++) {
                JSONObject image = images.getJSONObject(i);
                imagesMap.put(image.getString("size"), Uri.parse(image.getString("#text")));
            }
        }
        return imagesMap;
    }

    private static String streamToString(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        for (String line; (line = reader.readLine()) != null;) {
            sb.append(line + '\n');
        }
        return sb.toString();
    }
}
