package com.beatonma.ledcontrol.widget;

import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

/**
 * Created by Michael on 26/01/2016.
 */
public class WidgetViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.v("WidgetViewsService", "onGetViewFactory(intent)");
        return new GridProvider(getApplicationContext(), intent);
    }
}
