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

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof MusicActivity)) {
			throw new RuntimeException("MusicFragment needs a MusicActivity as its parent.");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, 
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list_fragment, parent, false);
	}
	
	/**
	 * See {@link MusicActivity#getMusicConnection()}
	 */
	protected MusicConnection getMusicConnection() {
		return ((MusicActivity) getActivity()).getMusicConnection();
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
