package com.beatonma.ledcontrol.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.beatonma.ledcontrol.service.IntentHandlingService;
import com.beatonma.self.led.ledcontrol.R;

/**
 * Created by Michael on 26/01/2016.
 */
public class GridProvider implements RemoteViewsService.RemoteViewsFactory {
    private final static String TAG = "GridProvider";

    private Context mContext;

    private final static int[] COLORS = new int[] {
            Color.BLACK,
            Color.WHITE,
            Color.RED,
            Color.YELLOW,
            Color.GREEN,
            Color.CYAN,
            Color.BLUE,
            Color.MAGENTA
    };
    private int[] mButtonColors;

    public GridProvider(Context context, Intent intent) {
        mContext = context;
        Resources resources = mContext.getResources();
        mButtonColors = new int[] {
                resources.getColor(R.color.White),
                resources.getColor(R.color.Black),
                resources.getColor(R.color.Red500),
                resources.getColor(R.color.Yellow500),
                resources.getColor(R.color.Green500),
                resources.getColor(R.color.Cyan500),
                resources.getColor(R.color.Blue500),
                resources.getColor(R.color.Purple500),
        };
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return COLORS.length;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
        views.setInt(R.id.button, "setBackgroundColor", mButtonColors[position]);
        Intent intent = new Intent();
        intent.setAction(IntentHandlingService.SET_COLOR);
        intent.putExtra(IntentHandlingService.EXTRA_COLOR, COLORS[position]);
        views.setOnClickFillInIntent(R.id.button, intent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
