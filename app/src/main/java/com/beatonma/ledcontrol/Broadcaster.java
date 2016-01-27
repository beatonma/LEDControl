package com.beatonma.ledcontrol;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.beatonma.ledcontrol.utility.PrefUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michael on 27/12/2015.
 */
public class Broadcaster {
	private final static String TAG = "Broadcast";
    private final static String LED_POST_PAGE = "post.php";
    private final static String LED_PREFS_PAGE = "prefs";

	private final static String ADD_NOTIFICATION = "add_notification";
	private final static String REMOVE_NOTIFICATION = "remove_notification";
	private final static String CLEAR_NOTIFICATIONS = "clear_notifications";
	private final static String RGB = "rgb";
    private final static String SET_PREFERENCES = "set_preferences";

	private static Broadcaster mInstance;
	private OnPostResponseListener mPostListener;
	private OnGetResponseListener mGetListener;
	private RequestQueue mRequestQueue;

    private String mLedDirectory = "";

	private Broadcaster(Context context) {
		mRequestQueue = Volley.newRequestQueue(context);
	}

	public static Broadcaster getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new Broadcaster(context);
		}

		mInstance.setPostListener(null);
		mInstance.setGetListener(null);

		mInstance.init(context);

		return mInstance;
	}

	public static Broadcaster getInstance(Context context, OnPostResponseListener listener) {
		if (mInstance == null) {
			mInstance = new Broadcaster(context);
		}

		mInstance.setPostListener(listener);

		mInstance.init(context);

		return mInstance;
	}

	public static Broadcaster getInstance(Context context, OnGetResponseListener listener) {
		if (mInstance == null) {
			mInstance = new Broadcaster(context);
		}

		mInstance.setGetListener(listener);

		mInstance.init(context);

		return mInstance;
	}

	private void init(Context context) {
		String fullAddress = PrefUtils.get(context).getString(PrefUtils.PREF_REMOTE_ADDRESS, "");
		if (fullAddress.contains("/post.php")) {
            mLedDirectory = fullAddress.replace("post.php", "");
            if (!mLedDirectory.startsWith("http://")) {
                mLedDirectory = "http://" + mLedDirectory;
            }
		}
	}

	public void broadcastColor(int color) {
		int red = Color.red(color);
		int green = Color.green(color);
		int blue = Color.blue(color);

        Map<String, String> params = new HashMap<>();
        params.put(RGB, red + " " + green + " " + blue);

        mRequestQueue.add(buildRequest("Set RGB", params));
	}

	public void broadcastColor(int red, int green, int blue) {
		broadcastColor(Color.rgb(red, green, blue));
	}

	public void broadcastColor(float[] hsv) {
		broadcastColor(Color.HSVToColor(hsv));
	}

	public static void finish() {
		if (mInstance != null) {
			mInstance = null;
		}
	}

	public void broadcastNewNotification(AppContainer app) {
		final String packageName = app.getPackageName();

		int color = app.getColor();

		int red = Color.red(color);
		int green = Color.green(color);
		int blue = Color.blue(color);

        Map<String, String> params = new HashMap<>();
        params.put(ADD_NOTIFICATION, packageName);
        params.put(RGB, red + " " + green + " " + blue);

        mRequestQueue.add(buildRequest("Add Notification", params));
	}

	public void broadcastRemovedNotification(final String packageName) {
        Map<String, String> params = new HashMap<>();
        params.put(REMOVE_NOTIFICATION, packageName);

        mRequestQueue.add(buildRequest("Remove Notification", params));
	}

	public void broadcastClearNotifications() {
        Map<String, String> params = new HashMap<>();
        params.put(CLEAR_NOTIFICATIONS, CLEAR_NOTIFICATIONS);
        mRequestQueue.add(buildRequest("Clear Notifications", params));
	}

    public void broadcastPreferences(String prefs) {
        Map<String, String> params = new HashMap<>();
        params.put(SET_PREFERENCES, prefs);
        mRequestQueue.add(buildRequest("Preferences", params));
    }

	public void getRemotePreferences() {
		StringRequest request = new StringRequest(Request.Method.GET,
				mLedDirectory + LED_PREFS_PAGE,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						notifyListener(response);
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "GET ERROR (Preferences): " + error);
					}
				});

		mRequestQueue.add(request);
	}

    private StringRequest buildRequest(final String broadcastName, final Map<String, String> params) {
        return new StringRequest(
                Request.Method.POST,
				mLedDirectory + LED_POST_PAGE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v(TAG, "RESPONSE (" + broadcastName + "): " + response);
                        notifyListener(true);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "ERROR (" + broadcastName + "): " + error);
                        notifyListener(false);
                    }}) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
    }

	private void notifyListener(boolean success) {
		if (mPostListener != null) {
			mPostListener.onResponse(success);
		}
	}

	private void notifyListener(String response) {
		if (mGetListener != null) {
			mGetListener.onResponse(response);
		}
	}

	public void setPostListener(OnPostResponseListener listener) {
		mPostListener = listener;
	}

	public void setGetListener(OnGetResponseListener listener) {
		mGetListener = listener;
	}

	public interface OnPostResponseListener {
		void onResponse(boolean success);
	}

	public interface OnGetResponseListener {
		void onResponse(String response);
	}

    public static void die() {
        mInstance = null;
    }
}
