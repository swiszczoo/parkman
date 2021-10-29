package cz.swisz.parkman.backend;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataWatcher implements Observable {
    private Set<Observer> m_observers;
    private DataProvider m_provider;
    private Timer m_timer;
    private Map<Long, ParkingData> m_lastSnapshot;
    private ExecutorService m_executor;

    public DataWatcher() {
        m_observers = new HashSet<>();
        m_provider = null;
        m_timer = null;
        m_lastSnapshot = null;

        m_executor = Executors.newSingleThreadExecutor();
    }

    public void start(DataProvider providerToWatch) {
        m_provider = providerToWatch;

        long interval = m_provider.getSuggestedRefreshTimeInMs();

        m_timer = new Timer();
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateNow();
            }
        }, interval, interval);
    }

    private void processData() throws FetchException {
        Map<Long, ParkingData> data = m_provider.fetchData();

        synchronized(this) {
            if (m_lastSnapshot != null) {
                boolean different = false;

                for (Long key : data.keySet()) {
                    ParkingData newData = data.get(key);
                    ParkingData oldData = m_lastSnapshot.get(key);

                    if (newData != null) {
                        different |= !newData.equals(oldData);
                    }
                }

                if (different) {
                    m_lastSnapshot = data;
                    notifyObservers();
                }
            } else {
                m_lastSnapshot = data;
                notifyObservers();
            }
        }
    }

    public synchronized Map<Long, ParkingData> getCurrentData() {
        return m_lastSnapshot;
    }

    public void stop() {
        if (m_provider != null) {
            assert m_timer != null;

            m_timer.cancel();

            m_provider = null;
            m_timer = null;
        }
    }

    public void updateNow() {
        m_executor.submit(() -> {
            try {
                processData();
            } catch (FetchException ignore) {
            }
        });
    }

    @Override
    public void addObserver(Observer observer) {
        m_observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        m_observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : m_observers) {
            observer.onStateChanged(this);
        }
    }
}
