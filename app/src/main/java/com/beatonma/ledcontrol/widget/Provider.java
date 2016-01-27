package com.beatonma.ledcontrol.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.beatonma.ledcontrol.service.IntentHandlingService;
import com.beatonma.self.led.ledcontrol.R;

/**
 * Created by Michael on 26/01/2016.
 */
public class Provider extends AppWidgetProvider {
    private final static String TAG = "WidgetProvider";
    private final static int REQUEST_CODE = 65;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "Updating widgets");
        for (int i = 0; i < appWidgetIds.length; i++) {
            updateWidget(context, appWidgetManager, appWidgetIds[i]);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetID) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_main);

        initList(context, views);
        setOnTouch(context, views);

        appWidgetManager.updateAppWidget(widgetID, views);
    }

    private void initList(Context context, RemoteViews views) {
        Intent gridIntent = new Intent(context, WidgetViewsService.class);
        views.setRemoteAdapter(R.id.widget_grid_view, gridIntent);

        Intent itemIntent = new Intent(context, IntentHandlingService.class);
        itemIntent.setAction(IntentHandlingService.SET_COLOR);
        PendingIntent pi = PendingIntent.getService(context, REQUEST_CODE, itemIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_grid_view, pi);
    }

    private void setOnTouch(Context context, RemoteViews views) {
        Intent intent = new Intent(context, QuickActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.top_level_container, pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }
}
