package com.outsource.monitor.other.map;

import android.content.Context;

/**
 * Created by xionghao on 2017/1/14.
 */

public class LocationManager {

    private static LocationManager sInstance;
    private LocationService mLocationService;

    public static LocationManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LocationManager.class) {
                if (sInstance == null) {
                    sInstance = new LocationManager(context);
                }
            }
        }
        return sInstance;
    }

    private LocationManager(Context context) {
        mLocationService = new LocationService(context.getApplicationContext());
        mLocationService.setLocationOption(mLocationService.getDefaultLocationClientOption());
    }

    public LocationService getLocationService () {
        return mLocationService;
    }
}
