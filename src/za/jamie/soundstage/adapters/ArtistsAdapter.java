package za.jamie.soundstage.adapters;

import java.util.ArrayList;
import java.util.List;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.ArtistAdapter;
import za.jamie.soundstage.bitmapfun.ImageFetcher;
import za.jamie.soundstage.utils.ImageUtils;
import za.jamie.soundstage.utils.TextUtils;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class ArtistsAdapter extends ArtistAdapter implements SectionIndexer {	
	
	private Object[] mSectionHeaders;
	private Object[] mSectionPositions;
	
	private int mNumAlbumsIdx;
	private int mNumTracksIdx;
	
	private ImageFetcher mImageWorker;
	
	public ArtistsAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
		mImageWorker = ImageUtils.getThumbImageFetcher(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {		
		final TextView title = (TextView) view.findViewById(R.id.artistName);
		final TextView numAlbumsText = (TextView) view.findViewById(R.id.artistAlbums);
		final TextView numTracksText = (TextView) view.findViewById(R.id.artistTracks);
		final ImageView artistImage = (ImageView) view.findViewById(R.id.artistImage);
		
		final String artist = cursor.getString(getArtistColIdx());
		final int numAlbums = cursor.getInt(mNumAlbumsIdx);
		final int numTracks = cursor.getInt(mNumTracksIdx);
		
		title.setText(artist);
		
		final Resources res = context.getResources();
		numAlbumsText.setText(TextUtils.getNumAlbumsText(res, numAlbums));
		numTracksText.setText(TextUtils.getNumTracksText(res, numTracks));
		
		final long artistId = cursor.getLong(getIdColIdx());
		
		mImageWorker.loadArtistImage(artistId, artist, artistImage);
	}
	
	@Override	
	public void getColumnIndices(Cursor cursor) {
		super.getColumnIndices(cursor);
		if (cursor != null) {
			mNumAlbumsIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
			mNumTracksIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS);
		}
	}
	
	@Override
	protected void onCursorLoad(Cursor cursor) {
		super.onCursorLoad(cursor);
		
		if (cursor.moveToFirst()) {
			String previousHeader = null;
			List<String> sections = new ArrayList<String>();
			List<Integer> positions = new ArrayList<Integer>();
			final int titleColIdx = getArtistColIdx();
			int count = 0;
			do {
				final String title = cursor.getString(titleColIdx);
				final String header = TextUtils.headerFor(title);
				if (!header.equals(previousHeader)) {
					sections.add(header);
					positions.add(count);
					
					previousHeader = header;
					// TODO: Add headers
				}
				count++;
			} while (cursor.moveToNext());
			
			mSectionHeaders = sections.toArray();
			mSectionPositions = positions.toArray();
		}
	}

	@Override
	public int getPositionForSection(int section) {
		return (mSectionPositions != null) ? (Integer) mSectionPositions[section] : 0;
	}

	@Override
	public int getSectionForPosition(int position) {
		if (mSectionPositions != null) {
            for (int i = 0; i < mSectionPositions.length - 1; i++) {
                if (position >= (Integer) mSectionPositions[i]
                        && position < (Integer) mSectionPositions[i + 1]) {
                    return i;
                }
            }
            if (position >= (Integer)mSectionPositions[mSectionPositions.length - 1]) {
                return mSectionPositions.length - 1;
            }
        }
        return 0;
	}

	@Override
	public Object[] getSections() {
		return mSectionHeaders;
	}

}
