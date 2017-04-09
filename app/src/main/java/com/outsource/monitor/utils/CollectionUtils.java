package com.outsource.monitor.utils;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/6.
 */
public class CollectionUtils {

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.size() == 0;
    }

    public static boolean isEmpty(Map map) {
        return map == null || map.size() == 0;
    }
}
