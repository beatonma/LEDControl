package com.beatonma.ledcontrol.utility;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Created by Michael on 28/05/2015.
 */
public class Utils {
	private final static String TAG = "Utils";

	private final static String RIPPLE_COLOR = "#21000000";

	public static boolean isKitkat() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
	}

	public static boolean isLollipop() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
	}

	public static Bitmap drawableToBitmap (Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable)drawable).getBitmap();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	public static int dpToPx(Context context, int dp) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
		return px;
	}

	public static int pxToDp(Context context, int px) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
		return dp;
	}

	public static int getScreenWidth(Context context) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		return dm.widthPixels;
	}

	public static int getScreenHeight(Context context) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		return dm.heightPixels;
	}

	// generate ripple drawable and set as view background
	public static void setBackground(View v, int color, int highlight) {
		if (highlight == -1) {
			highlight = Color.parseColor(RIPPLE_COLOR);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			int[][] states = new int[][]{
					new int[]{android.R.attr.state_enabled},
					new int[]{android.R.attr.state_checked},
					new int[]{android.R.attr.state_pressed},
			};
			int [] colors = new int[] {
					highlight,
					highlight,
					highlight
			};
			ColorStateList stateList = new ColorStateList(states, colors);

			RippleDrawable ripple = new RippleDrawable(stateList, new ColorDrawable(color), null);
			v.setBackground(ripple);
		}
		else {
			StateListDrawable states = new StateListDrawable();
			states.addState(new int[] {android.R.attr.state_pressed}, new ColorDrawable(highlight));
			states.addState(new int[] {}, new ColorDrawable(color));
			v.setBackground(states);
		}
	}

	public static void setBackground(View v, int color) {
		float[] hsv = new float[3];

		Color.colorToHSV(color, hsv);
		hsv[0] = (hsv[0] + 50) % 360; // hue
		hsv[1] += hsv[1] < 0.5f ? 0.2f : -0.2f; // saturation
		hsv[2] += hsv[2] < 0.5f ? 0.3f : -0.3f; // value/brightness

		setBackground(v, color, Color.HSVToColor(hsv));
	}

	public static ColorStateList getSimpleSelector(int color, int highlight) {
		int[][] states = new int[][]{
				new int[]{
//						android.R.attr.state_enabled,
						android.R.attr.state_checked,
//						android.R.attr.state_pressed,
//						android.R.attr.state_activated
				},
				new int[]{}
		};
		int [] colors = new int[] {
				highlight,
				color
		};
		return new ColorStateList(states, colors);
	}

	public static Point getCenter(View v) {
		Point point = new Point();
		point.x = (int) (v.getX() + (v.getMeasuredWidth() / 2));
		point.y = (int) (v.getY() + (v.getMeasuredHeight() / 2));
		return point;
	}
}
