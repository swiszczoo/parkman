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

public class ParkingFragment extends ParkingFragmentTemplate {
    private boolean m_disabled;

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

    @Override
    protected void initState() {
        super.initState();
        m_disabled = false;
    }

    @Override
    protected void inflateView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.fragment_places, this);

        m_parkingLayout = view.findViewById(R.id.parking_group);
        m_parkingLabel = view.findViewById(R.id.parking_name);
        m_placesLabel = view.findViewById(R.id.parking_places);
    }

    @Override
    protected void updateView() {
        Resources res = getResources();

        m_parkingLabel.setText(m_parkingName);
        m_placesLabel.setText(res.getQuantityString(
                R.plurals.place_count, m_placeCount, m_placeCount));

        if (m_disabled) {
            m_parkingLayout.setBackgroundResource(R.drawable.bg_ignored);
        } else if (m_placeCount == 0) {
            m_parkingLayout.setBackgroundResource(R.drawable.bg_no_places);
        } else if (m_placeCount <= FEW_THRESHOLD) {
            m_parkingLayout.setBackgroundResource(R.drawable.bg_few_places);
        } else {
            m_parkingLayout.setBackgroundResource(R.drawable.bg_many_places);
        }
    }

    public void setDisabled(boolean disabled) {
        m_disabled = disabled;
        updateView();
    }

    public boolean isDisabled() {
        return m_disabled;
    }
}
