package za.jamie.soundstage.fragments.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.SongsAdapter;
import za.jamie.soundstage.cursormanager.CursorDefinitions;
import za.jamie.soundstage.cursormanager.CursorManager;
import za.jamie.soundstage.fragments.FastscrollTrackListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class SongsFragment extends FastscrollTrackListFragment {
    
    private SongsAdapter mAdapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAdapter = new SongsAdapter(getActivity(),
        		R.layout.list_item_two_line, R.layout.list_item_header, null, 0);
        setListAdapter(mAdapter);
        
        CursorManager cm = new CursorManager(getActivity(), mAdapter, 
        		CursorDefinitions.getSongsCursorParams());
        getLoaderManager().initLoader(0, null, cm);
    }
    
    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, mAdapter.getRealPosition(position), id);
    }
  
}