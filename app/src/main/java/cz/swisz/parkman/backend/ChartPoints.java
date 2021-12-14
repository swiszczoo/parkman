package cz.swisz.parkman.backend;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class ChartPoints {
    private static class Point {
        Calendar time;
        int quantity;

        private Point(Calendar time, int quantity) {
            this.time = time;
            this.quantity = quantity;
        }
    }

    private final ArrayList<Point> m_points;

    ChartPoints() {
        m_points = new ArrayList<>();
    }

    public void addPoint(Calendar time, int quantity) {
        Point pnt = new Point(time, quantity);
        m_points.add(pnt);
    }

    public void parsePoint(String timeString, int quantity) {
        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            cal.setTime(Objects.requireNonNull(timeFormat.parse(timeString)));

            addPoint(cal, quantity);
        } catch (ParseException | NullPointerException ignore) {
        }
    }

    public int getQuantityAt(int hour, int minute) throws IndexOutOfBoundsException {
        while (!(minute >= 0 && minute <= 59)) {
            if (minute >= 60) {
                hour--;
                minute -= 60;
            }

            if (minute < 0) {
                hour++;
                minute += 60;
            }
        }

        hour = (hour + 24) % 24;

        int p = 0;
        int k = m_points.size();

        while (k - p > 1) {
            int center = (p + k) / 2;
            Calendar central = m_points.get(center).time;

            if (isCalendarLaterThan(central, hour, minute)) {
                k = center;
            } else if (central.get(Calendar.HOUR) == hour && central.get(Calendar.MINUTE) == minute) {
                return m_points.get(center).quantity;
            } else {
                p = center;
            }
        }

        throw new IndexOutOfBoundsException("Element not found");
    }

    private boolean isCalendarLaterThan(Calendar calendar, int hour, int minute) {
        if (calendar.get(Calendar.HOUR) > hour) {
            return true;
        }

        if (calendar.get(Calendar.HOUR) < hour ) {
            return false;
        }

        return calendar.get(Calendar.MINUTE) > minute;
    }
}
