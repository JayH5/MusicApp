package za.jamie.soundstage.utils;

import android.net.Uri;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by jamie on 2014/02/02.
 */
public final class UriUtils {

    private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?\\d+$");

    public static String getFirstPathSegment(Uri uri) {
        List<String> segments = uri.getPathSegments();
        if (segments.isEmpty()) {
            return null;
        }
        return segments.get(0);
    }

    public static int getIntegerQueryParameter(Uri uri, String key, int defaultValue) {
        String flag = uri.getQueryParameter(key);
        if (flag == null || !INTEGER_PATTERN.matcher(flag).matches()) {
            return defaultValue;
        }
        return Integer.parseInt(flag);
    }
}
