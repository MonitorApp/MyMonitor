package com.outsource.monitor.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.outsource.monitor.model.Level;
import com.outsource.monitor.model.TagLevel;
import com.outsource.monitor.utils.CollectionUtils;
import com.outsource.monitor.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/10/6.
 */
public class BandScanningTextureView extends LevelTextureView {

    private static final int TRIANGLE_WIDTH = DisplayUtils.dp2px(6);
    private Paint mDashPaint;
    private Paint mTagTrianglePaint;
    private Paint mTagNamePaint;
    private List<TagLevel> mTagLevels = new ArrayList<>(0);
    private long mDashTime;

    public BandScanningTextureView(Context context) {
        super(context);
        init();
    }

    public BandScanningTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TagLevel addTag() {
        if (mDashTime > 0) {
            float lastLevel = 0;
            long lastTime = 0;
            for (Level level : mLevels) {
                if (mDashTime < level.timestamp) {
                    float offset = (float) (level.timestamp - mDashTime) / (level.timestamp - lastTime);
                    float tagLevel = lastLevel + offset * (level.level - lastLevel);
                    String name = mTagLevels.size() == 0 ? "" : String.valueOf(mTagLevels.size());
                    TagLevel tag = new TagLevel(tagLevel, mDashTime, name);
                    mTagLevels.add(tag);
                    mDashTime = 0;
                    return tag;
                } else {
                    lastTime = level.timestamp;
                    lastLevel = level.level;
                }
            }
        }
        return null;
    }

    public void deleteTag() {
        if (!CollectionUtils.isEmpty(mTagLevels)) {
            mTagLevels.remove(mTagLevels.size() - 1);
        }
    }

    @Override
    void drawCanvas(Canvas canvas) {
        super.drawCanvas(canvas);
        if (mDashTime > 0) {
            Path path = new Path();
            float x = Y_AXIS_WIDTH + (System.currentTimeMillis() - mDashTime) / DURATION_PER_PX;
            path.moveTo(x, mHeight - X_AXIS_HEIGHT);
            path.lineTo(x, 0);
            PathEffect effects = new DashPathEffect(new float[]{DisplayUtils.dp2px(3), DisplayUtils.dp2px(1)}, 0);
            mDashPaint.setPathEffect(effects);
            canvas.drawPath(path, mDashPaint);
        }
        for (TagLevel level : mTagLevels) {
            drawTagLevel(canvas, level);
        }
    }

    private void drawTagLevel(Canvas canvas, TagLevel level) {
        float x = Y_AXIS_WIDTH + (System.currentTimeMillis() - level.timestamp) / DURATION_PER_PX;
        float y = mHeight - X_AXIS_HEIGHT - level.level / LEVEL_PER_PX;
        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x - TRIANGLE_WIDTH / 2, y - 0.866f * TRIANGLE_WIDTH);
        path.lineTo(x + TRIANGLE_WIDTH / 2, y - 0.866f * TRIANGLE_WIDTH);
        path.close();
        canvas.drawPath(path, mTagTrianglePaint);

        if (!TextUtils.isEmpty(level.name)) {
            float textWidth = mTagNamePaint.measureText(level.name);
            canvas.drawText(level.name, x - textWidth / 2, y - 0.866f * TRIANGLE_WIDTH - DisplayUtils.dp2px(1), mTagNamePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            mDashTime = System.currentTimeMillis() - (x - Y_AXIS_WIDTH) * DURATION_PER_PX;
        }
        return true;
    }

    private void init() {
        mDashPaint = new Paint();
        mDashPaint.setStyle(Paint.Style.STROKE);
        mDashPaint.setColor(Color.RED);
        mDashPaint.setStrokeWidth(DisplayUtils.dp2px(1));

        mTagTrianglePaint = new Paint();
        mTagTrianglePaint.setStyle(Paint.Style.FILL);
        mTagTrianglePaint.setColor(Color.RED);

        mTagNamePaint = new Paint();
        mTagNamePaint.setColor(Color.RED);
        mTagNamePaint.setTextSize(DisplayUtils.dp2px(8));
    }
}
