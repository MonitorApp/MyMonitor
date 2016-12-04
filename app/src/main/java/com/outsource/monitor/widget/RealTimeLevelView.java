package com.outsource.monitor.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.outsource.monitor.R;
import com.outsource.monitor.model.FrequencyLevel;
import com.outsource.monitor.service.DataReceiver;
import com.outsource.monitor.utils.DateUtils;

import java.util.List;

/**
 * Created by Administrator on 2016/10/5.
 */
public class RealTimeLevelView extends LinearLayout implements DataReceiver {

    private static final int SPAN = 60 * 1000;

    private TextView mTvFrequency;
    private TextView mTvRealTimeLevel;
    private TextView mTvMaxLevel;
    private TextView mTvMinLevel;
    private TextView mTvAvgLevel;
    private long mLastMinuteTime;
    private float mMaxLevel;
    private float mMinLevel;
    private float mAvgLevel;
    private int mCount;

    public RealTimeLevelView(Context context) {
        super(context);
        init(context);
    }

    public RealTimeLevelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        inflate(context, R.layout.merge_real_time_level, this);

        mTvFrequency = (TextView) findViewById(R.id.tv_real_time_frequency);
        mTvRealTimeLevel = (TextView) findViewById(R.id.tv_real_time_level);
        mTvMaxLevel = (TextView) findViewById(R.id.tv_real_time_max_level);
        mTvAvgLevel = (TextView) findViewById(R.id.tv_real_time_avg_level);
        mTvMinLevel = (TextView) findViewById(R.id.tv_real_time_min_level);
    }

    @Override
    public void onReceiveItuData(float[] ituData) {
        final float level = ituData[0];
        long currentTime = System.currentTimeMillis();
        long currentMinuteTime = DateUtils.getCurrentMinuteTime(currentTime);
        if (currentTime - mLastMinuteTime < SPAN) {
            mMaxLevel = Math.max(mMaxLevel, level);
            mMinLevel = Math.min(mMinLevel, level);
            mAvgLevel = (mAvgLevel * mCount + level) / (++mCount);
        } else {
            mCount = 1;
            mLastMinuteTime = currentMinuteTime;
            mMaxLevel = level;
            mMinLevel = level;
            mAvgLevel = level;
        }
        post(new Runnable() {
            @Override
            public void run() {
                refreshUI(level);
            }
        });
    }

    @Override
    public void onReceiveBandLevel(List<FrequencyLevel> levels) {

    }

    private void refreshUI(float level) {
        mTvFrequency.setText("7421.555");
        mTvRealTimeLevel.setText(String.format("%.1f", level) + "dBpV");
        mTvMaxLevel.setText("MAX " + String.format("%.1f", mMaxLevel));
        mTvAvgLevel.setText("AVG " + String.format("%.1f", mAvgLevel));
        mTvMinLevel.setText("MIN " + String.format("%.1f", mMinLevel));
    }
}
