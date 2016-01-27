package com.beatonma.ledcontrol.app;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.beatonma.ledcontrol.service.AmbientService;
import com.beatonma.self.led.ledcontrol.R;

/**
 * Created by Michael on 20/01/2016.
 */
public class AmbientSettingsFragment extends BaseSettingsFragment {
    public static AmbientSettingsFragment newInstance() {
        return new AmbientSettingsFragment();
    }

    @Override
    int getLayout() {
        return R.layout.fragment_settings_ambient;
    }

    @Override
    void initLayout(View v) {
        Button startService = (Button) v.findViewById(R.id.start_service);
        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMainActivity().checkFilePermissions();
                startAmbientService();
            }
        });

        Button stopService = (Button) v.findViewById(R.id.stop_service);
        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAmbientService();
            }
        });
    }

    private void startAmbientService() {
        Intent serviceIntent = new Intent(getActivity(), AmbientService.class);
        getActivity().startService(serviceIntent);
    }

    private void stopAmbientService() {
        Intent stopServiceIntent = new Intent(AmbientService.STOP_SERVICE);
        getActivity().sendBroadcast(stopServiceIntent);
    }
}
