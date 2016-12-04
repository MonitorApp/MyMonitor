package com.outsource.monitor.ifpan.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/20.
 */

public class FallRow {

    public long timestamp;
    public List<Float> mValues;

    public FallRow(long timestamp, List<Float> values) {
        this.timestamp = timestamp;
        mValues = values;
        if (mValues == null) {
            mValues = new ArrayList<>(0);
        }
    }
}
