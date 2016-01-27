package com.beatonma.ledcontrol.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.beatonma.ledcontrol.AppContainer;

import java.util.ArrayList;

/**
 * Created by Michael on 20/01/2016.
 */
public class DbHelper extends SQLiteOpenHelper {
    private final static String TAG = "DbHelper";

    private final static int DB_VERSION = 1;

    private final static String DB_MAIN = "leds";
    private final static String TABLE_NOTIFICATIONS = "notifications";

    private final static String FIELD_PACKAGE_NAME = "package_name";
    private final static String FIELD_ACTIVITY_NAME = "activity_name";
    private final static String FIELD_FRIENDLY_NAME = "friendly_name";
    private final static String FIELD_COLOR = "color";
    private final static String FIELD_SELECTED = "selected";

    private final static int FALSE = 0;
    private final static int TRUE = 1;

    private static DbHelper mInstance;

    public static DbHelper getInstance(Context context) {
        mInstance = new DbHelper(context);
        return mInstance;
    }

    private DbHelper(Context context) {
        super(context, DB_MAIN, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createSubscriptionsTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, String.format("Upgrading database from version %d to %d. THIS WILL DESTROY OLD DATA", oldVersion, newVersion));
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);

        onCreate(db);
    }

    private void createSubscriptionsTable(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_NOTIFICATIONS
                        + " ("
                        + FIELD_PACKAGE_NAME + " TEXT NOT NULL PRIMARY KEY, "
                        + FIELD_ACTIVITY_NAME + " TEXT NOT NULL, "
                        + FIELD_FRIENDLY_NAME + " TEXT NOT NULL, "
                        + FIELD_COLOR + " INTEGER NOT NULL, "
                        + FIELD_SELECTED + " INTEGER NOT NULL"
                        + ");");
    }

    private void resetSubscriptions(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        createSubscriptionsTable(db);
    }

    public boolean updateSubscriptions(ArrayList<AppContainer> apps) {
        SQLiteDatabase db = getWritableDatabase();

        resetSubscriptions(db);

        boolean allSuccess = true;

        db.beginTransaction();
        for (AppContainer app : apps) {
            boolean success = updateSubscription(db, app);
            if (!success) {
                allSuccess = false;
            }
        }

        if (allSuccess) {
            db.setTransactionSuccessful();
            Log.v(TAG, String.format("Notification subscriptions updated successfully (%d items saved)", apps.size()));
        }
        else {
            Log.v(TAG, "Notification subscriptions could not be updated.");
        }

        db.endTransaction();
        db.close();

        return allSuccess;
    }

    public boolean updateSubscription(SQLiteDatabase db, AppContainer app) {
        ContentValues values = new ContentValues();
        values.put(FIELD_PACKAGE_NAME, app.getPackageName());
        values.put(FIELD_ACTIVITY_NAME, app.getActivityName());
        values.put(FIELD_FRIENDLY_NAME, app.getFriendlyName());
        values.put(FIELD_COLOR, app.getColor());
        values.put(FIELD_SELECTED, (app.isChecked() ? TRUE : FALSE));

        boolean success = db.insertWithOnConflict(TABLE_NOTIFICATIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1;

        return success;
    }

    public boolean updateSingleSubscription(AppContainer app) {
        ContentValues values = new ContentValues();
        values.put(FIELD_PACKAGE_NAME, app.getPackageName());
        values.put(FIELD_ACTIVITY_NAME, app.getActivityName());
        values.put(FIELD_FRIENDLY_NAME, app.getFriendlyName());
        values.put(FIELD_COLOR, app.getColor());
        values.put(FIELD_SELECTED, (app.isChecked() ? TRUE : FALSE));

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        boolean success = db.insertWithOnConflict(TABLE_NOTIFICATIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1;
        if (success) {
            db.setTransactionSuccessful();
        }

        db.endTransaction();
        db.close();

        return success;
    }

    public boolean removeSubscription(AppContainer app) {
        String packageName = app.getPackageName();
        ContentValues values = new ContentValues();
        values.put(FIELD_PACKAGE_NAME, packageName);

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        boolean success = db.delete(TABLE_NOTIFICATIONS, FIELD_PACKAGE_NAME + " = \'" + packageName + "\'", null) == 1;

        if (success) {
            db.setTransactionSuccessful();
            Log.v(TAG, String.format("Successfully removed notification %s", packageName));
        }
        else {
            Log.v(TAG, String.format("Could not remove notification %s", packageName));
        }
        db.endTransaction();
        db.close();

        return success;
    }

    public AppContainer findSubscription(String packageName) {
        AppContainer app = null;

        SQLiteDatabase db = getReadableDatabase();
        db.beginTransaction();

        String columns[] = new String[] { FIELD_PACKAGE_NAME, FIELD_ACTIVITY_NAME, FIELD_FRIENDLY_NAME, FIELD_COLOR, FIELD_SELECTED };

        Cursor cursor = db.query(TABLE_NOTIFICATIONS, columns, getWhereClause(FIELD_PACKAGE_NAME, packageName), null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            app = new AppContainer();
            app.setPackageName(cursor.getString(0));
            app.setActivityName(cursor.getString(1));
            app.setFriendlyName(cursor.getString(2));
            app.setColor(cursor.getInt(3));
            app.setChecked(cursor.getInt(4) == TRUE);
            cursor.close();
            db.setTransactionSuccessful();
        }
        db.endTransaction();
        db.close();

        return app;
    }

    public ArrayList<AppContainer> getSubscriptions() {
        ArrayList<AppContainer> subscriptions = new ArrayList<>();
        try {
            SQLiteDatabase db = getReadableDatabase();
            db.beginTransaction();

            String columns[] = new String[]{FIELD_PACKAGE_NAME, FIELD_ACTIVITY_NAME, FIELD_FRIENDLY_NAME, FIELD_COLOR, FIELD_SELECTED};

            Cursor cursor = db.query(TABLE_NOTIFICATIONS, columns, null, null, null, null, null);
            if (cursor != null) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    AppContainer app = new AppContainer();
                    app.setPackageName(cursor.getString(0));
                    app.setActivityName(cursor.getString(1));
                    app.setFriendlyName(cursor.getString(2));
                    app.setColor(cursor.getInt(3));
                    app.setChecked(cursor.getInt(4) == TRUE);

                    subscriptions.add(app);
                }
                cursor.close();
            }
            db.endTransaction();
        }
        catch (Exception e) {
            Log.e(TAG, "Error getting subscriptions: " + e.toString());
        }
        return subscriptions;
    }

    private String getWhereClause(String fieldName, String value) {
        return fieldName + " = \'" + value + "\'";
    }
}
