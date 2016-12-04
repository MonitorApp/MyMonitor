package com.github.mikephil.charting.buffer;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

/**
 * Created by Administrator on 2016/11/20.
 */

public class ColorBuffer {

    public static int valueCount;
    public int[][] colors;
    public int entryCount;

    public ColorBuffer(int entryCount) {
        this.entryCount = entryCount;
        colors = new int[entryCount][valueCount];
    }

    public void feed(IBarDataSet dataSet) {
        int count = dataSet.getEntryCount();
        if (valueCount <= 0 || entryCount != count) return;
        for (int i = 0; i < count && i < colors.length; i++) {
            BarEntry e = dataSet.getEntryForIndex(i);
            if (e == null) continue;
            int[] colors = (int[]) e.getData();
            if (colors == null) continue;
            int len = Math.min(colors.length, valueCount);
            System.arraycopy(colors, 0, this.colors[i], 0, len);
        }
    }
}
