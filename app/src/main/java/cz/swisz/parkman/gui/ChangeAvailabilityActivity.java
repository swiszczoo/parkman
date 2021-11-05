package cz.swisz.parkman.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cz.swisz.parkman.R;
import cz.swisz.parkman.backend.DataProvider;
import cz.swisz.parkman.backend.DataWatcher;
import cz.swisz.parkman.backend.GlobalData;
import cz.swisz.parkman.backend.Observable;
import cz.swisz.parkman.backend.Observer;
import cz.swisz.parkman.backend.ParkingData;

public class ChangeAvailabilityActivity extends AppCompatActivity implements Observer {
    public static final class Extras {
        public static final String ALL_OCCUPIED = "all_occupied";
        public static final String PARK_NAME = "park_name";
    }

    private FrameLayout m_root;
    private TextView m_label;
    private TextView m_parkNameLabel;
    private String m_parkName;
    private Long m_parkKey;
    private LinearLayout m_parkRoot;
    private Button m_exitButton;

    private Map<Long, String> m_otherParkNames;
    private Map<Long, SmallParkingFragment> m_fragments;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_availability);

        m_root = findViewById(R.id.avail_root);
        m_label = findViewById(R.id.avail_text);
        m_parkNameLabel = findViewById(R.id.avail_park);
        m_parkRoot = findViewById(R.id.avail_other_parks);
        m_exitButton = findViewById(R.id.avail_exit);

        m_exitButton.setOnClickListener(v -> finish());

        setupActivityFromIntent();
        setupKnownParks();
        updateData();

        DataWatcher watcher = GlobalData.getInstance().getWatcher();
        if (watcher != null) {
            watcher.addObserver(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        DataWatcher watcher = GlobalData.getInstance().getWatcher();

        if (watcher != null) {
            watcher.removeObserver(this);
        }
    }

    private void setupActivityFromIntent() {
        Intent intent = getIntent();

        boolean ended = true;
        String parkName = "Unknown";

        if (intent != null) {
            ended = intent.getBooleanExtra(Extras.ALL_OCCUPIED, true);
            parkName = intent.getStringExtra(Extras.PARK_NAME);
            m_parkName = parkName;
        } else {
            m_parkName = "";
        }

        if (ended) {
            m_root.setBackgroundResource(R.color.not_available);
            m_label.setText(R.string.not_available);
        } else {
            m_root.setBackgroundResource(R.color.available);
            m_label.setText(R.string.available);
        }

        m_parkNameLabel.setText(parkName);
    }

    public void setupKnownParks() {
        DataProvider provider = GlobalData.getInstance().getProvider();
        if (provider != null) {
            Map<Long, String> allParks = provider.getParkNames();

            m_otherParkNames = new HashMap<>();
            m_fragments = new HashMap<>();

            for (Long id : allParks.keySet()) {
                String value = allParks.get(id);
                if (!m_parkName.equals(value)) {
                    m_otherParkNames.put(id, value);

                    SmallParkingFragment fragment = new SmallParkingFragment(this);
                    fragment.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    fragment.setParkingName(value);
                    fragment.setPlaceCount(0);

                    m_fragments.put(id, fragment);
                    m_parkRoot.addView(fragment);
                }
                else {
                    m_parkKey = id;
                }
            }
        }
    }

    public void updateData() {
        DataWatcher watcher = GlobalData.getInstance().getWatcher();
        if (watcher != null) {
            Map<Long, ParkingData> data = watcher.getCurrentData();

            if (data != null) {
                for (Long id : data.keySet()) {
                    ParkingData value = data.get(id);
                    if (m_fragments.containsKey(id)) {
                        SmallParkingFragment fragment = m_fragments.get(id);
                        fragment.setPlaceCount((int) value.freeCount);
                    }
                    else if (id.equals(m_parkKey)) {
                        m_parkNameLabel.setText(String.format(Locale.GERMANY, "%s (%d)",
                                m_parkName, value.freeCount));
                    }
                }
            }
        }
    }

    @Override
    public void onStateChanged(Observable subject) {
        if (subject == GlobalData.getInstance().getWatcher()) {
            runOnUiThread(this::updateData);
        }
    }
}
