package com.example.weatherforecast.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherforecast.R;
import com.example.weatherforecast.WeatherUtils;
import com.example.weatherforecast.model.HourlyItem;

import java.util.List;

public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.ViewHolder> {

    private List<HourlyItem> hourlyList;
    private WeatherUtils wu = new WeatherUtils();

    // 1. Thêm biến trạng thái đơn vị
    private boolean isFahrenheit = false;

    public HourlyAdapter(List<HourlyItem> hourlyList) {
        this.hourlyList = hourlyList;
    }

    // 2. Thêm hàm để MainActivity có thể ra lệnh đổi đơn vị
    public void setFahrenheit(boolean fahrenheit) {
        this.isFahrenheit = fahrenheit;
        notifyDataSetChanged(); // Vẽ lại danh sách khi đổi đơn vị
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hourly_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HourlyItem item = hourlyList.get(position);

        // Xử lý hiển thị giờ (giữ nguyên code cũ của bạn)
        try {
            if (item.time != null && item.time.length() >= 16) {
                String shortTime = item.time.substring(11, 16);
                holder.tvHour.setText(shortTime);
            } else {
                holder.tvHour.setText(item.time);
            }
        } catch (Exception e) {
            holder.tvHour.setText(item.time);
        }

        // 3. LOGIC CHUYỂN ĐỔI NHIỆT ĐỘ TRONG LIST
        if (isFahrenheit) {
            float tempF = (item.temp * 1.8f) + 32;
            holder.tvHourTemp.setText(Math.round(tempF) + "°F");
        } else {
            holder.tvHourTemp.setText(item.temp + "°C");
        }

        if (holder.tvRainProb != null) {
            holder.tvRainProb.setText(item.rainProb + "%");
        }

        holder.imgHourIcon.setImageResource(wu.getIconResource(item.weatherCode, item.isDay, item.time));
    }

    @Override
    public int getItemCount() {
        return hourlyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHour, tvHourTemp;
        TextView tvRainProb;
        ImageView imgHourIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHour = itemView.findViewById(R.id.tvHour);
            tvHourTemp = itemView.findViewById(R.id.tvHourTemp);
            imgHourIcon = itemView.findViewById(R.id.imgHourIcon);

            tvRainProb = itemView.findViewById(R.id.tvRainProb);
        }
    }
}