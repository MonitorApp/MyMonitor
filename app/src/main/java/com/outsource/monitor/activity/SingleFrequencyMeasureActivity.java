package com.outsource.monitor.activity;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;

import com.outsource.monitor.R;
import com.outsource.monitor.parser.Command;
import com.outsource.monitor.singlefrequency.fragment.ContentFragmentSingleFrequencyMeasure;
import com.outsource.monitor.singlefrequency.fragment.MenuFragmentSingleFrequencyMeasure;
import com.outsource.monitor.singlefrequency.model.SingleFrequencyParam;
import com.outsource.monitor.utils.ParamChangeEvent;
import com.outsource.monitor.utils.PreferenceUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2016/10/2.
 */
public class SingleFrequencyMeasureActivity extends TemplateActivity {

    static private String TAG = "SingleFrequencyMeasureActivity";

    private  SingleFrequencyParam param;

    @Override
    public Fragment createMenuFragment() {
        return MenuFragmentSingleFrequencyMeasure.newInstance();
    }

    @Override
    public Fragment createContentFragment() {
        return ContentFragmentSingleFrequencyMeasure.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public String getMyTitle() {
        return "单频测量";
    }

    @Override
    public void onOptionChange() {
        //// TODO: 2016/10/2
        Log.d(TAG, "onOptionChange");
        SyncUI2Param();
        SingleFrequencyParam.SaveParam(param);
        EventBus.getDefault().post(new ParamChangeEvent());
    }

    public Command getCmd() {
        if(param == null)
        {
            return  null;
        }
        return new Command(param.GetCommand(), Command.Type.ITU);
    }

    private void SyncUI2Param()
    {
        MenuFragmentSingleFrequencyMeasure fragment = (MenuFragmentSingleFrequencyMeasure)mMenuFragment;
        param = fragment.GetParamFromUI();
    }
}
