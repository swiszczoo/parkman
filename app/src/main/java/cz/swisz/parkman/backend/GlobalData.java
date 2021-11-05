package cz.swisz.parkman.backend;

public class GlobalData {
    private static GlobalData s_inst;

    private DataProvider m_provider;
    private DataWatcher m_watcher;

    private GlobalData() {
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

    public DataProvider getProvider() {
        return m_provider;
    }

    public void setProvider(DataProvider provider) {
        m_provider = provider;
    }

    public DataWatcher getWatcher() {
        return m_watcher;
    }

    public void setWatcher(DataWatcher watcher) {
        m_watcher = watcher;
    }
}
