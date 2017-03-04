package com.outsource.monitor.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xionghao on 2017/3/4.
 */
@XStreamAlias("func")
public class Func {

    @XStreamAsAttribute
    public String id;
    @XStreamAsAttribute
    public String secs;
    public Items items;
    @XStreamImplicit(itemFieldName = "constraint")
    public List<Constraint> constraints = new ArrayList<>(0);

    @XStreamAlias("items")
    public static class Items {

        @XStreamImplicit(itemFieldName = "item")
        public List<FuncItem> items = new ArrayList<>(0);

        public static class FuncItem {
            @XStreamAsAttribute
            public String name;
            @XStreamAsAttribute
            public boolean living;
        }
    }

    public static class Constraint {
        // item名称冲突，解析出错，先注释掉
        @XStreamImplicit(itemFieldName = "item")
        private List<Constraint.ConstraintItem> normalItems = new ArrayList<>(0);
        @XStreamImplicit(itemFieldName = "items")
        private List<Constraint.ConstraintItems> items = new ArrayList<>(0);

        public List<ConstraintItem> getItems() {
            if (normalItems == null) {
                normalItems = new ArrayList<>(0);
            }
            return normalItems;
        }

        public List<ConstraintItems> getConstraintItems() {
            if (items == null) {
                items = new ArrayList<>(0);
            }
            return items;
        }

        public static class ConstraintItem {
            @XStreamAsAttribute
            public String type;
            @XStreamAsAttribute
            public String master;
            @XStreamAsAttribute
            public String slave;
            @XStreamAsAttribute
            public String mvalue;
            @XStreamAsAttribute
            public String svalue;
        }

        public static class ConstraintItems {
            @XStreamAsAttribute
            public String type;
            @XStreamAsAttribute
            public String master;
            @XStreamAsAttribute
            public String slave;

            @XStreamImplicit(itemFieldName = "item")
            private List<Constraint.ConstraintItems.Value> values = new ArrayList<>(0);

            @XStreamAlias("item")
            public static class Value {
                @XStreamAsAttribute
                public String masteritems;
                @XStreamAsAttribute
                public String slaveitems;
            }

            public List<Value> getValues() {
                if (values == null) {
                    values = new ArrayList<>(0);
                }
                return values;
            }
        }

    }
}
