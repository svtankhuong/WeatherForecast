package com.example.weatherforecast.api;

import com.example.weatherforecast.model.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {

    @GET("v1/forecast?current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,wind_direction_10m,is_day,uv_index&hourly=temperature_2m,relative_humidity_2m,weather_code,precipitation_probability,rain,cloud_cover,visibility,uv_index,is_day&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum,wind_speed_10m_max,precipitation_probability_max,sunrise,sunset&timezone=auto")
    Call<WeatherResponse> getWeatherData(
            // Đây chính là chỗ app của bạn nhét tọa độ GPS vào!
            @Query("latitude") double latitude,   // Vĩ độ (VD: 10.82)
            @Query("longitude") double longitude // Kinh độ (VD: 106.62)
    );
}