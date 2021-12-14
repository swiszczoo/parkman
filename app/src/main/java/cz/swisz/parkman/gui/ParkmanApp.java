package cz.swisz.parkman.gui;

import android.app.Application;

import cz.swisz.parkman.backend.HistoryManager;

public class ParkmanApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HistoryManager.getInstance().initialize(getApplicationContext().getExternalCacheDir());
    }
}
