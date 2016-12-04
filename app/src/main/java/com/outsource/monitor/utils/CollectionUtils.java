package com.outsource.monitor.utils;

import java.util.Collection;

/**
 * Created by Administrator on 2016/10/6.
 */
public class CollectionUtils {

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.size() == 0;
    }
}
