package com.beatonma.ledcontrol.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.beatonma.ledcontrol.Broadcaster;

/**
 * Created by Michael on 26/01/2016.
 */
public class IntentHandlingService extends Service {
    private final static String TAG = "IntentHandler";

    public final static String SET_COLOR = "get_color";
    public final static String EXTRA_COLOR = "color";

    private final static int INVALID_COLOR = -192837465;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (intent != null) {
            Log.d(TAG, "onStartCommand: " + intent.getAction());
            setColor(intent);
        }

        stopSelf();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setColor(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            int color = extras.getInt(EXTRA_COLOR, INVALID_COLOR);
            Log.d(TAG, "Broadcasting color: " + color);
            if (color != INVALID_COLOR) {
                Broadcaster.getInstance(this).broadcastColor(color);
            }
        }
    }
}
