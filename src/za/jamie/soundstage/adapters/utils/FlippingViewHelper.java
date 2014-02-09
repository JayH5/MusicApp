package za.jamie.soundstage.adapters.utils;

import android.view.View;
import android.widget.AbsListView;
import android.widget.Toast;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.MusicActivity;
import za.jamie.soundstage.animation.ViewFlipper;
import za.jamie.soundstage.fragments.MoreDialogFragment;
import za.jamie.soundstage.models.MusicItem;
import za.jamie.soundstage.service.MusicService;

public class FlippingViewHelper implements View.OnClickListener, View.OnLongClickListener {

	private static final String TAG_MORE_DIALOG = "tag_more_dialog";
	
	private ViewFlipper mFlipper;
	private MusicActivity mActivity;
	
	public FlippingViewHelper(MusicActivity activity, ViewFlipper flipper) {
		mActivity = activity;
		mFlipper = flipper;
	}
	
	public void initFlipper(AbsListView lv) {
		lv.setOnItemLongClickListener(mFlipper);
		lv.setOnScrollListener(mFlipper);
	}
	
	public void bindFlippedViewButtons(View listItem, MusicItem item) {
		View flippedView = listItem.findViewById(mFlipper.getBackViewId());
        bindSubView(flippedView.findViewById(R.id.flipped_view_now), item);
		bindSubView(flippedView.findViewById(R.id.flipped_view_next), item);
        bindSubView(flippedView.findViewById(R.id.flipped_view_last), item);
        bindSubView(flippedView.findViewById(R.id.flipped_view_more), item);
	}

    private void bindSubView(View view, MusicItem item) {
        view.setTag(item);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
    }

	@Override
	public void onClick(View v) {
		final MusicItem item = (MusicItem) v.getTag();
		
		int action = -1;
		switch(v.getId()) {
		case R.id.flipped_view_now:
			action = MusicService.NOW;
			mActivity.showPlayer();
			break;
		case R.id.flipped_view_next:
			action = MusicService.NEXT;
			Toast.makeText(mActivity, "'" + item.getTitle() + "' will play next." , Toast.LENGTH_SHORT).show();
			break;
		case R.id.flipped_view_last:
			action = MusicService.LAST;
			Toast.makeText(mActivity, "'" + item.getTitle() + "' will play last." , Toast.LENGTH_SHORT).show();
			break;
		case R.id.flipped_view_more:
			MoreDialogFragment frag = MoreDialogFragment.newInstance(item);
			frag.show(mActivity.getFragmentManager(), TAG_MORE_DIALOG);
			break;
		}

		if (action > -1) {
            mActivity.getMusicConnection().enqueue(item, action);
        }
		
		mFlipper.unflip();
	}

	@Override
	public boolean onLongClick(View v) {
		mFlipper.unflip();
		return true;
	}
}
