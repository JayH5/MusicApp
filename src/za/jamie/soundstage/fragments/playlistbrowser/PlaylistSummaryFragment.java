package za.jamie.soundstage.fragments.playlistbrowser;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.SortedSet;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.AlbumBrowserActivity;
import za.jamie.soundstage.activities.PlaylistBrowserActivity;
import za.jamie.soundstage.fragments.MusicFragment;
import za.jamie.soundstage.models.MusicItem;
import za.jamie.soundstage.models.PlaylistStatistics;
import za.jamie.soundstage.utils.AppUtils;
import za.jamie.soundstage.utils.TextUtils;

/**
 * Created by jamie on 2014/02/09.
 */
public class PlaylistSummaryFragment extends MusicFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist_summary, parent, false);
    }

    public void loadSummary(PlaylistStatistics stats) {
        View v = getView();
        if (v != null) {
            GridLayout art = (GridLayout) v.findViewById(R.id.album_thumbs);
            TextView artists = (TextView) v.findViewById(R.id.playlist_artists);
            TextView tracks = (TextView) v.findViewById(R.id.playlist_tracks);
            TextView duration = (TextView) v.findViewById(R.id.playlist_duration);

            /*final Uri uri = SoundstageUris.albumImage(stats);
            Pablo.with(getActivity())
                    .load(uri)
                    .resizeDimen(R.dimen.image_thumb_album, R.dimen.image_thumb_album)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_grey)
                    .into(art);*/

            SortedSet<MusicItem> playlistArtists = stats.artists;

            final Resources res = getResources();
            artists.setText(TextUtils.getNumArtistsText(res, playlistArtists.size()));
            tracks.setText(TextUtils.getNumTracksText(res, stats.numTracks));
            duration.setText(TextUtils.getStatsDurationText(res, stats.duration));

            // If orientation is portrait fragment provides buttons for shuffle/artist
            if (AppUtils.isPortrait(getResources())) {
                final ImageButton browseArtistButton =
                        (ImageButton) v.findViewById(R.id.browse_artist_button);
                browseArtistButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((PlaylistBrowserActivity) getActivity()).browseArtists();
                    }
                });

                final ImageButton shuffleButton =
                        (ImageButton) v.findViewById(R.id.shuffle_button);
                shuffleButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((PlaylistBrowserActivity) getActivity()).shufflePlaylist();
                    }
                });
            }
        }
    }
}
