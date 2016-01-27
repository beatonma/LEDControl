package com.beatonma.colorpicker;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.beatonma.ledcontrol.Broadcaster;
import com.beatonma.self.led.ledcontrol.R;
import com.beatonma.ledcontrol.utility.AnimationUtils;
import com.beatonma.ledcontrol.utility.Utils;

/**
 * Created by Michael on 08/11/2015.
 * Very quickly/roughly cut down in Jan 2016 for LED Control - removed some stuff for
 * simplification but that may have left some ugly artifacts in the code.
 *
 */
public class ColorPickerFragment extends Fragment {
	private final static String TAG = "ColorPickerFragment";

	public final static String KEY = "key";
	public final static String ACCENT_COLOR = "accent_color";

	private Button mCustomOkButton;

	private View mCustomContainer;
	private View mCustomPreview;
	private ColorSeekbarView mRedSlider;
	private ColorSeekbarView mGreenSlider;
	private ColorSeekbarView mBlueSlider;
	private ColorSeekbarView mBrightnessSlider;
	private AppCompatEditText mCustomHexEdit;

	private int mAccentColor;

	private int mCustomRed = 0;
	private int mCustomGreen = 0;
	private int mCustomBlue = 0;

	private OnColorPickedListener mListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			mAccentColor = args.getInt(ACCENT_COLOR);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.view_color_picker, parent, false);

		initLayout(v);

		return v;
	}

	public static ColorPickerFragment newInstance(String key, int accentColor) {
		ColorPickerFragment fragment = new ColorPickerFragment();
		Bundle args = new Bundle();
		args.putString(KEY, key);
		args.putInt(ACCENT_COLOR, accentColor);
		fragment.setArguments(args);
		return fragment;
	}

	public void close() {
		getActivity().getSupportFragmentManager().popBackStack();
	}

	protected void initLayout(View v) {
		Log.d(TAG, "Initiating fragment layout");

        View background = v.findViewById(R.id.overlay);
        AnimationUtils.fadeIn(background);
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });

		mCustomContainer = v.findViewById(R.id.custom_color_container);
		mCustomPreview = v.findViewById(R.id.custom_preview);

		mCustomOkButton = (Button) v.findViewById(R.id.custom_ok);
		mCustomOkButton.setTextColor(mAccentColor);
		mCustomOkButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveCustomColor();
				close();
			}
		});

		mRedSlider = (ColorSeekbarView) v.findViewById(R.id.red_slider);
		mGreenSlider = (ColorSeekbarView) v.findViewById(R.id.green_slider);
		mBlueSlider = (ColorSeekbarView) v.findViewById(R.id.blue_slider);
        mBrightnessSlider = (ColorSeekbarView) v.findViewById(R.id.brightness_slider);

		ColorSeekbarView.OnValueChangedListener listener = new ColorSeekbarView.OnValueChangedListener() {
			@Override
			public void onValueChanged(int channel, int value) {
				switch (channel) {
					case ColorSeekbarView.CHANNEL_RED:
						mCustomRed = value;
						break;
					case ColorSeekbarView.CHANNEL_GREEN:
						mCustomGreen = value;
						break;
					case ColorSeekbarView.CHANNEL_BLUE:
						mCustomBlue = value;
						break;
                    case ColorSeekbarView.CHANNEL_BRIGHTNESS:
                        changeBrightness(value);
                        return;
				}

				updateCustomColorViews();
			}
		};

		mRedSlider.setListener(listener);
		mGreenSlider.setListener(listener);
		mBlueSlider.setListener(listener);
        mBrightnessSlider.setListener(listener);

		mCustomHexEdit = (AppCompatEditText) v.findViewById(R.id.custom_hex);
		mCustomHexEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				String text = s.toString();
				int length = s.length();
				if (!text.matches("[a-fA-F0-9]+") && length > 0) {
					s.delete(length - 1, length);
				}

				if (s.length() == 6) {
					updateCustomColorViews(s.toString());
				}
			}
		});
	}

	public void setColorPickerListener(OnColorPickedListener listener) {
		mListener = listener;
	}

	private void notifyListener(int color) {
		if (mListener != null) {
			mListener.onColorPicked(color);
		}
	}

    private void changeBrightness(int brightness) {
        float[] hsv = new float[3];
        int color = Color.rgb(mCustomRed, mCustomGreen, mCustomBlue);
        Color.colorToHSV(color, hsv);

        hsv[2] = (float) brightness / 255f;

        color = Color.HSVToColor(hsv);

        mCustomRed = Color.red(color);
        mCustomGreen = Color.green(color);
        mCustomBlue = Color.blue(color);

        updateCustomColorViews(color);
    }

	private void updateCustomColorViews(int color) {
		mCustomRed = Color.red(color);
		mCustomGreen = Color.green(color);
		mCustomBlue = Color.blue(color);

		mRedSlider.setValue(mCustomRed);
		mGreenSlider.setValue(mCustomGreen);
		mBlueSlider.setValue(mCustomBlue);

		mCustomHexEdit.setText(ColorUtils.toHex(color));

		broadcastColorChange(Color.rgb(mCustomRed, mCustomGreen, mCustomBlue));
	}

	private void updateCustomColorViews(String hex) {
		int color = Color.parseColor("#" + hex);

		mCustomRed = Color.red(color);
		mCustomGreen = Color.green(color);
		mCustomBlue = Color.blue(color);

		mRedSlider.setValue(mCustomRed);
		mGreenSlider.setValue(mCustomGreen);
		mBlueSlider.setValue(mCustomBlue);

		broadcastColorChange(color);
	}

	private void updateCustomColorViews() {
		int customColor = Color.rgb(mCustomRed, mCustomGreen, mCustomBlue);
		mCustomPreview.setBackgroundColor(customColor);

		if (Utils.isLollipop()) {
			mCustomHexEdit.setSupportBackgroundTintList(ColorStateList.valueOf(customColor));
		}
		else {
			mCustomHexEdit.setSupportBackgroundTintList(Utils.getSimpleSelector(customColor, customColor));
		}

		mCustomHexEdit.setText(ColorUtils.toHex(customColor));

		broadcastColorChange(customColor);
	}

	private void saveCustomColor() {
		int color = Color.rgb(mCustomRed, mCustomGreen, mCustomBlue);
		broadcastColorChange(color);
		notifyListener(color);
	}

	private void broadcastColorChange(int color) {
		Broadcaster.getInstance(getActivity()).broadcastColor(color);
	}

	@Override
	public void onPause() {
		Broadcaster.finish();
		super.onPause();
	}

	public interface OnColorPickedListener {
		void onColorPicked(int color);
	}
}
