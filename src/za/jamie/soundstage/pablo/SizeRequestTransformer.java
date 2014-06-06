package za.jamie.soundstage.pablo;

import android.net.Uri;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;

/**
 * Created by jamie on 2014/01/29.
 */
public class SizeRequestTransformer implements Picasso.RequestTransformer {

    @Override
    public Request transformRequest(Request request) {
        if (request.hasSize() && request.uri.getScheme().equals("soundstage")) {
            Uri uri = request.uri.buildUpon()
                    .appendQueryParameter("width", String.valueOf(request.targetWidth))
                    .appendQueryParameter("height", String.valueOf(request.targetHeight))
                    .build();
            return request.buildUpon().setUri(uri).build();
        } else {
            return request;
        }
    }

}
