package za.jamie.soundstage.pablo;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;

import za.jamie.soundstage.R;

/**
 * Created by jamie on 2013/12/08.
 */
public class AlbumGridGradient implements Transformation {

    private final Paint mPaint;
    private final Rect mDrawArea;

    public AlbumGridGradient(Resources res, int width, int height) {
        final int y0 = height - res.getDimensionPixelSize(R.dimen.grid_info_holder_height);
        final int color1 = res.getColor(R.color.blackish_trans);
        final LinearGradient gradient = new LinearGradient(0, y0, 0, height, Color.TRANSPARENT,
                color1, Shader.TileMode.REPEAT);

        mDrawArea = new Rect(0, y0, width, height);

        mPaint = new Paint();
        mPaint.setShader(gradient);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public Bitmap transform(Bitmap bitmap) {
        final Canvas canvas = new Canvas(bitmap);
        canvas.drawRect(mDrawArea, mPaint);
        return bitmap;
    }

    @Override
    public String key() {
        return "Gradientv1.0";
    }
}
