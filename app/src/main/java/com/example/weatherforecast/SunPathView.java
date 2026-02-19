package com.example.weatherforecast;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SunPathView extends View {

    private Paint passedPaint, futurePaint, nightPaint, nightFuturePaint;
    private Path curvePath;
    private float sunProgress = 0.5f;
    private boolean shouldDrawSun = true;

    public SunPathView(Context context) { super(context); init(); }
    public SunPathView(Context context, AttributeSet attrs) { super(context, attrs); init(); }

    private void init() {
        // 1. Màu vàng (Đã đi qua - Trùng màu mặt trời)
        passedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        passedPaint.setColor(Color.parseColor("#FFC107"));
        passedPaint.setStyle(Paint.Style.STROKE);
        passedPaint.setStrokeWidth(8f);
        passedPaint.setStrokeCap(Paint.Cap.ROUND);

        // 2. Màu trắng mờ (Phần bầu trời chưa tới)
        futurePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        futurePaint.setColor(Color.parseColor("#66FFFFFF"));
        futurePaint.setStyle(Paint.Style.STROKE);
        futurePaint.setStrokeWidth(8f);
        futurePaint.setStrokeCap(Paint.Cap.ROUND);

        // 3. Màu trắng (Dành cho phần dưới chân trời khi mặt trời biến mất)
        nightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nightPaint.setColor(Color.WHITE); // Trắng hoàn toàn theo yêu cầu
        nightPaint.setStyle(Paint.Style.STROKE);
        nightPaint.setStrokeWidth(8f);
        nightPaint.setStrokeCap(Paint.Cap.ROUND);

        // 4. Màu trắng xám mờ (Phần đêm tương lai khi mặt trời vẫn còn đó)
        nightFuturePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nightFuturePaint.setColor(Color.parseColor("#22FFFFFF"));
        nightFuturePaint.setStyle(Paint.Style.STROKE);
        nightFuturePaint.setStrokeWidth(8f);
        nightFuturePaint.setStrokeCap(Paint.Cap.ROUND);

        curvePath = new Path();
    }

    // ... (Giữ nguyên hàm updateSunPosition của bạn vì logic tính sunProgress đã rất chuẩn)
    public void updateSunPosition(String sunriseStr, String sunsetStr, String nowStr) {
        try {
            if (sunriseStr.length() > 5) sunriseStr = sunriseStr.substring(0, 5);
            if (sunsetStr.length() > 5) sunsetStr = sunsetStr.substring(0, 5);
            if (nowStr.length() > 5) nowStr = nowStr.substring(0, 5);

            SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
            long sunrise = df.parse(sunriseStr).getTime();
            long sunset = df.parse(sunsetStr).getTime();
            long now = df.parse(nowStr).getTime();

            long oneHour = 60 * 60 * 1000L;
            long twoHours = 2 * 60 * 60 * 1000L;
            long threeHours = 3 * 60 * 60 * 1000L;

            if (now >= (sunrise - oneHour) && now <= (sunset + threeHours)) {
                shouldDrawSun = true;
            } else {
                shouldDrawSun = false;
            }

            if (now < sunrise) {
                float timeRatio = (float) (now - (sunrise - oneHour)) / oneHour;
                sunProgress = -0.25f + (timeRatio * 0.25f);
            } else if (now <= sunset) {
                sunProgress = (float) (now - sunrise) / (sunset - sunrise);
            } else {
                long timeAfterSunset = now - sunset;
                if (timeAfterSunset <= twoHours) {
                    float timeRatio = (float) timeAfterSunset / twoHours;
                    sunProgress = 1.0f + (timeRatio * 0.5f);
                } else {
                    sunProgress = 1.5f;
                }
            }
            invalidate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float horizonY = h / 2f;

        float sunRiseX = w * 0.25f;
        float sunSetX = w * 0.75f;
        float amplitude = h * 0.40f;

        float currentSunX = sunRiseX + (sunProgress * (sunSetX - sunRiseX));

        // Vẽ đường cong chuẩn bị
        curvePath.reset();
        for (float x = 0; x <= w; x += 1f) {
            float y = calculateSmartSineY(x, sunRiseX, sunSetX, horizonY, amplitude);
            if (x == 0) curvePath.moveTo(x, y);
            else curvePath.lineTo(x, y);
        }

        // --- BẮT ĐẦU VẼ CÁC VÙNG ---

        // 1. Vẽ vùng TRÊN chân trời (Bầu trời)
        canvas.save();
        canvas.clipRect(0, 0, w, horizonY);

        // Đoạn đã đi qua (Màu vàng)
        canvas.save();
        canvas.clipRect(0, 0, currentSunX, horizonY);
        canvas.drawPath(curvePath, passedPaint);
        canvas.restore();

        // Đoạn tương lai (Màu trắng mờ)
        canvas.save();
        canvas.clipRect(currentSunX, 0, w, horizonY);
        canvas.drawPath(curvePath, futurePaint);
        canvas.restore();

        canvas.restore();

        // 2. Vẽ vùng DƯỚI chân trời (Đêm/Hoàng hôn)
        canvas.save();
        canvas.clipRect(0, horizonY, w, h);

        if (!shouldDrawSun) {
            // MỚI: Khi mặt trời biến mất hoàn toàn -> Dưới chân trời có màu Trắng
            canvas.drawPath(curvePath, nightPaint);
        } else {
            // Khi mặt trời còn xuất hiện:
            // Đoạn đuôi đã đi qua (Màu vàng - Mặt trời đi qua đâu màu vàng tới đó)
            canvas.save();
            canvas.clipRect(0, horizonY, currentSunX, h);
            canvas.drawPath(curvePath, passedPaint);
            canvas.restore();

            // Đoạn đêm tương lai (Màu trắng xám rất mờ)
            canvas.save();
            canvas.clipRect(currentSunX, horizonY, w, h);
            canvas.drawPath(curvePath, nightFuturePaint);
            canvas.restore();
        }
        canvas.restore();

        // --- VẼ MẶT TRỜI ---
        if (shouldDrawSun) {
            float sunY = calculateSmartSineY(currentSunX, sunRiseX, sunSetX, horizonY, amplitude);
            Paint sunPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            sunPaint.setColor(Color.parseColor("#FFC107"));
            canvas.drawCircle(currentSunX, sunY, 18f, sunPaint);
            sunPaint.setAlpha(70);
            canvas.drawCircle(currentSunX, sunY, 35f, sunPaint);
        }
    }

    private float calculateSmartSineY(float x, float startX, float endX, float baseY, float amp) {
        double angle = Math.PI * (x - startX) / (endX - startX);
        float sinVal = (float) Math.sin(angle);
        float finalAmp = (sinVal < 0) ? amp * 0.7f : amp;
        return baseY - (finalAmp * sinVal);
    }
}