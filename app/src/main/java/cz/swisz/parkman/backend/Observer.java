package cz.swisz.parkman.backend;

public interface Observer {
    void onStateChanged(Observable subject);
}
