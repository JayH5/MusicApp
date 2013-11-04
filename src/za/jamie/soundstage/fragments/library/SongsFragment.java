package za.jamie.soundstage.fragments.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.MusicActivity;
import za.jamie.soundstage.adapters.library.SongsAdapter;
import za.jamie.soundstage.adapters.utils.FlippingViewHelper;
import za.jamie.soundstage.animation.ViewFlipper;
import za.jamie.soundstage.fragments.TrackListFragment;
import za.jamie.soundstage.musicstore.CursorManager;
import za.jamie.soundstage.musicstore.MusicStore;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class SongsFragment extends TrackListFragment {
    
    private SongsAdapter mAdapter;
    private FlippingViewHelper mFlipHelper;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAdapter = new SongsAdapter(getActivity(),
        		R.layout.list_item_two_line, R.layout.list_item_header, null, 0);
        setListAdapter(mAdapter);
        
        ViewFlipper flipper = new ViewFlipper(R.id.list_item, R.id.flipped_view);
        mFlipHelper = new FlippingViewHelper((MusicActivity) getActivity(), flipper);
        mAdapter.setFlippingViewHelper(mFlipHelper);
        
        
        CursorManager cm = new CursorManager(getActivity(), mAdapter, 
        		MusicStore.Tracks.CURSOR);
        getLoaderManager().initLoader(0, null, cm);
    }
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, mAdapter.getCursorPosition(position), id);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, 
    		Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.list_fragment_fastscroll, parent, false);
    }
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mFlipHelper.initFlipper(getListView());
	}
  
}