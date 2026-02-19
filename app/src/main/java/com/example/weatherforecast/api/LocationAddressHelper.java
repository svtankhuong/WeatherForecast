package com.example.weatherforecast.api;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class LocationAddressHelper {

    // Hàm chính để lấy địa chỉ
    public String getAddress(Context context, double lat, double lon) {
        String addressResult = "";

        // CÁCH 1: Dùng Geocoder chính chủ của Android
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            // Lấy 1 kết quả duy nhất
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                // Ưu tiên lấy Quận/Huyện, Tỉnh/Thành phố
                // locality = Thành phố/Huyện, adminArea = Tỉnh
                String city = address.getLocality();
                String province = address.getAdminArea();

                // Nếu không có Locality (thường gặp ở nông thôn), lấy SubAdminArea
                if (city == null) city = address.getSubAdminArea();

                if (city != null && province != null) {
                    addressResult = city + ", " + province;
                } else if (province != null) {
                    addressResult = province;
                } else {
                    addressResult = address.getCountryName();
                }
            }
        } catch (Exception e) {
            Log.e("AddressHelper", "Cách 1 (Geocoder) bị lỗi: " + e.getMessage());
        }

        // CÁCH 2: DỰ PHÒNG (FALLBACK) - Nếu Cách 1 thất bại (trả về rỗng)
        // Gọi API miễn phí của OpenStreetMap (Nominatim)
        if (addressResult.isEmpty() || addressResult.equals("")) {
            Log.d("AddressHelper", "Đang chuyển sang Cách 2 (OpenStreetMap API)...");
            addressResult = getAddressFromOSM(lat, lon);
        }

        // Nếu cả 2 cách đều thua
        if (addressResult.isEmpty()) {
            addressResult = "Không xác định (" + String.format("%.2f", lat) + ", " + String.format("%.2f", lon) + ")";
        }

        return addressResult;
    }

    // Hàm gọi API OpenStreetMap (Không cần Key, miễn phí)
    private String getAddressFromOSM(double lat, double lon) {
        String result = "";
        try {
            // URL API Nominatim
            String urlString = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + lat + "&lon=" + lon + "&zoom=10&addressdetails=1";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // Nominatim yêu cầu phải có User-Agent để không bị chặn
            conn.setRequestProperty("User-Agent", "WeatherApp/1.0");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();

            // Parse JSON trả về
            JSONObject jsonObject = new JSONObject(content.toString());
            JSONObject address = jsonObject.getJSONObject("address");

            // Lấy tên thành phố/tỉnh từ JSON
            String city = address.optString("city", "");
            if (city.isEmpty()) city = address.optString("town", "");
            if (city.isEmpty()) city = address.optString("county", "");

            String country = address.optString("country", "");

            if (!city.isEmpty()) {
                result = city + ", " + country;
            } else {
                result = country;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // Hàm viết hoa chữ cái đầu (như bạn đang dùng)
    public String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;
        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            } else {
                c = Character.toLowerCase(c);
            }
            titleCase.append(c);
        }
        return titleCase.toString();
    }
}