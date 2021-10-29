package cz.swisz.parkman.backend;

public class DataProviderFactory {
    public static DataProvider newDefaultProvider()
    {
        return new PwrDataProvider(
                "https://iparking.pwr.edu.pl/modules/iparking/scripts/ipk_operations.php");
    }
}
