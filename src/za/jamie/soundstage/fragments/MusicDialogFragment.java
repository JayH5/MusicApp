package za.jamie.soundstage.fragments;

import za.jamie.soundstage.activities.MusicActivity;
import za.jamie.soundstage.service.MusicConnection;
import android.app.Activity;
import android.app.DialogFragment;

public class MusicDialogFragment extends DialogFragment {

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

}
