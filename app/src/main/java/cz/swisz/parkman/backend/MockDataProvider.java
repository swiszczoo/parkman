package cz.swisz.parkman.backend;

import java.util.HashMap;
import java.util.Map;

public class MockDataProvider implements DataProvider {
    int current = 1;

    @Override
    public long getSuggestedRefreshTimeInMs() {
        return 1000;
    }

    @Override
    public Map<Long, ParkingData> fetchData() throws FetchException {
        current--;
        if (current == -1) {
            current = 100;
        }

        Map<Long, ParkingData> result = new HashMap<>();
        ParkingData data = new ParkingData(0,1L,
                "AAAAA", ParkingData.Trend.DOWN, current);
        result.put(1L, data);

        return result;
    }

    @Override
    public Map<Long, String> getParkNames() {
        Map<Long, String> parks = new HashMap<>();
        parks.put(1L, "Test");
        return parks;
    }
}
