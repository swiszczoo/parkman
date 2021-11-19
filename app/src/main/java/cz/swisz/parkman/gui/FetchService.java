package cz.swisz.parkman.gui;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.swisz.parkman.R;
import cz.swisz.parkman.backend.DataProvider;
import cz.swisz.parkman.backend.DataProviderFactory;
import cz.swisz.parkman.backend.DataWatcher;
import cz.swisz.parkman.backend.GlobalData;
import cz.swisz.parkman.backend.Observable;
import cz.swisz.parkman.backend.Observer;
import cz.swisz.parkman.backend.ParkingData;

public class FetchService extends Service implements Observer {
    private static final String NOTIFICATION_CHANNEL = "parkman.service";
    private boolean m_ready;

    private DataWatcher m_watcher;
    private DataProvider m_provider;
    private Map<Long, Boolean> m_previousData;

    public static class FetchBinder extends Binder {
        private final FetchService m_service;

        private FetchBinder(FetchService service) {
            m_service = service;
        }

        public FetchService getService() {
            return m_service;
        }
    }

    public FetchService() {
        m_ready = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new FetchBinder(this);
    }

    @Override
    @SuppressLint("UnspecifiedImmutableFlag")
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent activityIntent = new Intent(this, MainActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setContentTitle(getResources().getString(R.string.service_title))
                .setContentText(getResources().getString(R.string.service_description))
                .setOngoing(true)
                .setColorized(true)
                .setColor(getResources().getColor(R.color.purple_500))
                .setSmallIcon(R.drawable.baseline_podcasts_24)
                .setContentIntent(PendingIntent.getActivity(
                        this,
                        1,
                        activityIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT))
                .build();

        startForeground(1, notification);
        setupService();

        return START_REDELIVER_INTENT;
    }

    private void setupService() {
        m_provider = DataProviderFactory.newDefaultProvider();
        m_watcher = new DataWatcher();
        m_watcher.addObserver(this);
        m_watcher.start(m_provider);

        GlobalData.getInstance().setWatcher(m_watcher);
        GlobalData.getInstance().setProvider(m_provider);

        m_ready = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (m_watcher != null) {
            m_watcher.stop();
        }

        m_ready = false;
        GlobalData.getInstance().setWatcher(null);
        GlobalData.getInstance().setProvider(null);
    }

    public boolean isServiceReady() {
        return m_ready;
    }

    @Override
    public void onStateChanged(Observable subject) {
        if (subject == m_watcher) {
            if (m_previousData == null) {
                m_previousData = getParkPlaceState();
            } else {
                updateState();
            }
        }
    }

    private Map<Long, Boolean> getParkPlaceState() {
        Map<Long, ParkingData> data = m_watcher.getCurrentData();
        Map<Long, Boolean> map = new HashMap<>();

        for (Map.Entry<Long, ParkingData> entry : data.entrySet()) {
            map.put(entry.getKey(), entry.getValue().freeCount > 0);
        }

        return map;
    }

    private void updateState() {
        Map<Long, Boolean> newState = getParkPlaceState();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String ignored = prefs.getString(MainActivity.PrefKeys.IGNORED_PARKS, "");
        ArrayList<Long> ignoredList = Utils.parseIgnoredParks(ignored);

        for (Long parkKey : m_previousData.keySet()) {
            Boolean old = m_previousData.get(parkKey);
            Boolean neu = newState.get(parkKey);

            if (old == null || neu == null)
                continue;

            if (ignoredList.contains(parkKey))
                continue;

            if (neu && !old) {
                showNewPlacesScreen(parkKey);
            } else if (!neu && old) {
                showNoPlacesScreen(parkKey);
            }
        }

        m_previousData = newState;
    }

    private void showNewPlacesScreen(Long key) {
        Log.i("FetchService", "New places available");

        Intent intent = new Intent(this, ChangeAvailabilityActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ChangeAvailabilityActivity.Extras.ALL_OCCUPIED, false);
        intent.putExtra(ChangeAvailabilityActivity.Extras.PARK_NAME, m_provider.getParkNames().get(key));
        startActivity(intent);
    }

    private void showNoPlacesScreen(Long key) {
        Log.i("FetchService", "Places have ended");

        Intent intent = new Intent(this, ChangeAvailabilityActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ChangeAvailabilityActivity.Extras.ALL_OCCUPIED, true);
        intent.putExtra(ChangeAvailabilityActivity.Extras.PARK_NAME, m_provider.getParkNames().get(key));
        startActivity(intent);
    }
}
