package za.jamie.soundstage.fragments;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.SearchAdapter;
import za.jamie.soundstage.models.MusicItem;

/**
 * Created by jamie on 2014/01/12.
 */
public class SearchFragment extends MusicListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String EXTRA_QUERY = "extra_query";

    private SearchAdapter mAdapter;
    private String mFilterString;

    private TextView mHeaderView;

    public static SearchFragment newInstance(String query) {
        Bundle args = new Bundle();
        args.putString(EXTRA_QUERY, query);

        SearchFragment frag = new SearchFragment();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new SearchAdapter(getActivity());
        mFilterString = getArguments().getString(EXTRA_QUERY);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_QUERY, mFilterString);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mHeaderView = (TextView) inflater.inflate(R.layout.list_item_header, null, false);
        return inflater.inflate(R.layout.list_fragment_search, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListAdapter(null);
        final ListView lv = getListView();
        lv.addHeaderView(mHeaderView);
        setListAdapter(mAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Uri.parse("content://media/external/audio/search/fancy/"
                + Uri.encode(mFilterString));
        String[] projection = new String[] {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                "data1", "data2"
        };
        return new CursorLoader(getActivity(), uri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        updateHeaderView(data != null ? data.getCount() : 0);
        mAdapter.swapCursor(data);
    }

    private void updateHeaderView(int count) {
        mHeaderView.setText(count + " RESULTS");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String mimeType = mAdapter.getItemMimeType(position - 1);
        if("artist".equals(mimeType)) {
            launchArtistBrowser(id);
        } else if ("album".equals(mimeType)) {
            launchAlbumBrowser(id);
        } else if (position >= 0 && id > 0) {
            playTrack(id);
        }
    }

    private void launchArtistBrowser(long id) {
        Uri data = ContentUris.withAppendedId(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, id);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(data, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE);
        startActivity(intent);
    }

    private void launchAlbumBrowser(long id) {
        Uri data = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(data, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE);
        startActivity(intent);
    }

    private void playTrack(long trackId) {
        MusicItem track = new MusicItem(trackId, "", MusicItem.TYPE_TRACK);
        getMusicConnection().open(track, 0);
        showPlayer();
    }

    public void setFilterString(String filter) {
        mFilterString = filter;
        getLoaderManager().restartLoader(0, null, this);
    }

}
