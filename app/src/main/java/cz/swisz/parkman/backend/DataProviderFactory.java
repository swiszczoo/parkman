package cz.swisz.parkman.backend;

public final class DataProviderFactory {
    // Provide unavailable default constructor that does nothing
    private DataProviderFactory() {
    }

    public static DataProvider newDefaultProvider() {
        return new PwrDataProvider(
                "https://iparking.pwr.edu.pl/modules/iparking/scripts/ipk_operations.php");
    }
}
