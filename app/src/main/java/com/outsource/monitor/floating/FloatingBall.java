package com.outsource.monitor.floating;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.outsource.monitor.R;
import com.outsource.monitor.event.PlayBallStateEvent;
import com.outsource.monitor.event.PlayPauseEvent;
import com.outsource.monitor.utils.DisplayUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by hao.xiong on 2016/6/22.
 */
public class FloatingBall extends FrameLayout {

    private ImageView mIvBall;
    private static final int BALL_SIZE = DisplayUtils.dp2px(48);
    private boolean isPlay = false;

    public FloatingBall(Context context) {
        super(context);

        mIvBall = new ImageView(context);
        mIvBall.setImageResource(isPlay ? R.drawable.ic_pause : R.drawable.ic_play);
        mIvBall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isPlay = !isPlay;
                mIvBall.setImageResource(isPlay ? R.drawable.ic_pause : R.drawable.ic_play);
                EventBus.getDefault().post(new PlayPauseEvent(isPlay));
            }
        });
        LayoutParams ballParams = new LayoutParams(BALL_SIZE, BALL_SIZE);
        addView(mIvBall, ballParams);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayBallStateevent(PlayBallStateEvent event) {
        mIvBall.setImageResource(event.isPlay ? R.drawable.ic_pause : R.drawable.ic_play);
        isPlay = event.isPlay;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    public boolean isPlaying() {
        return isPlay;
    }
}
