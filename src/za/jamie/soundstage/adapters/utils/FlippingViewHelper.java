package za.jamie.soundstage.adapters.utils;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.MusicActivity;
import za.jamie.soundstage.animation.ViewFlipper;
import za.jamie.soundstage.fragments.MoreDialogFragment;
import za.jamie.soundstage.models.MusicItem;
import za.jamie.soundstage.service.MusicService;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Toast;

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
		final View flippedView = listItem.findViewById(mFlipper.getBackViewId());
		
		View now = flippedView.findViewById(R.id.flipped_view_now);
		now.setTag(item);
		now.setOnClickListener(this);
		now.setOnLongClickListener(this);
		View next = flippedView.findViewById(R.id.flipped_view_next);
		next.setTag(item);
		next.setOnClickListener(this);
		next.setOnLongClickListener(this);
		View last = flippedView.findViewById(R.id.flipped_view_last);
		last.setTag(item);
		last.setOnClickListener(this);
		last.setOnLongClickListener(this);
		View more = flippedView.findViewById(R.id.flipped_view_more);
		more.setTag(item);
		more.setOnClickListener(this);
		more.setOnLongClickListener(this);
	}

	@Override
	public void onClick(View v) {
		final MusicItem item = (MusicItem) v.getTag();
		
		int action = 0; 
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

		mActivity.getMusicConnection().enqueue(item, action);
		
		mFlipper.unflip();
	}

	@Override
	public boolean onLongClick(View v) {
		mFlipper.unflip();
		return true;
	}
}
