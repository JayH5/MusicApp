package za.jamie.soundstage.fragments.albumbrowser;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.AlbumBrowserActivity;
import za.jamie.soundstage.fragments.ImageDialogFragment;
import za.jamie.soundstage.models.AlbumStatistics;
import za.jamie.soundstage.pablo.LastfmUris;
import za.jamie.soundstage.pablo.Pablo;
import za.jamie.soundstage.utils.AppUtils;
import za.jamie.soundstage.utils.TextUtils;
import android.app.Fragment;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class AlbumSummaryFragment extends Fragment {
	
	private static final String TAG_IMAGE_DIALOG = "tag_image_dialog";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_album_summary, parent, false);
	}
	
	public void loadSummary(AlbumStatistics stats) {
		View v = getView();
		if (v != null) {
			ImageView art = (ImageView) v.findViewById(R.id.albumThumb);
			TextView tracks = (TextView) v.findViewById(R.id.albumTracks);
			TextView duration = (TextView) v.findViewById(R.id.albumDuration);
			TextView year = (TextView) v.findViewById(R.id.albumYear);
			
			final Uri uri = LastfmUris.getAlbumInfoUri(stats.title,
					stats.artists.firstKey().getName(), stats.id);
			Pablo.with(getActivity())
				.load(uri)
				.resizeDimen(R.dimen.image_thumb_album, R.dimen.image_thumb_album)
				.centerCrop()
				.into(art);
			
			art.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					ImageDialogFragment.newInstance(uri)
						.show(getFragmentManager(), TAG_IMAGE_DIALOG);
				}
			});
			
			final Resources res = getResources();
			tracks.setText(TextUtils.getNumTracksText(res, stats.numTracks));
			duration.setText(TextUtils.getStatsDurationText(res, stats.duration));
			year.setText(TextUtils.getYearText(stats.firstYear, stats.lastYear));
			
			// If orientation is portrait fragment provides buttons for shuffle/artist
			if (AppUtils.isPortrait(getResources())) {
				final ImageButton browseArtistButton = 
						(ImageButton) v.findViewById(R.id.browse_artist_button);
				browseArtistButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						((AlbumBrowserActivity) getActivity()).browseArtist();					
					}
				});
				
				final ImageButton shuffleButton = 
						(ImageButton) v.findViewById(R.id.shuffle_button);
				shuffleButton.setOnClickListener(new View.OnClickListener() {				
					@Override
					public void onClick(View v) {
						((AlbumBrowserActivity) getActivity()).shuffleAlbum();
					}
				});
			}
		}
	}

}
