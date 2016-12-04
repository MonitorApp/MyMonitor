package com.outsource.monitor.singlefrequency.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.outsource.monitor.R;
import com.outsource.monitor.parser.ItuParser48278;
import com.outsource.monitor.singlefrequency.model.ItuItemData;
import com.outsource.monitor.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/5.
 */
public class MeasureItemAdapter extends RecyclerView.Adapter {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private List<ItuItemData> mData = new ArrayList<>(0);
    private OnItemClickListener mOnItemClickListener;
    private List<ItuParser48278.DataHead.HeadItem> mItuHeads;
    private int choosePosition;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MeasureTypeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sgl_item_measure_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        MeasureTypeViewHolder viewHolder = (MeasureTypeViewHolder) holder;
        if (position == choosePosition) {
            viewHolder.itemView.setBackgroundColor(Color.parseColor("#555555"));
        } else {
            viewHolder.itemView.setBackgroundColor(viewHolder.itemView.getContext().getResources().getColor(R.color.text_color_333));
        }
        ItuParser48278.DataHead.HeadItem head = mItuHeads.get(position);
        viewHolder.tvTitle.setText(head.name + "[" + head.unit + "]");
        if (position >= mData.size()) return;
        final ItuItemData data = mData.get(position);
        viewHolder.tvRealTimeValue.setText(Float.toString(data.realtimeValue));
        viewHolder.tvAverageValue.setText(Float.toString(data.averageValue));
        viewHolder.tvMaxValue.setText(Float.toString(data.maxValue));
        viewHolder.tvMinValue.setText(Float.toString(data.minValue));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePosition = position;
                notifyDataSetChanged();
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItuHeads != null ? mItuHeads.size() : 0;
    }

    public void update(List<ItuItemData> data) {
        if (!CollectionUtils.isEmpty(data) && data.size() == getItemCount()) {
            mData.clear();
            mData.addAll(data);
            notifyDataSetChanged();
        }
    }

    public void initWithItuHeads(List<ItuParser48278.DataHead.HeadItem> ituHeads) {
        mData.clear();
        mItuHeads = ituHeads;
        notifyDataSetChanged();
    }

    private class MeasureTypeViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvRealTimeValue;
        TextView tvAverageValue;
        TextView tvMaxValue;
        TextView tvMinValue;

        public MeasureTypeViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_sgl_measure_item_name);
            tvRealTimeValue = (TextView) itemView.findViewById(R.id.tv_sgl_measure_realtime_value);
            tvAverageValue = (TextView) itemView.findViewById(R.id.tv_sgl_measure_average_value);
            tvMaxValue = (TextView) itemView.findViewById(R.id.tv_sgl_measure_max_value);
            tvMinValue = (TextView) itemView.findViewById(R.id.tv_sgl_measure_min_value);
        }
    }
}
