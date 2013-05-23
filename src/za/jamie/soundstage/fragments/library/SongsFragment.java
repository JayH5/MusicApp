package za.jamie.soundstage.fragments.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.SongsAdapter;
import za.jamie.soundstage.adapters.abs.TrackAdapter;
import za.jamie.soundstage.cursormanager.CursorDefinitions;
import za.jamie.soundstage.cursormanager.CursorManager;
import za.jamie.soundstage.fragments.FastscrollTrackListFragment;
import android.os.Bundle;

public class SongsFragment extends FastscrollTrackListFragment {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        TrackAdapter adapter = new SongsAdapter(getActivity(),
        		R.layout.list_item_two_line, null, 0);
        setListAdapter(adapter);
        
        CursorManager cm = new CursorManager(getActivity(), adapter, 
        		CursorDefinitions.getSongsCursorParams());
        getLoaderManager().initLoader(0, null, cm);
    }
  
}