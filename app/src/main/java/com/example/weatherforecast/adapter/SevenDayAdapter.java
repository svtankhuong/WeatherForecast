package com.example.weatherforecast.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherforecast.R;
import com.example.weatherforecast.WeatherUtils;
import com.example.weatherforecast.model.SevenDayItem;

import java.util.List;

public class SevenDayAdapter extends RecyclerView.Adapter<SevenDayAdapter.ViewHolder> {

    private List<SevenDayItem> sevenDayList;
    private WeatherUtils wu = new WeatherUtils();

    // Thêm vào SevenDayAdapter.java
    private boolean isFahrenheit = false;

    public void setFahrenheit(boolean fahrenheit) {
        this.isFahrenheit = fahrenheit;
        notifyDataSetChanged();
    }

    public SevenDayAdapter(List<SevenDayItem> sevenDayList) {
        this.sevenDayList = sevenDayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seven_days_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SevenDayItem item = sevenDayList.get(position);

        holder.tvDayName.setText(item.dayName);
        holder.tvRainProbRow.setText((int)item.rainProb + "%");

        // Xử lý chuyển đổi nhiệt độ ngay tại đây
        if (isFahrenheit) {
            int maxF = Math.round((item.maxTemp * 1.8f) + 32);
            int minF = Math.round((item.minTemp * 1.8f) + 32);
            holder.tvTempRange.setText(maxF + "°  " + minF + "°");
        } else {
            holder.tvTempRange.setText(item.maxTemp + "°  " + item.minTemp + "°");
        }

        holder.imgRainIcon.setImageLevel((int) item.rainProb);
        holder.imgIconRow.setImageResource(wu.getIconResource(item.weatherCode, 1, item.sunRiseTime));
        holder.imgIconLowRow.setImageResource(wu.getIconResource(item.minWeatherCode, 0, item.sunSetTime));

    }

    @Override
    public int getItemCount() {
        return sevenDayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName, tvRainProbRow, tvTempRange;
        ImageView imgIconRow, imgRainIcon, imgIconLowRow;
        LinearLayout layoutRain;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvRainProbRow = itemView.findViewById(R.id.tvRainProbRow);
            tvTempRange = itemView.findViewById(R.id.tvTempRange);
            imgIconRow = itemView.findViewById(R.id.imgIconRow);
            imgRainIcon = itemView.findViewById(R.id.imgRainIcon); // ID mới của icon mưa
            imgIconLowRow = itemView.findViewById(R.id.imgIconLowRow); // ID mới của icon thấp nhất
            layoutRain = itemView.findViewById(R.id.layoutRain);
        }
    }
}