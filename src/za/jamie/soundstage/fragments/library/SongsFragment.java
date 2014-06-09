package za.jamie.soundstage.fragments.library;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.library.SongsAdapter;
import za.jamie.soundstage.adapters.utils.FlippingViewHelper;
import za.jamie.soundstage.animation.ViewFlipper;
import za.jamie.soundstage.fragments.MusicListFragment;
import za.jamie.soundstage.providers.MusicLoaders;

public class SongsFragment extends MusicListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private SongsAdapter mAdapter;
    private FlippingViewHelper mFlipHelper;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new SongsAdapter(getActivity(),
        		R.layout.list_item_two_line, R.layout.list_item_header, null, 0);
        setListAdapter(mAdapter);

        ViewFlipper flipper = new ViewFlipper(getActivity(), R.id.list_item, R.id.flipped_view);
        mFlipHelper = new FlippingViewHelper(getMusicActivity(), flipper);
        mAdapter.setFlippingViewHelper(mFlipHelper);
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		getMusicConnection().playAll(mAdapter.getCursorPosition(position));
        showPlayer();
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
    		Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.list_fragment_fastscroll, parent, false);
    }

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mFlipHelper.initFlipper(getListView());
	}

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return MusicLoaders.songs(getActivity());
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