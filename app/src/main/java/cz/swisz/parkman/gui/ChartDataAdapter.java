package cz.swisz.parkman.gui;

import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

public interface ChartDataAdapter {
    LineData constructDataSet(DatasetFormatter formatter);

    interface DatasetFormatter {
        void formatDataSet(LineDataSet dataSet);
    }
}
