package za.jamie.soundstage.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import za.jamie.soundstage.R;
import za.jamie.soundstage.pablo.LastfmUris;
import za.jamie.soundstage.pablo.Pablo;

/**
 * Created by jamie on 2014/01/12.
 */
public class SearchAdapter extends CursorAdapter {

    private int mIdColIdx;
    private int mMimeTypeColIdx;
    private int mArtistColIdx;
    private int mAlbumColIdx;
    private int mTitleColIdx;
    private int mData1ColIdx;
    private int mData2ColIdx;

    private final LayoutInflater mInflater;

    public SearchAdapter(Context context) {
        super(context, null, 0);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        final String mimeType = cursor.getString(mMimeTypeColIdx);
        final int res;
        if ("artist".equals(mimeType)) {
            res = R.layout.list_item_artist;
        } else if ("album".equals(mimeType)) {
            res = R.layout.list_item_album;
        } else  {
            res = R.layout.list_item_track;
        }
        return mInflater.inflate(res, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final String mimeType = cursor.getString(mMimeTypeColIdx);

        if ("artist".equals(mimeType)) {
            TextView title = (TextView) view.findViewById(R.id.artistName);
            String artist = cursor.getString(mArtistColIdx);
            title.setText(artist);

            TextView subtitle = (TextView) view.findViewById(R.id.artistAlbums);
            final Resources res = context.getResources();
            int numAlbums = cursor.getInt(mData1ColIdx);
            String albums = za.jamie.soundstage.utils.TextUtils.getNumAlbumsText(res, numAlbums);
            subtitle.setText(albums);

            TextView description = (TextView) view.findViewById(R.id.artistTracks);
            int numTracks = cursor.getInt(mData2ColIdx);
            String tracks = za.jamie.soundstage.utils.TextUtils.getNumTracksText(res, numTracks);
            description.setText(tracks);

            ImageView image = (ImageView) view.findViewById(R.id.artistImage);
            Uri uri = LastfmUris.getArtistInfoUri(artist);
            Pablo.with(context)
                    .load(uri)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_grey)
                    .into(image);
        } else if ("album".equals(mimeType)) {
            TextView title = (TextView) view.findViewById(R.id.albumName);
            String album = cursor.getString(mAlbumColIdx);
            title.setText(album);

            TextView subtitle = (TextView) view.findViewById(R.id.albumArtist);
            String artist = cursor.getString(mArtistColIdx);
            subtitle.setText(artist);

            ImageView image = (ImageView) view.findViewById(R.id.albumImage);
            Uri uri = LastfmUris.getAlbumInfoUri(album, artist, cursor.getLong(mIdColIdx));
            Pablo.with(context)
                    .load(uri)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_grey)
                    .into(image);
        } else if (mimeType.startsWith("audio/") || mimeType.equals("application/ogg")
                || mimeType.equals("application/x-ogg")) {
            TextView title = (TextView) view.findViewById(R.id.trackName);
            title.setText(cursor.getString(mTitleColIdx));

            TextView subtitle = (TextView) view.findViewById(R.id.trackAlbum);
            subtitle.setText(cursor.getString(mAlbumColIdx));

            TextView description = (TextView) view.findViewById(R.id.trackArtist);
            description.setText(cursor.getString(mArtistColIdx));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return super.getItemViewType(position); // Header
        }

        final String mimeType = getItemMimeType(position);
        final int viewType;
        if ("artist".equals(mimeType)) {
            viewType = 0;
        } else if ("album".equals(mimeType)) {
            viewType = 1;
        } else {
            viewType = 2;
        }
        return viewType;
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + 2;
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        Cursor oldCursor = super.swapCursor(newCursor);
        if (newCursor != null) {
            getColumnIndices(newCursor);
        }
        return oldCursor;
    }

    protected void getColumnIndices(Cursor cursor) {
        mIdColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
        mMimeTypeColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE);
        mArtistColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
        mAlbumColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
        mTitleColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
        mData1ColIdx = cursor.getColumnIndexOrThrow("data1");
        mData2ColIdx = cursor.getColumnIndexOrThrow("data2");
    }

    public String getItemMimeType(int position) {
        Cursor cursor = (Cursor) getItem(position);
        if (cursor != null) {
            return cursor.getString(mMimeTypeColIdx);
        }
        return null;
    }

}
