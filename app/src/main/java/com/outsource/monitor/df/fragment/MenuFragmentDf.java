package com.outsource.monitor.df.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.outsource.monitor.R;
import com.outsource.monitor.df.event.DfParamChangeEvent;
import com.outsource.monitor.df.model.DfParam;
import com.outsource.monitor.parser.DFParser48278;
import com.outsource.monitor.service.DfDataReceiver;
import com.outsource.monitor.utils.DisplayUtils;
import com.outsource.monitor.utils.PromptUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2016/10/2.
 */
public class MenuFragmentDf extends Fragment implements DfDataReceiver {

    private EditText mEtFrequency;

    public static MenuFragmentDf newInstance() {
        return new MenuFragmentDf();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_menu_df, container, false);
        mEtFrequency = (EditText) view.findViewById(R.id.et_df_frequency);
        view.findViewById(R.id.btn_change_param).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkFrequency(true)) {
                    DfParam param = new DfParam(getInputFrequency());
                    param.save();
                    EventBus.getDefault().post(new DfParamChangeEvent(param));
                }
            }
        });
        return view;
    }


    private float getInputFrequency() {
        if (checkFrequency(false)) {
            return Float.valueOf(mEtFrequency.getText().toString());
        }
        return 0;
    }

    private boolean checkFrequency(boolean showToast) {
        String frequency = mEtFrequency.getText().toString().trim();
        if (TextUtils.isEmpty(frequency)) {
            if (showToast) PromptUtils.showToast("请输入起始频率");
            return false;
        }
        return true;
    }

    @Override
    public void onReceiveDfData(DFParser48278.DataValue dfData) {

    }

    @Override
    public void onReceiveDfHead(final DFParser48278.DataHead dfHead) {
        if (dfHead == null) {
            return;
        }
        if (mEtFrequency != null) {
            mEtFrequency.post(new Runnable() {
                @Override
                public void run() {
                    mEtFrequency.setText(String.format("%.1f", DisplayUtils.toDisplayFrequency(dfHead.freq)));
                }
            });
        }
    }
}
