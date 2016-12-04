
package com.github.mikephil.charting.data;

import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.List;

/**
 * Data object that represents all data for the BarChart.
 *
 * @author Philipp Jahoda
 */
public class HorizontalBarData extends BarData {

    public HorizontalBarData() {
        init();
    }

    public HorizontalBarData(IBarDataSet... dataSets) {
        super(dataSets);
        init();
    }

    public HorizontalBarData(List<IBarDataSet> dataSets) {
        super(dataSets);
        init();
    }

    private void init() {
        mOrientation = Orientation.HORIZONTAL;
    }
}
