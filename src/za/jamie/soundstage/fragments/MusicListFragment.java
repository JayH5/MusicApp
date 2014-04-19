package za.jamie.soundstage.fragments;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.MusicActivity;
import za.jamie.soundstage.service.MusicConnection;
import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MusicListFragment extends ListFragment {

    private MusicActivity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mActivity = (MusicActivity) activity;
        } catch (ClassCastException e) {
            throw new RuntimeException("MusicFragment needs a MusicActivity as its parent.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_fragment, parent, false);
    }

    /**
     *
     * @return The MusicActivity that this fragment is attached to.
     */
    protected MusicActivity getMusicActivity() {
        return mActivity;
    }

    /**
     * See {@link MusicActivity#getMusicConnection()}
     */
    protected MusicConnection getMusicConnection() {
        return mActivity.getMusicConnection();
    }

    /**
     * See {@link MusicActivity#showPlayer()}
     */
    protected void showPlayer() {
        mActivity.showPlayer();
    }

    /**
     * See {@link MusicActivity#hidePlayer()}
     */
    protected void hidePlayer() {
        mActivity.hidePlayer();
    }

    /**
     * Run {@param action} on the UI thread if this fragment is attached to an Activity.
     */
    protected void safeRunOnUiThread(Runnable action) {
        if (mActivity != null) {
            mActivity.runOnUiThread(action);
        }
    }

}
