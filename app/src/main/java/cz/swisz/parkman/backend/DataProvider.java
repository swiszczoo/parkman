package cz.swisz.parkman.backend;

import java.io.Closeable;
import java.util.Map;

public interface DataProvider extends Closeable {
    long getSuggestedRefreshTimeInMs();
    Map<Long, ParkingData> fetchData() throws FetchException;
    Map<Long, String> getParkNames();
}
