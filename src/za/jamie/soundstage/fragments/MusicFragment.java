package za.jamie.soundstage.fragments;

import za.jamie.soundstage.activities.MusicActivity;
import za.jamie.soundstage.service.MusicService;
import android.app.Activity;
import android.app.Fragment;

public class MusicFragment extends Fragment {

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof MusicActivity)) {
			throw new RuntimeException("MusicFragment needs a MusicActivity as its parent.");
		}
	}
	
	/**
	 * See {@link MusicActivity#getMusicConnection()}
	 */
	protected MusicService getMusicService() {
		return isAdded() ? ((MusicActivity) getActivity()).getMusicService() : null;
	}
	
	/**
	 * See {@link MusicActivity#showPlayer()}
	 */
	protected void showPlayer() {
		((MusicActivity) getActivity()).showPlayer();
	}
	
	/**
	 * See {@link MusicActivity#hidePlayer()}
	 */
	protected void hidePlayer() {
		((MusicActivity) getActivity()).hidePlayer();
	}

}
