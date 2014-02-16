package za.jamie.soundstage.fragments.library;

import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.GridView;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.MusicActivity;
import za.jamie.soundstage.adapters.library.AlbumsAdapter;
import za.jamie.soundstage.adapters.utils.FlippingViewHelper;
import za.jamie.soundstage.animation.ViewFlipper;
import za.jamie.soundstage.fragments.GridFragment;
import za.jamie.soundstage.providers.MusicLoaders;

public class AlbumsFragment extends GridFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	public static final String EXTRA_ITEM_ID = "extra_item_id";

    private static final String EXTRA_INDEX = "extra_index";
    private static final String EXTRA_OFFSET = "extra_offset";
    private static final String EXTRA_COLUMNS = "extra_columns";
	
	//private static final String TAG = "AlbumGridFragment";
	
	private FlippingViewHelper mFlipHelper;
    
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
        grid.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int numColumns = grid.getNumColumns();
                        int columnWidth = grid.getColumnWidth();
                        if (numColumns > 0 && columnWidth > 0) {
                            mAdapter.setColumnCountAndWidth(numColumns, columnWidth);
                            grid.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                });

        final long itemId = getArguments().getLong(EXTRA_ITEM_ID, 0);
        if (itemId > 0) {
            mAdapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    if (mAdapter.getCount() > 0) {
                        grid.setSelection(mAdapter.getItemPosition(itemId));
                        mAdapter.unregisterDataSetObserver(this);
                    }
                }
            });
            getArguments().remove(EXTRA_ITEM_ID);
        } else if (savedInstanceState != null) {
            final int index = savedInstanceState.getInt(EXTRA_INDEX);
            final int offset = savedInstanceState.getInt(EXTRA_OFFSET);
            final int columns = savedInstanceState.getInt(EXTRA_COLUMNS);
            grid.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int numColumns = grid.getNumColumns();
                    if (numColumns > 0 && mAdapter.getCount() > 0) {
                        int newIndex = index + (numColumns - columns);
                        grid.setSelection(newIndex);
                        grid.smoothScrollToPositionFromTop(newIndex, offset, 0);
                        grid.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        GridView grid = getGridView();
        int index = grid.getFirstVisiblePosition();
        View v = grid.getChildAt(0);
        int top = 0;
        if (v != null) {
            top = v.getTop();
        }
        outState.putInt(EXTRA_INDEX, index);
        outState.putInt(EXTRA_OFFSET, top);
        outState.putInt(EXTRA_COLUMNS, grid.getNumColumns());
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
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

}
