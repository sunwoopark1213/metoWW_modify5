package com.example.metoww;

import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private ImageView weatherIcon;
    private TextView weatherDescription;
    private Map<String, Integer> weatherDataMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);
        weatherIcon = findViewById(R.id.weatherIcon);
        weatherDescription = findViewById(R.id.weatherDescription);

        weatherDataMap = new HashMap<>();

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                Integer weatherCode = weatherDataMap.get(date);
                if (weatherCode != null) {
                    weatherIcon.setImageResource(getWeatherIcon(weatherCode));
                    weatherDescription.setText(getWeatherDescription(weatherCode));
                } else {
                    weatherIcon.setImageResource(R.drawable.ic_unknown);
                    weatherDescription.setText("No data available");
                }
                Toast.makeText(CalendarActivity.this, "Selected date: " + date, Toast.LENGTH_SHORT).show();
            }
        });

        fetchWeatherData();
    }

    private void fetchWeatherData() {
        String url = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&hourly=temperature_2m,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min&past_days=92&forecast_days=16";

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject daily = response.getJSONObject("daily");
                            JSONArray timeArray = daily.getJSONArray("time");
                            JSONArray weatherCodeArray = daily.getJSONArray("weather_code");

                            for (int i = 0; i < timeArray.length(); i++) {
                                String date = timeArray.getString(i);
                                int weatherCode = weatherCodeArray.getInt(i);
                                weatherDataMap.put(date, weatherCode);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    private int getWeatherIcon(int weatherCode) {
        switch (weatherCode) {
            case 0: return R.drawable.ic_clear_sky;
            case 1: return R.drawable.ic_mainly_clear;
            case 2: return R.drawable.ic_partly_cloudy;
            case 3: return R.drawable.ic_overcast;
            case 45: return R.drawable.ic_fog;
            case 48: return R.drawable.ic_rime_fog;
            case 51: return R.drawable.ic_drizzle_light;
            case 53: return R.drawable.ic_drizzle_moderate;
            case 55: return R.drawable.ic_drizzle_dense;
            case 56: return R.drawable.ic_freezing_drizzle_light;
            case 57: return R.drawable.ic_freezing_drizzle_dense;
            case 61: return R.drawable.ic_rain_slight;
            case 63: return R.drawable.ic_rain_moderate;
            case 65: return R.drawable.ic_rain_heavy;
            case 66: return R.drawable.ic_freezing_rain_light;
            case 67: return R.drawable.ic_freezing_rain_heavy;
            case 71: return R.drawable.ic_snow_slight;
            case 73: return R.drawable.ic_snow_moderate;
            case 75: return R.drawable.ic_snow_heavy;
            case 77: return R.drawable.ic_snow_grains;
            case 80: return R.drawable.ic_rain_showers_slight;
            case 81: return R.drawable.ic_rain_showers_moderate;
            case 82: return R.drawable.ic_rain_showers_violent;
            case 85: return R.drawable.ic_snow_showers_slight;
            case 86: return R.drawable.ic_snow_showers_heavy;
            case 95: return R.drawable.ic_thunderstorm_slight_moderate;
            case 96: return R.drawable.ic_thunderstorm_slight_hail;
            case 99: return R.drawable.ic_thunderstorm_heavy_hail;
            default: return R.drawable.ic_unknown;
        }
    }

    private String getWeatherDescription(int weatherCode) {
        switch (weatherCode) {
            case 0: return "Clear sky";
            case 1: return "Mainly clear";
            case 2: return "Partly cloudy";
            case 3: return "Overcast";
            case 45: return "Fog";
            case 48: return "Depositing rime fog";
            case 51: return "Drizzle: Light";
            case 53: return "Drizzle: Moderate";
            case 55: return "Drizzle: Dense intensity";
            case 56: return "Freezing Drizzle: Light";
            case 57: return "Freezing Drizzle: Dense intensity";
            case 61: return "Rain: Slight";
            case 63: return "Rain: Moderate";
            case 65: return "Rain: Heavy intensity";
            case 66: return "Freezing Rain: Light";
            case 67: return "Freezing Rain: Heavy intensity";
            case 71: return "Snow fall: Slight";
            case 73: return "Snow fall: Moderate";
            case 75: return "Snow fall: Heavy intensity";
            case 77: return "Snow grains";
            case 80: return "Rain showers: Slight";
            case 81: return "Rain showers: Moderate";
            case 82: return "Rain showers: Violent";
            case 85: return "Snow showers: Slight";
            case 86: return "Snow showers: Heavy";
            case 95: return "Thunderstorm: Slight or moderate";
            case 96: return "Thunderstorm with slight hail";
            case 99: return "Thunderstorm with heavy hail";
            default: return "Unknown";
        }
    }
}
