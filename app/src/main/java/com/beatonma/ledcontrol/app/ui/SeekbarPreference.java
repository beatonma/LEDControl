package com.beatonma.ledcontrol.app.ui;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.SeekBar;
import android.widget.TextView;

import com.beatonma.colorpicker.ColorUtils;
import com.beatonma.self.led.ledcontrol.R;
import com.beatonma.ledcontrol.utility.AnimationUtils;
import com.beatonma.ledcontrol.utility.PrefUtils;
import com.beatonma.ledcontrol.utility.Utils;

/**
 * Created by Michael on 18/11/2015.
 */
public class SeekbarPreference extends Preference {
    protected final static String TAG = "SeekbarPreference";

	protected TextView mTitle;
    protected TextView mLeftText;
    protected AppCompatSeekBar mSlider;
    protected TextView mValue;
    protected View mCenterMarker;

    protected boolean mUseCustomListener = false;

    protected OnSeekbarMovedListener mListener;

	public SeekbarPreference(Context context) {
		super(context);
	}

	public SeekbarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SeekbarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public SeekbarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
		}
		finally {
			a.recycle();
		}

		initColor();

        mSlider.setMax(maxValue);

        int initProgress;
        try {
            initProgress = PrefUtils.get(getContext()).getInt(mKey, defaultProgress);
        }
        catch (Exception e) {
            initProgress = (int) PrefUtils.get(getContext()).getFloat(mKey, defaultProgress);
        }

		setProgress(initProgress);
		setValue(String.valueOf(initProgress));

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

				mValue.setText(String.valueOf(progress));
				PrefUtils.get(getContext()).edit()
						.putInt(mKey, progress)
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

	public void initColor() {
		setColor(getResources().getColor(R.color.Accent));
	}

	@SuppressWarnings("NewApi")
	public void setColor(int color) {
		if (Utils.isLollipop()) {
			mSlider.setThumbTintList(ColorStateList.valueOf(color));
			mSlider.setProgressTintList(ColorStateList.valueOf(color));
		}
		else {
			Drawable d = mSlider.getThumb();
			d.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
			mSlider.setThumb(d);

			d = mSlider.getProgressDrawable();
			d.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
			mSlider.setProgressDrawable(d);
		}

		mCenterMarker.setBackgroundColor(ColorUtils.lighten(color, 0.2f));
	}

	@Override
	public void setTitle(String title) {
		mTitle.setText(title);
	}

	public void setOnSeekbarMovedListener(OnSeekbarMovedListener listener) {
		mListener = listener;
	}

	public void setProgress(int progress) {
		mSlider.setProgress(progress);
	}

	public void setValue(String value) {
		mValue.setText(value);
	}

    public int getProgress() {
        return mSlider.getProgress();
    }

	public int getTitleWidth() {
		return mTitle.getMeasuredWidth();
	}

	// Use to align with other seekbars in layout, if necessary
	public void setTitleWidth(final int targetWidth) {
		final int startWidth = mTitle.getMeasuredWidth();

		ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.setDuration(AnimationUtils.ANIMATION_DURATION);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float fraction = animation.getAnimatedFraction();
				ViewGroup.LayoutParams lp = mTitle.getLayoutParams();
				lp.width = (int) (startWidth + ((targetWidth - startWidth) * fraction));
				mTitle.setLayoutParams(lp);
			}
		});
		animator.start();
	}

	public interface OnSeekbarMovedListener {
		void onSeekbarMoved(int progress);
	}
}
