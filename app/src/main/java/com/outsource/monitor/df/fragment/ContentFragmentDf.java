
package com.outsource.monitor.df.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.outsource.monitor.R;
import com.outsource.monitor.activity.MonitorCenterActivity;
import com.outsource.monitor.df.model.DfParam;
import com.outsource.monitor.df.widget.CompassView;
import com.outsource.monitor.event.PlayBallStateEvent;
import com.outsource.monitor.event.PlayPauseEvent;
import com.outsource.monitor.fscan.model.FscanParam;
import com.outsource.monitor.parser.DFParser48278;
import com.outsource.monitor.service.DfDataReceiver;
import com.outsource.monitor.utils.DisplayUtils;
import com.outsource.monitor.utils.LogUtils;
import com.outsource.monitor.utils.PromptUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.atomic.AtomicReference;

public class ContentFragmentDf extends Fragment implements DfDataReceiver {

    public static ContentFragmentDf newInstance() {
        return new ContentFragmentDf();
    }

    private AtomicReference<DFParser48278.DataValue> mCurrentData = new AtomicReference<>();
    private static final long REFRESH_CHART_INTERVAL = 100;
    private static final int MSG_ID_REFRESH_COMPASS = 1;

    private boolean isPlay = false;

    private TextView mTvFrequency;
    private TextView mTvAngle;
    private CompassView mCompassView;

    private Handler mRefreshHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ID_REFRESH_COMPASS) {
                if (isPlay) {
                    if (mCurrentData.get() != null) {
                        float angle = mCurrentData.get().pitchAngle;
                        mCompassView.setBearing(angle);
                        mTvAngle.setText(String.format("角度：%.1f°", angle));
                    }
                }
                mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH_COMPASS, REFRESH_CHART_INTERVAL);
            }
        }

    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayPauseEvent(PlayPauseEvent event) {
        if (event.isPlay) {
            DfParam param = DfParam.loadFromCache();
            if (param.frequency == 0) {
                PromptUtils.showToast("请先设置有效的参数再开始");
                EventBus.getDefault().post(new PlayBallStateEvent(false));
                return;
            }
        }
        isPlay = event.isPlay;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_content_df, null);
        mTvFrequency = (TextView) view.findViewById(R.id.tv_df_frequency);
        mTvAngle = (TextView) view.findViewById(R.id.tv_df_angle);
        mCompassView = (CompassView) view.findViewById(R.id.df_compassView);
        mRefreshHandler.sendEmptyMessageDelayed(MSG_ID_REFRESH_COMPASS, 500);

        if (((MonitorCenterActivity) getActivity()).isPlaying()) {
            FscanParam param = FscanParam.loadFromCache();
            if (param.startFrequency == 0 || param.endFrequency == 0 || param.step == 0) {
                EventBus.getDefault().post(new PlayBallStateEvent(false));
            } else {
                isPlay = true;
            }
        }

        EventBus.getDefault().register(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onReceiveDfData(DFParser48278.DataValue dfData) {
        if (dfData == null) {
            LogUtils.d("单频测向接受数据为空！");
            return;
        }
        mCurrentData.set(dfData);
    }

    @Override
    public void onReceiveDfHead(final DFParser48278.DataHead dfHead) {
        if (dfHead == null) {
            LogUtils.d("单频测向接受数据为空！");
            return;
        }
        if (mTvFrequency != null) {
            mTvFrequency.post(new Runnable() {
                @Override
                public void run() {
                    mTvFrequency.setText(String.format("频率：%.1fMHz", DisplayUtils.toDisplayFrequency(dfHead.freq)));
                }
            });
        }
    }
}
