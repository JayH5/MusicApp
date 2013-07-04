package za.jamie.soundstage.bitmapfun.transitions;

import za.jamie.soundstage.bitmapfun.ImageWorker.ImageAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

public class FadeIn implements ImageAdapter {

	private static final long ANIMATION_DURATION = 150;
	
	@Override
	public void bindView(Context context, ImageView imageView, Bitmap bitmap) {
		imageView.setImageBitmap(bitmap);
		ObjectAnimator.ofInt(imageView, "imageAlpha", 0, 255)
				.setDuration(ANIMATION_DURATION)
				.start();
	}

}
