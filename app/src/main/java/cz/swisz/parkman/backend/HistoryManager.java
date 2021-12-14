package cz.swisz.parkman.backend;

import android.util.Log;

import java.io.File;

public class HistoryManager {
    private static HistoryManager m_instance;

    private File m_outDir;
    boolean m_init;

    private HistoryManager() {
        m_outDir = null;
        m_init = false;
    }

    public static HistoryManager getInstance() {
        if (m_instance == null)
            m_instance = new HistoryManager();
        return m_instance;
    }

    public void initialize(File sdcardRoot) {
        m_outDir = new File(sdcardRoot, "data");
        if (!m_outDir.mkdir()) {
            m_init = m_outDir.exists();
            if (m_init) {
                Log.i("PARKMAN",
                        "External storage dir already exists at " + m_outDir.getAbsolutePath());

            }
        } else {
            m_init = true;
            Log.i("PARKMAN",
                    "Initialized empty external storage dir at " + m_outDir.getAbsolutePath());
        }
    }
}
