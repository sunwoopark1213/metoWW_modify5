package com.example.metoww;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
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
import org.json.JSONObject;

public class PollenForecastActivity extends AppCompatActivity {

    private static final String API_URL = "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=52.52&longitude=13.41&hourly=pm10,pm2_5,alder_pollen,birch_pollen,grass_pollen,mugwort_pollen,olive_pollen,ragweed_pollen&forecast_days=7";
    private LinearLayout pollenContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pollen_forecast);

        pollenContainer = findViewById(R.id.pollenContainer);

        fetchPollenData();
    }

    private void fetchPollenData() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, API_URL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        displayPollenData(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("PollenForecastActivity", "API Request error: ", error);
                        Toast.makeText(PollenForecastActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void displayPollenData(JSONObject response) {
        Log.d("PollenForecastActivity", "API Response: " + response.toString());

        if (!response.has("hourly")) {
            Log.e("PollenForecastActivity", "JSON Response does not have 'hourly' key.");
            Toast.makeText(this, "Error: Invalid data structure", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject hourly = response.optJSONObject("hourly");

        JSONArray timeArray = hourly.optJSONArray("time");
        JSONArray alderPollenArray = hourly.optJSONArray("alder_pollen");
        JSONArray birchPollenArray = hourly.optJSONArray("birch_pollen");
        JSONArray grassPollenArray = hourly.optJSONArray("grass_pollen");
        JSONArray mugwortPollenArray = hourly.optJSONArray("mugwort_pollen");
        JSONArray olivePollenArray = hourly.optJSONArray("olive_pollen");
        JSONArray ragweedPollenArray = hourly.optJSONArray("ragweed_pollen");

        if (timeArray == null || alderPollenArray == null || birchPollenArray == null ||
                grassPollenArray == null || mugwortPollenArray == null || olivePollenArray == null || ragweedPollenArray == null) {
            Log.e("PollenForecastActivity", "One or more JSON arrays are null.");
            Toast.makeText(this, "Error: Missing data in response", Toast.LENGTH_SHORT).show();
            return;
        }

        int arrayLength = timeArray.length();
        if (alderPollenArray.length() != arrayLength || birchPollenArray.length() != arrayLength ||
                grassPollenArray.length() != arrayLength || mugwortPollenArray.length() != arrayLength ||
                olivePollenArray.length() != arrayLength || ragweedPollenArray.length() != arrayLength) {
            Log.e("PollenForecastActivity", "JSON arrays have inconsistent lengths.");
            Toast.makeText(this, "Error: Inconsistent data lengths", Toast.LENGTH_SHORT).show();
            return;
        }

        pollenContainer.removeAllViews();

        for (int i = 0; i < arrayLength; i++) {
            String time = timeArray.optString(i);
            if (time.endsWith("14:00")) { // Filter for 2 PM data
                String date = time.substring(0, 10);
                double alderPollen = alderPollenArray.optDouble(i, -1);
                double birchPollen = birchPollenArray.optDouble(i, -1);
                double grassPollen = grassPollenArray.optDouble(i, -1);
                double mugwortPollen = mugwortPollenArray.optDouble(i, -1);
                double olivePollen = olivePollenArray.optDouble(i, -1);
                double ragweedPollen = ragweedPollenArray.optDouble(i, -1);

                if (alderPollen == -1 || birchPollen == -1 || grassPollen == -1 ||
                        mugwortPollen == -1 || olivePollen == -1 || ragweedPollen == -1) {
                    Log.e("PollenForecastActivity", "Invalid data at index: " + i);
                    continue;
                }

                String pollenInfo = "Date: " + date + "\n" +
                        "오리나무: " + alderPollen + "\n" +
                        "자작나무: " + birchPollen + "\n" +
                        "잔디: " + grassPollen + "\n" +
                        "쑥: " + mugwortPollen + "\n" +
                        "올리브: " + olivePollen + "\n" +
                        "돼지풀: " + ragweedPollen;

                TextView pollenTextView = new TextView(this);
                pollenTextView.setText(pollenInfo);
                pollenTextView.setPadding(8, 8, 8, 8);

                pollenContainer.addView(pollenTextView);
            }
        }
    }
}
