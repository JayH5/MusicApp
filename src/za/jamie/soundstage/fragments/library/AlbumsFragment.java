package za.jamie.soundstage.fragments.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.AlbumsAdapter;
import za.jamie.soundstage.cursormanager.CursorDefinitions;
import za.jamie.soundstage.cursormanager.CursorManager;
import android.content.ContentUris;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;

public class AlbumsFragment extends Fragment implements AdapterView.OnItemClickListener {
	
	public static final String EXTRA_ITEM_ID = "extra_item_id";
	
	private static final String STATE_SCROLL_POSITION = "state_scroll_position";
	
	//private static final String TAG = "AlbumGridFragment";
    
    private AlbumsAdapter mAdapter;
    
    private GridView mGridView;
    
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
        
        mGridView = (GridView) v.findViewById(R.id.grid);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(mGridLayoutListener);
        
        if (savedInstanceState != null) {
    		mGridView.setSelection(savedInstanceState.getInt(STATE_SCROLL_POSITION, 0));
    	} else {
    		// Scroll to the album specified by the child activity
            final long itemId = getArguments().getLong(EXTRA_ITEM_ID, -1);
            if (itemId > 0) {
    	        mAdapter.registerDataSetObserver(new DataSetObserver() {
    	        	@Override
    	        	public void onChanged() {
    	        		//mGridView.setSelection(mAdapter.getPosition(itemId));
    	        	}
    	        });
            }
    	}
        
        return v;
    }
    
    @Override
    public void onDestroyView() {
    	super.onDestroyView();
    	
    	mGridView = null;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	
    	if (mGridView != null) {
    		outState.putInt(STATE_SCROLL_POSITION, mGridView.getFirstVisiblePosition());
    	}
    }
    
    private ViewTreeObserver.OnGlobalLayoutListener mGridLayoutListener =
    		new ViewTreeObserver.OnGlobalLayoutListener() {
				
		@Override
		public void onGlobalLayout() {
			if (mGridView == null) {
				return;
			}
			
			if (mAdapter.getItemHeight() == 0 && mGridView.getNumColumns() > 0) {
				mAdapter.setItemHeight(mGridView.getColumnWidth());
			}							
		}	
    };
    
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		final Uri data = ContentUris.withAppendedId(
				MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id);
		final Intent intent = new Intent(Intent.ACTION_VIEW)
			.setDataAndType(data, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE);
		
		startActivity(intent);
	}

}
