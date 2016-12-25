package com.outsource.monitor.itu.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.outsource.monitor.R;
import com.outsource.monitor.parser.ItuParser48278;
import com.outsource.monitor.service.ItuDataReceiver;
import com.outsource.monitor.itu.event.ItuParamChangeEvent;
import com.outsource.monitor.itu.model.SingleFrequencyParam;
import com.outsource.monitor.utils.PromptUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by Administrator on 2016/10/2.
 */
public class MenuFragmentItu extends Fragment implements View.OnClickListener, ItuDataReceiver {

    private static final String TAG = "MenuFragmentSingleFrequency";

    private EditText mEtFrequency;          //频率
    private EditText mEtMdlFrequencyBand;  //中频带宽
    private TextView mTvDemodulate;         //解调模式
    private EditText mEtStep;               //跨距
    private TextView mTvDetectionMode;    //检波方式
    private TextView mTvBandScanMode;      //带宽测量模式
    private TextView mTvIncreaseMode;      //增益模式
    private TextView mTvRfAttenuation;      //射频衰减
    private CheckBox mCbRecord;                //记录保存

    public static Fragment newInstance() {
        return new MenuFragmentItu();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_menu_itu, container, false);
        mEtFrequency = (EditText) view.findViewById(R.id.et_itu_frequence);

        mEtMdlFrequencyBand = (EditText) view.findViewById(R.id.et_itu_mdl_band);
        mTvDemodulate  = (TextView)view.findViewById(R.id.tv_demodulate);
        mEtStep = (EditText) view.findViewById(R.id.et_itu_step);
        mTvDetectionMode = (TextView) view.findViewById(R.id.tv_detection_mode);
        mTvBandScanMode = (TextView) view.findViewById(R.id.tv_band_scan_mode);
        mTvIncreaseMode = (TextView) view.findViewById(R.id.tv_increase_mode);
        mTvRfAttenuation = (TextView) view.findViewById(R.id.tv_rf_attenuation);
        mCbRecord = (CheckBox) view.findViewById(R.id.cb_record);
        view.findViewById(R.id.btn_change_param).setOnClickListener(this);

        SetParam2UI(SingleFrequencyParam.loadFromCache());
        return view;
    }

    public SingleFrequencyParam GetParamFromUI()
    {
        SingleFrequencyParam param = new SingleFrequencyParam();
        param.frequecy = getInputFrequency();
        param.ifbw = getInputBand();
        param.demodmode = mTvDemodulate.getText().toString();
        param.span = getInputStep();
        return param;
    }

    public void SetParam2UI(SingleFrequencyParam param)
    {
        mEtFrequency.setText(String.format ("%.1f", param.frequecy));
        mEtMdlFrequencyBand.setText(String.format("%.1f", param.ifbw));
        mTvDemodulate.setText(param.demodmode);
        mEtStep.setText(String.format("%d", param.span));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_change_param:
                if (checkInput()) {
                    SingleFrequencyParam param = GetParamFromUI();
                    param.save();
                    EventBus.getDefault().post(new ItuParamChangeEvent(param));
                }
                break;
        }
    }

    @Override
    public void onReceiveItuData(List<Float> ituData) {
    }

    @Override
    public void onReceiveItuHead(final ItuParser48278.DataHead ituHead) {
        if (ituHead != null && mEtFrequency != null) {
            mEtFrequency.post(new Runnable() {
                @Override
                public void run() {
                    mEtFrequency.setText(String.format ("%.1f", ituHead.frequence / 1000000f));
                }
            });
        }
    }

    private boolean checkInput() {
        return checkFrequency() && checkBand() && checkStep();
    }

    private float getInputFrequency() {
        if (checkFrequency()) {
            return Float.valueOf(mEtFrequency.getText().toString());
        }
        return 0;
    }

    private boolean checkFrequency() {
        String frequency = mEtFrequency.getText().toString().trim();
        if (TextUtils.isEmpty(frequency)) {
            PromptUtils.showToast("请输入频率");
            return false;
        }
        return true;
    }

    private float getInputBand() {
        if (checkBand()) {
            return Float.valueOf(mEtMdlFrequencyBand.getText().toString());
        }
        return 0;
    }

    private boolean checkBand() {
        String band = mEtMdlFrequencyBand.getText().toString().trim();
        if (TextUtils.isEmpty(band)) {
            PromptUtils.showToast("请输入带宽");
            return false;
        }
        return true;
    }

    private int getInputStep() {
        if (checkStep()) {
            return Integer.valueOf(mEtStep.getText().toString());
        }
        return 0;
    }

    private boolean checkStep() {
        String span = mEtStep.getText().toString().trim();
        if (TextUtils.isEmpty(span)) {
            PromptUtils.showToast("请输入跨距");
            return false;
        }
        return true;
    }
}
