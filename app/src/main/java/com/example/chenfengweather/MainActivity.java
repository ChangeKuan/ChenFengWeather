package com.example.ChenFengWeather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.ChenFengWeather.gson.Weather;
import com.example.ChenFengWeather.service.AutoUpdateService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getString("weather",null)!=null)
        {
            Intent intent=new Intent(this, WeatherActivity.class);
            Intent intent2 = new Intent(this, AutoUpdateService.class);
            startActivity(intent);
            startService(intent2);
            finish();
        }
    }
}
