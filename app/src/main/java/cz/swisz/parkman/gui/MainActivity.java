package cz.swisz.parkman.gui;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cz.swisz.parkman.R;
import cz.swisz.parkman.backend.DataProvider;
import cz.swisz.parkman.backend.DataProviderFactory;
import cz.swisz.parkman.backend.FetchException;
import cz.swisz.parkman.backend.ParkingData;

public class MainActivity extends AppCompatActivity {
    private DataProvider m_provider;
    private Map<Long, String> m_parkingNames;

    private Map<Long, ParkingFragment> m_fragments;
    private LinearLayout m_panel;
    private View m_refreshOverlay;
    private View m_errorOverlay;
    private Timer m_timer;

    private ExecutorService m_executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_provider = DataProviderFactory.newDefaultProvider();
        m_executor = Executors.newFixedThreadPool(4);

        m_panel = findViewById(R.id.parking_parent);
        m_refreshOverlay = findViewById(R.id.data_refreshing);
        m_errorOverlay = findViewById(R.id.data_fetch_error);
        m_timer = new Timer();

        setupKnownParkings();
        setupFixedIntervalRefreshJob();

        postUpdateDataJob(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        m_executor.shutdownNow();
        m_timer.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_refresh) {
            postUpdateDataJob(true);
            return true;
        }

        return false;
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

    private void setupFixedIntervalRefreshJob() {
        m_timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> postUpdateDataJob(false));
            }
        }, 0, m_provider.getSuggestedRefreshTimeInMs());
    }

    private void postUpdateDataJob(final boolean userRequested) {
        if(userRequested) {
            m_refreshOverlay.setVisibility(View.VISIBLE);
            m_errorOverlay.setVisibility(View.GONE);
        }

        m_executor.submit(() -> {
            Log.i("Parkman", "Fetching data...");

            Map<Long, ParkingData> data = null;
            try {
                data = m_provider.fetchData();
            }
            catch(FetchException e) {
                e.printStackTrace();
            }

            final Map<Long, ParkingData> finalData = data;

            runOnUiThread(() -> {
                if(finalData != null) {
                    updateData(finalData);
                } else if(userRequested) {
                    m_errorOverlay.setVisibility(View.VISIBLE);
                }
                m_refreshOverlay.setVisibility(View.GONE);
            });
        });
    }

    private void updateData(Map<Long, ParkingData> data) {
        for(Map.Entry<Long, ParkingData> entry : data.entrySet()) {
            if(m_fragments.containsKey(entry.getKey())) {
                m_fragments.get(entry.getKey()).setPlaceCount((int)entry.getValue().freeCount);
            }
        }
    }
}