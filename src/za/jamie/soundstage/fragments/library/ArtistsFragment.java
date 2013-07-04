package za.jamie.soundstage.fragments.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.ArtistsAdapter;
import za.jamie.soundstage.adapters.abs.ArtistAdapter;
import za.jamie.soundstage.adapters.utils.OneTimeDataSetObserver;
import za.jamie.soundstage.cursormanager.CursorDefinitions;
import za.jamie.soundstage.cursormanager.CursorManager;
import za.jamie.soundstage.fragments.FastscrollListFragment;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ListView;

public class ArtistsFragment extends FastscrollListFragment {
	
	public static final String EXTRA_ITEM_ID = "extra_item_id";
	
	public static ArtistsFragment newInstance(long itemId) {
		final Bundle args = new Bundle();
		args.putLong(EXTRA_ITEM_ID, itemId);
		
		ArtistsFragment frag = new ArtistsFragment();
		frag.setArguments(args);
		return frag;
	}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final ArtistAdapter adapter = new ArtistsAdapter(getActivity(), 
        		R.layout.list_item_artist, R.layout.list_item_header, null, 0);
        
        setListAdapter(adapter);
        
        final long itemId = getArguments().getLong(EXTRA_ITEM_ID, -1);
        if (itemId > 0) {
	        new OneTimeDataSetObserver(adapter) {
				@Override
				public void onFirstChange() {
					setSelection(adapter.getPosition(itemId));
				}
	        };
        }
        
        CursorManager cm = new CursorManager(getActivity(), adapter, 
        		CursorDefinitions.getArtistsCursorParams());
        getLoaderManager().initLoader(0, null, cm);
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {		
		Uri data = ContentUris.withAppendedId(
				MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, id);
    	
    	Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(data, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE);

		startActivity(intent);
    }

}
