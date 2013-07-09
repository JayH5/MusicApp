package za.jamie.soundstage.fragments.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.SongsAdapter;
import za.jamie.soundstage.fragments.FastscrollTrackListFragment;
import za.jamie.soundstage.musicstore.CursorManager;
import za.jamie.soundstage.musicstore.MusicStore;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.ListView;

public class SongsFragment extends FastscrollTrackListFragment {
    
    private SongsAdapter mAdapter;
    
    private Interpolator mAccelerator = new AccelerateInterpolator();
    private Interpolator mDecelerator = new DecelerateInterpolator();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAdapter = new SongsAdapter(getActivity(),
        		R.layout.list_item_two_line_flip, R.layout.list_item_header, null, 0);
        setListAdapter(mAdapter);
        
        CursorManager cm = new CursorManager(getActivity(), mAdapter, 
        		MusicStore.Tracks.CURSOR);
        getLoaderManager().initLoader(0, null, cm);
    }
    
    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, mAdapter.getRealPosition(position), id);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	super.onViewCreated(view, savedInstanceState);
    	
    	getListView().setOnItemLongClickListener(mOnLongClickListener);
    }
    
    private void flipit(View view) {
        final View listItem = view.findViewById(R.id.list_item);
    	final View flippedView = view.findViewById(R.id.flipped_view);
    	final View visibleView, invisibleView;
    	if (listItem.getVisibility() == View.GONE) {
    		visibleView = flippedView;
            invisibleView = listItem;
        } else {
        	invisibleView = flippedView;
        	visibleView = listItem;
        }
        ObjectAnimator visToInvis = ObjectAnimator.ofFloat(visibleView, "rotationX", 0f, 90f);
        visToInvis.setDuration(500);
        visToInvis.setInterpolator(mAccelerator);
        final ObjectAnimator invisToVis = ObjectAnimator.ofFloat(invisibleView, "rotationX",
                -90f, 0f);
        invisToVis.setDuration(500);
        invisToVis.setInterpolator(mDecelerator);
        visToInvis.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator anim) {
            	visibleView.setVisibility(View.GONE);
                invisToVis.start();
                invisibleView.setVisibility(View.VISIBLE);
            }
        });
        visToInvis.start();
    }
    
    private AdapterView.OnItemLongClickListener mOnLongClickListener = 
    		new AdapterView.OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> adapterView, View view,
				int position, long id) {
			
			flipit(view);
			return true;
		}
	};
  
}