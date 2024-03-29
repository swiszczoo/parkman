package cz.swisz.parkman.gui;

import android.content.res.Resources;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import cz.swisz.parkman.backend.ChartPoints;
import cz.swisz.parkman.backend.DataProvider;
import cz.swisz.parkman.backend.ParkingData;

public class ProviderDataAdapter implements ChartDataAdapter {
    private ParkingData m_snapshot;

    public ProviderDataAdapter(ParkingData snapshot) {
        this.m_snapshot = snapshot;
    }

    @Override
    public LineData constructDataSet(DatasetFormatter formatter) {
        ChartPoints cp = m_snapshot.chart;
        List<ChartPoints.DataPoint> data;
        if (cp != null) {
            data = cp.getPoints();
        } else {
            return null;
        }

        LineDataSet lineDataSet = new LineDataSet(new ArrayList<>(), "");

        for(ChartPoints.DataPoint point : data) {
            float hourDecimal = point.hours + point.minutes / 60f;
            lineDataSet.addEntry(new Entry(hourDecimal, point.quantity));
        }

        formatter.formatDataSet(lineDataSet);

        return new LineData(lineDataSet);
    }
}
