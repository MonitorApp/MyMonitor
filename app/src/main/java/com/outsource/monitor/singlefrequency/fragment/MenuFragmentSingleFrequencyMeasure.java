package com.outsource.monitor.singlefrequency.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.outsource.monitor.R;
import com.outsource.monitor.activity.SingleFrequencyMeasureActivity;
import com.outsource.monitor.singlefrequency.event.ReceiveItuHeadEvent;
import com.outsource.monitor.singlefrequency.model.SingleFrequencyParam;
import com.outsource.monitor.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Administrator on 2016/10/2.
 */
public class MenuFragmentSingleFrequencyMeasure extends Fragment implements View.OnClickListener {

    private static final String TAG = "MenuFragmentSingleFrequency";

    private TextView mTvFrequency;          //频率
    private TextView mTvMdlFrequencyBand;  //中频带宽
    private TextView mTvDemodulate;         //解调模式
    private TextView mTvStep;               //跨距
    private TextView mTvDetectionMode;    //检波方式
    private TextView mTvBandScanMode;      //带宽测量模式
    private TextView mTvIncreaseMode;      //增益模式
    private TextView mTvRfAttenuation;      //射频衰减
    private CheckBox mCbRecord;                //记录保存

    public static Fragment newInstance() {
        return new MenuFragmentSingleFrequencyMeasure();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_menu_single_frequncy_measure, container, false);
        mTvFrequency = (TextView) view.findViewById(R.id.tv_frequence);

        mTvMdlFrequencyBand = (TextView) view.findViewById(R.id.tv_mdl_band);
        mTvDemodulate  = (TextView)view.findViewById(R.id.tv_demodulate);
        mTvStep = (TextView) view.findViewById(R.id.tv_step);
        mTvDetectionMode = (TextView) view.findViewById(R.id.tv_detection_mode);
        mTvBandScanMode = (TextView) view.findViewById(R.id.tv_band_scan_mode);
        mTvIncreaseMode = (TextView) view.findViewById(R.id.tv_increase_mode);
        mTvRfAttenuation = (TextView) view.findViewById(R.id.tv_rf_attenuation);
        mCbRecord = (CheckBox) view.findViewById(R.id.cb_record);
        view.findViewById(R.id.btn_ifpan_change_param).setOnClickListener(this);

        SetParam2UI(SingleFrequencyParam.LoadParam());
        ((SingleFrequencyMeasureActivity)getActivity()).onOptionChange();
        EventBus.getDefault().register(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ReceiveItuHeadEvent event) {
        if (event.ituHead != null) {
            mTvFrequency.setText(Utils.parseFrequency(event.ituHead.frequence));
        }
    }

    public SingleFrequencyParam GetParamFromUI()
    {
        SingleFrequencyParam param = new SingleFrequencyParam();
        String freq = mTvFrequency.getText().toString();
        freq = freq.substring(0, freq.length()-3);
        param.frequecy = (long)(Float.parseFloat(freq)*1000000);
        param.mdlFrequencyBand = Float.parseFloat(mTvMdlFrequencyBand.getText().toString());
        param.demodulate = mTvDemodulate.getText().toString();
        param.step = Integer.parseInt(mTvStep.getText().toString());
        return param;
    }

    public void SetParam2UI(SingleFrequencyParam param)
    {
        mTvFrequency.setText(String.format ("%.1fMHz", (float)param.frequecy/1000000));
        mTvMdlFrequencyBand.setText(String.format("%f", param.mdlFrequencyBand));
        mTvDemodulate.setText(param.demodulate);
        mTvStep.setText(String.format("%d", param.step));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_ifpan_change_param:
                ((SingleFrequencyMeasureActivity)getActivity()).onOptionChange();
                break;
        }
    }
}
