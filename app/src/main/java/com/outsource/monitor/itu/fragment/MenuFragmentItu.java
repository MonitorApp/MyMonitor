package com.outsource.monitor.itu.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.outsource.monitor.R;
import com.outsource.monitor.config.ConfigManager;
import com.outsource.monitor.config.Param;
import com.outsource.monitor.config.UIParam;
import com.outsource.monitor.itu.event.ItuParamChangeEvent;
import com.outsource.monitor.itu.model.SingleFrequencyParam;
import com.outsource.monitor.utils.PromptUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/10/2.
 */
public class MenuFragmentItu extends Fragment implements View.OnClickListener {

    private static final String TAG = "MenuFragmentSingleFrequency";


    private LinearLayout mLlLabel;
    private LinearLayout mLlValue;

    public static Fragment newInstance() {
        return new MenuFragmentItu();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_menu_itu2, container, false);
        mLlLabel = (LinearLayout) view.findViewById(R.id.ll_itu_param_label);
        mLlValue = (LinearLayout) view.findViewById(R.id.ll_itu_param_value);
        view.findViewById(R.id.btn_save_param).setOnClickListener(this);
        loadParams();
        return view;
    }

    private void loadParams() {
        mLlLabel.removeAllViews();
        mLlValue.removeAllViews();
        SingleFrequencyParam param = SingleFrequencyParam.loadFromCache();
        List<Param.Item> params = ConfigManager.getInstance().getFuncParams(0);
        for (final Param.Item p : params) {
            final String title = p.title + (TextUtils.isEmpty(p.unit) ? "" : "(单位：" + p.unit + ")");
            TextView tv = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.item_param_label, mLlLabel, false);
            tv.setText(title);
            tv.setTag(p);
            mLlLabel.addView(tv);

            if (TextUtils.equals(p.type, "int") || TextUtils.equals(p.type, "double")) {
                EditText et = (EditText) LayoutInflater.from(getActivity()).inflate(R.layout.item_param_value, mLlValue, false);
                et.setInputType(TextUtils.equals(p.type, "int") ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                et.setHint("请输入" + p.title);
                et.setText(p.defaultValue);
                et.setTag(p.defaultValue);
                mLlValue.addView(et);

                for (UIParam uiParam : param.uiParams) {
                    if (TextUtils.equals(uiParam.name, p.name)) {
                        et.setText(uiParam.value);
                    }
                }
            } else if (TextUtils.equals(p.type, "enum")) {
                final TextView tvOptions = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.item_param_label, mLlLabel, false);
                tvOptions.setText(p.defaultValue);
                tvOptions.setTag(p.defaultValue);
                tvOptions.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final List<Param.Item.Value> options = p.values;
                        final ArrayList<CharSequence> titles = new ArrayList<>(options.size());
                        final ArrayList<CharSequence> values = new ArrayList<>(options.size());
                        for (Param.Item.Value option : options) {
                            titles.add(option.title);
                            values.add(option.value);
                        }
                        CharSequence[] ts = new CharSequence[options.size()];
                        titles.toArray(ts);
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("请选择" + p.title);
                        builder.setItems(ts, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                tvOptions.setText(titles.get(which));
                                tvOptions.setTag(values.get(which));
                            }
                        });
                        builder.create().show();
                    }
                });

                for (UIParam uiParam : param.uiParams) {
                    if (TextUtils.equals(uiParam.name, p.name)) {
                        tvOptions.setText(uiParam.value);
                    }
                }
                mLlValue.addView(tvOptions);
            }
        }
    }

    public SingleFrequencyParam getParamFromUI()
    {
        SingleFrequencyParam singleFrequencyParam = new SingleFrequencyParam();
        List<UIParam> uiParams = new ArrayList<>();
        int childCount = mLlValue.getChildCount();
        for (int i = 0; i < childCount; i++) {
            TextView tv = (TextView) mLlLabel.getChildAt(i);
            TextView tvValue = (TextView) mLlValue.getChildAt(i);
            Param.Item p = (Param.Item) tv.getTag();
            UIParam uiParam = new UIParam();
            uiParam.name = p.name;
            uiParam.unit = p.unit;
            if (TextUtils.equals(p.type, "enum")) {
                uiParam.unit = "";
                uiParam.value = tvValue.getTag().toString();
            } else {
                uiParam.value = tvValue.getText().toString();
            }
            uiParams.add(uiParam);
        }
        singleFrequencyParam.uiParams.clear();
        singleFrequencyParam.uiParams.addAll(uiParams);
        return singleFrequencyParam;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_save_param:
                if (checkInput()) {
                    SingleFrequencyParam param = getParamFromUI();
                    param.save();
                    EventBus.getDefault().post(new ItuParamChangeEvent(param));
                }
                break;
        }
    }

    private boolean checkInput() {
        int childCount = mLlValue.getChildCount();
        for (int i = 0; i < childCount; i++) {
            TextView tvValue = (TextView) mLlValue.getChildAt(i);
            if (TextUtils.isEmpty(tvValue.getText().toString())) {
                PromptUtils.showToast("请填写有效的参数");
                return false;
            }
        }
        return true;
    }
}
