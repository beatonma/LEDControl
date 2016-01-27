package com.beatonma.ledcontrol.widget;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.beatonma.ledcontrol.app.ColorSettingsFragment;
import com.beatonma.ledcontrol.app.MainActivity;
import com.beatonma.self.led.ledcontrol.R;

/**
 * Created by Michael on 26/01/2016.
 */
public class QuickActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_quick);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, ColorSettingsFragment.newInstance())
                .commit();

        View more = findViewById(R.id.more_button);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
