package com.outsource.monitor.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Administrator on 2016/11/20.
 */

public class CusVie extends View {

    Paint mPaint;
    public CusVie(Context context) {
        super(context);
    }

    public CusVie(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        int[] colors = new int[2];
        colors[0] = Color.RED;
        colors[1] = Color.BLUE;
        LinearGradient rectShader = new LinearGradient(0, 0, 300, 0, colors, null, Shader.TileMode.MIRROR);
        mPaint.setShader(rectShader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, 300, 300, mPaint);
    }
}
