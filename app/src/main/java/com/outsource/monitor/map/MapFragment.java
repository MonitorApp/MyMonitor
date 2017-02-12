package com.outsource.monitor.map;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.outsource.monitor.R;
import com.outsource.monitor.df.DfDataReceiver;
import com.outsource.monitor.fscan.FscanDataReceiver;
import com.outsource.monitor.ifpan.IfpanDataReceiver;
import com.outsource.monitor.itu.ItuDataReceiver;
import com.outsource.monitor.parser.DFParser48278;
import com.outsource.monitor.parser.FscanParser48278;
import com.outsource.monitor.parser.IfpanParser48278;
import com.outsource.monitor.parser.ItuParser48278;
import com.outsource.monitor.service.LocationService;
import com.outsource.monitor.utils.CollectionUtils;
import com.outsource.monitor.utils.DisplayUtils;
import com.outsource.monitor.utils.LogUtils;
import com.outsource.monitor.utils.PromptUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xionghao on 2017/1/14.
 */

public class MapFragment extends Fragment implements ItuDataReceiver, IfpanDataReceiver, FscanDataReceiver, DfDataReceiver {

    private static final int MIN_DISTANCE = 50;
    private float mCurrentLevel;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_map, container, false);
        startLocation();

        mMapView = (MapView) view.findViewById(R.id.map_view);

        mLocationMockHandler.sendEmptyMessage(1);

        mBasePoi = new MyPoi();
        mBasePoi.longitude = 120.19;
        mBasePoi.latitude = 30.26;
        mBasePoi.level = 100;
        return view;
    }

    private static final String KEY_POI = "poi";
    private MapView mMapView;
    private List<Overlay> mOverlayList = new ArrayList<Overlay>(0); // 地图没有只清除所有Overlay功能，只能把Overlay保存下来遍历remove
    private List<MyPoi> mPois = new ArrayList<>();
    private MyPoi mBasePoi;
    private int t = 1;
    private boolean zoomed = false;

    private Handler mUIHandler = new Handler();

    private Handler mLocationMockHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (getActivity() == null) return;
            MyPoi newPoi = new MyPoi();
            newPoi.latitude = mBasePoi.latitude + t * 0.0002;
            newPoi.longitude = mBasePoi.longitude + t * 0.0001;
            newPoi.level = mCurrentLevel;
            t++;
            mPois.add(newPoi);
            refreshMapInfo();
            sendEmptyMessageDelayed(1, 2000);
        }
    };

    private void startLocation() {
        LocationService locationService = LocationManager.getInstance(getActivity()).getLocationService();
        locationService.registerListener(mListener);
        locationService.start();
    }

    private void refreshMapInfo() {
        mMapView.getMap().clear();
        refreshMarkOverlays();
        refreshDrivingRoute();
    }

    private void clearMapInfo() {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                mPois.clear();
                refreshMapInfo();
            }
        });
    }

    private void refreshMarkOverlays() {
        if (getActivity() == null) return;
        for (Overlay overlay : mOverlayList) {
            overlay.remove();
        }
        mOverlayList.clear();
        mMapView.getMap().setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Bundle bundle = marker.getExtraInfo();
                if (bundle != null) {
                    MyPoi poi = (MyPoi) bundle.getSerializable(KEY_POI);
                    if (poi != null) {
                        moveToCenterPoint(poi);
                        popupGetOnOffStationWindow(poi);
                    }
                }
                return false;
            }
        });
        int size = mPois.size();
        for (int i = 0; i < size; i++) {
            MyPoi poi = mPois.get(i);
            Bundle bundle = new Bundle();
            bundle.putSerializable(KEY_POI, poi);
            FrameLayout frameLayout = (FrameLayout) View.inflate(getActivity(), R.layout.map_layout_mark, null);
            TextView textView = (TextView) frameLayout.findViewById(R.id.tv_map_mark_level);
            textView.setText(String.format("%.1f", poi.level));
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromBitmap(getBitmapFromView(frameLayout));
            OverlayOptions option = new MarkerOptions().position(new LatLng(poi.latitude, poi.longitude))
                    .extraInfo(bundle).zIndex(Integer.MAX_VALUE).icon(bitmap);
            Overlay overlay = mMapView.getMap().addOverlay(option);
            mOverlayList.add(overlay);
        }
    }

    private void refreshDrivingRoute() {
        if (mPois.size() == 0) return;
        if (mPois.size() == 1) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (MyPoi poi : mPois) {
                builder.include(new LatLng(poi.latitude, poi.longitude));
            }
            mMapView.getMap().animateMapStatus(MapStatusUpdateFactory.newLatLngBounds(builder.build()), 500);
            return;
        }
        ArrayList<MyPoi> passByPoiList = new ArrayList<>(mPois.size());
        passByPoiList.addAll(mPois);
        passByPoiList.remove(0);
        passByPoiList.remove(passByPoiList.size() - 1);
        ArrayList<PlanNode> passByPlanNodes = new ArrayList<>(passByPoiList.size());
        for (MyPoi poi : passByPoiList) {
            passByPlanNodes.add(PlanNode.withLocation(new LatLng(poi.latitude, poi.longitude)));
        }
        MyPoi startPoi = mPois.get(0);
        MyPoi endPoi = mPois.get(mPois.size() - 1);
        PlanNode startNode = PlanNode.withLocation(new LatLng(startPoi.latitude, startPoi.longitude));
        PlanNode endNode = PlanNode.withLocation(new LatLng(endPoi.latitude, endPoi.longitude));
        RoutePlanSearch search = RoutePlanSearch.newInstance();
        search.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) { }
            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) { }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult result) {
                if (!isAdded()) {
                    return;
                }
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR ||
                        CollectionUtils.isEmpty(result.getRouteLines())) {
                    PromptUtils.showToast("抱歉，未找到线路信息");
                    return;
                }

                DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mMapView.getMap());
                DrivingRouteLine route = result.getRouteLines().get(0);
                overlay.setData(route);
                overlay.addToMap();
