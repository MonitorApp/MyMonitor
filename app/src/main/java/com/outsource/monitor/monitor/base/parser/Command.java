package com.outsource.monitor.monitor.base.parser;

/**
 * Created by Administrator on 2016/11/20.
 */

public class Command {

    public String command;
    public Type type;

    public Command(String command, Type type) {
        this.command = command;
        this.type = type;
    }

    public enum Type {
        ITU, IFPAN, FSCAN, DF, DISCRETE, DIGIT
    }
}
