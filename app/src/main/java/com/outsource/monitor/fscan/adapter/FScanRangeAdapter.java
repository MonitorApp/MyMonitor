package com.outsource.monitor.fscan.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.outsource.monitor.R;
import com.outsource.monitor.parser.FscanParser48278;
import com.outsource.monitor.utils.DisplayUtils;

import java.util.List;

/**
 * Created by Administrator on 2016/11/5.
 */
public class FscanRangeAdapter extends RecyclerView.Adapter {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener mOnItemClickListener;
    private List<FscanParser48278.DataHead.FcanParam> mHeads;
    private int choosePosition;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MeasureTypeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fscan_item_frequency_range, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        MeasureTypeViewHolder viewHolder = (MeasureTypeViewHolder) holder;
        if (position == choosePosition) {
            viewHolder.itemView.setBackgroundColor(Color.parseColor("#555555"));
        } else {
            viewHolder.itemView.setBackgroundColor(viewHolder.itemView.getContext().getResources().getColor(R.color.text_color_333));
        }
        FscanParser48278.DataHead.FcanParam head = mHeads.get(position);
        viewHolder.tvStartFrequency.setText(String.format("%.1f", DisplayUtils.toDisplayFrequency(head.startFreq)));
        viewHolder.tvEndFrequency.setText(String.format("%.1f", DisplayUtils.toDisplayFrequency(head.endFreq)));
        viewHolder.tvStep.setText(String.format("%.1f", DisplayUtils.toDisplayStep(head.step)));
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
        return mHeads != null ? mHeads.size() : 0;
    }

    public void update(List<FscanParser48278.DataHead.FcanParam> fscanHeads) {
        mHeads = fscanHeads;
        notifyDataSetChanged();
    }

    private class MeasureTypeViewHolder extends RecyclerView.ViewHolder {

        TextView tvStartFrequency;
        TextView tvEndFrequency;
        TextView tvStep;

        public MeasureTypeViewHolder(View itemView) {
            super(itemView);
            tvStartFrequency = (TextView) itemView.findViewById(R.id.tv_fscan_start_frequency);
            tvEndFrequency = (TextView) itemView.findViewById(R.id.tv_fscan_end_frequency);
            tvStep = (TextView) itemView.findViewById(R.id.tv_fscan_end_step);
        }
    }
}
