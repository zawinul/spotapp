package com.andrenacci.spotapp;

import android.widget.TextView;

public class LogContent {
    private static String x = "";
    public static String get() {
        return x;
    }
    public static String add(String msg) {
        if (msg.equals("|clear|"))
            return x="";
        else {
            System.out.println("||| " + msg);
            x += msg + "\n";
            return x;
        }
    }

}
