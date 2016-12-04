package com.outsource.monitor.singlefrequency.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.outsource.monitor.R;
import com.outsource.monitor.singlefrequency.event.ReceiveItuHeadEvent;
import com.outsource.monitor.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Administrator on 2016/10/2.
 */
public class MenuFragmentSingleFrequencyMeasure extends Fragment {

    private TextView mTvFrequency;

    public static Fragment newInstance() {
        return new MenuFragmentSingleFrequencyMeasure();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_menu_single_frequncy_measure, container, false);
        mTvFrequency = (TextView) view.findViewById(R.id.tv_frequence);
        EventBus.getDefault().register(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ReceiveItuHeadEvent event) {
        if (event.ituHead != null) {
            mTvFrequency.setText(Utils.parseFrequency(event.ituHead.frequence));
        }
    }
}
