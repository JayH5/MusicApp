package za.jamie.soundstage.pablo;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import za.jamie.soundstage.utils.UriUtils;

public final class LastfmUtils {

    private static final Uri BASE_URI = Uri.parse("http://ws.audioscrobbler.com/2.0/");
	private static final String LAST_FM_API_KEY = "5221fc4823ffde4ec61f8aef509d247c";
	private static final String AUTOCORRECT = "1";
    private static final String FORMAT = "json";
    
    private static final List<ImageSize> SIZES;
    static {
        List<ImageSize> sizes = new ArrayList<ImageSize>(5);
        sizes.add(ImageSize.MEGA);
        sizes.add(ImageSize.EXTRALARGE);
        sizes.add(ImageSize.LARGE);
        sizes.add(ImageSize.MEDIUM);
        sizes.add(ImageSize.SMALL);
        SIZES = Collections.unmodifiableList(sizes);
    }
    private static final ImageSize DEFAULT_SIZE = ImageSize.EXTRALARGE;

    public static Uri queryFromSoundstageUri(Uri soundstageUri) {
        Uri.Builder builder = BASE_URI.buildUpon();

        String type = UriUtils.getFirstPathSegment(soundstageUri);
        if (!type.equals("artist") && !type.equals("album")) {
            throw new IllegalArgumentException("Only albums and artists allowed.");
        }

        builder.appendQueryParameter("method", type + ".getinfo");
        if (type.equals("album")) {
            builder.appendQueryParameter("album", soundstageUri.getQueryParameter("album"));
        }
        builder.appendQueryParameter("artist", soundstageUri.getQueryParameter("artist"))
                .appendQueryParameter("autocorrect", AUTOCORRECT)
                .appendQueryParameter("format", FORMAT)
                .appendQueryParameter("api_key", LAST_FM_API_KEY);

        return builder.build();
    }
    
    public static Uri pickBestImageUri(Map<String, Uri> uris, int width, int height) {
        // First try to get the best size
        String bestImageSize = (width > 0 && height > 0) ?
                bestImageSize(width, height) : DEFAULT_SIZE.size;
        Uri bestImage = uris.get(bestImageSize);
        
        if (bestImage == null) {
            // Damn. Let's try something bigger
            int sizeRank = SIZES.indexOf(bestImageSize);
            for (int i = sizeRank - 1; i <= 0; i--) {
                bestImage = uris.get(SIZES.get(i));
                if (bestImage != null) {
                    break;
                }
            }

            if (bestImage == null) {
                // Try find something smaller if still no image
                int sizeCount = SIZES.size();
                for (int i = sizeRank + 1; i < sizeCount; i++) {
                    bestImage = uris.get(SIZES.get(i));
                    if (bestImage != null) {
                        break;
                    }
                }
            }
        }
        
        return bestImage;
    }
    
    private static String bestImageSize(int width, int height) {
        if (width <= ImageSize.SMALL.width && height <= ImageSize.SMALL.height) {
            return ImageSize.SMALL.size;
        } else if (width <= ImageSize.MEDIUM.width && height <= ImageSize.MEDIUM.height) {
            return ImageSize.MEDIUM.size;
        } else if (width <= ImageSize.LARGE.width && height <= ImageSize.LARGE.height) {
            return ImageSize.LARGE.size;
        } else if (width <= ImageSize.EXTRALARGE.width && height <= ImageSize.EXTRALARGE.height) {
            return ImageSize.EXTRALARGE.size;
        } else {
            return ImageSize.MEGA.size;
        }
    }

    enum ImageSize {
        SMALL(34, 34, "small"),
        MEDIUM(64, 64, "medium"),
        LARGE(174, 174, "large"),
        EXTRALARGE(300, 300, "extralarge"),
        MEGA(-1, -1, "mega");

        final int width;
        final int height;
        final String size;

        ImageSize(int width, int height, String size) {
            this.width = width;
            this.height = height;
            this.size = size;
        }

    }

}
