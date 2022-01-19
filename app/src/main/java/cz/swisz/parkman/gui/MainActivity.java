package cz.swisz.parkman.gui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cz.swisz.parkman.R;
import cz.swisz.parkman.backend.DataProvider;
import cz.swisz.parkman.backend.DataProviderFactory;
import cz.swisz.parkman.backend.DataWatcher;
import cz.swisz.parkman.backend.FetchException;
import cz.swisz.parkman.backend.GlobalData;
import cz.swisz.parkman.backend.Observable;
import cz.swisz.parkman.backend.Observer;
import cz.swisz.parkman.backend.ParkingData;
import cz.swisz.parkman.gui.views.ParkingFragment;
import cz.swisz.parkman.utils.RefCounter;

public class MainActivity extends AppCompatActivity implements Observer {
    public static final class PrefKeys {
        public static String IGNORED_PARKS = "ignored_parks";
    }

    private static final String TAG = "PARKMAN";

    private Map<Long, String> m_parkNames;
    private ArrayList<Long> m_ignoredParks;

    private Map<Long, ParkingFragment> m_fragments;
    private LinearLayout m_panel;
    private View m_refreshOverlay;
    private View m_errorOverlay;
    private ExecutorService m_executor;
    private ServiceConnection m_connection = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_ignoredParks = new ArrayList<>();

        if (!GlobalData.getInstance().getProvider().isAllocated())
        {
            GlobalData.getInstance().setProvider(
                    new RefCounter<>(DataProviderFactory.newDefaultProvider()));
        }
        else {
            GlobalData.getInstance().getProvider().acquire();
        }

        m_panel = findViewById(R.id.parking_parent);
        m_refreshOverlay = findViewById(R.id.data_refreshing);
        m_errorOverlay = findViewById(R.id.data_fetch_error);
        m_executor = Executors.newSingleThreadExecutor();

        createNotificationChannel();
        loadSharedPrefs();
        setupKnownParks();
        setupFixedIntervalRefreshJob();

        postUpdateDataJob();
        initService();

        // Test
        Intent serv = new Intent(this, FetchService.class);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serv);
            } else {
                startService(serv);
            }
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Destroying main activity");

        DataProvider provider = GlobalData.getInstance().getProvider().get();
        GlobalData.getInstance().getProvider().release(provider);

        if (GlobalData.getInstance().getWatcher().isAllocated()) {
            DataWatcher watcher = GlobalData.getInstance().getWatcher().get();
            watcher.removeObserver(this);

            GlobalData.getInstance().getWatcher().release(watcher);
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
        if (item.getItemId() == R.id.menu_refresh) {
            postUpdateDataJob();
            return true;
        }

        return false;
    }

    private void setupKnownParks() {
        m_parkNames = GlobalData.getInstance().getProvider().get().getParkNames();
        m_fragments = new HashMap<>();

        for (Map.Entry<Long, String> parking : m_parkNames.entrySet()) {
            ParkingFragment fragment = new ParkingFragment(this);
            fragment.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            fragment.setParkingName(parking.getValue());
            fragment.setPlaceCount(0);
            fragment.setDisabled(m_ignoredParks.contains(parking.getKey()));

            fragment.setOnClickListener(v -> {
                Long key = parking.getKey();
                if (m_ignoredParks.contains(key)) {
                    m_ignoredParks.remove(key);
                    fragment.setDisabled(false);
                } else {
                    m_ignoredParks.add(key);
                    fragment.setDisabled(true);
                }

                saveSharedPrefs();
            });

            fragment.setOnLongClickListener(v -> {
                Long key = parking.getKey();
                openParkDetails(key);

                return true;
            });

            m_fragments.put(parking.getKey(), fragment);
            m_panel.addView(fragment);
        }
    }

    private void setupFixedIntervalRefreshJob() {
        if (!GlobalData.getInstance().getWatcher().isAllocated()) {
            DataWatcher watcher = new DataWatcher();
            watcher.addObserver(this);
            watcher.start(GlobalData.getInstance().getProvider().get());

            GlobalData.getInstance().setWatcher(new RefCounter<>(watcher));
        }
        else {
            GlobalData.getInstance().getWatcher().acquire();
            GlobalData.getInstance().getWatcher().get().addObserver(this);
        }

        GlobalData.getInstance().getWatcher().get().updateNow();
    }

    private void postUpdateDataJob() {
        m_refreshOverlay.setVisibility(View.VISIBLE);
        m_errorOverlay.setVisibility(View.GONE);

        m_executor.submit(() -> {
            Log.i(TAG, "Fetching data...");

            Map<Long, ParkingData> data = null;
            try {
                data = GlobalData.getInstance().getProvider().get().fetchData();
            } catch (FetchException e) {
                e.printStackTrace();
            }

            final Map<Long, ParkingData> finalData = data;

            runOnUiThread(() -> {
                if (finalData != null) {
                    updateData(finalData);
                } else {
                    m_errorOverlay.setVisibility(View.VISIBLE);
                }
                m_refreshOverlay.setVisibility(View.GONE);
            });
        });
    }

    private void updateData(Map<Long, ParkingData> data) {
        for (Map.Entry<Long, ParkingData> entry : data.entrySet()) {
            if (m_fragments.containsKey(entry.getKey())) {
                m_fragments.get(entry.getKey()).setPlaceCount((int) entry.getValue().freeCount);
            }
        }
    }

    private void loadSharedPrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String keyString = prefs.getString(PrefKeys.IGNORED_PARKS, "");
        m_ignoredParks = Utils.parseIgnoredParks(keyString);
    }

    private void saveSharedPrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String serialized = Utils.serializeIgnoredParks(m_ignoredParks);

        prefs.edit().putString(PrefKeys.IGNORED_PARKS, serialized).apply();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    FetchService.NOTIFICATION_CHANNEL,
                    getResources().getString(R.string.service_title),
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationChannel placeChannel = new NotificationChannel(
                    FetchService.FREEPLACE_CHANNEL,
                    getResources().getString(R.string.freeplace_channel),
                    NotificationManager.IMPORTANCE_HIGH
            );
            placeChannel.setSound(null, null);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
            manager.createNotificationChannel(placeChannel);
        }
    }

    private void initService() {
        Intent service = new Intent(this, FetchService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(service);
        } else {
            startService(service);
        }

        m_connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(TAG, "ParkMan service connected");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.i(TAG, "ParkMan service disconnected");
            }
        };

        bindService(service, m_connection, BIND_ABOVE_CLIENT);
    }

    @Override
    public synchronized void onStateChanged(Observable subject) {
        if (subject == GlobalData.getInstance().getWatcher().get()) {
            Log.i(TAG, "New data arrived!");
            runOnUiThread(() -> updateData(GlobalData.getInstance().getWatcher().get().getCurrentData()));
        }
    }

    private void openParkDetails(Long key) {
        Intent intt = new Intent(this, ParkDetailsActivity.class);
        intt.putExtra(ParkDetailsActivity.EXTRA_PARK_ID, key.longValue());
        startActivity(intt);
    }
}