//                overlay.zoomToSpan();
//                popupGetOnOffStationWindow(mPois.get(0));
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mPois.size() > 0) {
                            moveToCenterPoint(mPois.get(mPois.size() - 1));
                        }
                    }
                });
            }
        });
        search.drivingSearch((new DrivingRoutePlanOption()).from(startNode).to(endNode).passBy(passByPlanNodes));
    }

    private void moveToCenterPoint(MyPoi centerPoi) {
        LatLng centerPoint = new LatLng(centerPoi.latitude, centerPoi.longitude);
        Point point = new Point(DisplayUtils.getScreenWidth() / 2, mMapView.getMeasuredHeight() / 2);
        MapStatus status = null;
        if (!zoomed) {
            status = new MapStatus.Builder().target(centerPoint).targetScreen(point).zoom(21).build();
            zoomed = true;
        } else {
            status = new MapStatus.Builder().target(centerPoint).targetScreen(point).build();
        }
        mMapView.getMap().animateMapStatus(MapStatusUpdateFactory.newMapStatus(status));
    }

    private void popupGetOnOffStationWindow(final MyPoi poi) {
        mMapView.getMap().hideInfoWindow();
        final View popupView = View.inflate(getActivity(), R.layout.map_popup, null);
        ((TextView) popupView.findViewById(R.id.tv_map_level)).setText(DisplayUtils.toDisplayLevel(poi.level));
        TextView tvTime = (TextView) popupView.findViewById(R.id.tv_map_time);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        tvTime.setText(simpleDateFormat.format(poi.timestamp));
        mMapView.getMap().showInfoWindow(new InfoWindow(BitmapDescriptorFactory.fromView(popupView),
                new LatLng(poi.latitude, poi.longitude), 0,
                new InfoWindow.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick() {
                        moveToCenterPoint(poi);
                    }
                }));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocationService locationService = LocationManager.getInstance(getActivity()).getLocationService();
        locationService.unregisterListener(mListener);
        locationService.stop();
    }

    /*****
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     */
    private BDLocationListener mListener = new BDLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            startLocation();
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
//                MyPoi myPoi = new MyPoi();
//                myPoi.latitude = location.getLatitude();
//                myPoi.longitude = location.getLongitude();
//                myPoi.level = mCurrentLevel;
//                myPoi.timestamp = System.currentTimeMillis();
//                if (mPois.size() == 0) {
//                    mPois.add(myPoi);
//                } else {
//                    MyPoi lastPoi = mPois.get(mPois.size() - 1);
//                    if (Utils.distance(myPoi.longitude, myPoi.latitude, lastPoi.longitude, lastPoi.latitude) >= MIN_DISTANCE) {
//                        mPois.add(myPoi);
//                    } else {
//                        lastPoi.level = myPoi.level;
//                    }
//                }
//                refreshMapInfo(mPois);
                StringBuffer sb = new StringBuffer(256);
                sb.append("time : ");
                /**
                 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                 */
                sb.append(location.getTime());
                sb.append("\nlocType : ");// 定位类型
                sb.append(location.getLocType());
                sb.append("\nlatitude : ");// 纬度
                sb.append(location.getLatitude());
                sb.append("\nlontitude : ");// 经度
                sb.append(location.getLongitude());
                sb.append("\nradius : ");// 半径
                sb.append(location.getRadius());
                sb.append("\nCountryCode : ");// 国家码
                sb.append(location.getCountryCode());
                sb.append("\nCountry : ");// 国家名称
                sb.append(location.getCountry());
                sb.append("\ncitycode : ");// 城市编码
                sb.append(location.getCityCode());
                sb.append("\ncity : ");// 城市
                sb.append(location.getCity());
                sb.append("\nDistrict : ");// 区
                sb.append(location.getDistrict());
                sb.append("\nStreet : ");// 街道
                sb.append(location.getStreet());
                sb.append("\naddr : ");// 地址信息
                sb.append(location.getAddrStr());
                sb.append("\nDirection(not all devices have value): ");
                sb.append(location.getDirection());// 方向
                sb.append("\nlocationdescribe: ");
                sb.append(location.getLocationDescribe());// 位置语义化信息
                sb.append("\nMyPoi: ");// POI信息
                if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
                    for (int i = 0; i < location.getPoiList().size(); i++) {
                        Poi poi = (Poi) location.getPoiList().get(i);
                        sb.append(poi.getName() + ";");
                    }
                }
                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                    sb.append("\nspeed : ");
                    sb.append(location.getSpeed());// 速度 单位：km/h
                    sb.append("\nsatellite : ");
                    sb.append(location.getSatelliteNumber());// 卫星数目
                    sb.append("\nheight : ");
                    sb.append(location.getAltitude());// 海拔高度 单位：米
                    sb.append("\ndescribe : ");
                    sb.append("gps定位成功");
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    // 运营商信息
                    if (location.hasAltitude()) {// *****如果有海拔高度*****
                        sb.append("\nheight : ");
                        sb.append(location.getAltitude());// 单位：米
                    }
                    sb.append("\noperationers : ");// 运营商信息
                    sb.append(location.getOperators());
                    sb.append("\ndescribe : ");
                    sb.append("网络定位成功");
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                    sb.append("\ndescribe : ");
                    sb.append("离线定位成功，离线定位结果也是有效的");
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    sb.append("\ndescribe : ");
                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    sb.append("\ndescribe : ");
                    sb.append("网络不同导致定位失败，请检查网络是否通畅");
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    sb.append("\ndescribe : ");
                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                }
                LogUtils.d("定位成功>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + sb.toString());
            } else {
                LogUtils.d("定位失败！！！！！！！!!!!!!!!!!!!!!!!!!!");
            }
        }

    };

    @Override
    public void onReceiveItuData(List<Float> ituData) {
        if (!CollectionUtils.isEmpty(ituData)) {
            mCurrentLevel = ituData.get(0);
        }
    }

    @Override
    public void onReceiveItuHead(ItuParser48278.DataHead ituHead) {
        clearMapInfo();
    }

    @Override
    public void onReceiveIfpanData(IfpanParser48278.DataValue ifpanData) {
        if (ifpanData != null && !CollectionUtils.isEmpty(ifpanData.levelList)) {
            mCurrentLevel = ifpanData.levelList.get(0);
        }
    }

    @Override
    public void onReceiveIfpanHead(IfpanParser48278.DataHead ifpanHead) {
        clearMapInfo();
    }

    @Override
    public void onReceiveFScanData(FscanParser48278.DataValue fscanData) {
        if (fscanData != null && !CollectionUtils.isEmpty(fscanData.values)) {
            mCurrentLevel = fscanData.values.get(0);
        }
    }

    @Override
    public void onReceiveFScanHead(FscanParser48278.DataHead fscanHead) {
        clearMapInfo();
    }

    @Override
    public void onReceiveDfData(DFParser48278.DataValue dfData) {
        if (dfData != null) {
            mCurrentLevel = dfData.value;
        }
    }

    @Override
    public void onReceiveDfHead(DFParser48278.DataHead dfHead) {
        clearMapInfo();
    }

    // 由于原生的Overlay无法获取起点和终点的点击事件，所以这里隐藏掉原生的起点和终点Marker，改用自定义的Maker
    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_transparent);
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_transparent);
        }
    }

    private Bitmap getBitmapFromView(View view) {
        view.destroyDrawingCache();
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        return view.getDrawingCache(true);
    }
}
