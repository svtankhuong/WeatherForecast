package com.example.weatherforecast;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.weatherforecast.adapter.HourlyAdapter;
import com.example.weatherforecast.adapter.SevenDayAdapter;
import com.example.weatherforecast.api.ApiClient;
import com.example.weatherforecast.api.LocationAddressHelper;
import com.example.weatherforecast.api.WeatherApiService;
import com.example.weatherforecast.model.HourlyItem;
import com.example.weatherforecast.model.WeatherResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import org.shredzone.commons.suncalc.MoonIllumination;
import org.shredzone.commons.suncalc.MoonTimes;
import org.shredzone.commons.suncalc.SunTimes;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvLocation;
    private NestedScrollView layOutWeather;
    private TextView tvCurrentTemp;
    private SwitchCompat switchUnit;
    private ImageView imgWeatherIcon, imgNeedle, ivMoonPhaseCenter;
    private float nhietDoGoc;
    private TextView tvWindSpeed, tvHumidity, tvUVIndex, tvRainProb, tvCloudCover, tvVisibility, tvSunrise, tvSunset, tvMoonrise, tvMoonset, tvWeatherDescription, ivMoonPhaseDescription;
    private ProgressBar progressHumidity, progressUV, progressRain, progressCloud;
    private SunPathView sunPathView;
    private RecyclerView rvHourlyForecast, rvSevenDayForecast;

    private SwipeRefreshLayout swipeRefresh;
    private HourlyAdapter hourlyAdapter;
    private SevenDayAdapter sevenDayAdapter; // Thêm biến này
    private double currentLat = 0.0;
    private double currentLon = 0.0;
    private boolean isLocationReady = false;

    private static final int QUYEN_GPS_CODE = 100;
    private static WeatherUtils wu = new WeatherUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.weather_forecast);

        swipeRefresh = findViewById(R.id.swipeRefresh);
        ViewCompat.setOnApplyWindowInsetsListener(swipeRefresh, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Ánh xạ View
        tvLocation = findViewById(R.id.tvLocation);
        layOutWeather = findViewById(R.id.weatherForecast);
        switchUnit = findViewById(R.id.switchUnit);
        imgWeatherIcon = findViewById(R.id.imgWeatherIcon);
        tvCurrentTemp = findViewById(R.id.tvCurrentTemp);
        tvWeatherDescription = findViewById(R.id.tvWeatherDescription);
        tvWindSpeed = findViewById(R.id.tvWindSpeed);
        imgNeedle = findViewById(R.id.imgNeedle);
        tvHumidity = findViewById(R.id.tvHumidity);
        progressHumidity = findViewById(R.id.progressHumidity);
        tvUVIndex = findViewById(R.id.tvUVIndex);
        progressUV = findViewById(R.id.progressUV);
        tvRainProb = findViewById(R.id.tvRainProb);
        progressRain = findViewById(R.id.progressRain);
        tvCloudCover = findViewById(R.id.tvCloudCover);
        progressCloud = findViewById(R.id.progressCloud);
        tvVisibility = findViewById(R.id.tvVisibility);
        tvSunrise = findViewById(R.id.tvSunrise);
        tvSunset = findViewById(R.id.tvSunset);
        tvMoonrise = findViewById(R.id.tvMoonrise);
        tvMoonset = findViewById(R.id.tvMoonset);
        ivMoonPhaseCenter = findViewById(R.id.ivMoonPhaseCenter);
        ivMoonPhaseDescription = findViewById(R.id.tvMoonPhaseDescription);
        sunPathView = findViewById(R.id.sunPathView);
        rvHourlyForecast = findViewById(R.id.rvHourlyForecast);
        rvSevenDayForecast = findViewById(R.id.rvSevenDayForecast);

        // 1. CẤU HÌNH NÚT CHUYỂN ĐỔI ĐƠN VỊ
        switchUnit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                doiDonViSangF();
            } else {
                doiDonViSangC();
            }
            // Cập nhật cho cả danh sách Hourly nếu đã có dữ liệu
            if (hourlyAdapter != null) {
                hourlyAdapter.setFahrenheit(isChecked);
            }

            if (sevenDayAdapter != null) {
                sevenDayAdapter.setFahrenheit(isChecked);
            }
        });

        // 2. Cấu hình SwipeRefresh
        swipeRefresh.setColorSchemeColors(Color.BLUE, Color.rgb(255, 165, 0));
        swipeRefresh.setOnRefreshListener(() -> {
            if (isLocationReady) {
                capNhatToanBoDuLieu(currentLat, currentLon);
            } else {
                swipeRefresh.setRefreshing(false);
                doGpsHienTai();
            }
        });

        // Chạy lần đầu
        kiemTraQuyenVaLayViTri();
    }

    private void capNhatToanBoDuLieu(double lat, double lon) {
        tinhToanThienVanOffline(lat, lon);
        goiApiThoiTiet(lat, lon);
    }

    private void doiDonViSangF() {
        float doF = (nhietDoGoc * 1.8f) + 32;
        tvCurrentTemp.setText(Math.round(doF) + "°F");
    }

    private void doiDonViSangC() {
        tvCurrentTemp.setText(Math.round(nhietDoGoc) + "°C");
    }

    // --- LOGIC GPS ---
    private void kiemTraQuyenVaLayViTri() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, QUYEN_GPS_CODE);
        } else {
            doGpsHienTai();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == QUYEN_GPS_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            doGpsHienTai();
        }
    }

    @SuppressLint("MissingPermission")
    private void doGpsHienTai() {
        swipeRefresh.setRefreshing(true);
        CancellationTokenSource cts = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLat = location.getLatitude();
                        currentLon = location.getLongitude();
                        isLocationReady = true;
                        capNhatToanBoDuLieu(currentLat, currentLon);

                        new Thread(() -> {
                            LocationAddressHelper lah = new LocationAddressHelper();
                            String diaChi = lah.getAddress(MainActivity.this, currentLat, currentLon);
                            runOnUiThread(() -> { if (tvLocation != null) tvLocation.setText(lah.toTitleCase(diaChi)); });
                        }).start();
                    } else {
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(this, "Không tìm thấy vị trí!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> swipeRefresh.setRefreshing(false));
    }

    // --- LOGIC API ---
    private void goiApiThoiTiet(double lat, double lon) {
        WeatherApiService apiService = ApiClient.getClient().create(WeatherApiService.class);
        Call<WeatherResponse> call = apiService.getWeatherData(lat, lon);

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse data = response.body();

                    // Cập nhật nhiệt độ gốc và hiển thị theo Switch hiện tại
                    nhietDoGoc = data.current.temp;
                    if (switchUnit.isChecked()) doiDonViSangF(); else doiDonViSangC();

                    layOutWeather.setBackgroundResource(data.current.isDay == 0 ? R.drawable.night_bg : R.drawable.day_bg);
                    tvWeatherDescription.setText(wu.getMoTaThoiTiet(data.current.weatherCode));
                    imgWeatherIcon.setImageResource(wu.getIconResource(data.current.weatherCode, data.current.isDay, data.current.time));

                    tvWindSpeed.setText(data.current.windSpeed + " km/h");
                    imgNeedle.setRotation(data.current.windDirection);
                    tvHumidity.setText(data.current.humidity + "%");
                    progressHumidity.setProgress(data.current.humidity);

                    float uv = data.current.uvIndex;
                    tvUVIndex.setText(wu.getUVDescription(uv) + " (" + (int)uv + ")");
                    progressUV.setProgress((int)uv);

                    if (data.hourly != null) xuLyHourly(data);

                    if (data.daily != null) xuLyDaily(data);
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void xuLyHourly(WeatherResponse data) {
        LocalDateTime now = LocalDateTime.now();
        if (now.getMinute() >= 30) now = now.plusHours(1);
        String roundedTime = now.withMinute(0).withSecond(0).withNano(0)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00"));

        List<HourlyItem> listGio = new ArrayList<>();
        int startIndex = data.hourly.time.indexOf(roundedTime);
        if (startIndex == -1) startIndex = 0;

        for (int i = startIndex; i < Math.min(startIndex + 24, data.hourly.time.size()); i++) {
            listGio.add(new HourlyItem(
                    data.hourly.time.get(i),
                    Math.round(data.hourly.temps.get(i)),
                    data.hourly.weatherCodes.get(i),
                    data.hourly.isDay.get(i),
                    data.hourly.pop.get(i)
            ));
        }

        // Khởi tạo adapter và áp dụng đơn vị đang chọn
        hourlyAdapter = new HourlyAdapter(listGio);
        hourlyAdapter.setFahrenheit(switchUnit.isChecked());
        rvHourlyForecast.setAdapter(hourlyAdapter);

        // Thông số khác
        tvRainProb.setText(data.hourly.pop.get(startIndex) + "%");
        progressRain.setProgress(data.hourly.pop.get(startIndex));
        tvCloudCover.setText(data.hourly.cloudCover.get(startIndex) + "%");
        progressCloud.setProgress(data.hourly.cloudCover.get(startIndex));
        tvVisibility.setText((int)(data.hourly.visibility.get(startIndex) / 1000) + " km");
    }

    private void xuLyDaily(WeatherResponse data) {
        List<com.example.weatherforecast.model.SevenDayItem> listDaily = new ArrayList<>();
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i < Math.min(7, data.daily.time.size()); i++) {
            String dateStr = data.daily.time.get(i);
            String dayLabel;

            if (i == 0) {
                dayLabel = "Hôm nay";
            } else {
                java.time.LocalDate date = java.time.LocalDate.parse(dateStr, inputFormatter);
                dayLabel = wu.getTenThu(date);
            }

            // --- BẮT ĐẦU ĐOẠN CẦN SỬA ---
            Float max_temperature = data.daily.maxTemps.get(i);
            Float min_temperature = data.daily.minTemps.get(i);

            // Xác định khoảng thời gian 24h của ngày thứ i (mỗi ngày có 24 giờ)
            int startHourIndex = i * 24;
            int endHourIndex = Math.min(startHourIndex + 24, data.hourly.temps.size());

            int max_index = startHourIndex;
            int min_index = startHourIndex;

            // Quét trong 24h của ngày đó để tìm đúng index của nhiệt độ max/min
            for (int j = startHourIndex; j < endHourIndex; j++) {
                if (data.hourly.temps.get(j).equals(max_temperature)) {
                    max_index = j;
                }
                if (data.hourly.temps.get(j).equals(min_temperature)) {
                    min_index = j;
                }
            }
            // --- KẾT THÚC ĐOẠN CẦN SỬA ---

            listDaily.add(new com.example.weatherforecast.model.SevenDayItem(
                    dayLabel,
                    data.daily.popMax.get(i),
                    Math.round(max_temperature),
                    Math.round(min_temperature),
                    data.hourly.weatherCodes.get(max_index),
                    data.hourly.weatherCodes.get(min_index),
                    data.daily.sunrise.get(i),
                    data.daily.sunset.get(i)
            ));
        }

        sevenDayAdapter = new SevenDayAdapter(listDaily);
        sevenDayAdapter.setFahrenheit(switchUnit.isChecked());
        rvSevenDayForecast.setAdapter(sevenDayAdapter);
    }

    private void tinhToanThienVanOffline(double lat, double lon) {
        new Thread(() -> {
            try {
                ZonedDateTime now = ZonedDateTime.now();
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
                SunTimes st = SunTimes.compute().on(now).at(lat, lon).execute();
                MoonTimes mt = MoonTimes.compute().on(now).at(lat, lon).execute();
                MoonIllumination mi = MoonIllumination.compute().on(now).execute();
                double phaseVal = (mi.getPhase() + 180) / 360.0;

                runOnUiThread(() -> {
                    tvSunrise.setText(st.getRise() != null ? st.getRise().format(fmt) : "--:--");
                    tvSunset.setText(st.getSet() != null ? st.getSet().format(fmt) : "--:--");
                    tvMoonrise.setText(mt.getRise() != null ? mt.getRise().format(fmt) : "--:--");
                    tvMoonset.setText(mt.getSet() != null ? mt.getSet().format(fmt) : "--:--");
                    ivMoonPhaseCenter.setImageResource(wu.getMoonPhaseIcon(phaseVal));
                    ivMoonPhaseDescription.setText(wu.getMoonPhaseName(phaseVal));
                    if (st.getRise() != null && st.getSet() != null) {
                        sunPathView.updateSunPosition(st.getRise().toLocalTime().toString(), st.getSet().toLocalTime().toString(), LocalTime.now().toString());
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}