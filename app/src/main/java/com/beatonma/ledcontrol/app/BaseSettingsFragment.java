package com.beatonma.ledcontrol.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beatonma.ledcontrol.Broadcaster;

/**
 * Created by Michael on 20/01/2016.
 */
public abstract class BaseSettingsFragment extends Fragment {
    protected final static String TAG = "SettingsFragment";

    protected Broadcaster.OnPostResponseListener mResponseListener = new Broadcaster.OnPostResponseListener() {
        @Override
        public void onResponse(boolean success) {
            if (getMainActivity() != null) {
                getMainActivity().showConnectionSuccess(success);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(getLayout(), container, false);

        initLayout(v);

        return v;
    }

    abstract int getLayout();
    abstract void initLayout(View v);

    public MainActivity getMainActivity() {
        if (getActivity() instanceof MainActivity) {
            return (MainActivity) getActivity();
        }
        else {
            return null;
        }
    }

    public void showLoading() {
        getMainActivity().showLoading();
    }

    public void hideLoading() {
        getMainActivity().hideLoading();
    }
}
