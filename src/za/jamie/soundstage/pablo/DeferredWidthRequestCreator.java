package za.jamie.soundstage.pablo;

import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.squareup.picasso.RequestCreator;

import java.lang.ref.WeakReference;

/**
 * Created by jamie on 2014/01/21.
 */
public class DeferredWidthRequestCreator implements ViewTreeObserver.OnPreDrawListener {

    private final RequestCreator mCreator;
    private final int mHeight;
    private final WeakReference<ImageView> mTarget;

    public DeferredWidthRequestCreator(RequestCreator creator, ImageView target, int height) {
        mCreator = creator;
        mTarget = new WeakReference<ImageView>(target);
        mHeight = height;
        target.getViewTreeObserver().addOnPreDrawListener(this);
    }

    @Override
    public boolean onPreDraw() {
        ImageView target = mTarget.get();
        if (target == null) {
            return true;
        }
        ViewTreeObserver vto = target.getViewTreeObserver();
        if (!vto.isAlive()) {
            return true;
        }

        int width = target.getMeasuredWidth();
        if (width <= 0) {
            return true;
        }

        vto.removeOnPreDrawListener(this);

        mCreator.resize(width, mHeight).into(target);
        return true;
    }
}
