package com.example.ChenFengWeather.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.ChenFengWeather.gson.Weather;
import com.example.ChenFengWeather.util.HttpUtil;

import java.io.IOException;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.example.ChenFengWeather.util.Utility.handleWeatherResponse;


public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 30*60* 1000; // 30分钟
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        if(weatherString!=null)
        {
            Weather weather= handleWeatherResponse(weatherString);
            String weatherId=weather.getHeWeather6().get(0).getBasicX().getCid();
            String weatherUrl="https://free-api.heweather.com/s6/weather?location="+weatherId.toString()+"&key=4b97bbe3cc074b2f8ff4dc1c393f8e15";

            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();

                Weather weather=handleWeatherResponse(responseText);

                            if((weather != null) && "ok".equals(weather.getHeWeather6().get(0).getStatusX()))
                            {
                                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();

                                editor.putString("weather",responseText);
                                editor.apply();

                            }
                        }

                    });
                }
        }
}
