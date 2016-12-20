package com.outsource.monitor.floating;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.outsource.monitor.R;
import com.outsource.monitor.event.PlayPauseEvent;
import com.outsource.monitor.utils.DisplayUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by hao.xiong on 2016/6/22.
 */
public class FloatingBall extends FrameLayout {

    private View mExpandToolbar;
    private ImageView mIvBall;
    private static final int BALL_SIZE = DisplayUtils.dp2px(48);
    private boolean isPlaying = true;

    public FloatingBall(Context context) {
        super(context);

//        initExpandLayout(context);

        mIvBall = new ImageView(context);
        mIvBall.setImageResource(R.drawable.ic_pause);
        mIvBall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mExpandToolbar != null) {
                    if (mExpandToolbar.getVisibility() == View.VISIBLE) {
                        mExpandToolbar.setVisibility(View.GONE);
                    } else {
                        mExpandToolbar.setVisibility(View.VISIBLE);
                    }
                } else {
                    isPlaying = !isPlaying;
                    mIvBall.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
                    EventBus.getDefault().post(new PlayPauseEvent(isPlaying));
                }
            }
        });
        LayoutParams ballParams = new LayoutParams(BALL_SIZE, BALL_SIZE);
        addView(mIvBall, ballParams);
    }

    private void initExpandLayout(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(DisplayUtils.dp2px(40), 0, 0, 0);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor("#2605244f"));
//        top-left, top-right, bottom-right, bottom-left
        int radius = DisplayUtils.dp2px(12);
        gd.setCornerRadii(new float[]{0, 0, radius, radius, radius, radius, 0, 0});
        layout.setBackgroundDrawable(gd);
        layout.setVisibility(View.GONE);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, BALL_SIZE);
        params.leftMargin = DisplayUtils.dp2px(12);
        addView(layout, params);
        mExpandToolbar = layout;

        ImageView ivAccountManager = new ImageView(context);
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(BALL_SIZE, BALL_SIZE);
        ivAccountManager.setLayoutParams(itemParams);
//        ivAccountManager.setImageResource(R.drawable.qysdk_icon_account_manager);
        ivAccountManager.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        layout.addView(ivAccountManager);

        ImageView ivOfficialBBS = new ImageView(context);
        ivOfficialBBS.setLayoutParams(itemParams);
//        ivOfficialBBS.setImageResource(R.drawable.qysdk_icon_bbs);
        ivOfficialBBS.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        layout.addView(ivOfficialBBS);

        ImageView ivRewardCenter = new ImageView(context);
        ivRewardCenter.setLayoutParams(itemParams);
//        ivRewardCenter.setImageResource(R.drawable.qysdk_icon_reward_center);
        ivRewardCenter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        layout.addView(ivRewardCenter);
    }

    public boolean isExpanded() {
         return mExpandToolbar != null && mExpandToolbar.getVisibility() == View.VISIBLE;
    }
}
