package za.jamie.soundstage.animation;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

import java.lang.ref.WeakReference;

import za.jamie.soundstage.R;

public class ViewFlipper implements OnScrollListener, OnItemLongClickListener {
	
	//private static final String TAG = "ViewFlipper";

	private WeakReference<View> mLastFlip = null;
	
	private final Context mContext;
    private int mFrontRes;
	private int mBackRes;
	
	public ViewFlipper(Context context, int frontRes, int backRes) {
        mContext = context;
		mFrontRes = frontRes;
		mBackRes = backRes;
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
	
	/**
	 * Performs a flip animation from one view to the other
	 * @param from
	 * @param to
	 */
	private void flip(final View from, final View to) {
        final Animator flipIn = AnimatorInflater.loadAnimator(mContext, R.animator.flip_in);
        flipIn.setTarget(from);
		
		final Animator flipOut = AnimatorInflater.loadAnimator(mContext, R.animator.flip_out);
        flipOut.setTarget(to);

        flipIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator anim) {
                from.setVisibility(View.GONE);
                flipOut.start();
                to.setVisibility(View.VISIBLE);
            }
        });
		flipIn.start();
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
