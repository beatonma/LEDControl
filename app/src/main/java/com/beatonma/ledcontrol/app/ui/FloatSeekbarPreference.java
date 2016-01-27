package com.beatonma.ledcontrol.app.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.beatonma.self.led.ledcontrol.R;
import com.beatonma.ledcontrol.utility.PrefUtils;

/**
 * Created by Michael on 25/01/2016.
 */
public class FloatSeekbarPreference extends SeekbarPreference {
    private float mMultiplier;
    private float mMinValue;

    public FloatSeekbarPreference(Context context) {
        super(context);
    }

    public FloatSeekbarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatSeekbarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FloatSeekbarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        View v = inflate(context, R.layout.view_preference_seekbar, this);

        mTitle = (TextView) v.findViewById(R.id.title);
        mLeftText = (TextView) v.findViewById(R.id.text);
        mSlider = (AppCompatSeekBar) v.findViewById(R.id.slider);
        mValue = (TextView) v.findViewById(R.id.value);
        mCenterMarker = v.findViewById(R.id.center_marker);

        boolean showCenter;
        boolean showValue;
        int defaultProgress = 0;
        int maxValue = 20;
        mMultiplier = 1.0f;

        TypedArray a = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.SeekbarPreference, defStyleAttr, defStyleRes);

        try {
            mKey = a.getString(R.styleable.SeekbarPreference_key);
            setTitle(a.getString(R.styleable.SeekbarPreference_title));
            defaultProgress = a.getInt(R.styleable.SeekbarPreference_progress, 0);
            setValue(String.valueOf(a.getInt(R.styleable.SeekbarPreference_progress, 0)));
            showCenter = a.getBoolean(R.styleable.SeekbarPreference_showCenter, false);
            showValue = a.getBoolean(R.styleable.SeekbarPreference_showValue, true);
            maxValue = a.getInt(R.styleable.SeekbarPreference_max_value, 20);
            mUseCustomListener = a.getBoolean(R.styleable.SeekbarPreference_customListener, false);
            mMultiplier = a.getFloat(R.styleable.SeekbarPreference_float_multiplier, 1.0f);
            mMinValue = a.getFloat(R.styleable.SeekbarPreference_min_value, 0f);
        }
        finally {
            a.recycle();
        }

        initColor();

        mSlider.setMax(maxValue);

        int initProgress = (int) (PrefUtils.get(getContext()).getFloat(mKey, defaultProgress) / mMultiplier);
        float value = ((float) initProgress * mMultiplier);
        setValue(String.valueOf(value));

        setProgress(initProgress);

        mCenterMarker.setVisibility(showCenter ? VISIBLE : GONE);
        mValue.setVisibility(showValue ? VISIBLE : GONE);

        mSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mListener != null) {
                    mListener.onSeekbarMoved(progress);
                }

                if (mUseCustomListener) {
                    // Ignore default listener
                    return;
                }

                float value = ((float) progress * mMultiplier) + mMinValue;
                mValue.setText(String.valueOf(value));
                PrefUtils.get(getContext()).edit()
                        .putFloat(mKey, value)
                        .commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void setProgress(float value) {
        int progress = (int) (value / mMultiplier);
        Log.d(TAG, "set value " + value + " -> " + progress);
        setProgress(progress);
    }
}
