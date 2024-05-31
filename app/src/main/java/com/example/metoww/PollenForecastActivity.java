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
import org.json.JSONException;
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
                        try {
                            displayPollenData(response);
                        } catch (JSONException e) {
                            Log.e("PollenForecastActivity", "JSON Parsing error: ", e);
                            Toast.makeText(PollenForecastActivity.this, "Error parsing JSON data", Toast.LENGTH_SHORT).show();
                        }
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

    private void displayPollenData(JSONObject response) throws JSONException {
        JSONObject hourly = response.getJSONObject("hourly");
        JSONArray timeArray = hourly.getJSONArray("time");
        JSONArray alderPollenArray = hourly.getJSONArray("alder_pollen");
        JSONArray birchPollenArray = hourly.getJSONArray("birch_pollen");
        JSONArray grassPollenArray = hourly.getJSONArray("grass_pollen");
        JSONArray mugwortPollenArray = hourly.getJSONArray("mugwort_pollen");
        JSONArray olivePollenArray = hourly.getJSONArray("olive_pollen");
        JSONArray ragweedPollenArray = hourly.getJSONArray("ragweed_pollen");

        pollenContainer.removeAllViews();

        for (int i = 0; i < timeArray.length(); i++) {
            String time = timeArray.getString(i);
            if (time.endsWith("14:00")) { // Filter for 2 PM data
                String date = time.substring(0, 10);
                double alderPollen = alderPollenArray.getDouble(i);
                double birchPollen = birchPollenArray.getDouble(i);
                double grassPollen = grassPollenArray.getDouble(i);
                double mugwortPollen = mugwortPollenArray.getDouble(i);
                double olivePollen = olivePollenArray.getDouble(i);
                double ragweedPollen = ragweedPollenArray.getDouble(i);

                String pollenInfo = "Date: " + date + "\n" +
                        "Alder: " + alderPollen + "\n" +
                        "Birch: " + birchPollen + "\n" +
                        "Grass: " + grassPollen + "\n" +
                        "Mugwort: " + mugwortPollen + "\n" +
                        "Olive: " + olivePollen + "\n" +
                        "Ragweed: " + ragweedPollen;

                TextView pollenTextView = new TextView(this);
                pollenTextView.setText(pollenInfo);
                pollenTextView.setPadding(8, 8, 8, 8);

                pollenContainer.addView(pollenTextView);
            }
        }
    }
}
