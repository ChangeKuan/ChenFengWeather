package com.example.ChenFengWeather.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class AlarmReceiver extends BroadcastReceiver {
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, com.example.ChenFengWeather.service.AutoUpdateService.class);
        context.startService(i);
    }
}
