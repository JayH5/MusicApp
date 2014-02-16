package za.jamie.soundstage.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListAdapter;

import za.jamie.soundstage.R;

/**
 * Created by jamie on 2014/02/16.
 */
public class GridFragment extends Fragment {

    final private Handler mHandler = new Handler();

    final private Runnable mRequestFocus = new Runnable() {
        public void run() {
            mGrid.focusableViewAvailable(mGrid);
        }
    };

    final private AdapterView.OnItemClickListener mOnClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            onGridItemClick((GridView) parent, v, position, id);
        }
    };

    ListAdapter mAdapter;
    GridView mGrid;
    View mEmptyView;
    boolean mGridShown;

    public GridFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.grid_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ensureGrid();
    }

    @Override
    public void onDestroyView() {
        mHandler.removeCallbacks(mRequestFocus);
        mGrid = null;
        mGridShown = false;
        mEmptyView = null;
        super.onDestroyView();
    }

    /**
     * This method will be called when an item in the list is selected.
     * Subclasses should override. Subclasses can call
     * getGridView().getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param g The GridView where the click happened
     * @param v The view that was clicked within the GridView
     * @param position The position of the view in the grid
     * @param id The row id of the item that was clicked
     */
    public void onGridItemClick(GridView g, View v, int position, long id) {
    }

    public void setListAdapter(ListAdapter adapter) {
        mAdapter = adapter;
        if (mGrid != null) {
            mGrid.setAdapter(adapter);
        }
    }

    public ListAdapter getListAdapter() {
        return mAdapter;
    }

    public void setSelection(int position) {
        ensureGrid();
        mGrid.setSelection(position);
    }

    public int getSelectedItemPosition() {
        ensureGrid();
        return mGrid.getSelectedItemPosition();
    }

    public long getSelectedItemId() {
        ensureGrid();
        return mGrid.getSelectedItemId();
    }

    public GridView getGridView() {
        ensureGrid();
        return mGrid;
    }

    private void ensureGrid() {
        if (mGrid != null) {
            return;
        }
        View root = getView();
        if (root == null) {
            throw new IllegalStateException("Content view not yet created");
        }
        if (root instanceof GridView) {
            mGrid = (GridView)root;
        } else {
            mEmptyView = root.findViewById(android.R.id.empty);
            View rawGridView = root.findViewById(R.id.grid);
            if (!(rawGridView instanceof GridView)) {
                throw new RuntimeException(
                        "Content has view with id attribute 'R.id.grid' "
                                + "that is not a GridView class");
            }
            mGrid = (GridView) rawGridView;
            if (mGrid == null) {
                throw new RuntimeException(
                        "Your content must have a GridView whose id attribute is " +
                                "'R.id.grid'");
            }
            if (mEmptyView != null) {
                mGrid.setEmptyView(mEmptyView);
            }
        }
        mGridShown = true;
        mGrid.setOnItemClickListener(mOnClickListener);
        if (mAdapter != null) {
            ListAdapter adapter = mAdapter;
            mAdapter = null;
            setListAdapter(adapter);
        }
        mHandler.post(mRequestFocus);
    }
}
