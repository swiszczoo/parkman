package cz.swisz.parkman.backend;

public class ParkingData {
    enum Trend
    {
        THE_SAME,
        UP,
        DOWN
    }

    public long snapshotId;
    public long parkingId;
    public String dataTimestamp; // ISO 8601
    public Trend trend;
    public long freeCount;

    ParkingData( long snapshotId, long parkingId,
                 String dataTimestamp, Trend trend, long freeCount )
    {
        this.snapshotId = snapshotId;
        this.parkingId = parkingId;
        this.dataTimestamp = dataTimestamp;
        this.trend = trend;
        this.freeCount = freeCount;
    }
}
