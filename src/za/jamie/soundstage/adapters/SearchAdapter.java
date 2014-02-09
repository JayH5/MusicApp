package za.jamie.soundstage.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.utils.FlippingViewHelper;
import za.jamie.soundstage.models.MusicItem;
import za.jamie.soundstage.pablo.LastfmUtils;
import za.jamie.soundstage.pablo.Pablo;
import za.jamie.soundstage.pablo.SoundstageUris;
import za.jamie.soundstage.utils.TextUtils;

/**
 * Created by jamie on 2014/01/12.
 */
public class SearchAdapter extends CursorAdapter {

    private static final int MIN_SKIP_SIZE = 5;

    private int mIdColIdx;
    private int mMimeTypeColIdx;
    private int mArtistColIdx;
    private int mAlbumColIdx;
    private int mTitleColIdx;
    private int mData1ColIdx;
    private int mData2ColIdx;

    private int mArtistHeaderPosition;
    private int mNumArtists;
    private int mAlbumHeaderPosition;
    private int mNumAlbums;
    private int mTrackHeaderPosition;
    private int mNumTracks;

    private final Context mContext;
    private final LayoutInflater mInflater;

    private OnScrollRequestListener mScrollRequestListener;

    private FlippingViewHelper mFlipHelper;

    private final View.OnClickListener mScrollToArtistsListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            scrollToPosition(mArtistHeaderPosition);
        }
    };

    private final View.OnClickListener mScrollToAlbumsListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            scrollToPosition(mAlbumHeaderPosition);
        }
    };

    private final View.OnClickListener mScrollToTracksListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            scrollToPosition(mTrackHeaderPosition);
        }
    };

    public SearchAdapter(Context context, FlippingViewHelper flipper) {
        super(context, null, 0);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mFlipHelper = flipper;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final String mimeType = cursor.getString(mMimeTypeColIdx);
        final int res;
        if (isArtist(mimeType)) {
            res = R.layout.list_item_artist;
        } else if (isAlbum(mimeType)) {
            res = R.layout.list_item_album;
        } else {
            res = R.layout.list_item_track;
        }
        Log.d("Search", "New view, mimeType: " + mimeType);
        return mInflater.inflate(res, parent, false);
    }

    public View newHeaderView(Context context, ViewGroup parent) {
        Log.d("Search", "New header view");
        return mInflater.inflate(R.layout.list_item_header_search, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final String mimeType = cursor.getString(mMimeTypeColIdx);

        MusicItem item = null;
        if (isArtist(mimeType)) {
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
            long id = cursor.getLong(mIdColIdx);
            Uri uri = SoundstageUris.artistImage(id, artist);
            Pablo.with(context)
                    .load(uri)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_grey)
                    .into(image);

            item = new MusicItem(id, artist, MusicItem.TYPE_ARTIST);
        } else if (isAlbum(mimeType)) {
            TextView title = (TextView) view.findViewById(R.id.albumName);
            String album = cursor.getString(mAlbumColIdx);
            title.setText(album);

            TextView subtitle = (TextView) view.findViewById(R.id.albumArtist);
            String artist = cursor.getString(mArtistColIdx);
            subtitle.setText(artist);

            ImageView image = (ImageView) view.findViewById(R.id.albumImage);
            long id = cursor.getLong(mIdColIdx);
            Uri uri = SoundstageUris.albumImage(id, album, artist);
            Pablo.with(context)
                    .load(uri)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_grey)
                    .into(image);

            item = new MusicItem(id, album, MusicItem.TYPE_ALBUM);
        } else if (isTrack(mimeType)) {
            TextView title = (TextView) view.findViewById(R.id.trackName);
            String name = cursor.getString(mTitleColIdx);
            title.setText(name);

            TextView subtitle = (TextView) view.findViewById(R.id.trackAlbum);
            subtitle.setText(cursor.getString(mAlbumColIdx));

            TextView description = (TextView) view.findViewById(R.id.trackArtist);
            description.setText(cursor.getString(mArtistColIdx));

            item = new MusicItem(cursor.getLong(mIdColIdx), name, MusicItem.TYPE_TRACK);
        }

        if (mFlipHelper != null && item != null) {
            mFlipHelper.bindFlippedViewButtons(view, item);
        }
    }

    public void bindHeaderView(View view, String header, int position) {
        TextView headerText = (TextView) view.findViewById(R.id.header_text);
        headerText.setText(header);

        if (position == mArtistHeaderPosition) {
            view.findViewById(R.id.btn_skip_up).setVisibility(View.GONE);
            View down = view.findViewById(R.id.btn_skip_down);
            if (mNumAlbums > 0 && mNumArtists >= MIN_SKIP_SIZE) {
                down.setVisibility(View.VISIBLE);
                down.setOnClickListener(mScrollToAlbumsListener);
            } else if (mNumTracks > 0 && mNumArtists >= MIN_SKIP_SIZE) {
                down.setVisibility(View.VISIBLE);
                down.setOnClickListener(mScrollToTracksListener);
            } else {
                down.setVisibility(View.GONE);
            }
        } else if (position == mAlbumHeaderPosition) {
            View up = view.findViewById(R.id.btn_skip_up);
            if (mNumArtists >= MIN_SKIP_SIZE) {
                up.setVisibility(View.VISIBLE);
                up.setOnClickListener(mScrollToArtistsListener);
            } else {
                up.setVisibility(View.GONE);
            }
            View down = view.findViewById(R.id.btn_skip_down);
            if (mNumTracks > 0 && mNumAlbums >= MIN_SKIP_SIZE) {
                down.setVisibility(View.VISIBLE);
                down.setOnClickListener(mScrollToTracksListener);
            } else {
                down.setVisibility(View.GONE);
            }
        } else if (position == mTrackHeaderPosition) {
            View up = view.findViewById(R.id.btn_skip_up);
            if (mNumAlbums > MIN_SKIP_SIZE) {
                up.setVisibility(View.VISIBLE);
                up.setOnClickListener(mScrollToAlbumsListener);
            } else if (mNumArtists > MIN_SKIP_SIZE) {
                up.setVisibility(View.VISIBLE);
                up.setOnClickListener(mScrollToArtistsListener);
            } else {
                up.setVisibility(View.GONE);
            }
            view.findViewById(R.id.btn_skip_down).setVisibility(View.GONE);
        }
    }

    public void setOnScrollRequestListener(OnScrollRequestListener listener) {
        mScrollRequestListener = listener;
    }

    private void scrollToPosition(int position) {
        if (mScrollRequestListener != null) {
            mScrollRequestListener.scrollToPosition(position);
        }
    }

    public void setFlippingViewHelper(FlippingViewHelper helper) {
        mFlipHelper = helper;
    }

    @Override
    public int getItemViewType(int position) {
        final String mimeType = getItemMimeType(position);
        final int viewType;
        if (isArtist(mimeType)) {
            viewType = 0;
        } else if (isAlbum(mimeType)) {
            viewType = 1;
        } else if (isTrack(mimeType)) {
            viewType = 2;
        } else {
            viewType = 3;
        }
        return viewType;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor != null) {
            getColumnIndices(newCursor);
        }
        setHeaderPositions(newCursor);
        return super.swapCursor(newCursor);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View v;
        if (position == mArtistHeaderPosition
                || position == mAlbumHeaderPosition
                || position == mTrackHeaderPosition) {
            v = convertView != null ? convertView : newHeaderView(mContext, parent);
            bindHeaderView(v, (String) getItem(position), position);
        } else {
            v = super.getView(getCursorPosition(position), convertView, parent);
        }
        return v;
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

    @Override
    public Object getItem(int position) {
        if (position == mArtistHeaderPosition) {
            return TextUtils.getNumArtistsText(mContext.getResources(), mNumArtists);
        } else if (position == mAlbumHeaderPosition) {
            return TextUtils.getNumAlbumsText(mContext.getResources(), mNumAlbums);
        } else if (position == mTrackHeaderPosition) {
            return TextUtils.getNumTracksText(mContext.getResources(), mNumTracks);
        } else {
            return super.getItem(getCursorPosition(position));
        }
    }

    @Override
    public long getItemId(int position) {
        if (position == mArtistHeaderPosition) {
            return 0;
        } else if (position == mAlbumHeaderPosition) {
            return 0;
        } else if (position == mTrackHeaderPosition) {
            return 0;
        } else {
            return super.getItemId(getCursorPosition(position));
        }
    }

    @Override
    public int getCount() {
        int count = super.getCount();
        if (count > 0) {
            // Count the headers
            if (mArtistHeaderPosition > -1) {
                count++;
            }
            if (mAlbumHeaderPosition > -1) {
                count++;
            }
            if (mTrackHeaderPosition > -1) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        if (getItem(position) instanceof String) {
            return false;
        }
        return super.isEnabled(getCursorPosition(position));
    }

    public String getItemMimeType(int position) {
        Object item = getItem(position);
        if (item instanceof Cursor) {
            return ((Cursor) item).getString(mMimeTypeColIdx);
        }
        return null;
    }

    private void setHeaderPositions(Cursor cursor) {
        mNumArtists = 0;
        mNumAlbums = 0;
        mNumTracks = 0;

        mArtistHeaderPosition = -1;
        mAlbumHeaderPosition = -1;
        mTrackHeaderPosition = -1;
        if (cursor != null && cursor.moveToFirst()) {
            // Determine the type of the row based on mime type
            String mimeType = cursor.getString(mMimeTypeColIdx);

            // Count the artists
            while (isArtist(mimeType)) {
                mNumArtists++;
                if (cursor.moveToNext()) {
                    mimeType = cursor.getString(mMimeTypeColIdx);
                } else {
                    break;
                }
            }

            // Count the albums
            while (isAlbum(mimeType)) {
                mNumAlbums++;
                if (cursor.moveToNext()) {
                    mimeType = cursor.getString(mMimeTypeColIdx);
                } else {
                    break;
                }
            }

            // Number of tracks remain
            mNumTracks = cursor.getCount() - mNumArtists - mNumAlbums;

            // Position the headers
            if (mNumArtists > 0) {
                mArtistHeaderPosition = 0;
            }
            if (mNumAlbums > 0) {
                mAlbumHeaderPosition = mNumArtists;
                if (mNumArtists > 0) {
                    mAlbumHeaderPosition++;
                }
            }
            if (mNumTracks > 0) {
                mTrackHeaderPosition = mNumArtists + mNumAlbums;
                if (mNumArtists > 0) {
                    mTrackHeaderPosition++;
                }
                if (mNumAlbums > 0) {
                    mTrackHeaderPosition++;
                }
            }
        }

        Log.d("Search", "Headers at positions: " + mArtistHeaderPosition + ":" + mAlbumHeaderPosition + ":" + mTrackHeaderPosition);
    }

    private int getCursorPosition(int position) {
        int cursorPosition = position;
        if (mNumArtists > 0 && cursorPosition >= mArtistHeaderPosition) {
            cursorPosition--;
        }
        if (mNumAlbums > 0 && cursorPosition >= mAlbumHeaderPosition) {
            cursorPosition--;
        }
        if (mNumTracks > 0 && cursorPosition >= mTrackHeaderPosition) {
            cursorPosition--;
        }
        return cursorPosition;
    }

    private static boolean isAlbum(String mimeType) {
        return "album".equals(mimeType);
    }

    private static boolean isArtist(String mimeType) {
        return "artist".equals(mimeType);
    }

    private static boolean isTrack(String mimeType) {
        return mimeType != null &&
                (mimeType.startsWith("audio/")
                || mimeType.equals("application/ogg")
                || mimeType.equals("application/x-ogg"));
    }

    public interface OnScrollRequestListener {
        void scrollToPosition(int position);
    }

}
