package cz.swisz.parkman.utils;

import android.util.Log;

import java.io.Closeable;
import java.io.IOException;

public class RefCounter<T extends Closeable> {
    private T m_obj;
    int m_refs;

    public RefCounter(T object) {
        m_obj = object;
        m_refs = 1;
    }

    public synchronized boolean isAllocated() {
        return m_obj != null;
    }

    public synchronized T acquire() {
        m_refs++;
        return m_obj;
    }

    public synchronized T get() {
        return m_obj;
    }

    public synchronized void release(T obj) {
        m_refs--;

        if (m_refs == 0) {
            Log.d("RefCounter", "Releasing object " + obj.toString());
            try {
                obj.close();
                obj = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
