package com.example.metoww;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

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

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private ImageView weatherIcon;
    private TextView weatherDescription, maxTemp, minTemp, memoTextView;
    private Button btnAddMemo, btnEditMemo, btnDeleteMemo;
    private Map<String, Integer> weatherDataMap;
    private Map<String, Double> maxTempMap;
    private Map<String, Double> minTempMap;
    private SharedPreferences sharedPreferences;
    private String selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calender, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        weatherIcon = view.findViewById(R.id.weatherIcon);
        weatherDescription = view.findViewById(R.id.weatherDescription);
        maxTemp = view.findViewById(R.id.maxTemp);
        minTemp = view.findViewById(R.id.minTemp);
        memoTextView = view.findViewById(R.id.memoTextView);
        btnAddMemo = view.findViewById(R.id.btnAddMemo);
        btnEditMemo = view.findViewById(R.id.btnEditMemo);
        btnDeleteMemo = view.findViewById(R.id.btnDeleteMemo);

        weatherDataMap = new HashMap<>();
        maxTempMap = new HashMap<>();
        minTempMap = new HashMap<>();
        sharedPreferences = getActivity().getSharedPreferences("CalendarMemo", Context.MODE_PRIVATE);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                showWeatherInfo(selectedDate);
                showMemo(selectedDate);
            }
        });

        btnAddMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMemoDialog(selectedDate, "Add Memo");
            }
        });

        btnEditMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMemoDialog(selectedDate, "Edit Memo");
            }
        });

        btnDeleteMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteMemo(selectedDate);
            }
        });

        fetchWeatherData();

        return view;
    }

    private void showWeatherInfo(String date) {
        Integer weatherCode = weatherDataMap.get(date);
        if (weatherCode != null) {
            weatherIcon.setImageResource(getWeatherIcon(weatherCode));
            weatherDescription.setText(getWeatherDescription(weatherCode));
        } else {
            weatherIcon.setImageResource(R.drawable.ic_unknown);
            weatherDescription.setText("No data available");
        }

        Double maxTemperature = maxTempMap.get(date);
        Double minTemperature = minTempMap.get(date);
        if (maxTemperature != null && minTemperature != null) {
            maxTemp.setText("최고 온도: " + maxTemperature + "°C");
            minTemp.setText("최저 온도: " + minTemperature + "°C");
        } else {
            maxTemp.setText("최고 온도: N/A");
            minTemp.setText("최저 온도: N/A");
        }
    }

    private void showMemo(String date) {
        String memo = sharedPreferences.getString(date, "");
        memoTextView.setText("메모: " + (memo.isEmpty() ? "메모 없음" : memo));
    }

    private void showMemoDialog(String date, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);

        final EditText input = new EditText(getActivity());
        input.setText(sharedPreferences.getString(date, ""));
        builder.setView(input);

        builder.setPositiveButton("저장", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String memo = input.getText().toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(date, memo);
                editor.apply();
                memoTextView.setText("Memo: " + memo);
                Toast.makeText(getActivity(), "메모 저장", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void deleteMemo(String date) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(date);
        editor.apply();
        memoTextView.setText("메모: 메모없음");
        Toast.makeText(getActivity(), "메모 삭제", Toast.LENGTH_SHORT).show();
    }

    private void fetchWeatherData() {
        String url = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&hourly=temperature_2m,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min&past_days=92&forecast_days=16";

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject daily = response.getJSONObject("daily");
                            JSONArray timeArray = daily.getJSONArray("time");
                            JSONArray weatherCodeArray = daily.getJSONArray("weather_code");
                            JSONArray tempMaxArray = daily.getJSONArray("temperature_2m_max");
                            JSONArray tempMinArray = daily.getJSONArray("temperature_2m_min");

                            for (int i = 0; i < timeArray.length(); i++) {
                                String date = timeArray.getString(i);
                                int weatherCode = weatherCodeArray.getInt(i);
                                double maxTemp = tempMaxArray.getDouble(i);
                                double minTemp = tempMinArray.getDouble(i);

                                weatherDataMap.put(date, weatherCode);
                                maxTempMap.put(date, maxTemp);
                                minTempMap.put(date, minTemp);
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
}