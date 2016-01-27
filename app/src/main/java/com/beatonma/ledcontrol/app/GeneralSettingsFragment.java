package com.beatonma.ledcontrol.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.beatonma.ledcontrol.Broadcaster;
import com.beatonma.self.led.ledcontrol.R;
import com.beatonma.ledcontrol.app.ui.FloatSeekbarPreference;
import com.beatonma.ledcontrol.app.ui.SeekbarPreference;
import com.beatonma.ledcontrol.utility.AnimationUtils;
import com.beatonma.ledcontrol.utility.PrefUtils;

/**
 * Created by Michael on 20/01/2016.
 */
public class GeneralSettingsFragment extends BaseSettingsFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private View mOnboardContainer;
    private SeekbarPreference mMaxBrightnessBar;
    private SeekbarPreference mMinBrightnessBar;

    public static GeneralSettingsFragment newInstance() {
        return new GeneralSettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrefUtils.get(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PrefUtils.get(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    int getLayout() {
        return R.layout.fragment_settings_general;
    }

    @Override
    void initLayout(View v) {
        FloatSeekbarPreference colorDuration = (FloatSeekbarPreference) v.findViewById(R.id.color_change_duration);

        colorDuration.setOnSeekbarMovedListener(new SeekbarPreference.OnSeekbarMovedListener() {
            @Override
            public void onSeekbarMoved(int progress) {
                float value = (float) progress / 2f;
                PrefUtils.get(getContext()).edit()
                        .putFloat(PrefUtils.PREF_COLOR_CHANGE_DURATION, value)
                        .commit();
            }
        });

        mMaxBrightnessBar = (SeekbarPreference) v.findViewById(R.id.max_brightness);
        mMinBrightnessBar = (SeekbarPreference) v.findViewById(R.id.min_brightness);

        mMaxBrightnessBar.setOnSeekbarMovedListener(new SeekbarPreference.OnSeekbarMovedListener() {
            @Override
            public void onSeekbarMoved(int progress) {
                if (progress < mMinBrightnessBar.getProgress()) {
                    mMinBrightnessBar.setProgress(progress);
                }
            }
        });
        mMinBrightnessBar.setOnSeekbarMovedListener(new SeekbarPreference.OnSeekbarMovedListener() {
            @Override
            public void onSeekbarMoved(int progress) {
                if (progress > mMaxBrightnessBar.getProgress()) {
                    mMaxBrightnessBar.setProgress(progress);
                }
            }
        });

        mOnboardContainer = v.findViewById(R.id.onboard_container);
        boolean showGeneralOnboarding = PrefUtils.get(getContext()).getBoolean(PrefUtils.ONBOARD_SHOW_ONBOARD_GENERAL, true);
        if (showGeneralOnboarding) {
            Button onboardOkButton = (Button) v.findViewById(R.id.ok_button);
            Button onboardHelpButton = (Button) v.findViewById(R.id.help_button);

            onboardOkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AnimationUtils.hidePreference(mOnboardContainer, mOnboardContainer.getMeasuredHeight());

                    PrefUtils.get(getContext()).edit()
                            .putBoolean(PrefUtils.ONBOARD_SHOW_ONBOARD_GENERAL, false)
                            .commit();
                }
            });

            onboardHelpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getMainActivity().showHelp();
                }
            });

            mOnboardContainer.requestFocus();
        }
        else {
            mOnboardContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        broadcastPreferences(sharedPreferences);
    }

    private void broadcastPreferences(SharedPreferences sp) {
        String prefs =
                formatPref(PrefUtils.PREF_MAX_BRIGHTNESS, sp.getInt(PrefUtils.PREF_MAX_BRIGHTNESS, 100))
                + formatPref(PrefUtils.PREF_MIN_BRIGHTNESS, sp.getInt(PrefUtils.PREF_MIN_BRIGHTNESS, 0))
                + formatPref(PrefUtils.PREF_INTERPOLATE_CHANGES, sp.getBoolean(PrefUtils.PREF_INTERPOLATE_CHANGES, false))
                + formatPref(PrefUtils.PREF_COLOR_CHANGE_DURATION, sp.getFloat(PrefUtils.PREF_COLOR_CHANGE_DURATION, 1f));

        Broadcaster.getInstance(getActivity()).broadcastPreferences(prefs);
    }

    private String formatPref(String name, String value) {
        return name + "=" + value + "\n";
    }

    private String formatPref(String name, int value) {
        return formatPref(name, String.valueOf(value));
    }

    private String formatPref(String name, boolean value) {
        return formatPref(name, (value ? "True" : "False"));
    }

    private String formatPref(String name, float value) {
        return formatPref(name, String.valueOf(value));
    }
}
