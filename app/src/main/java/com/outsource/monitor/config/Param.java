package com.outsource.monitor.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("param")
public class Param {
    @XStreamImplicit(itemFieldName = "item")
    public List<Item> items = new ArrayList<>(0);

    @XStreamAlias("item")
    public static class Item {
        @XStreamAsAttribute
        public String name;
        @XStreamAsAttribute
        public String title;
        @XStreamAsAttribute
        public String type;
        @XStreamAlias("default")
        @XStreamAsAttribute
        public String defaultValue;
        @XStreamAsAttribute
        public String min;
        @XStreamAsAttribute
        public String max;
        @XStreamAsAttribute
        public String unit;
        @XStreamImplicit(itemFieldName = "item")
        public List<Value> values = new ArrayList<>(0);

        public static class Value {
            @XStreamAsAttribute
            public String title;
            @XStreamAsAttribute
            public String value;
        }
    }
}
