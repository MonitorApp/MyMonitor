package com.outsource.monitor.widget;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.outsource.monitor.utils.DisplayUtils;
import com.outsource.monitor.utils.Utils;

/**
 * Created by Administrator on 2016/10/6.
 */
public class PaletteView extends View {

    private ArgbEvaluator mEvaluator = new ArgbEvaluator();
    private Paint mPaint = new Paint();
    private Paint mTextPaint = new Paint();
    private int mWidth;
    private int mHeight;
    private int[] colors;

    public PaletteView(Context context) {
        super(context);
        init();
    }

    public PaletteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mTextPaint = new TextPaint();
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(DisplayUtils.dp2px(10));
        mPaint.setStyle(Paint.Style.FILL);
        colors = reverseArray(Utils.COLORS);
    }

    private static int[] reverseArray(int[] array) {
        int[] newArray = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[array.length - i - 1] = array[i];
        }
        return newArray;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int size = colors.length;
        int BLOCK = mHeight / (size - 1);
        for (int i = 0; i < size - 1; i++) {
            int startY = BLOCK * i;
            for (int j = 0; j < BLOCK; j++) {
                float offset = (float) j / BLOCK;
                Object color = mEvaluator.evaluate(offset, colors[i], colors[i + 1]);
                mPaint.setColor(Integer.valueOf(color.toString()));
                canvas.drawLine(0, startY + j, mWidth, startY + j, mPaint);
            }
            canvas.drawText(Utils.LEVELS[size - i - 1] + "", DisplayUtils.dp2px(5), startY + DisplayUtils.dp2px(10), mTextPaint);
        }
    }
}
