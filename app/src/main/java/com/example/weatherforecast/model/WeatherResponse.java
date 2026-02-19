package com.example.weatherforecast.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {

    // =========================================================
    // PHẦN 1: THÔNG TIN ĐỊA LÝ
    // =========================================================
    @SerializedName("latitude")
    public double latitude;

    @SerializedName("longitude")
    public double longitude;

    // =========================================================
    // PHẦN 2: CÁC GÓI DỮ LIỆU CHÍNH
    // =========================================================

    // Dữ liệu hiện tại
    @SerializedName("current")
    public CurrentWeather current;

    // Dự báo theo giờ
    @SerializedName("hourly")
    public HourlyWeather hourly;

    // Dự báo theo ngày
    @SerializedName("daily")
    public DailyWeather daily;

    // =========================================================
    // PHẦN 3: CÁC LỚP CON (INNER CLASSES)
    // =========================================================

    // 1. Lớp chứa thông tin thời tiết HIỆN TẠI
    public static class CurrentWeather {
        @SerializedName("time")
        public String time;

        @SerializedName("temperature_2m")
        public float temp;

        @SerializedName("is_day")
        public Integer isDay; // 1 = Ngày, 0 = Đêm

        @SerializedName("weather_code")
        public int weatherCode;

        @SerializedName("wind_speed_10m")
        public float windSpeed; // Mặc định là km/h

        @SerializedName("wind_direction_10m")
        public float windDirection; // Hướng gió

        // [MỚI] Độ ẩm hiện tại (%)
        @SerializedName("relative_humidity_2m")
        public int humidity;

        @SerializedName("uv_index")
        public Float uvIndex;
    }

    // 2. Lớp chứa thông tin thời tiết THEO GIỜ
    public static class HourlyWeather {
        @SerializedName("time")
        public List<String> time;

        @SerializedName("temperature_2m")
        public List<Float> temps;

        @SerializedName("relativehumidity_2m")
        public List<Integer> humidities;

        @SerializedName("weather_code")
        public List<Integer> weatherCodes;

        @SerializedName("precipitation_probability")
        public List<Integer> pop; // Xác suất mưa (%)

        @SerializedName("cloud_cover")
        public List<Integer> cloudCover; // Mây (%)

        @SerializedName("visibility")
        public List<Float> visibility; // Tầm nhìn (m)

        @SerializedName("rain")
        public List<Float> rain; // Lượng mưa (mm)

        @SerializedName("uv_index")
        public List<Float> uvIndex;

        @SerializedName("is_day")
        public List<Integer> isDay; // 1 = Ngày, 0 = Đêm
    }

    // 3. Lớp chứa thông tin thời tiết THEO NGÀY
    public static class DailyWeather {
        @SerializedName("time")
        public List<String> time;

        @SerializedName("weather_code")
        public List<Integer> weatherCodes;

        @SerializedName("temperature_2m_max")
        public List<Float> maxTemps;

        @SerializedName("temperature_2m_min")
        public List<Float> minTemps;

        @SerializedName("precipitation_sum")
        public List<Float> rainSum; // Tổng mưa (mm)

        @SerializedName("windspeed_10m_max")
        public List<Float> maxWindSpeed; // Gió mạnh nhất (km/h)

        // [MỚI] Chỉ số UV cao nhất trong ngày
        @SerializedName("uv_index_max")
        public List<Float> uvIndexMax;

        @SerializedName("precipitation_probability_max")
        public List<Integer> popMax; // Xác suất mưa (%)

        @SerializedName("sunrise")
        public List<String> sunrise;

        @SerializedName("sunset")
        public List<String> sunset;

    }
}