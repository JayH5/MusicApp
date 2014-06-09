package za.jamie.soundstage.fragments;

/**
 * An interface for Activities to implement that host a {@link MusicListFragment} that contains a
 * list of tracks. The Activity "hosts" the Fragment and the Fragment may request that its contents
 * be played.
 * Created by jamie on 2014/06/08.
 */
public interface TrackListHost {
    /**
     * Play the hosted list of tracks starting at the given position.
     * @param position
     */
    void playAt(int position);

    /**
     * Shuffle and play all the tracks in the hosted list.
     */
    void shuffleAll();
}
