package cz.swisz.parkman.gui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Map;

import cz.swisz.parkman.R;
import cz.swisz.parkman.backend.DataProvider;
import cz.swisz.parkman.backend.DataProviderFactory;
import cz.swisz.parkman.backend.ParkingData;
import cz.swisz.parkman.backend.PwrDataProvider;

public class MainActivity extends AppCompatActivity {
    private LinearLayout m_panel;
    private DataProvider m_provider;
    private Map<Long, String> m_parkingNames;
    private Map<Long, ParkingFragment> m_fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_provider = DataProviderFactory.newDefaultProvider();

        m_panel = findViewById(R.id.parking_parent);
        setupKnownParkings();

        new Thread() {
            @Override
            public void run() {
                Map<Long, ParkingData> data = m_provider.fetchData();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateData(data);
                    }
                });
            }
        }.start();
    }

    private void setupKnownParkings() {
        m_parkingNames = m_provider.getParkingNames();
        m_fragments = new HashMap<>();

        for (Map.Entry<Long, String> parking : m_parkingNames.entrySet()) {
            ParkingFragment fragment = new ParkingFragment(this);
            fragment.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            fragment.setParkingName(parking.getValue());
            fragment.setPlaceCount(0);

            m_fragments.put(parking.getKey(), fragment);
            m_panel.addView(fragment);
        }
    }

    private void updateData(Map<Long, ParkingData> data) {
        for(Map.Entry<Long, ParkingData> entry : data.entrySet()) {
            if(m_fragments.containsKey(entry.getKey())) {
                m_fragments.get(entry.getKey()).setPlaceCount((int)entry.getValue().freeCount);
            }
        }
    }
}