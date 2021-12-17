package cz.swisz.parkman.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.Map;

import cz.swisz.parkman.R;
import cz.swisz.parkman.backend.GlobalData;
import cz.swisz.parkman.backend.ParkingData;

public class ParkDetailsActivity extends Activity {
    public static String EXTRA_PARK_ID = "park_id";

    private TextView m_titleLabel;
    private LineChart m_chart;

    private long m_parkId;
    private String m_parkName;
    private ParkingData m_data;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_details);
        if (readIntentExtra()) {
            setupUI();
        }
    }

    private boolean readIntentExtra() {
        Intent intent = getIntent();

        if (intent != null) {
            m_parkId = intent.getLongExtra(EXTRA_PARK_ID, -1);
        } else {
            m_parkId = -1;
        }

        Map<Long, ParkingData> data = GlobalData.getInstance().getWatcher().getCurrentData();
        if (data != null && data.containsKey(m_parkId)) {
            m_data = data.get(m_parkId);
            m_parkName = GlobalData.getInstance().getProvider().getParkNames().get(m_parkId);

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

        m_chart = findViewById(R.id.park_chart);

        renderChart(new ProviderDataAdapter(getResources().getColor(R.color.teal_700), m_data));
    }

    private void renderChart(ProviderDataAdapter data) {
        m_chart.setData(data.constuctDataSet());
    }
}
