package com.outsource.monitor.monitor.itu;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.outsource.monitor.R;
import com.outsource.monitor.monitor.itu.model.ItuTimePercentageThreshold;
import com.outsource.monitor.utils.PromptUtils;

/**
 * Created by xionghao on 2017/5/30.
 */

public class ItuTimePercentageThresholdDialog extends Dialog {

    public interface OnTimePercentageThresholdChangeListener {
        void onTimePercentageThresholdChanged(int threshold);
    }

    private OnTimePercentageThresholdChangeListener mOnTimePercentageThresholdChangeListener;

    public void setOnTimePercentageThresholdChangeListener(OnTimePercentageThresholdChangeListener listener) {
        mOnTimePercentageThresholdChangeListener = listener;
    }

    private EditText mEtThreshold;
    private ItuTimePercentageThreshold mItuTimePercentageThreshold;

    public ItuTimePercentageThresholdDialog(@NonNull Context context, ItuTimePercentageThreshold threshold) {
        super(context);
        mItuTimePercentageThreshold = threshold;
        setCanceledOnTouchOutside(true);
        setContentView(R.layout.dialog_itu_time_percentage_threshold);
        TextView tvName = (TextView) findViewById(R.id.tv_time_percentage_threshold_name);
        tvName.setText("设置" + threshold.name + "门限值");
        TextView mTvUnit = (TextView) findViewById(R.id.tv_time_percentage_threshold_unit);
        mTvUnit.setText(threshold.unit);

        mEtThreshold = (EditText) findViewById(R.id.et_time_percentage_threshold);
        mEtThreshold.setText(threshold.threshold + "");
        findViewById(R.id.btn_time_percentage_threshold_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int threshold = Integer.valueOf(mEtThreshold.getText().toString());
                    if (mOnTimePercentageThresholdChangeListener != null) {
                        mOnTimePercentageThresholdChangeListener.onTimePercentageThresholdChanged(threshold);
                    }
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                    PromptUtils.showToast("请输入门限值");
                    dismiss();
                }
            }
        });
        findViewById(R.id.btn_time_percentage_threshold_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

}
