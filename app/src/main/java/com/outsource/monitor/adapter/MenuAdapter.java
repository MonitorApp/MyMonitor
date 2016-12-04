package com.outsource.monitor.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.outsource.monitor.R;
import com.outsource.monitor.utils.Const;

/**
 * Created by Administrator on 2016/10/2.
 */
public class MenuAdapter extends BaseAdapter {

    private Context mContext;

    public MenuAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return Const.Menu.TITLES.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.list_item_menu, null);
        }
        ((TextView) convertView).setText(Const.Menu.TITLES[position]);
        return convertView;
    }
}
