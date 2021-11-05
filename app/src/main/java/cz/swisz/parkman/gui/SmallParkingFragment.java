package cz.swisz.parkman.gui;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cz.swisz.parkman.R;

public class SmallParkingFragment extends ParkingFragmentTemplate {
    public SmallParkingFragment(@NonNull Context context) {
        super(context);
        initState();
        inflateView();

        updateView();
    }

    public SmallParkingFragment(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initState();
        inflateView();

        updateView();
    }

    public SmallParkingFragment(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initState();
        inflateView();

        updateView();
    }

    public SmallParkingFragment(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initState();
        inflateView();

        updateView();
    }

    @Override
    protected void inflateView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.fragment_places2, this);

        m_parkingLayout = view.findViewById(R.id.sm_parking_group);
        m_parkingLabel = view.findViewById(R.id.sm_parking_name);
        m_placesLabel = view.findViewById(R.id.sm_parking_places);
    }

    @Override
    protected void updateView() {
        Resources res = getResources();

        m_parkingLabel.setText(m_parkingName);
        m_placesLabel.setText(String.valueOf(m_placeCount));

        if (m_placeCount == 0) {
            m_parkingLayout.setBackgroundResource(R.color.free_zero);
        } else if (m_placeCount <= FEW_THRESHOLD) {
            m_parkingLayout.setBackgroundResource(R.color.free_few);
        } else {
            m_parkingLayout.setBackgroundResource(R.color.free_many);
        }
    }
}