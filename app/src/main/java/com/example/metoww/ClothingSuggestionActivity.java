package com.example.metoww;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class ClothingSuggestionActivity extends AppCompatActivity {

    private TextView tvCurrentTemp, tvMaxTemp, tvMinTemp, tvClothingSuggestion;
    private ImageView ivClothingImage;
    private double latitude = 37.566; // 서울의 위도
    private double longitude = 126.9784; // 서울의 경도

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clothing_suggestion);

        tvCurrentTemp = findViewById(R.id.tvCurrentTemp);
        tvMaxTemp = findViewById(R.id.tvMaxTemp);
        tvMinTemp = findViewById(R.id.tvMinTemp);
        tvClothingSuggestion = findViewById(R.id.tvClothingSuggestion);
        ivClothingImage = findViewById(R.id.ivClothingImage);

        fetchWeatherData();

        getWindow().getDecorView().setBackgroundColor(WeatherBackgroundManager.getInstance().getBackgroundColor());
    }

    private void fetchWeatherData() {
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude + "&longitude=" + longitude + "&current=temperature_2m,relative_humidity_2m,precipitation,rain,snowfall,weather_code,wind_speed_10m,wind_direction_10m&hourly=temperature_2m&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_sum,precipitation_hours,precipitation_probability_max,wind_speed_10m_max,wind_gusts_10m_max";

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            displayWeatherData(response);
                        } catch (JSONException e) {
                            Log.e("ClothingSuggestion", "JSON Parsing error: ", e);
                            Toast.makeText(ClothingSuggestionActivity.this, "Error parsing JSON data", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ClothingSuggestion", "API Request error: ", error);
                        Toast.makeText(ClothingSuggestionActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void displayWeatherData(JSONObject response) throws JSONException {
        JSONObject currentWeather = response.getJSONObject("current");
        double currentTemp = currentWeather.getDouble("temperature_2m");

        JSONObject dailyWeather = response.getJSONObject("daily");
        JSONArray tempMaxArray = dailyWeather.getJSONArray("temperature_2m_max");
        JSONArray tempMinArray = dailyWeather.getJSONArray("temperature_2m_min");

        double maxTemp = tempMaxArray.getDouble(0);
        double minTemp = tempMinArray.getDouble(0);

        tvCurrentTemp.setText("현재 온도: " + currentTemp + "°C");
        tvMaxTemp.setText("최고 온도: " + maxTemp + "°C");
        tvMinTemp.setText("최저 온도: " + minTemp + "°C");

        String clothingSuggestion = getClothingSuggestion(currentTemp);
        tvClothingSuggestion.setText(clothingSuggestion);

        setClothingImage(currentTemp);
    }

    private String getClothingSuggestion(double temp) {
        if (temp >= 28) {
            return "28℃~ : 민소매, 반팔, 반바지, 원피스" +
                    "\n진짜 한여름 날씨 시작에요! 매미가 울고 아지랑이가 피는 더운 계절이에요! 에어컨과 선풍기가 필수이며, 여름휴가를 떠나는 날씨입니다. 많은 사람들이 계곡으로, 바다로 피서를 떠나고 시원한 수박이 갈증을 채워주는 더운 날씨입니다.";
        } else if (temp >= 23) {
            return "23℃~27℃ : 반팔, 얇은 셔츠, 반바지, 면바지" +
                    "\n이젠 정말 여름 시작을 알리는 날씨예요! 밤에도 시원할 정도로 날씨가 많이 따뜻해졌어요. 낮에는 아이스크림을 절로 찾게 되는 더운 날씨예요. 하지만 아직까지는 야외활동을 하는 데에는 딱 좋을 날씨죠. 보통 5월 말 ~6월 초, 9월 말 ~ 10월 초 날씨예요. 여행 가기에도 좋은 날씨입니다.";
        } else if (temp >= 20) {
            return "20℃~22℃ : 얇은 가디건, 긴팔, 면바지, 청바지" +
                    "\n낮에는 덥다는 소리가 나올 정도로 봄 끝, 가을 시작을 알리는 온도에요. 아직까진 입고 싶은 옷을 입을 수 있는 날씨죠! 이젠 겉옷이 없어도 활동하기 좋은 날씨예요. 점점 가벼워 지는 옷차림에 여름을 준비하는 날씨예요!";
        } else if (temp >= 17) {
            return "17℃~19℃ : 얇은 니트, 맨투맨, 가디건, 청바지" +
                    "\n이젠 겨울의 차가운 바람이 완전히 물러나고 차갑지만 따듯한 봄 냄새가 나는 날씨예요! 산책과 피크닉을 하기 좋은 날씨에 선선한 가을 날씨도 이 온도에 포함된답니다! 입고 싶은 옷을 마음껏 입을 수 있는 날씨예요~";
        } else if (temp >= 9) {
            return "9℃~16℃ : 재킷, 트렌치코트, 야상, 가디건, 니트, 청바지, 스타킹" +
                    "\n드디어 이제 봄 날씨 시작이라고 할 수 있는 온도에요. 하지만 아침저녁으로는 추운 시기기 때문에 꼭 재킷이나 겉옷을 챙겨 다녀야 하는 날씨입니다!";
        } else if (temp >= 5) {
            return "5℃~8℃ : 코트, 가죽 재킷, 히트텍, 니트, 레깅스" +
                    "\n기온이 약간 올라오긴 했지만 여전히 코끝이 시린 겨울 날씨예요. 롱패딩은 탈출했지만 코트와 목도리 정도는 입어야 몸의 온도를 보호할 수 있겠죠!";
        } else {
            return "~4℃ : 패딩, 두꺼운 코트, 목도리, 기모 제품" +
                    "\n~4℃가 내려가는 날씨는 굉장히 추운 한겨울 날씨로 롱패딩은 필수 템인 날씨입니다!";
        }
    }

    private void setClothingImage(double temp) {
        if (temp >= 28) {
            ivClothingImage.setImageResource(R.drawable.temperature_hot);
        } else if (temp >= 23) {
            ivClothingImage.setImageResource(R.drawable.temperature_warm);
        } else if (temp >= 17) {
            ivClothingImage.setImageResource(R.drawable.temperature_mild);
        } else if (temp >= 9) {
            ivClothingImage.setImageResource(R.drawable.temperature_cool);
        } else if (temp >= 5) {
            ivClothingImage.setImageResource(R.drawable.temperature_cooler);
        } else {
            ivClothingImage.setImageResource(R.drawable.temperature_cold);
        }
    }
}
