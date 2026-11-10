package com.zamcan.madrassa.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/** Sensor-driven compass; the Kaaba marker rotates relative to device heading. */
public final class QiblaCompassView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private double bearing;
    private double heading;
    private boolean calibrated;
    public QiblaCompassView(Context context) { super(context); setContentDescription("Qibla compass"); }
    public void setBearing(double degrees) { bearing = degrees; invalidate(); }
    public void setHeading(double degrees, boolean reliable) {
        heading = degrees; calibrated = reliable; invalidate();
        setContentDescription(reliable ? "Qibla relative bearing " +
                Math.round((bearing - heading + 360) % 360) + " degrees clockwise"
                : "Compass accuracy low; move phone in a figure eight to calibrate");
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth()/2f, cy = getHeight()/2f;
        float radius = Math.min(cx,cy)*0.77f;
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(3);
        paint.setColor(0xffbca67c); canvas.drawCircle(cx,cy,radius,paint);
        paint.setStyle(Paint.Style.FILL); paint.setColor(0xff69513d);
        paint.setTextAlign(Paint.Align.CENTER); paint.setTextSize(radius*0.17f);
        canvas.drawText("N",cx,cy-radius-12,paint);
        canvas.drawText("E",cx+radius+15,cy+6,paint);
        canvas.drawText("S",cx,cy+radius+23,paint);
        canvas.drawText("W",cx-radius-15,cy+6,paint);
        canvas.save();
        canvas.rotate((float)(bearing-heading),cx,cy);
        paint.setColor(0xffb28a44);
        Path arrow = new Path();
        arrow.moveTo(cx,cy-radius+16); arrow.lineTo(cx-12,cy-radius+43);
        arrow.lineTo(cx+12,cy-radius+43); arrow.close();
        canvas.drawPath(arrow,paint);
        float kx=cx, ky=cy-radius*0.58f;
        paint.setColor(0xff292522);
        canvas.drawRoundRect(kx-20,ky-17,kx+20,ky+17,3,3,paint);
        paint.setColor(0xffd9b966);
        canvas.drawRect(kx-20,ky-5,kx+20,ky-1,paint);
        canvas.restore();
        paint.setColor(calibrated?0xff4e6a4d:0xffa25c3f);
        canvas.drawCircle(cx,cy,6,paint);
    }
}
