package com.jamie.play.fragments.library;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;

import com.jamie.play.BuildConfig;
import com.jamie.play.R;
import com.jamie.play.activities.AlbumBrowserActivity;
import com.jamie.play.adapters.AlbumsAdapter;
import com.jamie.play.bitmapfun.ImageFetcher;
import com.jamie.play.cursormanager.CursorDefinitions;
import com.jamie.play.cursormanager.CursorManager;
import com.jamie.play.utils.ImageUtils;

public class AlbumsFragment extends Fragment implements AdapterView.OnItemClickListener {
	private static final String TAG = "AlbumGridFragment";

    private int mImageThumbSize;
    private int mImageThumbSpacing;
    
    private AlbumsAdapter mAdapter;
    private ImageFetcher mImageWorker;
    
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
        
        // Get the image fetcher
        mImageWorker = ImageUtils.getImageFetcher(getActivity());
        
        // Set up the adapter to create the views
        mAdapter = new AlbumsAdapter(getActivity(), 
        		R.layout.grid_item_two_line, null, 0, mImageWorker);        
        
        // Load up the cursor
        final CursorManager cm = new CursorManager(getActivity(), mAdapter, 
        		CursorDefinitions.getAlbumsCursorParams());
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
        final GridView mGridView = (GridView) v.findViewById(R.id.gridView);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);

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
}
