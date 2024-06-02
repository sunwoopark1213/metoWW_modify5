package com.example.metoww;

import android.graphics.Color;

public class WeatherBackgroundManager {
    private static WeatherBackgroundManager instance;
    private int backgroundColor = Color.WHITE; // Default color

    private WeatherBackgroundManager() {}

    public static synchronized WeatherBackgroundManager getInstance() {
        if (instance == null) {
            instance = new WeatherBackgroundManager();
        }
        return instance;
    }

    public void setBackgroundColor(int weatherCode) {
        switch (weatherCode) {
            case 0: // Clear sky
            case 1: // Mainly clear
                backgroundColor = Color.parseColor("#93D8EE");
                break;
            case 2: // Partly cloudy
            case 3: // Overcast
                backgroundColor = Color.parseColor("#90A7B0");
                break;
            case 51: // Drizzle: Light
            case 53: // Drizzle: Moderate
            case 55: // Drizzle: Dense intensity
            case 61: // Rain: Slight
            case 63: // Rain: Moderate
            case 65: // Rain: Heavy intensity
                backgroundColor = Color.parseColor("#686868");
                break;
            default:
                backgroundColor = Color.WHITE; // Default color
                break;
        }
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }
}