package com.beatonma.ledcontrol.app.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;

import com.beatonma.self.led.ledcontrol.R;
import com.beatonma.ledcontrol.utility.PrefUtils;

/**
 * Created by Michael on 25/01/2016.
 */
public class EditTextPreference extends Preference {
    AppCompatEditText mTextField;

    public EditTextPreference(Context context) {
        super(context);
    }

    public EditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.init(context, attrs, defStyleAttr, defStyleRes);

        View v = inflate(context, R.layout.view_preference_text, this);
        mTextField = (AppCompatEditText) v.findViewById(R.id.edit);

        TypedArray a = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.EditTextPreference, defStyleAttr, defStyleRes);

        try {
            int ref = a.getResourceId(R.styleable.EditTextPreference_hint, 0);
            if (ref != 0) {
                mTextField.setHint(getResources().getString(ref));
            }
        }
        finally {
            a.recycle();
        }

        String savedText = PrefUtils.get(getContext()).getString(PrefUtils.PREF_REMOTE_ADDRESS, "");
        mTextField.setText(savedText);

        mTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                PrefUtils.get(getContext()).edit()
                        .putString(PrefUtils.PREF_REMOTE_ADDRESS, s.toString())
                        .commit();
            }
        });
    }
}
