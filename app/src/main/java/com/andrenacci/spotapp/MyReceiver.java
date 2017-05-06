package com.andrenacci.spotapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // assumes WordService is a registered service
        Log.d("MyReceiver", "---------------------" +intent.getAction());
        Object k = intent.getExtras().get("aaa");
        Log.d("MyReceiver", "---------------------" +k);

    }
}