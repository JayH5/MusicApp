package za.jamie.soundstage.fragments.settings;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import za.jamie.soundstage.R;

/**
 * Created by jamie on 2014/06/06.
 */
public class AboutFragment extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle onSaveInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about, container, false);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        return v;
    }
}
