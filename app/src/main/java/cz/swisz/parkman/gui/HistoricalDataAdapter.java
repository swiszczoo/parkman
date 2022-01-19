package cz.swisz.parkman.gui;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.swisz.parkman.backend.ChartPoints;
import cz.swisz.parkman.backend.HistoryManager;

public class HistoricalDataAdapter implements ChartDataAdapter{
    private final HistoryManager m_mgr;
    private final Date m_date;
    private final long m_key;

    public HistoricalDataAdapter(HistoryManager manager, Date date, long parkingKey) {
        m_mgr = manager;
        m_date = date;
        m_key = parkingKey;
    }

    private ChartPoints retrieveChartObject() {
        if (!m_mgr.isDayDataAvailable(m_date)) {
            return null;
        }

        return m_mgr.getChartDataForDateAndPark(m_date, m_key);
    }

    @Override
    public LineData constructDataSet(DatasetFormatter formatter) {
        ChartPoints cp = retrieveChartObject();
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
