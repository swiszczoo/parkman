package cz.swisz.parkman.gui;

import java.util.ArrayList;

public final class Utils {
    public static ArrayList<Long> parseIgnoredParks(String bundle) {
        String[] splittedKeys = bundle.split(";");

        ArrayList<Long> result = new ArrayList<>();
        for (String part : splittedKeys) {
            if (!part.isEmpty())
                result.add(Long.parseLong(part));
        }

        return result;
    }

    public static String serializeIgnoredParks(ArrayList<Long> parks) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parks.size(); i++) {
            if (i > 0)
                builder.append(';');
            builder.append(parks.get(i));
        }

        return builder.toString();
    }
}
