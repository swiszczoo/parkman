package cz.swisz.parkman.gui;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import cz.swisz.parkman.R;
import cz.swisz.parkman.backend.DataProvider;
import cz.swisz.parkman.backend.DataProviderFactory;
import cz.swisz.parkman.backend.DataWatcher;
import cz.swisz.parkman.backend.GlobalData;
import cz.swisz.parkman.backend.Observable;
import cz.swisz.parkman.backend.Observer;

public class FetchService extends Service implements Observer {
    private static final String NOTIFICATION_CHANNEL = "parkman.service";
    private boolean m_ready;

    private DataWatcher m_watcher;
    private DataProvider m_provider;

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

    }
}
