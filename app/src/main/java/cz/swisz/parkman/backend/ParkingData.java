package cz.swisz.parkman.backend;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParkingData that = (ParkingData) o;
        return snapshotId == that.snapshotId
                && parkingId == that.parkingId
                && freeCount == that.freeCount
                && dataTimestamp.equals(that.dataTimestamp)
                && trend == that.trend;
    }

    @Override
    public int hashCode() {
        return Objects.hash(snapshotId, parkingId, dataTimestamp, trend, freeCount);
    }
}
