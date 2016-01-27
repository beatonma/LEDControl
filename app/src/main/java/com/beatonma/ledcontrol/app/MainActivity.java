package com.beatonma.ledcontrol.app;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.beatonma.ledcontrol.AppContainer;
import com.beatonma.ledcontrol.Broadcaster;
import com.beatonma.self.led.ledcontrol.R;
import com.beatonma.ledcontrol.app.ui.AppSelectFragment;
import com.beatonma.ledcontrol.utility.PrefUtils;
import com.beatonma.ledcontrol.utility.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Michael on 27/12/2015.
 */
public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private String[] PERMISSIONS = new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private final static int PERMISSIONS_REQUEST_CODE = 25;

    private final static int TAB_COLOR = 0;
    private final static int TAB_GENERAL = 1;
    private final static int TAB_NOTIFICATIONS = 2;
    private final static int TAB_AMBIENT = 3;
    private final static int TAB_HELP = 4;

    private Context mContext;

    private ViewPager mViewPager;
    private ViewPagerAdapter mAdapter;
    private FloatingActionButton mFab;

    private ArrayList<AppContainer> mInstalledApps;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        init();
	}

    private void init() {
        mContext = this;

        View appbar = findViewById(R.id.toolbar);
        appbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.notify(14, new Notification.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("LED Control Test")
                        .setContentText("Use this to test your setup.")
                        .setStyle(new Notification.BigTextStyle()
                                .bigText("Add LED Control to your monitored apps then tap the toolar to active this notification and test your setup!"))
                        .build());
            }
        });

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.hide();

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mAdapter = new ViewPagerAdapter();

        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(ViewPagerAdapter.TAB_COUNT);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == TAB_NOTIFICATIONS) {
                    mFab.show();
                    mFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AppSelectFragment fragment = AppSelectFragment.newInstance();
                            fragment.setAccentColor(getResources().getColor(R.color.Accent));
                            fragment.setEntries(mInstalledApps);
                            fragment.setOnItemsSavedListener(new AppSelectFragment.OnItemsSavedListener() {
                                @Override
                                public void onItemsSaved(ArrayList<AppContainer> items) {
                                    processSelectedApps(items);
                                }
                            });

                            String tag = getString(R.string.fragtag_applist);
                            getSupportFragmentManager().beginTransaction()
                                    .add(R.id.top_level_container, fragment, tag)
                                    .addToBackStack(tag)
                                    .commit();
                        }
                    });
                }
                else {
                    mFab.hide();
                    mFab.setOnClickListener(null);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(mViewPager);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabs.setTabTextColors(getResources().getColor(R.color.TextSecondaryLight), getResources().getColor(R.color.TextPrimaryLight));

        SharedPreferences sp = PrefUtils.get(this);
        boolean showSetupTab = sp.getBoolean(PrefUtils.ONBOARD_FIRST_RUN, true)
                || sp.getString(PrefUtils.PREF_REMOTE_ADDRESS, "").equals("");
        if (showSetupTab) {
            sp.edit()
                    .putBoolean(PrefUtils.ONBOARD_FIRST_RUN, false)
                    .commit();
            mViewPager.setCurrentItem(TAB_GENERAL);
        }

        loadInstalledAppsList();
        Broadcaster.getInstance(this, new Broadcaster.OnGetResponseListener() {
            @Override
            public void onResponse(String response) {
                parseRemotePreferences(response);
            }
        }).getRemotePreferences();
    }

    private void processSelectedApps(ArrayList<AppContainer> apps) {
        new IconExtractor(apps).execute();
    }

    private class IconExtractor extends AsyncTask<Void, Void, Void> {
        private ArrayList<AppContainer> mApps;

        public IconExtractor(ArrayList<AppContainer> apps) {
            mApps = apps;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ArrayList<AppContainer> subscriptions = DbHelper.getInstance(mContext).getSubscriptions();
            PackageManager pm = mContext.getPackageManager();
            int defaultColor = mContext.getResources().getColor(R.color.Accent);
            for (AppContainer app : mApps) {
                boolean alreadySubscribed = false;
                for (AppContainer sub : subscriptions) {
                    if (sub.equals(app)) {
                        app.setColor(sub.getColor());
                        alreadySubscribed = true;
                    }
                }
                if (!alreadySubscribed) {
                    try {
                        Bitmap icon = Utils.drawableToBitmap(pm.getApplicationIcon(app.getPackageName()));
                        if (icon != null) {
                            Palette palette = Palette.from(icon).generate();
                            int c = palette.getVibrantColor(
                                    palette.getLightVibrantColor(
                                            palette.getLightMutedColor(defaultColor)));
                            app.setColor(c);
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.e(TAG, String.format("Error getting color from icon for app %s:", app.getPackageName()) + e.toString());
                    }
                }
            }

            DbHelper.getInstance(mContext).updateSubscriptions(mApps);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateChosenSubscriptions(mApps);
        }
    }

    public void checkFilePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean hasAllNecessaryPermissions = true;
            for (int i = 0; i < PERMISSIONS.length; i++) {
                if (checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {
                    hasAllNecessaryPermissions = false;
                }
            }

            if (!hasAllNecessaryPermissions) {
                Log.d(TAG, "Requesting permissions");
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                boolean granted = true;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        granted = false;
                    }
                }
                if (granted) {
                    View container = findViewById(R.id.top_level_container);
                    Snackbar.make(container, "File access granted successfully", Snackbar.LENGTH_LONG).show();
                }
                else {
                    Log.e(TAG, "Permissions denied!");
                }
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Broadcaster.die();
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

    private void parseRemotePreferences(String response) {
        String[] lines = response.split("\n");

        boolean interpolateColorChanges = false;
        int maxBrightness = 100;
        int minBrightness = 0;
        float colorChangeDuration = 1.0f;

        for (String line : lines) {
            if (!line.contains("=")) {
                // Skip any invalid lines
                continue;
            }

            if (line.contains(PrefUtils.PREF_INTERPOLATE_CHANGES)) {
                interpolateColorChanges = Boolean.valueOf(line.split("=")[1]);
            }
            else if (line.contains(PrefUtils.PREF_MAX_BRIGHTNESS)) {
                maxBrightness = Integer.valueOf(line.split("=")[1]);
            }
            else if (line.contains(PrefUtils.PREF_MIN_BRIGHTNESS)) {
                minBrightness = Integer.valueOf(line.split("=")[1]);
            }
            else if (line.contains(PrefUtils.PREF_COLOR_CHANGE_DURATION)) {
                colorChangeDuration = Float.valueOf(line.split("=")[1]);
            }
        }

        PrefUtils.get(this).edit()
                .putBoolean(PrefUtils.PREF_INTERPOLATE_CHANGES, interpolateColorChanges)
                .putInt(PrefUtils.PREF_MAX_BRIGHTNESS, maxBrightness)
                .putInt(PrefUtils.PREF_MIN_BRIGHTNESS, minBrightness)
                .putFloat(PrefUtils.PREF_COLOR_CHANGE_DURATION, colorChangeDuration)
                .commit();

        Log.d(TAG, String.format("Remote preferences loaded:\n%b\n%d\n%d\n%f", interpolateColorChanges, maxBrightness, minBrightness, colorChangeDuration));
    }

    public void showLoading() {

    }

    public void hideLoading() {

    }

    public void showFab() {
        mFab.show();
    }

    public void hideFab() {
        mFab.hide();
    }

    public void showHelp() {
        mViewPager.setCurrentItem(TAB_HELP);
    }

    public void showConnectionSuccess(final boolean success) {
        ImageView marker = (ImageView) findViewById(R.id.connection_status_marker);
        int resId = success ? R.drawable.ic_signal_wifi_4_bar_black_24dp : R.drawable.ic_signal_wifi_off_black_24dp;
        Drawable icon = getResources().getDrawable(resId);
        icon.mutate().setColorFilter((success ? Color.WHITE : Color.RED), PorterDuff.Mode.SRC_IN);
        marker.setImageDrawable(icon);

        marker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(findViewById(R.id.top_level_container), "Connection " + (success ? "successful" : "error."), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    public void updateChosenSubscriptions(ArrayList<AppContainer> apps) {
        NotificationsSettingsFragment fragment = (NotificationsSettingsFragment) mAdapter.get(TAB_NOTIFICATIONS);
        if (fragment != null) {
            fragment.setSubscriptionDataset(apps);
        }
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private String[] mTitles;
        private BaseSettingsFragment[] mFragments;
        public final static int TAB_COUNT = 5;

        public ViewPagerAdapter() {
            super(getSupportFragmentManager());
            mTitles = getResources().getStringArray(R.array.tab_titles);
            mFragments = new BaseSettingsFragment[TAB_COUNT];
        }

        @Override
        public Fragment getItem(int position) {
            BaseSettingsFragment fragment;

            switch (position) {
                case TAB_COLOR:
                    fragment = ColorSettingsFragment.newInstance();
                    mFragments[TAB_COLOR] = fragment;
                    break;
                case TAB_GENERAL:
                    fragment = GeneralSettingsFragment.newInstance();
                    mFragments[TAB_GENERAL] = fragment;
                    break;
                case TAB_NOTIFICATIONS:
                    fragment = NotificationsSettingsFragment.newInstance();
                    mFragments[TAB_NOTIFICATIONS] = fragment;
                    break;
                case TAB_AMBIENT:
                    fragment = AmbientSettingsFragment.newInstance();
                    mFragments[TAB_AMBIENT] = fragment;
                    break;
                case TAB_HELP:
                    fragment = HelpFragment.newInstance();
                    mFragments[TAB_HELP] = fragment;
                    break;
                default:
                    fragment = null;
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        public BaseSettingsFragment get(int position) {
            if (mFragments == null) {
                return null;
            }

            return mFragments[position];
        }
    }

    private void loadInstalledAppsList() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute () {
                showLoading();
            }

            @Override
            protected Void doInBackground (Void...params){
                // Load shortcut list
                try {
                    ArrayList<AppContainer> subscriptions = DbHelper.getInstance(mContext).getSubscriptions();
                    mInstalledApps = new ArrayList<>();

                    PackageManager pm = getPackageManager();
                    Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

                    List<ResolveInfo> launchables = pm.queryIntentActivities(mainIntent, 0);

                    for (ResolveInfo info : launchables) {
                        String niceName = (String) info.activityInfo.loadLabel(pm);
                        String packageName = info.activityInfo.packageName;
                        String activityName = info.activityInfo.name;

                        if (!niceName.equals("")) {
                            AppContainer app = new AppContainer();

                            app.setFriendlyName(niceName);
                            app.setPackageName(packageName);
                            app.setActivityName(activityName);

                            if (subscriptions.contains(app)) {
//                                for (AppContainer sub : subscriptions) {
//                                    if (app.equals(sub)) {
//                                        app.setColor(sub.getColor());
//                                    }
//                                }
                                app.setChecked(true);
                            }

                            mInstalledApps.add(app);
                        }
                    }

                    Collections.sort(mInstalledApps, new Comparator<AppContainer>() {
                        @Override
                        public int compare(AppContainer lhs, AppContainer rhs) {
                            return lhs.getFriendlyName().toLowerCase()
                                    .compareTo(rhs.getFriendlyName().toLowerCase());
                        }
                    });
                }
                catch (Exception e) {
                    Log.e(TAG, "Error loading packages: " + e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute (Void aVoid){
                hideLoading();
            }
        }.execute();
    }

    public interface OnAppsLoadedListener {
        void onAppsListLoaded(ArrayList<AppContainer> apps);
    }
}
