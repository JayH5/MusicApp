package za.jamie.soundstage.fragments;

import za.jamie.soundstage.R;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DefaultListFragment extends ListFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, 
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list_fragment_default, parent, false);
	}

}
