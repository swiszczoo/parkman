package cz.swisz.parkman.gui;

import cz.swisz.parkman.backend.DataProvider;
import cz.swisz.parkman.backend.DataWatcher;
import cz.swisz.parkman.utils.RefCounter;

public class GlobalData {
    private static GlobalData s_inst;

    private RefCounter<DataProvider> m_provider;
    private RefCounter<DataWatcher> m_watcher;

    private GlobalData() {
        m_provider = new RefCounter<>(null);
        m_watcher = new RefCounter<>(null);
    }

    public static GlobalData getInstance() {
        if (s_inst == null) {
            s_inst = new GlobalData();
        }

        return s_inst;
    }

    public void reset() {
        m_provider = null;
        m_watcher = null;
    }

    public RefCounter<DataProvider> getProvider() {
        return m_provider;
    }

    public void setProvider(RefCounter<DataProvider> provider) {
        m_provider = provider;
    }

    public RefCounter<DataWatcher> getWatcher() {
        return m_watcher;
    }

    public void setWatcher(RefCounter<DataWatcher> watcher) {
        m_watcher = watcher;
    }
}
