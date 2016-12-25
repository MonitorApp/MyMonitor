package com.outsource.monitor.fscan.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.outsource.monitor.R;
import com.outsource.monitor.fscan.event.FscanParamsChangeEvent;
import com.outsource.monitor.fscan.model.FscanParam;
import com.outsource.monitor.parser.FscanParser48278;
import com.outsource.monitor.service.FscanDataReceiver;
import com.outsource.monitor.utils.DisplayUtils;
import com.outsource.monitor.utils.PromptUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2016/10/2.
 */
public class MenuFragmentFscan extends Fragment implements FscanDataReceiver {

    private EditText mEtStartFrequency;
    private EditText mEtEndFrequency;
    private EditText mEtStep;

    public static MenuFragmentFscan newInstance() {
        return new MenuFragmentFscan();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_menu_fscan, container, false);
        mEtStartFrequency = (EditText) view.findViewById(R.id.et_frequence_start);
        mEtEndFrequency = (EditText) view.findViewById(R.id.et_frequence_end);
        mEtStep = (EditText) view.findViewById(R.id.et_fscan_step);
        view.findViewById(R.id.btn_change_param).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInput(true)) {
                    FscanParam param = new FscanParam(getInputStartFrequency(), getInputEndFrequency(), getInputStep());
                    param.save();
                    EventBus.getDefault().post(new FscanParamsChangeEvent(param));
                }
            }
        });
        return view;
    }

    private boolean checkInput(boolean showToast) {
        return checkStartFrequency(showToast) && checkEndFrequency(showToast) && checkStep(showToast) && checkFrequencyValueInvalid();
    }

    private float getInputStartFrequency() {
        if (checkStartFrequency(false)) {
            return Float.valueOf(mEtStartFrequency.getText().toString());
        }
        return 0;
    }

    private boolean checkStartFrequency(boolean showToast) {
        String frequency = mEtStartFrequency.getText().toString().trim();
        if (TextUtils.isEmpty(frequency)) {
            if (showToast) PromptUtils.showToast("请输入起始频率");
            return false;
        }
        return true;
    }

    private int getInputEndFrequency() {
        if (checkEndFrequency(false)) {
            return Integer.valueOf(mEtEndFrequency.getText().toString());
        }
        return 0;
    }

    private boolean checkEndFrequency(boolean showToast) {
        String band = mEtEndFrequency.getText().toString().trim();
        if (TextUtils.isEmpty(band)) {
            if (showToast) PromptUtils.showToast("请输入终止频率");
            return false;
        }
        return true;
    }

    private boolean checkFrequencyValueInvalid() {
        float start = getInputStartFrequency();
        float end = getInputEndFrequency();
        boolean valid = end > start;
        if (!valid) {
            PromptUtils.showToast("终止频率必须大于起始频率");
        }
        return valid;
    }

    private int getInputStep() {
        if (checkStep(false)) {
            return Integer.valueOf(mEtStep.getText().toString());
        }
        return 0;
    }

    private boolean checkStep(boolean showToast) {
        String span = mEtStep.getText().toString().trim();
        if (TextUtils.isEmpty(span)) {
            if (showToast) PromptUtils.showToast("请输入步长");
            return false;
        }
        return true;
    }

    @Override
    public void onReceiveFScanData(FscanParser48278.DataValue fscanData) {

    }

    @Override
    public void onReceiveFScanHead(FscanParser48278.DataHead fscanHead) {
        if (fscanHead == null || fscanHead.fscanParamList.size() == 0) {
            return;
        }
        if (mEtStartFrequency != null) {
            final FscanParser48278.DataHead.FcanParam param = fscanHead.fscanParamList.get(0);
            mEtStartFrequency.post(new Runnable() {
                @Override
                public void run() {
                    mEtStartFrequency.setText(String.format("%.1f", DisplayUtils.toDisplayFrequency(param.startFreq)));
                    mEtEndFrequency.setText(String.format("%.1f", DisplayUtils.toDisplayFrequency(param.endFreq)));
                    mEtStep.setText(String.format("%d", param.step / 100));
                }
            });
        }
    }
}
