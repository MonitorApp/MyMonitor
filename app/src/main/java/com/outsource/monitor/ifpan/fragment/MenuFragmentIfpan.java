package com.outsource.monitor.ifpan.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.outsource.monitor.R;
import com.outsource.monitor.ifpan.model.IfpanParam;
import com.outsource.monitor.ifpan.event.IfpanParamsChangeEvent;
import com.outsource.monitor.parser.IfpanParser48278;
import com.outsource.monitor.ifpan.IfpanDataReceiver;
import com.outsource.monitor.utils.DisplayUtils;
import com.outsource.monitor.utils.PromptUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2016/10/2.
 */
public class MenuFragmentIfpan extends Fragment implements IfpanDataReceiver {

    private EditText mEtFrequency;
    private EditText mEtBand;
    private EditText mEtSpan;

    public MenuFragmentIfpan() {

    }

    public static MenuFragmentIfpan newInstance() {
        return new MenuFragmentIfpan();
    }

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_menu_ifpan, container, false);
        mEtFrequency = (EditText) view.findViewById(R.id.et_ifpan_frequence);
        mEtBand = (EditText) view.findViewById(R.id.et_ifpan_band);
        mEtSpan = (EditText) view.findViewById(R.id.et_ifpan_span);
        view.findViewById(R.id.btn_change_param).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInput(true)) {
                    IfpanParam param = new IfpanParam(getInputFrequency(), getInputBand(), getInputSpan());
                    param.save();
                    EventBus.getDefault().post(new IfpanParamsChangeEvent(param));
                }
            }
        });
        return view;
    }

    @Override
    public void onReceiveIfpanData(IfpanParser48278.DataValue ifpanData) {

    }

    @Override
    public void onReceiveIfpanHead(final IfpanParser48278.DataHead ifpanHeads) {
        if (ifpanHeads == null) {
            return;
        }
        if (mEtFrequency != null) {
            mEtFrequency.post(new Runnable() {
                @Override
                public void run() {
                    mEtFrequency.setText(String.format("%.1f", DisplayUtils.toDisplayFrequency(ifpanHeads.frequence)));
                    mEtBand.setText(String.format("%d", (int) DisplayUtils.toDisplaySpan(ifpanHeads.ifbw)));
                    mEtSpan.setText(String.format("%d", (int) DisplayUtils.toDisplaySpan(ifpanHeads.span)));
                }
            });
        }
    }

    private boolean checkInput(boolean showToast) {
        return checkFrequency(showToast) && checkBand(showToast) && checkSpan(showToast);
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
            if (showToast) PromptUtils.showToast("请输入频率");
            return false;
        }
        return true;
    }

    private int getInputBand() {
        if (checkBand(false)) {
            return Integer.valueOf(mEtBand.getText().toString());
        }
        return 0;
    }

    private boolean checkBand(boolean showToast) {
        String band = mEtBand.getText().toString().trim();
        if (TextUtils.isEmpty(band)) {
            if (showToast) PromptUtils.showToast("请输入带宽");
            return false;
        }
        return true;
    }

    private int getInputSpan() {
        if (checkSpan(false)) {
            return Integer.valueOf(mEtSpan.getText().toString());
        }
        return 0;
    }

    private boolean checkSpan(boolean showToast) {
        String span = mEtSpan.getText().toString().trim();
        if (TextUtils.isEmpty(span)) {
            if (showToast) PromptUtils.showToast("请输入跨距");
            return false;
        }
        return true;
    }

}
