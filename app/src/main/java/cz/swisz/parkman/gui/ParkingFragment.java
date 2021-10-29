package cz.swisz.parkman.gui;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cz.swisz.parkman.R;

public class ParkingFragment extends FrameLayout {
    private String m_parkingName;
    private int m_placeCount;

    private View m_parkingLayout;
    private TextView m_parkingLabel;
    private TextView m_placesLabel;

    private static final int FEW_THRESHOLD = 5;

    public ParkingFragment(@NonNull Context context) {
        super(context);
        initState();
        inflateView();

        updateView();
    }

    public ParkingFragment(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initState();
        inflateView();

        updateView();
    }

    public ParkingFragment(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initState();
        inflateView();

        updateView();
    }

    public ParkingFragment(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initState();
        inflateView();

        updateView();
    }

    private void initState() {
        m_parkingName = "Parking";
        m_placeCount = 0;
    }

    private void inflateView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.fragment_places, this);

        m_parkingLayout = view.findViewById(R.id.parking_group);
        m_parkingLabel = view.findViewById(R.id.parking_name);
        m_placesLabel = view.findViewById(R.id.parking_places);
    }

    private void updateView() {
        Resources res = getResources();

        m_parkingLabel.setText(m_parkingName);
        m_placesLabel.setText(res.getQuantityString(
                R.plurals.place_count, m_placeCount, m_placeCount));

        if (m_placeCount == 0) {
            m_parkingLayout.setBackgroundResource(R.drawable.bg_no_places);
        } else if (m_placeCount <= FEW_THRESHOLD) {
            m_parkingLayout.setBackgroundResource(R.drawable.bg_few_places);
        } else {
            m_parkingLayout.setBackgroundResource(R.drawable.bg_many_places);
        }
    }

    public void setParkingName(String name) {
        m_parkingName = name;

        updateView();
    }

    public String getParkingName() {
        return m_parkingName;
    }

    public void setPlaceCount(int freePlaces) {
        m_placeCount = freePlaces;

        updateView();
    }

    public int getPlaceCount() {
        return m_placeCount;
    }


}
