package cz.swisz.parkman.gui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class ParkingFragmentTemplate extends FrameLayout {
    protected String m_parkingName;
    protected int m_placeCount;

    protected View m_parkingLayout;
    protected TextView m_parkingLabel;
    protected TextView m_placesLabel;

    protected static final int FEW_THRESHOLD = 5;

    public ParkingFragmentTemplate(@NonNull Context context) {
        super(context);
    }

    public ParkingFragmentTemplate(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ParkingFragmentTemplate(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ParkingFragmentTemplate(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void initState() {
        m_parkingName = "Parking";
        m_placeCount = 0;
    }

    protected abstract void inflateView();
    protected abstract void updateView();

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

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        m_parkingLayout.setOnClickListener(l);
    }

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
        m_parkingLayout.setOnLongClickListener(l);
    }
}
