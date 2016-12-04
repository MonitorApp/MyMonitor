package com.outsource.monitor.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.outsource.monitor.R;
import com.outsource.monitor.activity.RecordActivity;
import com.outsource.monitor.model.Record;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2016/10/2.
 */
public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder>{

    private static String TAG = "RecordViewHolder";

    private Context mContext;

    public List<Record> mRecordList = new ArrayList<Record>();

    public RecordAdapter(Context ctx)
    {
        mContext = ctx;
        initTestData();
    }

    private  void initTestData()
    {
        int i;
        Record rec;
        mRecordList.clear();
        for(i=0; i<20; ++i)
        {
            rec = new Record();
            rec.oper = "oper"+i;
            rec.time = "2016-10-03";
            rec.druation = "1分30秒";
            mRecordList.add(rec);
        }
    }

    @Override
    public RecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecordViewHolder holder = new RecordViewHolder(LayoutInflater.from(
                mContext).inflate(R.layout.item_record, parent,
                false));
        return holder;
    }

    @Override
    public int getItemCount() {
        return mRecordList.size();
    }

    @Override
    public void onBindViewHolder(RecordViewHolder holder, int position) {
        holder.setData(mRecordList.get(position));
    }

    public void doDeleteSelectItem()
    {
        Iterator<Record> iterator = mRecordList.iterator();
        while (iterator.hasNext()) {
            Record record = iterator.next();
            if (record.isSelect) {
                iterator.remove();
            }
        }
        notifyDataSetChanged();
    }

    class RecordViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView mTvOper;
        private TextView mTvTime;
        private TextView mTvDuration;
        private CheckBox mCbSelect;

        private Record mData;

        public RecordViewHolder(View view){
            super(view);

            mTvOper = (TextView) view.findViewById(R.id.tv_oper);
            mTvTime = (TextView) view.findViewById(R.id.tv_time);
            mTvDuration = (TextView) view.findViewById(R.id.tv_duration);
            mCbSelect = (CheckBox) view.findViewById(R.id.cb_select);
            mCbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(mData != null)
                    {
                        mData.isSelect = isChecked;
                    }
                }
            });

            view.setOnClickListener(this);
        }

        public void setData(Record record)
        {
            mData = record;
            mTvOper.setText(mData.oper);
            mTvTime.setText(mData.time);
            mTvDuration.setText(mData.druation);
            mCbSelect.setChecked(mData.isSelect);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick play record here");
            //play record here
            //// TODO: 2016/10/5
        }
    }
}
