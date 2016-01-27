package com.beatonma.ledcontrol.app;

import android.graphics.Color;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import com.beatonma.colorpicker.ColorPickerFragment;
import com.beatonma.ledcontrol.Broadcaster;
import com.beatonma.self.led.ledcontrol.R;
import com.larswerkman.lobsterpicker.ColorAdapter;
import com.larswerkman.lobsterpicker.LobsterPicker;

/**
 * Created by Michael on 20/01/2016.
 */
public class ColorSettingsFragment extends BaseSettingsFragment {
    private int[] COLORS = new int[] { Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.WHITE };

    private GestureDetector mGestureDetector;

    private LobsterPicker mColorWheel;
    private SeekBar mBrightnessSlider;

    public static ColorSettingsFragment newInstance() {
        return new ColorSettingsFragment();
    }

    @Override
    int getLayout() {
        return R.layout.fragment_settings_color;
    }

    @Override
    void initLayout(View v) {
        mGestureDetector = new GestureDetector(getContext(), new GestureListener());

        mColorWheel = (LobsterPicker) v.findViewById(R.id.color_picker);
        mColorWheel.setColorAdapter(new ColorAdapter() {
            @Override
            public int color(int position, int shade) {
                return COLORS[position];
            }

            @Override
            public int shades(int position) {
                return 0;
            }

            @Override
            public int size() {
                return COLORS.length;
            }
        });
        mColorWheel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                updateColor();
                return mGestureDetector.onTouchEvent(event);
            }
        });

        mBrightnessSlider = (SeekBar) v.findViewById(R.id.brightness_slider);
        mBrightnessSlider.setProgress(100);
        mBrightnessSlider.setMax(100);
        mBrightnessSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateColor();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void showComplexColorPicker() {
        String tag = "customcolor";
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.top_level_container, ColorPickerFragment.newInstance("", getContext().getResources().getColor(R.color.Accent)), tag)
                .addToBackStack(tag)
                .commit();
    }

    private void setColor(int color, int brightness) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        hsv[2] = (float) brightness / 100f;

        Broadcaster.getInstance(getContext(), mResponseListener).broadcastColor(hsv);
    }

    private void updateColor() {
        setColor(mColorWheel.getColor(), mBrightnessSlider.getProgress());
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent e) {
            showComplexColorPicker();
            Log.d(TAG, "LongPress");
        }
    }
}
