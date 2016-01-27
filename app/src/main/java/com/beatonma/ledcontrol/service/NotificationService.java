package com.beatonma.ledcontrol.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.beatonma.ledcontrol.AppContainer;
import com.beatonma.ledcontrol.Broadcaster;
import com.beatonma.ledcontrol.app.DbHelper;
import com.beatonma.ledcontrol.utility.PrefUtils;

/**
 * Created by Michael on 20/01/2016.
 */
public class NotificationService extends NotificationListenerService {
    private final static String TAG = "NotifyListener";

    public final static String START_SERVICE = "start_service";
    public final static String STOP_SERVICE = "stop_service";
    private final static IntentFilter INTENT_FILTER;

    static {
        INTENT_FILTER = new IntentFilter();
        INTENT_FILTER.addAction(START_SERVICE);
        INTENT_FILTER.addAction(STOP_SERVICE);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case STOP_SERVICE:
                    clearNotifications();
                    break;
                case START_SERVICE:
                    onListenerConnected();
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(mReceiver, INTENT_FILTER);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private boolean isEnabled() {
        return PrefUtils.get(this).getBoolean(PrefUtils.PREF_NOTIFICATION_ENABLE, false);
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();

        clearNotifications();

        if (isEnabled()) {
            for (StatusBarNotification sbn : getActiveNotifications()) {
                addNotification(sbn);
            }
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        if (isEnabled()) {
            addNotification(sbn);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        if (isEnabled()) {
            removeNotification(sbn);
        }
    }

    public void addNotification(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();

        AppContainer app = DbHelper.getInstance(this).findSubscription(packageName);
        if (app != null) {
            Broadcaster.getInstance(this).broadcastNewNotification(app);
        }
        else {
            Log.v(TAG, "User is not subscribed to " + sbn.getPackageName());
        }
    }

    public void removeNotification(StatusBarNotification sbn) {
        Broadcaster.getInstance(this).broadcastRemovedNotification(sbn.getPackageName());
    }

    public void clearNotifications() {
        Broadcaster.getInstance(this).broadcastClearNotifications();
    }
}
