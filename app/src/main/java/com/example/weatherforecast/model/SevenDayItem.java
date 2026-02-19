package com.example.weatherforecast.model;

public class SevenDayItem {
    public String dayName;   // Hôm nay, Mai, Kia...
    public float rainProb;   // Xác suất mưa %
    public int maxTemp;
    public int minTemp;
    public int weatherCode;     // Mã thời tiết chính (cao nhất)
    public int minWeatherCode;  // Mã thời tiết cho icon thấp nhất
    public String sunRiseTime;
    public String sunSetTime;
    public SevenDayItem(String dayName, float rainProb, int maxTemp, int minTemp, int weatherCode, int minWeatherCode, String sunRiseTime, String sunSetTime) {
        this.dayName = dayName;
        this.rainProb = rainProb;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.weatherCode = weatherCode;
        this.minWeatherCode = minWeatherCode;
        this.sunRiseTime = sunRiseTime;
        this.sunSetTime = sunSetTime;
    }
}