package za.jamie.soundstage.fragments.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.AlbumsAdapter;
import za.jamie.soundstage.adapters.utils.OneTimeDataSetObserver;
import za.jamie.soundstage.cursormanager.CursorDefinitions;
import za.jamie.soundstage.cursormanager.CursorManager;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

public class AlbumsFragment extends Fragment implements AdapterView.OnItemClickListener {
	
	public static final String EXTRA_ITEM_ID = "extra_item_id";
	
	//private static final String TAG = "AlbumGridFragment";
    
    private AlbumsAdapter mAdapter;
    
    public static AlbumsFragment newInstance(long albumId) {
    	final Bundle args = new Bundle();
    	args.putLong(EXTRA_ITEM_ID, albumId);
    	
    	AlbumsFragment frag = new AlbumsFragment();
    	frag.setArguments(args);
    	return frag;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set up the adapter to create the views
        mAdapter = new AlbumsAdapter(getActivity(), 
        		R.layout.grid_item_two_line, R.layout.grid_item_header, null, 0);
        mAdapter.setMinSectionSize(5);
        
        // Load up the cursor
        final CursorManager cm = new CursorManager(getActivity(), mAdapter, 
        		CursorDefinitions.getAlbumsCursorParams());
        getLoaderManager().initLoader(0, null, cm);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
    		Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_album_grid, container, false);
        final GridView gridView = (GridView) v.findViewById(R.id.grid);
        
        gridView.setAdapter(mAdapter);        
        gridView.setOnItemClickListener(this);
        
        final long itemId = getArguments().getLong(EXTRA_ITEM_ID, -1);
        if (itemId > 0) {
        	new OneTimeDataSetObserver(mAdapter) {
				@Override
				public void onFirstChange() {
					gridView.setSelection(mAdapter.getPosition(itemId));
				}
        	};
        }
        
        return v;
    }
    
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		final Uri data = ContentUris.withAppendedId(
				MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id);
		final Intent intent = new Intent(Intent.ACTION_VIEW)
			.setDataAndType(data, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE);
		
		startActivity(intent);
	}

}
