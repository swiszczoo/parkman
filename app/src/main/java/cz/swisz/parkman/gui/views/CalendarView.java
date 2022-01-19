package cz.swisz.parkman.gui.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import cz.swisz.parkman.R;
import cz.swisz.parkman.backend.HistoryManager;

public class CalendarView extends LinearLayout implements View.OnClickListener {
    public interface OnDateChangeListener {
        void onDateChanged(int day, int month, int year);
    }

    private static class InternalDate {
        int day, month, year;

        InternalDate(int day, int month, int year) {
            this.day = day;
            this.month = month;
            this.year = year;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InternalDate that = (InternalDate) o;
            return day == that.day && month == that.month;
        }

        @Override
        public int hashCode() {
            return Objects.hash(day, month);
        }
    }
    private int m_month;
    private int m_year;

    private InternalDate m_selectedDate;
    private Map<InternalDate, TextView> m_days;
    private Set<InternalDate> m_availableDays;

    private OnDateChangeListener m_listener;

    public CalendarView(Context context) {
        super(context);
        initialize();
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        setOrientation(VERTICAL);
        m_listener = null;

        Calendar today = Calendar.getInstance();
        m_month = today.get(Calendar.MONTH) + 1;
        m_year = today.get(Calendar.YEAR);

        m_selectedDate = new InternalDate(
                today.get(Calendar.DAY_OF_MONTH),
                today.get(Calendar.MONTH) + 1,
                today.get(Calendar.YEAR));
        m_days = new HashMap<>();
        m_availableDays = new HashSet<>();

        generateMonthView();
        updateSelection();
    }

    private void generateMonthView() {
        removeAllViews();
        m_days.clear();
        m_availableDays.clear();

        Calendar iterator = Calendar.getInstance();
        iterator.setFirstDayOfWeek(Calendar.MONDAY);

        long now = iterator.getTimeInMillis();

        iterator.set(m_year, m_month - 1, 1);

        int normMonth = m_month - 1;

        int dayOfWeek = iterator.get(Calendar.DAY_OF_WEEK);
        dayOfWeek -= 2;
        if (dayOfWeek == -1) {
            dayOfWeek = 6;
        }
        iterator.set(Calendar.DAY_OF_MONTH, -dayOfWeek + 1);

        while (iterator.get(Calendar.YEAR) < m_year ||
                (iterator.get(Calendar.MONTH) <= normMonth
                        && iterator.get(Calendar.YEAR) == m_year)) {
            LinearLayout week = new LinearLayout(getContext());
            week.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            week.setOrientation(HORIZONTAL);

            for (int i = 0; i < 7; i++) {
                TextView tv = new TextView(getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0, (int) pxFromDp(getContext(), 36)
                );
                params.weight = 1;
                tv.setLayoutParams(params);
                tv.setGravity(Gravity.CENTER);
                tv.setOnClickListener(this);

                tv.setText(String.valueOf(iterator.get(Calendar.DAY_OF_MONTH)));
                if (iterator.get(Calendar.MONTH) == normMonth) {
                    if (iterator.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                            || iterator.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                        tv.setTextColor(getResources().getColor(R.color.purple_500));
                    } else {
                        tv.setTextColor(Color.BLACK);
                    }

                    if (iterator.getTimeInMillis() == now) {
                        tv.setTypeface(null, Typeface.BOLD);
                    }
                } else {
                    tv.setTextColor(Color.GRAY);
                }

                InternalDate idate = new InternalDate(
                        iterator.get(Calendar.DAY_OF_MONTH),
                        iterator.get(Calendar.MONTH) + 1,
                        iterator.get(Calendar.YEAR));
                tv.setTag(idate);

                m_days.put(idate, tv);
                if (HistoryManager.getInstance().isDayDataAvailable(iterator.getTime())) {
                    m_availableDays.add(idate);
                }

                week.addView(tv);
                iterator.set(Calendar.DAY_OF_MONTH, iterator.get(Calendar.DAY_OF_MONTH) + 1);
            }

            addView(week);
        }
    }

    private static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    private void updateSelection() {
        for (TextView tv : m_days.values()) {
            if (m_availableDays.contains((InternalDate) tv.getTag())) {
                tv.setBackgroundResource(R.drawable.bg_cal_available);
            } else {
                tv.setBackground(null);
            }
        }

        TextView current = m_days.get(m_selectedDate);
        if (current != null) {
            if (m_availableDays.contains((InternalDate) current.getTag())) {
                current.setBackgroundResource(R.drawable.bg_cal_available_selected);
            } else {
                current.setBackgroundResource(R.drawable.bg_cal_selected);
            }
        }
    }

    private Date convertInternalDateToDate(InternalDate date) {
        Calendar cal = Calendar.getInstance();
        cal.set(date.year, date.month - 1, date.day);
        return cal.getTime();
    }

    public void setOnDateChangeListener(OnDateChangeListener listener) {
        m_listener = listener;
    }

    public OnDateChangeListener getOnDateChangeListener() {
        return m_listener;
    }

    public int getDisplayedMonth() {
        return m_month;
    }

    public int getDisplayedYear() {
        return m_year;
    }

    public void setDisplayedMonth(int month) {
        m_month = month;
        while (m_month <= 0) {
            m_year--;
            m_month += 12;
        }
        while (m_month > 12) {
            m_year++;
            m_month -= 12;
        }
        generateMonthView();
        updateSelection();
    }

    public void setDisplayedYear(int year) {
        m_year = year;
        generateMonthView();
        updateSelection();
    }

    public String getDisplayTitle() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
        return sdf.format(convertInternalDateToDate(new InternalDate(1, m_month, m_year)));
    }

    @Override
    public void onClick(View v) {
        m_selectedDate = (InternalDate) v.getTag();
        updateSelection();

        if (m_listener != null) {
            m_listener.onDateChanged(m_selectedDate.day, m_selectedDate.month, m_selectedDate.year);
        }
    }
}
