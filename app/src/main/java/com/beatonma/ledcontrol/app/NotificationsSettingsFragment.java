package com.beatonma.ledcontrol.app;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.beatonma.colorpicker.ColorPickerFragment;
import com.beatonma.ledcontrol.AppContainer;
import com.beatonma.ledcontrol.utility.AnimationUtils;
import com.beatonma.ledcontrol.utility.PrefUtils;
import com.beatonma.self.led.ledcontrol.R;
import com.beatonma.ledcontrol.app.ui.SwitchPreference;
import com.beatonma.ledcontrol.service.NotificationService;
import com.beatonma.ledcontrol.utility.Utils;

import java.util.ArrayList;

/**
 * Created by Michael on 20/01/2016.
 */
public class NotificationsSettingsFragment extends BaseSettingsFragment {
    private NotificationsAdapter mAdapter;
    private ProgressBar mProgressBar;
    private Snackbar mSnackbar;
    private View mOnboardContainer;

    public static NotificationsSettingsFragment newInstance() {
        return new NotificationsSettingsFragment();
    }

    @Override
    int getLayout() {
        return R.layout.fragment_settings_notifications;
    }

    @Override
    void initLayout(View v) {
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressbar);

        SwitchPreference enableSwitch = (SwitchPreference) v.findViewById(R.id.enable_switch);
        enableSwitch.setSwitchListener(new SwitchPreference.OnSwitchChangedListener() {
            @Override
            public void onSwitchChanged(boolean isChecked) {
                Intent toggleServiceIntent;
                if (isChecked) {
                    MainActivity context = getMainActivity();
                    View v = context.findViewById(R.id.top_level_container);
                    mSnackbar = Snackbar.make(v, context.getString(R.string.system_notification_settings_message), Snackbar.LENGTH_LONG);
                    mSnackbar.setAction(context.getString(R.string.system_notification_settings_open), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                                startActivity(intent);
                            }
                            catch (Exception e) {
                                Log.e(TAG, "Error opening system notification listener settings: " + e.toString());
                            }
                        }
                    });
                    mSnackbar.show();

                    toggleServiceIntent = new Intent(NotificationService.START_SERVICE);
                }
                else {
                    if (mSnackbar != null) {
                        mSnackbar.dismiss();
                    }
                    toggleServiceIntent = new Intent(NotificationService.STOP_SERVICE);
                }
                getActivity().sendBroadcast(toggleServiceIntent);
            }
        });

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recyclerview);
        mAdapter = new NotificationsAdapter();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int topItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                if (topItem != 0) {
                    getMainActivity().hideFab();
                }
                else {
                    getMainActivity().showFab();
                }
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback();
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);

        mOnboardContainer = v.findViewById(R.id.onboard_container);
        boolean showNotificationOnboarding = PrefUtils.get(getContext()).getBoolean(PrefUtils.ONBOARD_SHOW_ONBOARD_NOTIFICATIONS, true);
        if (showNotificationOnboarding) {
            Button onboardOkButton = (Button) v.findViewById(R.id.ok_button);
            onboardOkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AnimationUtils.hidePreference(mOnboardContainer, mOnboardContainer.getMeasuredHeight());

                    PrefUtils.get(getContext()).edit()
                            .putBoolean(PrefUtils.ONBOARD_SHOW_ONBOARD_NOTIFICATIONS, false)
                            .commit();
                }
            });
        }
        else {
            mOnboardContainer.setVisibility(View.GONE);
        }

        loadSubscriptions();
    }

    @Override
    public void showLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        mProgressBar.setVisibility(View.GONE);
    }

    public void setSubscriptionDataset(ArrayList<AppContainer> apps) {
        mAdapter.setDataset(apps);
    }

    private void loadSubscriptions() {
        new AsyncTask<Void, Void, ArrayList<AppContainer>>() {
            @Override
            protected void onPreExecute() {
                showLoading();
            }

            @Override
            protected ArrayList<AppContainer> doInBackground(Void... params) {
                ArrayList<AppContainer> apps = DbHelper.getInstance(getActivity()).getSubscriptions();
                return apps;
            }

            @Override
            protected void onPostExecute(ArrayList<AppContainer> apps) {
                mAdapter.setDataset(apps);
                hideLoading();
            }
        }.execute();
    }

    public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> implements ItemTouchHelperAdapter {
        private ArrayList<AppContainer> mDataset = new ArrayList<>();

        @Override
        public void onItemDismiss(int position) {
            AppContainer app = mDataset.get(position);
            mDataset.remove(position);
            notifyItemRemoved(position);

            DbHelper.getInstance(getContext()).removeSubscription(app);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            TextView description;
            Button colorButton;

            public ViewHolder(View v) {
                super(v);

                title = (TextView) v.findViewById(R.id.title);
                description = (TextView) v.findViewById(R.id.description);
                colorButton = (Button) v.findViewById(R.id.button_color);
            }

            public void bind(int position) {
                final AppContainer app = mDataset.get(position);
                title.setText(app.getFriendlyName());
                description.setText(app.getPackageName());
                Utils.setBackground(colorButton, app.getColor(), -1);
                colorButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String tag = getContext().getString(R.string.fragtag_color);
                        ColorPickerFragment fragment = ColorPickerFragment.newInstance("", getContext().getResources().getColor(R.color.Accent));
                        fragment.setColorPickerListener(new ColorPickerFragment.OnColorPickedListener() {
                            @Override
                            public void onColorPicked(int color) {
                                app.setColor(color);
                                DbHelper.getInstance(getContext()).updateSingleSubscription(app);
                                Utils.setBackground(colorButton, color, -1);
                            }
                        });
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .add(R.id.top_level_container, fragment, tag)
                                .addToBackStack(tag)
                                .commit();
                    }
                });
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.view_notification_app_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return mDataset == null ? 0 : mDataset.size();
        }

        public void setDataset(ArrayList<AppContainer> dataset) {
            mDataset = dataset;
            notifyDataSetChanged();
        }
    }

    public interface ItemTouchHelperAdapter {
//        void onItemMove(int fromPosition, int toPosition);
        void onItemDismiss(int position);
    }

    public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = 0;
            int swipeFlags = ItemTouchHelper.START;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }
    }
}
