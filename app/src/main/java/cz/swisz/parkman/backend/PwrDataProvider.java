package cz.swisz.parkman.backend;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class PwrDataProvider implements DataProvider {
    private final String m_endpointUrl;
    private static final String OP_NAME = "get_parks";

    public PwrDataProvider(String endpointUrl) {
        m_endpointUrl = endpointUrl;
    }

    @Override
    public long getSuggestedRefreshTimeInMs() {
        return 20000;
    }

    @Override
    public Map<Long, ParkingData> fetchData() {
        long timestamp = System.currentTimeMillis();
        try {
            Map<Long, ParkingData> response = new HashMap<>();

            String json = getJsonFromHttpPost(
                    m_endpointUrl, constructRequestBody(timestamp, OP_NAME));

            JsonReader reader = Json.createReader(new StringReader(json));
            JsonObject root = reader.readObject();
            JsonArray places = root.getJsonArray("places");

            for (int i = 0; i < places.size(); i++) {
                ParkingData data = parseParkingJsonObject(places.getJsonObject(i));
                response.put(data.parkingId, data);
            }

            return response;
        } catch (IOException e) {
            Log.e("PwrDataProvider", "An error occured: " + e.getMessage());
            e.printStackTrace();

            return null;
        }
    }

    private String constructRequestBody(long timestamp, String operation) {
        return String.format(Locale.GERMANY, "o=%s&ts=%d", operation, timestamp);
    }

    private String getJsonFromHttpPost(String urlString, String body) throws IOException {
        URL url = new URL(urlString);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        conn.setRequestProperty("Content-Length", String.valueOf(body.length()));
        conn.setRequestProperty("Referer", "https://iparking.pwr.edu.pl/");
        conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        try (InputStream is = conn.getInputStream()) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            return builder.toString();
        }
    }

    private ParkingData parseParkingJsonObject(JsonObject json) throws NumberFormatException {
        ParkingData.Trend freeTrend = ParkingData.Trend.THE_SAME;
        int trendValue = Integer.parseInt(json.getString("trend"));

        if (trendValue == 1) {
            freeTrend = ParkingData.Trend.UP;
        } else if (trendValue == -1) {
            freeTrend = ParkingData.Trend.DOWN;
        }

        return new ParkingData(
                Long.parseLong(json.getString("id")),
                Long.parseLong(json.getString("parking_id")),
                json.getString("czas_pomiaru"),
                freeTrend,
                Long.parseLong(json.getString("liczba_miejsc"))
        );
    }
}
