package cz.swisz.parkman.backend;

import java.util.Map;

public interface DataProvider {
    long getSuggestedRefreshTimeInMs();
    Map<Long, ParkingData> fetchData() throws FetchException;
    Map<Long, String> getParkingNames();
}
