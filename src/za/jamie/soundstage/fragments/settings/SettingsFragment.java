package za.jamie.soundstage.fragments.settings;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.audiofx.AudioEffect;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import za.jamie.soundstage.R;

/**
 * Created by jamie on 2014/06/06.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_base);

        Preference equalizer = findPreference("equaliser");
        equalizer.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                displayAudioEffectControlPanel();
                return true;
            }
        });
    }

    private void displayAudioEffectControlPanel() {
        try {
            Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
            getActivity().startActivity(effects);
        } catch (ActivityNotFoundException notFound) {
            Toast.makeText(getActivity(), R.string.error_no_equaliser, Toast.LENGTH_SHORT).show();
        }
    }
}
