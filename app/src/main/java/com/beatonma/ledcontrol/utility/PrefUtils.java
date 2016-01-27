package com.beatonma.ledcontrol.utility;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Michael on 03/06/2015.
 *
 * Convenience class to minimise need to look up preference names
 */
public class PrefUtils {
	private final static String TAG = "PrefUtils";

	public static boolean DEBUG = false;

	public final static String PREFS = "preferences";

    public final static String ONBOARD_FIRST_RUN = "onboard_first_run";
    public final static String ONBOARD_SHOW_ONBOARD_GENERAL = "onboard_show_onboard_general";
    public final static String ONBOARD_SHOW_ONBOARD_NOTIFICATIONS = "onboard_show_onboard_notifications";

	// General options
    public final static String PREF_REMOTE_ADDRESS = "pref_remote_address";
    public final static String PREF_REMOTE_DIRECTORY = "pref_remote_directory";
	public final static String PREF_INTERPOLATE_CHANGES = "pref_interpolate_color_changes";
	public final static String PREF_MAX_BRIGHTNESS = "pref_max_brightness";
	public final static String PREF_MIN_BRIGHTNESS = "pref_min_brightness";
	public final static String PREF_COLOR_CHANGE_DURATION = "pref_color_change_duration";
	public final static String PREF_NOTIFICATION_FREQUENCY = "pref_notification_frequency";
	public final static String PREF_NOTIFICATION_PULSE_DURATION = "pref_notification_pulse_duration";

	// Notification options
	public final static String PREF_NOTIFICATION_ENABLE = "pref_notifications_enable";


	public static SharedPreferences get(Context context) {
		return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
	}
}
