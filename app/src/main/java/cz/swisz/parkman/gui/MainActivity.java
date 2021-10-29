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
import cz.swisz.parkman.backend.DataWatcher;
import cz.swisz.parkman.backend.FetchException;
import cz.swisz.parkman.backend.Observable;
import cz.swisz.parkman.backend.Observer;
import cz.swisz.parkman.backend.ParkingData;

public class MainActivity extends AppCompatActivity implements Observer {
    private static final String TAG = "PARKMAN";

    private DataProvider m_provider;
    private Map<Long, String> m_parkingNames;

    private Map<Long, ParkingFragment> m_fragments;
    private LinearLayout m_panel;
    private View m_refreshOverlay;
    private View m_errorOverlay;
    private DataWatcher m_watcher;
    private ExecutorService m_executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_provider = DataProviderFactory.newDefaultProvider();

        m_panel = findViewById(R.id.parking_parent);
        m_refreshOverlay = findViewById(R.id.data_refreshing);
        m_errorOverlay = findViewById(R.id.data_fetch_error);
        m_executor = Executors.newSingleThreadExecutor();

        setupKnownParkings();
        setupFixedIntervalRefreshJob();

        postUpdateDataJob();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(m_watcher != null) {
            m_watcher.stop();
        }
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
            postUpdateDataJob();
            return true;
        }

        return false;
    }

    private void setupKnownParkings() {
        m_parkingNames = m_provider.getParkNames();
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
        m_watcher = new DataWatcher();
        m_watcher.addObserver(this);
        m_watcher.start(m_provider);
    }

    private void postUpdateDataJob() {
        m_refreshOverlay.setVisibility(View.VISIBLE);
        m_errorOverlay.setVisibility(View.GONE);

        m_executor.submit(() -> {
            Log.i(TAG, "Fetching data...");

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
                } else {
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

    @Override
    public void onStateChanged(Observable subject) {
        if (subject == m_watcher) {
            Log.i(TAG, "New data arrived!");
            runOnUiThread(() -> updateData(m_watcher.getCurrentData()));
        }
    }
}