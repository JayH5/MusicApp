package za.jamie.soundstage.bitmapfun.transitions;

import za.jamie.soundstage.bitmapfun.ImageWorker.ImageAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

public class CrossFade implements ImageAdapter {

	private static final long ANIMATION_DURATION = 150;
	
	private Bitmap mOldImage;
	
	@Override
	public void bindView(Context context, ImageView imageView,
			Bitmap bitmap) {
		
		//imageView.setBackground(new BitmapDrawable(context.getResources(), mOldImage));
		imageView.setImageBitmap(bitmap);
		ObjectAnimator.ofInt(imageView, "imageAlpha", 0, 255)
				.setDuration(ANIMATION_DURATION)
				.start();
	}
	
	public void setOldImage(Bitmap bitmap) {
		mOldImage = bitmap;
	}

}
