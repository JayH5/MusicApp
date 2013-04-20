package za.jamie.soundstage.fragments.library;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.AlbumsAdapter;
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
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;

public class AlbumsFragment extends Fragment implements AdapterView.OnItemClickListener {
	
	public static final String EXTRA_ITEM_ID = "extra_item_id";
	
	//private static final String TAG = "AlbumGridFragment";

    private int mImageThumbSize;
    private int mImageThumbSpacing;
    
    private AlbumsAdapter mAdapter;
    
    private GridView mGridView;
    
    public static AlbumsFragment newInstance(long albumId) {
    	final Bundle args = new Bundle();
    	args.putLong(EXTRA_ITEM_ID, albumId);
    	
    	AlbumsFragment frag = new AlbumsFragment();
    	frag.setArguments(args);
    	return frag;
    }
    
    /**
     * Empty constructor as per the Fragment documentation
     */
    public AlbumsFragment() {}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get the sizes for the grid items
        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
        
        // Set up the adapter to create the views
        mAdapter = new AlbumsAdapter(getActivity(), 
        		R.layout.grid_item_two_line, null, 0);        
        
        // Load up the cursor
        final CursorManager cm = new CursorManager(getActivity(), mAdapter, 
        		CursorDefinitions.getAlbumsCursorParams());
        getLoaderManager().initLoader(0, null, cm);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
    		Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_album_grid, container, false);
        
        mGridView = (GridView) v.findViewById(R.id.gridView);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        mGridView.setFastScrollEnabled(true);

        // This listener is used to get the final width of the GridView and then calculate the
        // number of columns and the width of each column. The width of each column is variable
        // as the GridView has stretchMode=columnWidth. The column width is used to set the height
        // of each view so we get nice square thumbnails.
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(mGridLayoutListener);

        return v;
    }
    
    private ViewTreeObserver.OnGlobalLayoutListener mGridLayoutListener =
    		new ViewTreeObserver.OnGlobalLayoutListener() {
				
		@Override
		public void onGlobalLayout() {
			if (mAdapter.getNumColumns() == 0) {
				final int numColumns = 
						mGridView.getWidth() / (mImageThumbSize + mImageThumbSpacing);
                        
				if (numColumns > 0) {
					final int columnWidth =
							(mGridView.getWidth() / numColumns) - mImageThumbSpacing;
                    
					mAdapter.setNumColumns(numColumns);
                    mAdapter.setItemHeight(columnWidth);
                }
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

	/*@Override
	public void onLoadFinished(Cursor data) {
		long itemId = getArguments().getLong(EXTRA_ITEM_ID, -1);
		if (itemId > 0 && data != null) {
			// Sequential search through the data for the id
			final int count = mAdapter.getCount();
			for (int i = 0; i < count; i++) {
				if (mAdapter.getItemId(i) == itemId) {
					mGridView.setSelection(i);
					break;
				}
			}
		}		
	}*/
}
