package za.jamie.soundstage.animation;

import java.lang.ref.WeakReference;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

public class ViewFlipper implements OnScrollListener, OnItemLongClickListener {
	
	//private static final String TAG = "ViewFlipper";

	private static final long DURATION_MILLIS = 250;

	private WeakReference<View> mLastFlip = null;
	
	private final Interpolator mAccelerator = new AccelerateInterpolator();
	private final Interpolator mDecelerator = new DecelerateInterpolator();
	
	private int mFrontRes;
	private int mBackRes;
	
	public ViewFlipper(int frontRes, int backRes) {
		mFrontRes = frontRes;
		mBackRes = backRes;
	}
	
	public void unflip() {
		if (mLastFlip == null) {
			return;
		}
		View lastFlip = mLastFlip.get();
		if (lastFlip != null) {
			flip(lastFlip.findViewById(mBackRes), lastFlip.findViewById(mFrontRes));
			mLastFlip.clear();
		}
		mLastFlip = null;
	}
	
	public void flip(View root) {
		// First unflip the previously flipped view
		if (mLastFlip != null) {
			final View lastFlip = mLastFlip.get();
			unflip();
			// If we unflipped the view we were going to flip then we're done
			if (lastFlip == root) {
				return;
			}
		}
		
		// Actually flip the view and keep a reference to it
		flip(root.findViewById(mFrontRes), root.findViewById(mBackRes));
		mLastFlip = new WeakReference<View>(root);
	}
	
	/**
	 * Performs a flip animation from one view to the other
	 * @param from
	 * @param to
	 */
	private void flip(final View from, final View to) {
		ObjectAnimator visToInvis = ObjectAnimator.ofFloat(from, "rotationX", 0f, 90f);
		visToInvis.setDuration(DURATION_MILLIS);
		visToInvis.setInterpolator(mAccelerator);
		
		final ObjectAnimator invisToVis = 
				ObjectAnimator.ofFloat(to, "rotationX", 90f, 0f);
		invisToVis.setDuration(DURATION_MILLIS);
		invisToVis.setInterpolator(mDecelerator);
		
		visToInvis.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator anim) {
				from.setVisibility(View.GONE);
				invisToVis.start();
				to.setVisibility(View.VISIBLE);
			}
		});
		visToInvis.start();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view,
			int position, long id) {
		flip(view);
		return true;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) { }

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
			unflip();
		}
	}
	
	public int getFrontViewId() {
		return mFrontRes;
	}
	
	public int getBackViewId() {
		return mBackRes;
	}

}
