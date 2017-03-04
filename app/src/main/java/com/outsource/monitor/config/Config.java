package com.outsource.monitor.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xionghao on 2017/3/4.
 */
@XStreamAlias("config")
public class Config {

    public Device device = new Device();

    @XStreamAlias("device")
    public static class Device {
        @XStreamAsAttribute
        public String name;
        public Param param;
        @XStreamImplicit(itemFieldName="func")
        public List<Func> funcs = new ArrayList<>(0);
    }
}
