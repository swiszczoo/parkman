package cz.swisz.parkman.gui;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cz.swisz.parkman.R;
import cz.swisz.parkman.backend.DataProvider;
import cz.swisz.parkman.backend.DataProviderFactory;
import cz.swisz.parkman.backend.DataWatcher;
import cz.swisz.parkman.backend.GlobalData;
import cz.swisz.parkman.backend.Observable;
import cz.swisz.parkman.backend.Observer;
import cz.swisz.parkman.backend.ParkingData;
import cz.swisz.parkman.utils.RefCounter;

public class FetchService extends Service implements Observer {
    public static final String NOTIFICATION_CHANNEL = "parkman.service";
    public static final String FREEPLACE_CHANNEL = "parkman.places";

    private static final String BROADCAST_STOP = "cz.swisz.parkman.ACTION_STOP";
    private static final String WAKELOCK_TAG = "Parkman:service";
    private static final int FREEPLACE_NOTIFICATION_ID = 1001;
    private boolean m_ready;

    private DataWatcher m_watcher;
    private DataProvider m_provider;
    private Map<Long, Boolean> m_previousData;
    private BroadcastReceiver m_receiver;
    private PowerManager.WakeLock m_wakelock;

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

        if (m_receiver == null) {
            m_receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    stop();
                }
            };

            registerReceiver(m_receiver, new IntentFilter(BROADCAST_STOP));

            Intent stopIntent = new Intent();
            stopIntent.setAction(BROADCAST_STOP);

            NotificationCompat.Action.Builder actionBuilder = new NotificationCompat.Action.Builder(
                    null,
                    getString(R.string.action_stop),
                    PendingIntent.getBroadcast(this, 1, stopIntent, PendingIntent.FLAG_ONE_SHOT)
            );

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
                    .addAction(actionBuilder.build())
                    .build();

            startForeground(1, notification);
            setupService();
        }

        return START_REDELIVER_INTENT;
    }

    private void setupService() {
        if (!GlobalData.getInstance().getProvider().isAllocated()) {
            m_provider = DataProviderFactory.newDefaultProvider();
            GlobalData.getInstance().setProvider(new RefCounter<>(m_provider));
        }
        else {
            m_provider = GlobalData.getInstance().getProvider().acquire();
        }

        if (!GlobalData.getInstance().getWatcher().isAllocated()) {
            m_watcher = new DataWatcher();
            m_watcher.start(m_provider);
            GlobalData.getInstance().setWatcher(new RefCounter<>(m_watcher));
        }
        else {
            m_watcher = GlobalData.getInstance().getWatcher().acquire();
        }

        m_watcher.addObserver(this);

        if (m_wakelock == null) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            m_wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG);
            m_wakelock.acquire(3 * 60 * 1000L /*3 minutes*/);
        }

        m_ready = true;
    }

    public void stop() {
        if (m_ready) {
            Log.i("PARKMAN", "Stopping service");

            m_ready = false;

            stopForeground(true);
            stopSelf();
        }

        if (m_wakelock != null) {
            m_wakelock.release();
            m_wakelock = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (m_wakelock != null) {
            m_wakelock.release();
            m_wakelock = null;
        }

        unregisterReceiver(m_receiver);

        m_ready = false;

        GlobalData.getInstance().getWatcher().release(m_watcher);
        GlobalData.getInstance().getProvider().release(m_provider);
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

    private void renewWakelock() {
        if (m_wakelock != null) {
            m_wakelock.acquire(5 * 60 * 1000L /*5 minutes*/);
        }
    }

    private void updateState() {
        renewWakelock();

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

    @SuppressLint("UnspecifiedImmutableFlag")
    private void showNewPlacesScreen(Long key) {
        Log.i("FetchService", "New places available");

        Intent intent = new Intent(this, ChangeAvailabilityActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_FROM_BACKGROUND
                | Intent.FLAG_ACTIVITY_NO_HISTORY
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.putExtra(ChangeAvailabilityActivity.Extras.ALL_OCCUPIED, false);
        intent.putExtra(ChangeAvailabilityActivity.Extras.PARK_NAME, m_provider.getParkNames().get(key));
        intent.putExtra(ChangeAvailabilityActivity.Extras.NOTIFICATION_TO_DISMISS, FREEPLACE_NOTIFICATION_ID);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            startActivity(intent);
        } else {
            PendingIntent pending = PendingIntent.getActivity(this, 2, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            Notification notification = new Notification.Builder(this, FREEPLACE_CHANNEL)
                    .setSmallIcon(R.drawable.baseline_sentiment_very_satisfied_24)
                    .setLargeIcon(BitmapFactory.decodeResource(
                            getResources(), R.drawable.baseline_sentiment_very_satisfied_48))
                    .setColorized(true)
                    .setColor(getResources().getColor(R.color.available, getTheme()))
                    .setContentTitle(getResources().getString(R.string.freeplace_new_title))
                    .setContentText(String.format(Locale.getDefault(),
                            getResources().getString(R.string.tts_few),
                            m_provider.getParkNames().get(key)))
                    .setFullScreenIntent(pending, true)
                    .build();

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.notify(FREEPLACE_NOTIFICATION_ID, notification);
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private void showNoPlacesScreen(Long key) {
        Log.i("FetchService", "Places have ended");

        Intent intent = new Intent(this, ChangeAvailabilityActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_FROM_BACKGROUND
                | Intent.FLAG_ACTIVITY_NO_HISTORY
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.putExtra(ChangeAvailabilityActivity.Extras.ALL_OCCUPIED, true);
        intent.putExtra(ChangeAvailabilityActivity.Extras.PARK_NAME, m_provider.getParkNames().get(key));
        intent.putExtra(ChangeAvailabilityActivity.Extras.NOTIFICATION_TO_DISMISS, FREEPLACE_NOTIFICATION_ID);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            startActivity(intent);
        } else {
            PendingIntent pending = PendingIntent.getActivity(this, 2, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            Notification notification = new Notification.Builder(this, FREEPLACE_CHANNEL)
                    .setSmallIcon(R.drawable.baseline_sentiment_very_dissatisfied_24)
                    .setLargeIcon(BitmapFactory.decodeResource(
                            getResources(), R.drawable.baseline_sentiment_very_dissatisfied_48))
                    .setColorized(true)
                    .setColor(getResources().getColor(R.color.available, getTheme()))
                    .setContentTitle(getResources().getString(R.string.freeplace_end_title))
                    .setContentText(String.format(Locale.getDefault(),
                            getResources().getString(R.string.tts_no_more),
                            m_provider.getParkNames().get(key)))
                    .setFullScreenIntent(pending, true)
                    .build();

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.notify(FREEPLACE_NOTIFICATION_ID, notification);
        }
    }
}
