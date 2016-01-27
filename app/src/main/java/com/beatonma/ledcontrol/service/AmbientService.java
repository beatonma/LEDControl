package com.beatonma.ledcontrol.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;

import com.beatonma.ledcontrol.Broadcaster;
import com.beatonma.self.led.ledcontrol.R;
import com.beatonma.ledcontrol.utility.AnimationUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Michael on 18/01/2016.
 */
public class AmbientService extends Service {
    private final static String TAG = "AmbientService";

    public final static String STOP_SERVICE = "com.beatonma.self.led.ledcontrol.STOP_SERVICE";
    private final static IntentFilter INTENT_FILTER;

    static {
        INTENT_FILTER = new IntentFilter();
        INTENT_FILTER.addAction(STOP_SERVICE);
    }

    private final static int NOTIFICATION_ID = 65;
    private final static int NOTIFICATION_STOP_SERVICE = 93;

    private final static String IMAGE_FILENAME = "ambient_screenshot.jpg";
    private final static int UPDATE_INTERVAL = 1000;

    private long mMergeStartTime = -1;
    private boolean mMergeColors = true;
    private int mMergeDuration = 800;
    private float mMergeProgress = 0f;
    private int mCurrentColor;
    private int mTargetColor;

    private Handler mHandler;
    private final Runnable mUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateColorsPalette();
        }
    };
    private final Runnable mMergeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMergeStartTime < 0) {
                mMergeStartTime = System.currentTimeMillis();
                broadcast(mCurrentColor);
            }
            else {
                long now = System.currentTimeMillis();
                mMergeProgress = (float) (now - mMergeStartTime) / (float) mMergeDuration;
                if (mCurrentColor != mTargetColor) {
                    Log.d(TAG, "Merging from " + getColorString(mCurrentColor)
                            + " to " + getColorString(mTargetColor)
                            + ". Currently: " + getColorString(AnimationUtils.morphColors(mCurrentColor, mTargetColor, mMergeProgress)));
                }
                if (mMergeProgress >= 1f) {
                    mMergeStartTime = -1;
                    mMergeProgress = 0f;
                    mCurrentColor = mTargetColor;
                    Log.d(TAG, "Color merge complete");
                    return;
                }
                else {
                    broadcast(AnimationUtils.morphColors(mCurrentColor, mTargetColor, mMergeProgress));
                }
            }

            mHandler.postDelayed(mMergeRunnable, 100);
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(STOP_SERVICE)) {
                stopSelf();
            }
        }
    };

    private String mImagePath = "";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "AmbientService starting.");

        registerReceiver(mReceiver, INTENT_FILTER);
        mHandler = new Handler(getMainLooper());

        mCurrentColor = Color.BLACK;
        mTargetColor = Color.WHITE;

        updateColorsPalette();

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, buildNotification());
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "AmbientService shutting down.");

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);

        unregisterReceiver(mReceiver);
        mHandler.removeCallbacks(mUpdateRunnable);
        mHandler.removeCallbacks(mMergeRunnable);
        super.onDestroy();
    }

    private String getColorString(int color) {
        return String.format("[%d, %d, %d]", Color.red(color), Color.green(color), Color.blue(color));
    }

    private void broadcast(int color) {
        Broadcaster.getInstance(this).broadcastColor(color);
    }

    public void updateColorsPalette() {
        long startTime = System.currentTimeMillis();
        getScreenshot();
        Palette palette = getImagePalette();
        if (palette != null) {
            ArrayList<Palette.Swatch> swatches = new ArrayList<>();
            swatches.addAll(palette.getSwatches());
            Collections.sort(swatches, new SwatchComparator());
            Palette.Swatch mostPopulousSwatch = swatches.get(0);
            int color = getSaturatedColor(mostPopulousSwatch.getRgb());
//            int color = getCanonicalColor(categoriseSwatch(mostPopulousSwatch));
            Log.d(TAG, String.format("Broadcasting color: " + color + " (r:%d, g:%d, b:%d)", Color.red(color), Color.green(color), Color.blue(color)));

            if (mMergeColors) {
                mTargetColor = color;
                mHandler.post(mMergeRunnable);
            }
            else {
                Broadcaster.getInstance(this).broadcastColor(color);
            }
        }
        Log.d(TAG, "Update took " + (System.currentTimeMillis() - startTime) + "ms");
        mHandler.postDelayed(mUpdateRunnable, UPDATE_INTERVAL);
    }

    private void getScreenshot() {
        try {
            Process sh = Runtime.getRuntime().exec("su", null, null);

            OutputStream os = sh.getOutputStream();
            os.write(("/system/bin/screencap -p " + getImagePath() + "| chmod 777 " + getImagePath()).getBytes("ASCII"));
            os.flush();

            os.close();
            sh.waitFor();
        }
        catch (IOException e) {
            Log.e(TAG, "IO exception: " + e.toString());
        }
        catch (InterruptedException e) {
            Log.e(TAG, "Interrupted exception: " + e.toString());
        }
        catch (Exception e) {
            Log.e(TAG, "Error: " + e.toString());
        }
    }

    private Palette getImagePalette() {
        Bitmap bitmap = BitmapFactory.decodeFile(getImagePath());

        if (bitmap != null) {
            Palette palette = Palette.from(bitmap).generate();
            bitmap.recycle();
            return palette;
        }
        else {
            Log.e(TAG, "Error getting image palette - bitmap is null");
            return null;
        }
    }

    private String getImagePath() {
        if (mImagePath.equals("")) {
            mImagePath = getCacheDir() + File.separator + IMAGE_FILENAME;
        }
        return mImagePath;
    }

    private int getSaturatedColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = 1f;
        hsv[2] = 1f;

        return Color.HSVToColor( hsv );
    }

    private Notification buildNotification() {
        Intent stopIntent = new Intent(STOP_SERVICE);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, NOTIFICATION_STOP_SERVICE, stopIntent, 0);

        Notification n = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("LED Control")
                .setContentText("Service is active")
                .setShowWhen(true)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setWhen(System.currentTimeMillis())
                .addAction(R.drawable.ic_launcher, "Stop", pIntent)
                .build();
        return n;
    }

    private class SwatchComparator implements Comparator<Palette.Swatch> {
        @Override
        public int compare(Palette.Swatch lhs, Palette.Swatch rhs) {
            int leftPopulation = lhs.getPopulation();
            int rightPopulation = rhs.getPopulation();

            if (leftPopulation > rightPopulation) {
                return -1;
            }
            else if (leftPopulation < rightPopulation) {
                return 1;
            }
            else {
                return 0;
            }
        }
    }
}
