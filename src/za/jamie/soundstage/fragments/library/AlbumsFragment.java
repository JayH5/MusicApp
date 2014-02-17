package za.jamie.soundstage.fragments.library;

import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.GridView;

import java.lang.ref.WeakReference;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.MusicActivity;
import za.jamie.soundstage.adapters.library.AlbumsAdapter;
import za.jamie.soundstage.adapters.utils.FlippingViewHelper;
import za.jamie.soundstage.animation.ViewFlipper;
import za.jamie.soundstage.fragments.GridFragment;
import za.jamie.soundstage.providers.MusicLoaders;

public class AlbumsFragment extends GridFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final String ARG_ITEM_ID = "extra_item_id";
    private static final String EXTRA_POSITION = "extra_position";
    private static final String EXTRA_NUM_COLUMNS = "extra_num_columns";
	
	private FlippingViewHelper mFlipHelper;
    
    private AlbumsAdapter mAdapter;
    
    public static AlbumsFragment newInstance(long albumId) {
    	final Bundle args = new Bundle();
    	args.putLong(ARG_ITEM_ID, albumId);
    	
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
        
        ViewFlipper flipper = new ViewFlipper(getActivity(), R.id.grid_item, R.id.flipped_view);
        mFlipHelper = new FlippingViewHelper((MusicActivity) getActivity(), flipper); 
        mAdapter.setFlippingViewHelper(mFlipHelper);
        setListAdapter(mAdapter);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
    		Bundle savedInstanceState) {
        return inflater.inflate(R.layout.grid_fragment_fastscroll, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final GridView grid = getGridView();
        mFlipHelper.initFlipper(grid);

        GridLayoutListener listener = new GridLayoutListener(grid, mAdapter, savedInstanceState);
        grid.getViewTreeObserver().addOnPreDrawListener(listener);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        GridView grid = getGridView();
        outState.putInt(EXTRA_POSITION, grid.getFirstVisiblePosition());
        outState.putInt(EXTRA_NUM_COLUMNS, grid.getNumColumns());
    }

    @Override
    public void onGridItemClick(GridView g, View v, int position, long id) {
        final Uri data = ContentUris.withAppendedId(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id);
        final Intent intent = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(data, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE);
        ActivityOptions options =
                ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
        getActivity().startActivity(intent, options.toBundle());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return MusicLoaders.albums(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        if (!mAdapter.isEmpty()) {
            long itemId = getArguments().getLong(ARG_ITEM_ID);
            if (itemId > 0) {
                int itemPosition = mAdapter.getItemPosition(itemId);
                if (itemPosition >= 0 && itemPosition < mAdapter.getCount()) {
                    setSelection(itemPosition);
                }
                getArguments().remove(ARG_ITEM_ID);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    private static class GridLayoutListener implements ViewTreeObserver.OnPreDrawListener {
        final WeakReference<GridView> mGrid;
        final AlbumsAdapter mAdapter;

        Bundle mSavedInstanceState;

        GridLayoutListener(GridView grid, AlbumsAdapter adapter, Bundle savedInstanceState) {
            mGrid = new WeakReference<GridView>(grid);
            mAdapter = adapter;
            mSavedInstanceState = savedInstanceState;
        }

        @Override
        public boolean onPreDraw() {
            GridView grid = mGrid.get();
            if (grid == null) {
                return true;
            }

            ViewTreeObserver vto = grid.getViewTreeObserver();
            if (!vto.isAlive()) {
                return true;
            }

            int numColumns = grid.getNumColumns();
            int columnWidth = grid.getColumnWidth();
            if (numColumns <= 0 || columnWidth <= 0) {
                return true;
            }
            mAdapter.setColumnCountAndWidth(numColumns, columnWidth);

            if (mSavedInstanceState != null) {
                int position = mSavedInstanceState.getInt(EXTRA_POSITION);
                int oldNumColumns = mSavedInstanceState.getInt(EXTRA_NUM_COLUMNS);

                // Adjust position for change in columns
                position += numColumns - oldNumColumns;
                grid.setSelection(position);
            }

            vto.removeOnPreDrawListener(this);
            return true;
        }
    }

}
