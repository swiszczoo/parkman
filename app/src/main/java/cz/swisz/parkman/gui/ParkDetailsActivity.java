package cz.swisz.parkman.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import cz.swisz.parkman.R;
import cz.swisz.parkman.backend.DataProvider;
import cz.swisz.parkman.backend.DataWatcher;
import cz.swisz.parkman.backend.HistoryManager;
import cz.swisz.parkman.backend.ParkingData;
import cz.swisz.parkman.gui.views.CalendarView;

public class ParkDetailsActivity extends Activity implements ChartDataAdapter.DatasetFormatter {
    public static String EXTRA_PARK_ID = "park_id";

    private TextView m_titleLabel;
    private LineChart m_chart;
    private CalendarView m_calendar;
    private TextView m_calendarLabel;

    private long m_parkId;
    private String m_parkName;
    private ParkingData m_data;

    private Calendar m_shownChart;

    private final ValueFormatter m_formatter = new ValueFormatter() {
        @Override
        public String getFormattedValue(float value) {
            int hours = (int)Math.floor(value);
            int minutes = (int)Math.floor((value - hours) * 60);
            return String.format(Locale.getDefault(), "%d:%02d", hours, minutes);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_details);

        GlobalData.getInstance().getProvider().acquire();
        GlobalData.getInstance().getWatcher().acquire();

        if (readIntentExtra()) {
            setupUI();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        DataProvider provider = GlobalData.getInstance().getProvider().get();
        DataWatcher watcher = GlobalData.getInstance().getWatcher().get();

        GlobalData.getInstance().getProvider().release(provider);
        GlobalData.getInstance().getWatcher().release(watcher);
    }

    private boolean readIntentExtra() {
        Intent intent = getIntent();

        if (intent != null) {
            m_parkId = intent.getLongExtra(EXTRA_PARK_ID, -1);
        } else {
            m_parkId = -1;
        }

        Map<Long, ParkingData> data = GlobalData.getInstance().getWatcher().get().getCurrentData();
        if (data != null && data.containsKey(m_parkId)) {
            m_data = data.get(m_parkId);
            m_parkName = GlobalData.getInstance().getProvider().get().getParkNames().get(m_parkId);

            assert m_data != null;
            assert m_parkName != null;

            return true;
        } else {
            return false;
        }
    }

    private void setupUI() {
        m_titleLabel = findViewById(R.id.label_park_name);
        m_titleLabel.setText(m_parkName);

        m_shownChart = Calendar.getInstance();


        m_calendar = findViewById(R.id.calendar_view);
        m_calendar.setOnDateChangeListener((d,m,y) -> {
            boolean changed;

            changed = m_shownChart.get(Calendar.DAY_OF_MONTH) != d;
            m_shownChart.set(Calendar.DAY_OF_MONTH, d);

            changed |= m_shownChart.get(Calendar.MONTH) != m - 1;
            m_shownChart.set(Calendar.MONTH, m - 1);

            changed |= m_shownChart.get(Calendar.YEAR) != y;
            m_shownChart.set(Calendar.YEAR, y);

            if (changed)
                updateChart();
        });

        m_calendarLabel = findViewById(R.id.month_text);
        m_calendarLabel.setText(m_calendar.getDisplayTitle());

        findViewById(R.id.prev_month).setOnClickListener(v -> {
            m_calendar.setDisplayedMonth(m_calendar.getDisplayedMonth() - 1);
            m_calendarLabel.setText(m_calendar.getDisplayTitle());
        });

        findViewById(R.id.next_month).setOnClickListener(v -> {
            m_calendar.setDisplayedMonth(m_calendar.getDisplayedMonth() + 1);
            m_calendarLabel.setText(m_calendar.getDisplayTitle());
        });

        m_chart = findViewById(R.id.park_chart);
        m_chart.setScaleEnabled(false);
        m_chart.setDrawMarkers(false);
        m_chart.setNoDataText(getString(R.string.chart_no_data));
        m_chart.getDescription().setEnabled(false);
        m_chart.getAxis(YAxis.AxisDependency.LEFT).setAxisMinimum(0);
        m_chart.getAxis(YAxis.AxisDependency.RIGHT).setAxisMinimum(0);
        m_chart.getXAxis().setValueFormatter(m_formatter);
        m_chart.getXAxis().setAxisMinimum(6);
        m_chart.getXAxis().setAxisMaximum(22);
        m_chart.getXAxis().setLabelCount(8);

        updateChart();
    }

    private void updateChart() {
        m_chart.clear();

        if (DateUtils.isToday(m_shownChart.getTimeInMillis())) {
            renderChart(new ProviderDataAdapter(m_data));
        } else {
            renderChart(new HistoricalDataAdapter(
                    HistoryManager.getInstance(), m_shownChart.getTime(), m_parkId));
        }
    }

    private void renderChart(ChartDataAdapter data) {
        m_chart.setData(data.constructDataSet(this));
        m_chart.animateX(1500, Easing.EaseOutCubic);
    }

    @Override
    public void formatDataSet(LineDataSet dataSet) {
        int color = getResources().getColor(R.color.teal_700);

        dataSet.setColor(color);
        dataSet.setDrawCircles(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(color);
        dataSet.setFillAlpha(255);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(false);
        dataSet.setLabel(getString(R.string.free_hour));
        dataSet.setCubicIntensity(0.75f);
    }
}
