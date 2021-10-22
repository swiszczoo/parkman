package cz.swisz.parkman;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.util.Map;

import cz.swisz.parkman.backend.ParkingData;
import cz.swisz.parkman.backend.PwrDataProvider;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread thread = new Thread()
        {
            @Override
            public void run() {
                PwrDataProvider test = new PwrDataProvider(
                        "https://iparking.pwr.edu.pl/modules/iparking/scripts/ipk_operations.php" );
                Map<Long, ParkingData> map = test.fetchData();
                if(map != null)
                {
                    Log.i("Parkman", String.valueOf(map.hashCode()));
                }
            }
        };
        thread.start();
    }
}