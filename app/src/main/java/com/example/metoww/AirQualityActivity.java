package com.example.metoww;

import android.os.Bundle;
import android.util.Log;
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

public class AirQualityActivity extends AppCompatActivity {

    private static final String TAG = "AirQualityActivity";
    private TextView tvAirQualityInfo;
    private TextView tvWarnings;
    private static final String API_URL = "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=37.566&longitude=126.9784&hourly=pm10,pm2_5,carbon_monoxide,nitrogen_dioxide,sulphur_dioxide,ozone,aerosol_optical_depth,dust,uv_index,uv_index_clear_sky,ammonia&past_days=92&forecast_days=7";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_quality);

        tvAirQualityInfo = findViewById(R.id.tvAirQualityInfo);
        tvWarnings = findViewById(R.id.tvWarnings);

        fetchAirQualityData();
    }

    private void fetchAirQualityData() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, API_URL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "JSON Response: " + response.toString());

                            JSONObject hourly = response.getJSONObject("hourly");
                            JSONArray pm10Array = hourly.optJSONArray("pm10");
                            JSONArray pm25Array = hourly.optJSONArray("pm2_5");
                            JSONArray carbonMonoxideArray = hourly.optJSONArray("carbon_monoxide");
                            JSONArray nitrogenDioxideArray = hourly.optJSONArray("nitrogen_dioxide");
                            JSONArray sulphurDioxideArray = hourly.optJSONArray("sulphur_dioxide");
                            JSONArray ozoneArray = hourly.optJSONArray("ozone");
                            JSONArray uvIndexArray = hourly.optJSONArray("uv_index");

                            // Get the latest valid values from the arrays
                            double pm10 = getLastValidDouble(pm10Array);
                            double pm25 = getLastValidDouble(pm25Array);
                            double carbonMonoxide = getLastValidDouble(carbonMonoxideArray);
                            double nitrogenDioxide = getLastValidDouble(nitrogenDioxideArray);
                            double sulphurDioxide = getLastValidDouble(sulphurDioxideArray);
                            double ozone = getLastValidDouble(ozoneArray);
                            double uvIndex = getLastValidDouble(uvIndexArray);

                            if (pm10 == -1 || pm25 == -1 || carbonMonoxide == -1 || nitrogenDioxide == -1 || sulphurDioxide == -1 || ozone == -1 || uvIndex == -1) {
                                Toast.makeText(AirQualityActivity.this, "Error: Missing valid data", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String airQualityInfo = "PM10: " + pm10 + " µg/m³\n" +
                                    "PM2.5: " + pm25 + " µg/m³\n" +
                                    "CO: " + carbonMonoxide + " ppm\n" +
                                    "NO2: " + nitrogenDioxide + " ppb\n" +
                                    "SO2: " + sulphurDioxide + " ppb\n" +
                                    "O3: " + ozone + " ppb\n" +
                                    "UV Index: " + uvIndex;

                            tvAirQualityInfo.setText(airQualityInfo);

                            evaluateAirQuality(pm10, pm25, carbonMonoxide, nitrogenDioxide, sulphurDioxide, ozone, uvIndex);

                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing JSON data", e);
                            Toast.makeText(AirQualityActivity.this, "Error parsing JSON data", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching data", error);
                        Toast.makeText(AirQualityActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    private double getLastValidDouble(JSONArray jsonArray) throws JSONException {
        if (jsonArray == null) return -1;
        for (int i = jsonArray.length() - 1; i >= 0; i--) {
            if (!jsonArray.isNull(i)) {
                return jsonArray.getDouble(i);
            }
        }
        return -1; // return -1 if no valid double is found
    }

    private void evaluateAirQuality(double pm10, double pm25, double co, double no2, double so2, double o3, double uvIndex) {
        StringBuilder warnings = new StringBuilder();

        if (pm10 > 50 || pm25 > 25) {
            warnings.append("경고 : 고미립자 물질(PM10/PM2.5) 수치가 높습니다\n");
        }
        if (co > 10) {
            warnings.append("경고: 일산화탄소(CO) 수치가 높습니다.\n");
        }
        if (no2 > 40) {
            warnings.append("경고 : 이산화질소(NO2) 수치가 높습니다.\n");
        }
        if (so2 > 20) {
            warnings.append("경고: 이산화황(SO2) 수치가 높습니다.\n");
        }
        if (o3 > 180) {
            warnings.append("경고 : 높은 오존(O3) 수치가 높습니다.\n");
        }
        if (uvIndex > 8) {
            warnings.append("경고 : 높은 UV 지수가 높습니다.\n");

        }

        if (warnings.length() == 0) {
            warnings.append("공기질이 좋습니다.");
        }

        // Display warnings as Toast
        Toast.makeText(this, warnings.toString(), Toast.LENGTH_LONG).show();

        // Display warnings as TextView
        tvWarnings.setText(warnings.toString());
    }
}
