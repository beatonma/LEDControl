package com.beatonma.ledcontrol;

import android.graphics.Color;
import android.util.Log;

/**
 * Created by Michael on 20/01/2016.
 */
public class AppContainer {
    private final static String TAG = "AppContainer";

    private final static String SEPARATOR = "_;_";
    private final static int FIELDS = 5;
    public final static int DEFAULT_COLOR = Color.GRAY;

    private String mFriendlyName;
    private String mPackageName;
    private String mActivityName;
    private int mColor;
    private boolean mChecked;

    public AppContainer() {
        mFriendlyName = "";
        mPackageName = "";
        mActivityName = "";
        mColor = DEFAULT_COLOR;
        mChecked = false;
    }

    public AppContainer(String asString) {
        String[] parts = asString.split(SEPARATOR);
        if (parts.length == FIELDS) {
            mPackageName = parts[0];
            mActivityName = parts[1];
            mFriendlyName = parts[2];

            try {
                mColor = Integer.valueOf(parts[3]);
            }
            catch (NumberFormatException e) {
                Log.e(TAG, String.format("Error parsing color from string \"%s\":", asString) + e.toString());
            }

            mChecked = Boolean.valueOf(parts[4]);
        }
    }

    public String getFriendlyName() {
        return mFriendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        mFriendlyName = friendlyName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    public String getActivityName() {
        return mActivityName;
    }

    public void setActivityName(String activityName) {
        mActivityName = activityName;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }

    @Override
    public String toString() {
        return mPackageName + SEPARATOR + mActivityName + SEPARATOR + mFriendlyName + SEPARATOR + mColor + SEPARATOR + mChecked;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof AppContainer)
                && this.getPackageName().equals(((AppContainer) other).getPackageName());
    }
}
