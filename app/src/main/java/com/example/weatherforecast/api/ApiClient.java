package com.example.weatherforecast.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // ĐÂY CHÍNH LÀ ĐƯỜNG LINK GỐC CỦA OPEN-METEO
    private static final String BASE_URL = "https://api.open-meteo.com/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    // Dùng Gson để tự động nhét dữ liệu vào "vali" WeatherResponse
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
