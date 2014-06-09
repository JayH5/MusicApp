package za.jamie.soundstage.fragments.settings;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.audiofx.AudioEffect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.SettingsActivity;
import za.jamie.soundstage.service.MusicConnection;
import za.jamie.soundstage.service.MusicService;

/**
 * Created by jamie on 2014/06/06.
 */
public class SettingsFragment extends PreferenceFragment {

    private SettingsActivity mActivity;
    private Messenger mSettingsClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Handler settingsClientHandler = new SettingsClientHandler(this);
        mSettingsClient = new Messenger(settingsClientHandler);

        addPreferencesFromResource(R.xml.prefs_base);

        Preference equalizer = findPreference("equaliser");
        equalizer.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                requestAudioSessionId();
                return true;
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mActivity = (SettingsActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("SettingsFragment needs a SettingsActivity as its parent");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    public MusicConnection getMusicConnection() {
        return mActivity.getMusicConnection();
    }

    /**
     * Request the audio session ID from the service. When the service responds, the audio effects
     * will be opened with an Intent.
     */
    private void requestAudioSessionId() {
        boolean success = getMusicConnection().getAudioSessionId(mSettingsClient);
        Log.d("SettingsFragment", "Requested audio session ID, success? " + success);
    }

    private void displayAudioEffectControlPanel(int audioSessionId) {
        try {
            Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
            effects.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId);
            getActivity().startActivity(effects);
        } catch (ActivityNotFoundException notFound) {
            Toast.makeText(getActivity(), R.string.error_no_equaliser, Toast.LENGTH_SHORT).show();
        }
    }

    private static class SettingsClientHandler extends Handler {

        final WeakReference<SettingsFragment> mFragment;

        SettingsClientHandler(SettingsFragment fragment) {
            mFragment = new WeakReference<SettingsFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            final SettingsFragment fragment = mFragment.get();
            if (fragment == null) {
                return;
            }

            switch (msg.what) {
                case MusicService.MSG_AUDIO_SESSION_ID:
                    fragment.displayAudioEffectControlPanel(msg.arg1);
                    break;
            }
        }
    }
}
