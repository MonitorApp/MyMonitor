package com.outsource.monitor.monitor.discrete.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.outsource.monitor.R;
import com.outsource.monitor.base.ParamChangeEvent;
import com.outsource.monitor.config.ConfigManager;
import com.outsource.monitor.config.Param;
import com.outsource.monitor.config.UIParam;
import com.outsource.monitor.utils.PromptUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xionghao on 2017/3/5.
 */

public class MScanMenuFragment extends Fragment {

    private static final String TAG = "ParamSettingFragment";

    private LinearLayout mLlLabel;
    private LinearLayout mLlValue;
    private ConfigManager.FuncType mFuncType = ConfigManager.FuncType.DISCRETE;
    private List<String> mFrequencyList = new ArrayList<>();

    public static Fragment newInstance() {
        MScanMenuFragment fragment = new MScanMenuFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_menu_discrete_scan, container, false);
        mLlLabel = (LinearLayout) view.findViewById(R.id.ll_itu_param_label);
        mLlValue = (LinearLayout) view.findViewById(R.id.ll_itu_param_value);
        view.findViewById(R.id.btn_add_mscan_param).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInput()) {
                    List<UIParam> params = getParamFromUI();
                    for (UIParam p : params) {
                        if ("frequency".equals(p.name)) {
                            if (mFrequencyList.contains(p.value + p.unit)) {
                                PromptUtils.showToast("已经添加过该频率了");
                                return;
                            }
                            mFrequencyList.add(p.value + p.unit);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
        view.findViewById(R.id.btn_save_param).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFrequencyList.size() == 0) {
                    PromptUtils.showToast("请先添加要扫描的频率");
                    return;
                }
                List<UIParam> params = getParamFromUI();
                StringBuilder sb = new StringBuilder();
                for (String freq : mFrequencyList) {
                    sb.append(freq).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                for (UIParam p : params) {
                    if ("frequency".equals(p.name)) {
                        p.value = sb.toString();
                        p.unit = "";
                    }
                }
                ConfigManager.getInstance().saveParams(mFuncType, params);
                EventBus.getDefault().post(new ParamChangeEvent(mFuncType, params));
            }
        });
        initParamItems();

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_mscan_param);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        recyclerView.setAdapter(mAdapter);
        return view;
    }

    private RecyclerView.Adapter<MScanParamViewHolder> mAdapter = new RecyclerView.Adapter<MScanParamViewHolder>() {

        @Override
        public MScanParamViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MScanParamViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.mscan_item_param, parent, false));
        }

        @Override
        public void onBindViewHolder(MScanParamViewHolder holder, final int position) {
            String frequency = mFrequencyList.get(position);
            holder.tvFreq.setText(frequency);
            holder.ivDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFrequencyList.remove(position);
                    mAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mFrequencyList.size();
        }
    };

    private class MScanParamViewHolder extends RecyclerView.ViewHolder {

        TextView tvFreq;
        View ivDel;

        public MScanParamViewHolder(View itemView) {
            super(itemView);
            tvFreq = (TextView) itemView.findViewById(R.id.tv_mscan_param);
            ivDel =itemView.findViewById(R.id.iv_mscan_param_delete);
        }
    }

    // 根据xml决定界面需要哪些参数设置项
    private void initParamItems() {
        mLlLabel.removeAllViews();
        mLlValue.removeAllViews();
//        List<UIParam> cacheParams = ConfigManager.getInstance().loadParams(mFuncType);
        List<Param.Item> items = ConfigManager.getInstance().getFuncParamItems(mFuncType);
        for (final Param.Item item : items) {
            // 设置左边的参数名称控件
            final String title = item.title + (TextUtils.isEmpty(item.unit) ? "" : "(单位：" + item.unit + ")");
            TextView tv = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.item_param_label, mLlLabel, false);
            tv.setText(title);
            tv.setTag(item);
            mLlLabel.addView(tv);

            // 设置右边的参数的填值控件
            if (TextUtils.equals(item.type, "int") || TextUtils.equals(item.type, "double")) {
                EditText et = (EditText) LayoutInflater.from(getActivity()).inflate(R.layout.item_param_value, mLlValue, false);
                et.setInputType(TextUtils.equals(item.type, "int") ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                et.setHint("请输入" + item.title);
                et.setText(item.defaultValue);
                et.setTag(item.defaultValue);
                mLlValue.addView(et);

                // 加载上次缓存的参数值
//                for (UIParam uiParam : cacheParams) {
//                    if (TextUtils.equals(uiParam.name, item.name)) {
//                        et.setText(uiParam.value);
//                    }
//                }
            } else if (TextUtils.equals(item.type, "enum")) {
                final TextView tvOptions = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.item_param_label, mLlLabel, false);
                tvOptions.setText(item.defaultValue);
                tvOptions.setTag(item.defaultValue);
                tvOptions.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final List<Param.Item.Value> options = item.values;
                        final ArrayList<CharSequence> titles = new ArrayList<>(options.size());
                        final ArrayList<CharSequence> values = new ArrayList<>(options.size());
                        for (Param.Item.Value option : options) {
                            titles.add(option.title);
                            values.add(option.value);
                        }
                        CharSequence[] ts = new CharSequence[options.size()];
                        titles.toArray(ts);
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("请选择" + item.title);
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

                // 加载上次缓存的参数值
//                for (UIParam uiParam : cacheParams) {
//                    if (TextUtils.equals(uiParam.name, item.name)) {
//                        tvOptions.setText(uiParam.value);
//                    }
//                }
                mLlValue.addView(tvOptions);
            }
        }
    }

    public List<UIParam> getParamFromUI() {
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
                uiParam.type = UIParam.TYPE_STRING;
            } else {
                uiParam.value = tvValue.getText().toString();
                if (TextUtils.equals(p.type, "int")) {
                    uiParam.type = UIParam.TYPE_INT;
                } else if (TextUtils.equals(p.type, "double")) {
                    uiParam.type = UIParam.TYPE_DOUBLE;
                }
            }
            uiParams.add(uiParam);
        }
        return uiParams;
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
