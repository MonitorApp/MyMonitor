package com.outsource.monitor.monitor.base.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.outsource.monitor.R;
import com.outsource.monitor.base.OnTabChangeEvent;
import com.outsource.monitor.config.Tab;
import com.outsource.monitor.monitor.base.event.UpdatePlayUIEvent;
import com.outsource.monitor.monitor.base.event.PlayPauseEvent;
import com.outsource.monitor.other.map.OfflineMapActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by xionghao on 2016/12/13.
 */

public class TabMenuFragment extends Fragment {

    private Tab mSelectedTab = Tab.ITU;
    private View.OnClickListener mMapSwitchClickListener;
    private View mMapSwitcher;
    private TextView mTvPlayPause;

    public TabMenuFragment() {

    }

    public static TabMenuFragment newInstance(Tab tab) {
        TabMenuFragment fragment = new TabMenuFragment();
        Bundle argument = new Bundle();
        argument.putSerializable("tab", tab);
        fragment.setArguments(argument);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_tab, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_tab);
        mSelectedTab = (Tab) getArguments().get("tab");
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new TabAdapter());
        mMapSwitcher = view.findViewById(R.id.tv_map_switch);
        mMapSwitcher.setOnClickListener(mMapSwitchClickListener);
        mTvPlayPause = (TextView) view.findViewById(R.id.tv_play_pause);
        mTvPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePlayState(!v.isSelected());
                EventBus.getDefault().post(new PlayPauseEvent(isPlaying()));
            }
        });
        view.findViewById(R.id.tv_map_offline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), OfflineMapActivity.class));
            }
        });

        EventBus.getDefault().register(this);
        return view;
    }

    public void setOnMapSwitchClickListener(View.OnClickListener listener) {
        mMapSwitchClickListener = listener;
        if (mMapSwitcher != null) {
            mMapSwitcher.setOnClickListener(mMapSwitchClickListener);
        }
    }

    class TabAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RecyclerView.ViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.item_tab, parent, false)) {};
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final Tab tab = Tab.values()[position];
            TextView tvTab = (TextView) holder.itemView;
            tvTab.setText(tab.name);
            tvTab.setSelected(tab.equals(mSelectedTab));
            tvTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!tab.equals(mSelectedTab)) {
                        mSelectedTab = tab;
                        notifyDataSetChanged();
                        EventBus.getDefault().post(new OnTabChangeEvent(tab));
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return Tab.values().length;
        }
    }

    public boolean isPlaying() {
        return mTvPlayPause != null && mTvPlayPause.isSelected();
    }

    private void updatePlayState(boolean isPlay) {
        if (isPlay) {
            mTvPlayPause.setText("暂停");
            mTvPlayPause.setSelected(true);
        } else {
            mTvPlayPause.setText("开始");
            mTvPlayPause.setSelected(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayStateEvent(UpdatePlayUIEvent event) {
        updatePlayState(event.isPlay);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}
