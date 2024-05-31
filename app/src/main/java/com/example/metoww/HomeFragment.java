package com.example.metoww;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvCityName, tvTemperature, tvWeather, tvMinMaxTemp, tvWind, tvHumidity, tvPrecipitation;
    private ImageView ivWeatherIcon;
    private Button btnOpenCalendar, btnOpenAirQuality, btnPollenForecast;
    private LinearLayout forecastContainer;
    private double latitude = 37.566;
    private double longitude = 126.9784;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvCityName = view.findViewById(R.id.tvCityName);
        tvTemperature = view.findViewById(R.id.tvTemperature);
        tvWeather = view.findViewById(R.id.tvWeather);
        tvMinMaxTemp = view.findViewById(R.id.tvMinMaxTemp);
        tvWind = view.findViewById(R.id.tvWind);
        tvHumidity = view.findViewById(R.id.tvHumidity);
        tvPrecipitation = view.findViewById(R.id.tvPrecipitation);
        ivWeatherIcon = view.findViewById(R.id.ivWeatherIcon);
        btnOpenCalendar = view.findViewById(R.id.btnOpenCalendar);
        btnOpenAirQuality = view.findViewById(R.id.btnOpenAirQuality);
        btnPollenForecast = view.findViewById(R.id.btnPollenForecast);
        forecastContainer = view.findViewById(R.id.forecastContainer);

        btnOpenCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCalendarActivity();
            }
        });

        btnOpenAirQuality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAirQualityActivity();
            }
        });

        btnPollenForecast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPollenForecastActivity();
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        getLastKnownLocation();

        fetchWeatherData();

        return view;
    }

    private void getLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                getAddressFromLocation(latitude, longitude);
                            } else {
                                fetchWeatherData();
                            }
                        }
                    });
        }
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String cityName = address.getLocality();
                tvCityName.setText(cityName != null ? cityName : "Unknown");
            }
        } catch (IOException e) {
            e.printStackTrace();
            tvCityName.setText("Unknown");
        }
        fetchWeatherData();
    }

    private void fetchWeatherData() {
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude + "&longitude=" + longitude + "&current=temperature_2m,relative_humidity_2m,precipitation,rain,snowfall,weather_code,wind_speed_10m,wind_direction_10m&hourly=temperature_2m&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_sum,precipitation_hours,precipitation_probability_max,wind_speed_10m_max,wind_gusts_10m_max";

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            displayWeatherData(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void displayWeatherData(JSONObject response) throws JSONException {
        JSONObject currentWeather = response.getJSONObject("current");
        double temperature = currentWeather.getDouble("temperature_2m");
        int humidity = currentWeather.getInt("relative_humidity_2m");
        double precipitation = currentWeather.getDouble("precipitation");
        int weatherCode = currentWeather.getInt("weather_code");
        double windSpeed = currentWeather.getDouble("wind_speed_10m");
        int windDirection = currentWeather.getInt("wind_direction_10m");

        // Set current weather data
        tvTemperature.setText("온도: " + temperature + "°C");
        tvHumidity.setText("습도: " + humidity + "%");
        tvPrecipitation.setText("강수량: " + precipitation + "mm");
        tvWind.setText("풍속: " + windSpeed + " m/s, 방향: " + windDirection + "°");

        // Fetch daily weather data
        JSONObject dailyWeather = response.getJSONObject("daily");
        JSONArray tempMaxArray = dailyWeather.getJSONArray("temperature_2m_max");
        JSONArray tempMinArray = dailyWeather.getJSONArray("temperature_2m_min");
        JSONArray weatherCodeArray = dailyWeather.getJSONArray("weather_code");
        JSONArray timeArray = dailyWeather.getJSONArray("time");

        if (tempMaxArray.length() > 0 && tempMinArray.length() > 0 && weatherCodeArray.length() > 0) {
            double tempMax = tempMaxArray.getDouble(0);
            double tempMin = tempMinArray.getDouble(0);
            int dailyWeatherCode = weatherCodeArray.getInt(0);

            tvMinMaxTemp.setText("최고/최저 온도: " + tempMax + "°C / " + tempMin + "°C");
            tvWeather.setText("날씨: " + getWeatherDescription(dailyWeatherCode));

            // Set weather icon based on weather code
            ivWeatherIcon.setImageResource(getWeatherIcon(dailyWeatherCode));

            // Populate the forecastContainer with 16-day weather forecast
            forecastContainer.removeAllViews();  // Clear previous views if any
            for (int i = 0; i < timeArray.length(); i++) {
                View forecastItem = getLayoutInflater().inflate(R.layout.forecast_item, forecastContainer, false);

                TextView tvForecastDate = forecastItem.findViewById(R.id.tvForecastDate);
                ImageView ivForecastIcon = forecastItem.findViewById(R.id.ivForecastIcon);
                TextView tvForecastWeather = forecastItem.findViewById(R.id.tvForecastWeather);
                TextView tvForecastTemp = forecastItem.findViewById(R.id.tvForecastTemp);

                String date = timeArray.getString(i);
                double maxTemp = tempMaxArray.getDouble(i);
                double minTemp = tempMinArray.getDouble(i);
                int code = weatherCodeArray.getInt(i);

                tvForecastDate.setText(date);
                ivForecastIcon.setImageResource(getWeatherIcon(code));
                tvForecastWeather.setText(getWeatherDescription(code));
                tvForecastTemp.setText(String.format("%s°C / %s°C", maxTemp, minTemp));

                forecastContainer.addView(forecastItem);
            }
        }
    }

    private void openCalendarActivity() {
        Intent intent = new Intent(getActivity(), CalendarActivity.class);
        startActivity(intent);
    }

    private void openAirQualityActivity() {
        Intent intent = new Intent(getActivity(), AirQualityActivity.class);
        startActivity(intent);
    }

    private void openPollenForecastActivity() {
        Intent intent = new Intent(getActivity(), PollenForecastActivity.class);
        startActivity(intent);
    }

    private String getWeatherDescription(int weatherCode) {
        switch (weatherCode) {
            case 0:
                return "Clear sky";
            case 1:
                return "Mainly clear";
            case 2:
                return "Partly cloudy";
            case 3:
                return "Overcast";
            case 45:
                return "Fog";
            case 48:
                return "Depositing rime fog";
            case 51:
                return "Drizzle: Light";
            case 53:
                return "Drizzle: Moderate";
            case 55:
                return "Drizzle: Dense intensity";
            case 56:
                return "Freezing Drizzle: Light";
            case 57:
                return "Freezing Drizzle: Dense intensity";
            case 61:
                return "Rain: Slight";
            case 63:
                return "Rain: Moderate";
            case 65:
                return "Rain: Heavy intensity";
            case 66:
                return "Freezing Rain: Light";
            case 67:
                return "Freezing Rain: Heavy intensity";
            case 71:
                return "Snow fall: Slight";
            case 73:
                return "Snow fall: Moderate";
            case 75:
                return "Snow fall: Heavy intensity";
            case 77:
                return "Snow grains";
            case 80:
                return "Rain showers: Slight";
            case 81:
                return "Rain showers: Moderate";
            case 82:
                return "Rain showers: Violent";
            case 85:
                return "Snow showers: Slight";
            case 86:
                return "Snow showers: Heavy";
            case 95:
                return "Thunderstorm: Slight or moderate";
            case 96:
                return "Thunderstorm with slight hail";
            case 99:
                return "Thunderstorm with heavy hail";
            default:
                return "Unknown";
        }
    }

    private int getWeatherIcon(int weatherCode) {
        switch (weatherCode) {
            case 0:
                return R.drawable.ic_clear_sky;
            case 1:
                return R.drawable.ic_mainly_clear;
            case 2:
                return R.drawable.ic_partly_cloudy;
            case 3:
                return R.drawable.ic_overcast;
            case 45:
                return R.drawable.ic_fog;
            case 48:
                return R.drawable.ic_rime_fog;
            case 51:
                return R.drawable.ic_drizzle_light;
            case 53:
                return R.drawable.ic_drizzle_moderate;
            case 55:
                return R.drawable.ic_drizzle_dense;
            case 56:
                return R.drawable.ic_freezing_drizzle_light;
            case 57:
                return R.drawable.ic_freezing_drizzle_dense;
            case 61:
                return R.drawable.ic_rain_slight;
            case 63:
                return R.drawable.ic_rain_moderate;
            case 65:
                return R.drawable.ic_rain_heavy;
            case 66:
                return R.drawable.ic_freezing_rain_light;
            case 67:
                return R.drawable.ic_freezing_rain_heavy;
            case 71:
                return R.drawable.ic_snow_slight;
            case 73:
                return R.drawable.ic_snow_moderate;
            case 75:
                return R.drawable.ic_snow_heavy;
            case 77:
                return R.drawable.ic_snow_grains;
            case 80:
                return R.drawable.ic_rain_showers_slight;
            case 81:
                return R.drawable.ic_rain_showers_moderate;
            case 82:
                return R.drawable.ic_rain_showers_violent;
            case 85:
                return R.drawable.ic_snow_showers_slight;
            case 86:
                return R.drawable.ic_snow_showers_heavy;
            case 95:
                return R.drawable.ic_thunderstorm_slight_moderate;
            case 96:
                return R.drawable.ic_thunderstorm_slight_hail;
            case 99:
                return R.drawable.ic_thunderstorm_heavy_hail;
            default:
                return R.drawable.ic_unknown;
        }
    }
}
