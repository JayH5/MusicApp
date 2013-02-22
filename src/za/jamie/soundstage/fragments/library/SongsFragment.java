package za.jamie.soundstage.fragments.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.SongsAdapter;
import za.jamie.soundstage.adapters.abs.TrackAdapter;
import za.jamie.soundstage.cursormanager.CursorDefinitions;
import za.jamie.soundstage.cursormanager.CursorManager;
import za.jamie.soundstage.fragments.TrackListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class SongsFragment extends TrackListFragment {
	private static final String TAG = "SongListFragment";
	
	private static final String STATE_LIST_POSITION = "list position";
	private static final String STATE_LIST_OFFSET = "list offset";
	
	private TrackAdapter mAdapter;
	
	/**
     * Empty constructor as per the Fragment documentation
     */
    public SongsFragment() {}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAdapter = new SongsAdapter(getActivity(),
        		R.layout.list_item_two_line, null, 0);
        setListAdapter(mAdapter);
        
        CursorManager cm = new CursorManager(getActivity(), mAdapter, 
        		CursorDefinitions.getSongsCursorParams());
        getLoaderManager().initLoader(0, null, cm);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	super.onViewCreated(view, savedInstanceState);
    	
    	final ListView lv = getListView();
    	lv.setFastScrollEnabled(true);
    	lv.setVerticalScrollBarEnabled(false);
    	
    	if (savedInstanceState != null) {
    		Log.d(TAG, "Saved instance state not null...");
    		int position = savedInstanceState.getInt(STATE_LIST_POSITION, 0);
        	int offset = savedInstanceState.getInt(STATE_LIST_OFFSET, 0);
        	
        	lv.setSelectionFromTop(position, offset);
    	} else {
    		Log.d(TAG, "Saved instance state IS null...");
    	}
    }
    
    /*@Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	
    	ListView lv = getListView();
    	int position = lv.getFirstVisiblePosition();
    	View v = lv.getChildAt(0);
    	int offset = v != null ? v.getTop() : 0;
    	
    	outState.putInt(STATE_LIST_POSITION, position);
    	outState.putInt(STATE_LIST_OFFSET, offset);
    }*/
}