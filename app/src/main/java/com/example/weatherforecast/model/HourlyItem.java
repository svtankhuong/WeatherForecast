package com.example.weatherforecast.model;

public class HourlyItem {
    public String time;
    public int temp;
    public int weatherCode;
    public int isDay;
    public int rainProb;

    public HourlyItem(String time, int temp, int weatherCode, int isDay, int rainProb) {
        this.time = time;
        this.temp = temp;
        this.weatherCode = weatherCode;
        this.isDay = isDay;
        this.rainProb = rainProb;
    }
}