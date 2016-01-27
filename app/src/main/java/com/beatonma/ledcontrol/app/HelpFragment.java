package com.beatonma.ledcontrol.app;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.beatonma.self.led.ledcontrol.R;

/**
 * Created by Michael on 25/01/2016.
 */
public class HelpFragment extends BaseSettingsFragment {
    public static HelpFragment newInstance() {
        return new HelpFragment();
    }

    @Override
    int getLayout() {
        return R.layout.fragment_help;
    }

    @Override
    void initLayout(View v) {
        Button buttonTutorial = (Button) v.findViewById(R.id.button_tutorial);
        Button buttonCommunity = (Button) v.findViewById(R.id.button_community);
        Button buttonEmail = (Button) v.findViewById(R.id.button_email);

        buttonTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getContext().getString(R.string.link_tutorial_address)));
                try {
                    getContext().startActivity(intent);
                }
                catch (Exception e) {
                    Log.e(TAG, "Error opening tutorial. No browser installed?");
                }
            }
        });

        buttonCommunity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getContext().getString(R.string.link_community_address)));
                try {
                    getContext().startActivity(intent);
                }
                catch (Exception e) {
                    Log.e(TAG, "Error opening community. No G+/browser installed?");
                }
            }
        });

        buttonEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", getContext().getString(R.string.link_email_address), null));
                intent.putExtra(Intent.EXTRA_SUBJECT, getContext().getString(R.string.link_email_subject));
//                intent.putExtra(Intent.EXTRA_EMAIL, getContext().getString(R.string.link_email_address));
//                intent.putExtra(Intent.EXTRA_SUBJECT, getContext().getString(R.string.link_email_subject));
                try {
                    getContext().startActivity(intent);
                }
                catch (Exception e) {
                    Log.e(TAG, "Error opening tutorial. No email client installed?");
                }
            }
        });
    }
}
