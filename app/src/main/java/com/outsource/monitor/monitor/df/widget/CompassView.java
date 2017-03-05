package com.outsource.monitor.monitor.df.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.outsource.monitor.R;
import com.outsource.monitor.utils.DisplayUtils;

/**
 * Created by C.L.Wang on 14-3-16.
 */
public class CompassView extends View {

    private float bearing; //方位

    public void setBearing(float _bearing) {
        bearing = _bearing;
        invalidate();
    }

    public float getBearing() {
        return bearing;
    }

    private Paint bgPaint;
    private Paint markerPaint;
    private Paint textPaint;
    private float textHeight;
    private static final int MIN_MARK_DEGREE = 5;
    private static final int TEXT_MARK_DEGREE = 90;
    private static final int MARK_LENGTH = DisplayUtils.dp2px(16);
    private static final int POINTER_LENGTH = DisplayUtils.dp2px(64);
    private RectF mMarkRect = new RectF();
    private RectF mPointerRect = new RectF();

    public CompassView(Context context) {
        super(context);
        initCompassView();
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCompassView();
    }

    public CompassView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
        initCompassView();
    }

    private void initCompassView() {
        setFocusable(true);

        Resources r = this.getResources();

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(r.getColor(R.color.compass_background_color));
        bgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        bgPaint.setStrokeWidth(1);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(r.getColor(R.color.compass_text_color));
        textPaint.setTextSize(DisplayUtils.dp2px(22));

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        textHeight = fontMetrics.bottom - fontMetrics.top;

        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        markerPaint.setColor(r.getColor(R.color.compass_marker_color));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int radius = Math.min(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
        mMarkRect.left = 0;
        mMarkRect.top = 0;
        mMarkRect.right = 2 * radius;
        mMarkRect.bottom = 2 * radius;

        mPointerRect.left = radius - POINTER_LENGTH;
        mPointerRect.top = radius - POINTER_LENGTH;
        mPointerRect.right = radius + POINTER_LENGTH;
        mPointerRect.bottom = radius + POINTER_LENGTH;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int mMeasuredWidth = getMeasuredWidth();
        int mMeasuredHeight = getMeasuredHeight();

        int px = mMeasuredWidth / 2;
        int py = mMeasuredHeight / 2;

        int radius = Math.min(px, py);

        canvas.drawCircle(px, py, radius, bgPaint);

        for (int i = 0; i < 360; i += MIN_MARK_DEGREE) {
            if (i % TEXT_MARK_DEGREE == 0) {
                canvas.drawArc(mMarkRect, i, 1f, true, markerPaint);
            } else {
                canvas.drawArc(mMarkRect, i, 0.1f, true, markerPaint);
            }
        }

        canvas.drawCircle(px, py, radius - MARK_LENGTH, bgPaint);

        for (int i = 0; i < 360; i += TEXT_MARK_DEGREE) {
            String text = i + "°";
            float textWidth = textPaint.measureText(text);
            switch (i) {
                case 0:
                {
                    canvas.drawText(text, px - textWidth / 2, MARK_LENGTH + textHeight / 2, textPaint);
                    break;
                }
                case 90:
                {
                    canvas.drawText(text, mMeasuredWidth - textWidth - MARK_LENGTH, py + textHeight / 2, textPaint);
                    break;
                }
                case 180:
                {
                    canvas.drawText(text, px - textWidth / 2, mMeasuredHeight - MARK_LENGTH, textPaint);
                    break;
                }
                case 270:
                {
                    canvas.drawText(text, MARK_LENGTH, py + textHeight / 2, textPaint);
                    break;
                }
                default:
                    break;
            }
        }

        canvas.drawArc(mPointerRect, bearing - 90, 2f, true, markerPaint);
    }
}