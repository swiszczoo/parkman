package cz.swisz.parkman.backend;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class HistoryManager {
    private static HistoryManager m_instance;

    private File m_outDir;
    private Set<String> m_availableData;
    boolean m_init;

    private HistoryManager() {
        m_outDir = null;
        m_availableData = new TreeSet<>();
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

        enumerateFiles();
    }

    private void enumerateFiles() {
        try {
            File[] files = m_outDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    m_availableData.add(file.getName());
                }
            }
        } catch (SecurityException ignore) {
        }
    }

    public void updateCurrentData(Map<Long, ChartPoints> todayCharts) {
        String fileName = getCurrentFileName();
        File outFile = new File(m_outDir, fileName);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outFile))) {
            oos.writeObject(todayCharts);
            m_availableData.add(fileName);
        } catch (IOException e) {
            Log.e("PARKMAN", "Can't save parking data");
            e.printStackTrace();
        }
    }

    private String getCurrentFileName() {
        return getFileNameForDate(new Date());
    }

    private String getFileNameForDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return String.format(Locale.getDefault(), "%s.bin", format.format(date));
    }

    public boolean isDayDataAvailable(Date date) {
        String name = getFileNameForDate(date);
        return m_availableData.contains(name);
    }

    public ChartPoints getChartDataForDateAndPark(Date date, Long key) {
        if (!isDayDataAvailable(date)) {
            return null;
        }

        try {
            File f = new File(m_outDir, getFileNameForDate(date));
            if (!f.exists()) {
                return null;
            }

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                Map<Long, ChartPoints> allData = (Map<Long, ChartPoints>) ois.readObject();
                if (allData.containsKey(key)) {
                    return allData.get(key);
                }
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
