package com.outsource.monitor.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.outsource.monitor.R;
import com.outsource.monitor.model.FrequencyLevel;
import com.outsource.monitor.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/10/6.
 */
public class BandScanningTagAdapter extends RecyclerView.Adapter<BandScanningTagAdapter.BandScanningTagViewHolder> {

    private List<FrequencyLevel> mLevels = new ArrayList<>(0);

    public void setData(List<FrequencyLevel> levels) {
        mLevels.clear();
        if (!CollectionUtils.isEmpty(levels)) {
            mLevels.addAll(levels);
        }
        notifyDataSetChanged();
    }

    public void addData(FrequencyLevel level) {
        if (level != null) {
            mLevels.add(level);
            notifyDataSetChanged();
        }
    }

    public void deleteLastData() {
        if (!CollectionUtils.isEmpty(mLevels)) {
            mLevels.remove(mLevels.size() - 1);
            notifyDataSetChanged();
        }
    }

    @Override
    public BandScanningTagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BandScanningTagViewHolder holder = new BandScanningTagViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_band_scanning_tag, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(BandScanningTagViewHolder holder, int position) {
        FrequencyLevel level = mLevels.get(position);
        holder.tvFrequency.setText(String.format("%.1f", level.frequency) + "MHz");
        holder.tvLevel.setText(String.format("%.1f", level.level) + "dBpv");
    }

    @Override
    public int getItemCount() {
        return mLevels.size();
    }

    public class BandScanningTagViewHolder extends RecyclerView.ViewHolder {

        TextView tvFrequency;
        TextView tvLevel;

        public BandScanningTagViewHolder(View itemView) {
            super(itemView);
            tvFrequency = (TextView) itemView.findViewById(R.id.tv_band_scan_tag_frequency);
            tvLevel = (TextView) itemView.findViewById(R.id.tv_band_scan_tag_level);
        }
    }
}
