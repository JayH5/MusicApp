package za.jamie.soundstage.fragments.library;

import za.jamie.soundstage.BuildConfig;
import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.AlbumBrowserActivity;
import za.jamie.soundstage.adapters.AlbumsAdapter;
import za.jamie.soundstage.bitmapfun.ImageFetcher;
import za.jamie.soundstage.cursormanager.CursorDefinitions;
import za.jamie.soundstage.cursormanager.CursorManager;
import za.jamie.soundstage.cursormanager.CursorManager.CursorLoaderListener;
import za.jamie.soundstage.utils.ImageUtils;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;

public class AlbumsFragment extends Fragment implements AdapterView.OnItemClickListener,
		CursorLoaderListener {
	
	public static final String EXTRA_ITEM_ID = "extra_item_id";
	
	private static final String TAG = "AlbumGridFragment";

    private int mImageThumbSize;
    private int mImageThumbSpacing;
    
    private AlbumsAdapter mAdapter;
    private ImageFetcher mImageWorker;
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
        
        // Set up the image fetcher
        mImageWorker = ImageUtils.getImageFetcher(getActivity());
        
        // Set up the adapter to create the views
        mAdapter = new AlbumsAdapter(getActivity(), 
        		R.layout.grid_item_two_line, null, 0, mImageWorker);        
        
        // Load up the cursor
        final CursorManager cm = new CursorManager(getActivity(), mAdapter, 
        		CursorDefinitions.getAlbumsCursorParams(), this);
        getLoaderManager().initLoader(0, null, cm);
    }
    
    @Override
	public void onResume() {
		super.onResume();
		mImageWorker.setExitTasksEarly(false);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mImageWorker.setExitTasksEarly(true);
	}
    
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_album_grid, container, false);
        mGridView = (GridView) v.findViewById(R.id.gridView);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        mGridView.setFastScrollEnabled(true);

        // This listener is used to get the final width of the GridView and then calculate the
        // number of columns and the width of each column. The width of each column is variable
        // as the GridView has stretchMode=columnWidth. The column width is used to set the height
        // of each view so we get nice square thumbnails.
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (mAdapter.getNumColumns() == 0) {
                            final int numColumns = (int) Math.floor(
                                    mGridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
                            if (numColumns > 0) {
                                final int columnWidth =
                                        (mGridView.getWidth() / numColumns) - mImageThumbSpacing;
                                mAdapter.setNumColumns(numColumns);
                                mAdapter.setItemHeight(columnWidth);
                                if (BuildConfig.DEBUG) {
                                    Log.d(TAG, "onCreateView - numColumns set to " + numColumns);
                                }
                            }
                        }
                    }
                });

        return v;
    }
    
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {		
		Cursor cursor = (Cursor) mAdapter.getItem(position);

		final String album = cursor.getString(mAdapter.getAlbumColIdx());
		final String artist = cursor.getString(mAdapter.getArtistColIdx());
		
		final Intent i = new Intent(getActivity(), AlbumBrowserActivity.class);		
		i.putExtra(AlbumBrowserActivity.EXTRA_ALBUM_ID, id);
		i.putExtra(AlbumBrowserActivity.EXTRA_ALBUM, album);
		i.putExtra(AlbumBrowserActivity.EXTRA_ARTIST, artist);
		
		startActivity(i);
	}

	@Override
	public void onLoadFinished(Cursor data) {
		long itemId = getArguments().getLong(EXTRA_ITEM_ID, -1);
		if (itemId > 0 && data != null) {
			int idColIdx = data.getColumnIndexOrThrow(BaseColumns._ID);
			
			// Sequential search through the cursor till we find the item
			if (data.moveToFirst()) {
				int position = 0;
				do {
					if (data.getLong(idColIdx) == itemId) {
						position = data.getPosition();
						break;
					}
				} while (data.moveToNext());
				
				// Then set the first list position to be at that item
				mGridView.setSelection(position);
			}
		}		
	}

	@Override
	public void onLoaderReset() {
		// TODO Auto-generated method stub
		
	}
}
