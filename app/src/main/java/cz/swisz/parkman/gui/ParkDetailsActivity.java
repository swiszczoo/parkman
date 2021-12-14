package cz.swisz.parkman.gui;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import cz.swisz.parkman.R;

public class ParkDetailsActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_details);
    }
}
