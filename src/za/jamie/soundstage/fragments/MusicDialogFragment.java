package za.jamie.soundstage.fragments;

import za.jamie.soundstage.activities.MusicActivity;
import za.jamie.soundstage.service.MusicConnection;
import android.app.Activity;
import android.app.DialogFragment;

public class MusicDialogFragment extends DialogFragment {

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof MusicActivity)) {
			throw new RuntimeException("MusicFragment needs a MusicActivity as its parent.");
		}
	}
	
	protected MusicConnection getMusicConnection() {
		return ((MusicActivity) getActivity()).getMusicConnection();
	}

}
