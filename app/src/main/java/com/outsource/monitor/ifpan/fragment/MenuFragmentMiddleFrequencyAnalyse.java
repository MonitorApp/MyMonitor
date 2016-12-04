package com.outsource.monitor.ifpan.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.outsource.monitor.R;

/**
 * Created by Administrator on 2016/10/2.
 */
public class MenuFragmentMiddleFrequencyAnalyse extends Fragment {

    public static MenuFragmentMiddleFrequencyAnalyse newInstance() {
        return new MenuFragmentMiddleFrequencyAnalyse();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_menu_middle_frequency_analyse, container, false);
        return view;
    }
}
