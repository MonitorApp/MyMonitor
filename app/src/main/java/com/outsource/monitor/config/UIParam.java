package com.outsource.monitor.config;

import android.text.TextUtils;

/**
 * Created by xionghao on 2017/3/5.
 */

public class UIParam {

    public static transient int TYPE_STRING =  0;
    public static transient int TYPE_INT = 1;
    public static transient int TYPE_DOUBLE = 2;

    public String name;
    public String value;
    public String unit;
    public int type = TYPE_STRING;

    public boolean isValueValid() {
        if (TextUtils.isEmpty(value)) return false;
        if (type == TYPE_INT) {
            try {
                Integer.valueOf(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return false;
            }
        } else if (type == TYPE_DOUBLE) {
            try {
                Double.valueOf(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UIParam uiParam = (UIParam) o;

        return name != null ? name.equals(uiParam.name) : uiParam.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
