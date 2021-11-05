package cz.swisz.parkman.gui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import cz.swisz.parkman.R;

public class ChangeAvailabilityActivity extends AppCompatActivity {
    public static final class Extras {
        public static final String ALL_OCCUPIED = "all_occupied";
        public static final String PARK_NAME = "park_name";
    }

    private FrameLayout m_root;
    private TextView m_label;
    private TextView m_parkNameLabel;
    private Button m_exitButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_availability);

        m_root = findViewById(R.id.avail_root);
        m_label = findViewById(R.id.avail_text);
        m_parkNameLabel = findViewById(R.id.avail_park);
        m_exitButton = findViewById(R.id.avail_exit);

        m_exitButton.setOnClickListener(v -> finish());

        setupActivityFromIntent();
    }

    private void setupActivityFromIntent() {
        Intent intent = getIntent();

        boolean ended = true;
        String parkName = "Unknown";

        if (intent != null) {
            ended = intent.getBooleanExtra(Extras.ALL_OCCUPIED, true);
            parkName = intent.getStringExtra(Extras.PARK_NAME);
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
}
