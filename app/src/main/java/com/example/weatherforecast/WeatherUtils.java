package com.example.weatherforecast;

import android.icu.util.ChineseCalendar;
import android.icu.util.TimeZone;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class WeatherUtils {

    /**
     * Hàm lấy icon dựa trên weather code và thời gian (ngày/đêm)
     * @param weatherCode Mã thời tiết từ API
     * @param isDay 1 = Ngày, 0 = Đêm (Lấy từ API: current.isDay)
     * @return Resource ID của ảnh (R.drawable.xxx)
     */
    public int getIconResource(int weatherCode, int isDay, String time) {
        try {
            LocalDateTime date = LocalDateTime.parse(time);
            boolean isSpecial = isRamOr30(date);

            // 1. Xử lý riêng cho trời quang (Code 0)
            if (weatherCode == 0) {
                if (isSpecial) return R.drawable.ic_full_moon;
                return (isDay == 0) ? R.drawable.ic_crescent_moon : R.drawable.ic_sunny;
            }

            // 2. Xử lý mây (Code 1, 2, 3)
            if (weatherCode == 1) {
                if (isSpecial) return R.drawable.ic_partly_cloudy_special;
                return (isDay == 0) ? R.drawable.ic_partly_cloudy_night : R.drawable.ic_partly_cloudy;
            }

            if (weatherCode == 2) {
                if (isSpecial) return R.drawable.ic_mostly_cloudy_special;
                return (isDay == 0) ? R.drawable.ic_mostly_cloudy_night : R.drawable.ic_mostly_cloudy;
            }

            if (weatherCode == 3) {
                return R.drawable.ic_overcast;
            }

            // 3. Các trường hợp còn lại dùng switch-case
            switch (weatherCode) {
                case 45: case 48:
                    return R.drawable.ic_fog;

                case 51: case 53: case 55:
                    if (isSpecial) return R.drawable.ic_drizzle_special;
                    return (isDay == 0) ? R.drawable.ic_drizzle_night : R.drawable.ic_drizzle;

                case 61: case 63: case 65:
                    if (isSpecial) return R.drawable.ic_rainy_light_special;
                    return (isDay == 0) ? R.drawable.ic_rainy_light_night : R.drawable.ic_rainy_light;

                case 80: case 81: case 82:
                    if (isSpecial) return R.drawable.ic_rainy_heavy_special;
                    return (isDay == 0) ? R.drawable.ic_rainy_heavy_night : R.drawable.ic_rainy_heavy;

                case 95: case 96: case 99:
                    return R.drawable.ic_thunderstorm;

                default:
                    // Luôn cần một giá trị mặc định nếu không khớp code nào
                    return (isDay == 1) ? R.drawable.ic_sunny : R.drawable.ic_crescent_moon;
            }
        } catch (Exception e) {
            // Phòng trường hợp lỗi parse thời gian
            return R.drawable.ic_sunny;
        }
    }

    // Hàm phụ để lấy tên trạng thái tiếng Việt (Nếu bạn muốn hiển thị text)
    public String getMoTaThoiTiet(int code) {
        switch (code) {
            case 0: return "Trời quang đãng";
            case 1: case 2: case 3: return "Nhiều mây";
            case 45: case 48: return "Sương mù";
            case 51: case 53: case 55: return "Mưa phùn";
            case 61: case 63: case 65: return "Mưa rào";
            case 71: case 73: case 75: return "Tuyết rơi";
            case 80: case 81: case 82: return "Mưa lớn";
            case 95: case 96: case 99: return "Dông bão";
            default: return "Không xác định";
        }
    }

    public boolean isRamOr30(LocalDateTime date) {
        if (date == null) return false;

        // 1. Khởi tạo
        ChineseCalendar lunarCalc = new ChineseCalendar(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        // 2. [SỬA LẠI] Chuyển đổi LocalDateTime sang Date (Java Util)
        // Để lấy đúng thời điểm (mili-giây)
        Date dateUtil = Date.from(date.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant());

        // 3. [QUAN TRỌNG] Set thời gian cho bộ lịch, chứ không set ngày tháng
        lunarCalc.setTime(dateUtil);

        // 4. Lấy ra ngày Âm lịch chuẩn
        int ngayAm = lunarCalc.get(ChineseCalendar.DAY_OF_MONTH);

        // Debug: In thử ra xem đúng không
        // System.out.println("Dương: " + date + " -> Âm: " + ngayAm);

        return (ngayAm == 15 || ngayAm == 30);
    }

    public String getUVDescription(double uvIndex) {
        if (uvIndex < 0) return "Không xác định"; // Phòng trường hợp dữ liệu lỗi

        if (uvIndex <= 2.9) {
            return "Thấp";
        } else if (uvIndex <= 5.9) {
            return "Trung bình";
        } else if (uvIndex <= 7.9) {
            return "Cao";
        } else if (uvIndex <= 10.9) {
            return "Rất cao";
        } else {
            return "Nguy hại cực độ";
        }
    }

    public int getMoonPhaseIcon(double phaseValue) {
        // phaseValue chạy từ 0.0 đến 1.0
        if (phaseValue < 0.03 || phaseValue > 0.97) {
            return R.drawable.ic_new_moon; // Trăng non
        } else if (phaseValue <= 0.22) {
            return R.drawable.ic_waxing_crescent; // Lưỡi liềm đầu tháng
        } else if (phaseValue <= 0.28) {
            return R.drawable.ic_first_quarter; // Bán nguyệt đầu tháng
        } else if (phaseValue <= 0.47) {
            return R.drawable.ic_waxing_gibbous; // Trăng khuyết đầu tháng
        } else if (phaseValue <= 0.53) {
            return R.drawable.ic_moon; // Trăng rằm (Trăng tròn)
        } else if (phaseValue <= 0.72) {
            return R.drawable.ic_waning_gibbous; // Trăng khuyết cuối tháng
        } else if (phaseValue <= 0.78) {
            return R.drawable.ic_last_quarter; // Bán nguyệt cuối tháng
        } else {
            return R.drawable.ic_waning_crescent; // Lưỡi liềm cuối tháng
        }
    }

    public String getMoonPhaseName(double phaseValue) {
        if (phaseValue < 0.03 || phaseValue > 0.97) return "Trăng non";
        if (phaseValue <= 0.22) return "Trăng lưỡi liềm\nđầu tháng";
        if (phaseValue <= 0.28) return "Trăng bán nguyệt\nđầu tháng";
        if (phaseValue <= 0.47) return "Trăng khuyết\nđầu tháng";
        if (phaseValue <= 0.53) return "Trăng rằm";
        if (phaseValue <= 0.72) return "Trăng khuyết\ncuối tháng";
        if (phaseValue <= 0.78) return "Trăng bán nguyệt\ncuối tháng";
        return "Trăng lưỡi liềm\ncuối tháng";
    }

    public String getTenThu(java.time.LocalDate date) {
        switch (date.getDayOfWeek()) {
            case MONDAY:
                return "Thứ 2";
            case TUESDAY:
                return "Thứ 3";
            case WEDNESDAY:
                return "Thứ 4";
            case THURSDAY:
                return "Thứ 5";
            case FRIDAY:
                return "Thứ 6";
            case SATURDAY:
                return "Thứ 7";
            case SUNDAY:
                return "Chủ Nhật";
            default:
                return "";
        }
    }
}
