package com.outsource.monitor.fragment;

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
import com.outsource.monitor.base.Tab;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by xionghao on 2016/12/13.
 */

public class TabMenuFragment extends Fragment {

    private Tab mSelectedTab = Tab.ITU;

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
        return view;
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
}
