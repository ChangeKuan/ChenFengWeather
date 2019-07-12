package com.example.ChenFengWeather;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.bumptech.glide.Glide;
import com.example.ChenFengWeather.gson.AQI;
import com.example.ChenFengWeather.gson.Weather;
import com.example.ChenFengWeather.service.AutoUpdateService;
import com.example.ChenFengWeather.util.HttpUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.text.SimpleDateFormat;

import static com.example.ChenFengWeather.util.Utility.handleAQIResponse;
import static com.example.ChenFengWeather.util.Utility.handleWeatherResponse;


public class WeatherActivity extends AppCompatActivity {

    public LocationClient mLocationClient = null;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private ImageView weatherIcon;
    private LinearLayout forecastLayout;
    private LinearLayout lifestyleLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView qltyText;
    private TextView mainText;
    private TextView soText;
    private TextView noText;
    private TextView maxText;
    private TextView minText;
    private TextView rainText;
    private String   time;
    private ImageView backImg;

    public SwipeRefreshLayout swipeRefresh;
    private String mWeatherId;
    private String mAqiID;
    public DrawerLayout drawerLayout;
    private Button navButton;
    private Button locButton;
    private LineChartView linechart;

    List<PointValue> pointValues=new ArrayList<PointValue>();
    List<AxisValue> axisValues=new ArrayList<AxisValue>();
    List<AxisValue> axisValues2=new ArrayList<AxisValue>();
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21)
        {
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "chat";
            String channelName = "特殊天气提醒";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);

            channelId = "subscribe";
            channelName = "实时天气通知";
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            createNotificationChannel(channelId, channelName, importance);
        }

        setContentView(R.layout.activity_weather);
        //初始化各控件
        swipeRefresh= findViewById(R.id.swipe_refresh);
        weatherLayout= findViewById(R.id.weather_layout);
        titleCity= findViewById(R.id.title_city);
        titleUpdateTime= findViewById(R.id.title_update_time);
        degreeText= findViewById(R.id.degree_text);
        weatherIcon= findViewById(R.id.weather_icon);
        forecastLayout= findViewById(R.id.forecast_layout);
        lifestyleLayout= findViewById(R.id.lifestyle_layout);
        aqiText=(TextView)findViewById(R.id.aqi_text);
        pm25Text=(TextView)findViewById(R.id.pm25_text);
        qltyText=(TextView)findViewById(R.id.ql_text);
        mainText=(TextView)findViewById(R.id.main_text);
        soText=(TextView)findViewById(R.id.so_text);
        noText=(TextView)findViewById(R.id.no_text);
        minText=(TextView)findViewById(R.id.now_minT);
        maxText=(TextView)findViewById(R.id.now_maxT);
        rainText=(TextView)findViewById(R.id.now_rain);
        backImg= findViewById(R.id.back_img);
        drawerLayout= findViewById(R.id.drawer_layout);
        navButton= findViewById(R.id.nav_button);
        locButton= findViewById(R.id.location_button);
        linechart = (LineChartView) findViewById(R.id.line);

        swipeRefresh.setColorSchemeColors(R.color.colorPrimary);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        String aqiString=prefs.getString("aqi",null);

        if(weatherString!=null&&aqiString!=null)
        {
            //有缓存时直接解析天气数据
            Weather weather= handleWeatherResponse(weatherString);
            AQI aqi=handleAQIResponse(aqiString);
            mWeatherId=weather.getHeWeather6().get(0).getBasicX().getCid();
            mAqiID=weather.getHeWeather6().get(0).getBasicX().getParent_city();
            initchart(weather);
            SimpleDateFormat formatter  = new SimpleDateFormat("HH");
            Date curDate =  new Date(System.currentTimeMillis());
            time =  formatter.format(curDate);
            showWeatherInfo(weather);
            showAQIInfo(aqi);

        }else
        {   //无缓存时,去服务器查询天气
            mWeatherId=getIntent().getStringExtra("weather_id");
            mAqiID=getIntent().getStringExtra("aqi_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId,mAqiID);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {//手动刷新
                requestWeather(mWeatherId,mAqiID);
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//列表按钮
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        locButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//；定位按钮
                BDLocationListener listener = new MyLocationListener();

                mLocationClient = new LocationClient(getApplicationContext());
//注册监听器
                mLocationClient.registerLocationListener(listener);

                setOption();
            }
        });

    }

    /**
     * 根据天气id请求城市天气信息
     */
    public void requestWeather(String weatherId,String aqiId)
    {
        final String weatherUrl="https://free-api.heweather.com/s6/weather?location="+ weatherId +"&key=4b97bbe3cc074b2f8ff4dc1c393f8e15";
        final String aqiUrl="https://free-api.heweather.com/s6/air/now?location="+ aqiId +"&key=4b97bbe3cc074b2f8ff4dc1c393f8e15";

        /*
         * 对基本天气的访问
         */
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败onFailure",Toast.LENGTH_LONG).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                final Weather weather=handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if((weather != null) && "ok".equals(weather.getHeWeather6().get(0).getStatusX()))
                        {
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();

                            editor.putString("weather",responseText);
                            editor.apply();
                            mAqiID=weather.getHeWeather6().get(0).getBasicX().getParent_city();
                            mWeatherId=weather.getHeWeather6().get(0).getBasicX().getCid();
                            SimpleDateFormat formatter  =  new   SimpleDateFormat   ("HH");
                            Date curDate =  new Date(System.currentTimeMillis());
                            time =  formatter.format(curDate);
                            initchart(weather);
                            showWeatherInfo(weather);

                            if(weather.getHeWeather6().get(0).getNowX().getCond_txt().contains("雨")||weather.getHeWeather6().get(0).getNowX().getCond_txt().contains("雪")){
                                sendChatMsg(weather,1);
                            }else if(weather.getHeWeather6().get(0).getNowX().getCond_txt().contains("冰")){
                                sendChatMsg(weather,2);
                            }else if(Integer.valueOf(weather.getHeWeather6().get(0).getNowX().getTmp()).intValue()>30){
                                sendChatMsg(weather,3);
                            }else if (Integer.valueOf(weather.getHeWeather6().get(0).getNowX().getTmp()).intValue()<10){
                                sendChatMsg(weather,4);
                            }else if (Integer.valueOf(weather.getHeWeather6().get(0).getNowX().getWind_spd()).intValue()>=5){
                                sendChatMsg(weather,5);
                            }else sendSubscribeMsg(weather);

                            HttpUtil.sendOkHttpRequest(aqiUrl, new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(WeatherActivity.this,"获取天气信息失败onFailure",Toast.LENGTH_LONG).show();
                                            swipeRefresh.setRefreshing(false);
                                        }
                                    });
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    final String responseText=response.body().string();
                                    final AQI aqi=handleAQIResponse(responseText);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            if((aqi != null) && "ok".equals(aqi.getHeWeather6().get(0).getStatus()))
                                            {
                                                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();

                                                editor.putString("aqi",responseText);
                                                editor.apply();
                                                showAQIInfo(aqi);
                                            }else
                                            {
                                                Toast.makeText(WeatherActivity.this, responseText, Toast.LENGTH_SHORT).show();
                                            }
                                            swipeRefresh.setRefreshing(false);
                                        }

                                    });
                                }
                            });
                        }else
                        {
                            Toast.makeText(WeatherActivity.this, responseText, Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }


                });

            }
        });
    }

    private void showAQIInfo(AQI aqi) {


        if(aqi!=null)
        {
            aqiText.setText(aqi.getHeWeather6().get(0).getAir_now_city().getAqi());
            pm25Text.setText(aqi.getHeWeather6().get(0).getAir_now_city().getPm25());
            qltyText.setText(aqi.getHeWeather6().get(0).getAir_now_city().getQlty());
            mainText.setText(aqi.getHeWeather6().get(0).getAir_now_city().getMain());
            soText.setText(aqi.getHeWeather6().get(0).getAir_now_city().getSo2());
            noText.setText(aqi.getHeWeather6().get(0).getAir_now_city().getNo2());
        }

    }


    private void showWeatherInfo(Weather weather) {

        String cityName=weather.getHeWeather6().get(0).getBasicX().getLocation();
        String updateTime=weather.getHeWeather6().get(0).getUpdate().getLoc();
        String degree=weather.getHeWeather6().get(0).getNowX().getTmp()+"℃";
        String weatherCode=weather.getHeWeather6().get(0).getNowX().getCond_code();
        String nowMin="最低："+weather.getHeWeather6().get(0).getDaily_forecast().get(0).getTmp_min()+"℃";
        String nowMax="最高："+weather.getHeWeather6().get(0).getDaily_forecast().get(0).getTmp_max()+"℃";
        String nowRain="降水："+weather.getHeWeather6().get(0).getDaily_forecast().get(0).getPop()+"%";
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        minText.setText(nowMin);
        maxText.setText(nowMax);
        rainText.setText(nowRain);
        int timeNum = Integer.valueOf(time).intValue();
        if(timeNum>6&&timeNum<18) {
            weatherIcon.setImageResource(getResources().getIdentifier("icon_" + weatherCode+"d", "drawable", "com.example.ChenFengWeather"));
            backImg.setImageResource(getResources().getIdentifier("back_"+weatherCode+"d","drawable","com.example.ChenFengWeather"));
        }else{
            weatherIcon.setImageResource(getResources().getIdentifier("icon_" + weatherCode+"n", "drawable", "com.example.ChenFengWeather"));
            backImg.setImageResource(getResources().getIdentifier("back_"+weatherCode+"n","drawable","com.example.ChenFengWeather"));
        }
        forecastLayout.removeAllViews();
        lifestyleLayout.removeAllViews();
        for(int i=1;i<7;i++ )
        {View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dataText= view.findViewById(R.id.data_text);
            ImageView weatherText= view.findViewById(R.id.weather_text);
            TextView maxText= view.findViewById(R.id.max_text);
            TextView minText= view.findViewById(R.id.min_text);
            if (i==1){
                dataText.setText("明  天");
            }
            else if(i==2){
                dataText.setText("后  天");
            }
            else{
                dataText.setText(weather.getHeWeather6().get(0).getDaily_forecast().get(i).getDate().substring(5));
            }
            weatherText.setImageResource(getResources().getIdentifier("i"+weather.getHeWeather6().get(0).getDaily_forecast().get(i).getCond_code_d(),"drawable","com.example.ChenFengWeather"));
            maxText.setText(weather.getHeWeather6().get(0).getDaily_forecast().get(i).getTmp_max()+"℃");
            minText.setText(weather.getHeWeather6().get(0).getDaily_forecast().get(i).getTmp_min()+"℃");
            forecastLayout.addView(view);
        }

        for(int i=0;i<7;i++ ) {
            View view1 = LayoutInflater.from(this).inflate(R.layout.lifestyle_item, lifestyleLayout, false);
            TextView sugTitle = view1.findViewById(R.id.life_title);
            TextView sugText = view1.findViewById(R.id.life_text);
            switch (i){
                case 0:
                    sugTitle.setText("舒适指数");
                    break;
                case 1:
                    sugTitle.setText("穿衣建议");
                    break;
                case 2:
                    sugTitle.setText("感冒概率");
                break;
                case 3:
                    sugTitle.setText("运动建议");
                break;
                case 4:
                    sugTitle.setText("风力指数");
                break;
                case 5:
                    sugTitle.setText("紫外线");
                break;
                case 6:
                    sugTitle.setText("洗车建议");
                break;
            }
            sugText.setText(weather.getHeWeather6().get(0).getLifestyle().get(i).getBrf());
            lifestyleLayout.addView(view1);
        }
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent=new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setShowBadge(true);
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }
    public void sendChatMsg(Weather weather,int flag) {//发送特殊天气提醒
        Intent mainIntent = new Intent(this, WeatherActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int timeNum = Integer.valueOf(time).intValue();
        switch (flag){
            case 1:
        if(timeNum>6&&timeNum<18) {
            Notification notification = new NotificationCompat.Builder(this, "chat")
                    .setContentTitle("♥小晨提醒")
                    .setContentText(weather.getHeWeather6().get(0).getBasicX().getLocation() + " 现在" + weather.getHeWeather6().get(0).getNowX().getCond_txt() + "  " +
                            weather.getHeWeather6().get(0).getNowX().getTmp() + "℃。记得带上伞( • ̀ω•́ )")
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(getResources().getIdentifier("icon_" + weather.getHeWeather6().get(0).getNowX().getCond_code() + "d", "drawable", "com.example.ChenFengWeather"))
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("icon_" + weather.getHeWeather6().get(0).getNowX().getCond_code() + "d", "drawable", "com.example.ChenFengWeather")))
                    .setContentIntent(mainPendingIntent)
                    .setAutoCancel(true)
                    .setNumber(1)
                    .build();
            manager.notify(1, notification);
        }else{
            Notification notification = new NotificationCompat.Builder(this, "chat")
                    .setContentTitle("♥小晨提醒")
                    .setContentText(weather.getHeWeather6().get(0).getBasicX().getLocation() + " 现在" + weather.getHeWeather6().get(0).getNowX().getCond_txt() + "  " +
                            weather.getHeWeather6().get(0).getNowX().getTmp() + "℃。记得带上伞( • ̀ω•́ )")
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(getResources().getIdentifier("icon_" + weather.getHeWeather6().get(0).getNowX().getCond_code() + "n", "drawable", "com.example.ChenFengWeather"))
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("icon_" + weather.getHeWeather6().get(0).getNowX().getCond_code() + "n", "drawable", "com.example.ChenFengWeather")))
                    .setContentIntent(mainPendingIntent)
                    .setAutoCancel(true)
                    .setNumber(1)
                    .build();
            manager.notify(1, notification);
        }
        break;
            case 2:
                if(timeNum>6&&timeNum<18) {
                    Notification notification = new NotificationCompat.Builder(this, "chat")
                            .setContentTitle("♥小晨提醒")
                            .setContentText(weather.getHeWeather6().get(0).getBasicX().getLocation() + " 现在" + weather.getHeWeather6().get(0).getNowX().getCond_txt() + "  " +
                                    weather.getHeWeather6().get(0).getNowX().getTmp() + "℃。注意安全")
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(getResources().getIdentifier("icon_" + weather.getHeWeather6().get(0).getNowX().getCond_code() + "d", "drawable", "com.example.ChenFengWeather"))
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("icon_" + weather.getHeWeather6().get(0).getNowX().getCond_code() + "d", "drawable", "com.example.ChenFengWeather")))
                            .setContentIntent(mainPendingIntent)
                            .setAutoCancel(true)
                            .setNumber(1)
                            .build();
                    manager.notify(1, notification);
                }else{
                    Notification notification = new NotificationCompat.Builder(this, "chat")
                            .setContentTitle("♥小晨提醒")
                            .setContentText(weather.getHeWeather6().get(0).getBasicX().getLocation() + " 现在" + weather.getHeWeather6().get(0).getNowX().getCond_txt() + "  " +
                                    weather.getHeWeather6().get(0).getNowX().getTmp() + "℃。注意安全")
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(getResources().getIdentifier("icon_" + weather.getHeWeather6().get(0).getNowX().getCond_code() + "n", "drawable", "com.example.ChenFengWeather"))
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("icon_" + weather.getHeWeather6().get(0).getNowX().getCond_code() + "n", "drawable", "com.example.ChenFengWeather")))
                            .setContentIntent(mainPendingIntent)
                            .setAutoCancel(true)
                            .setNumber(1)
                            .build();
                    manager.notify(1, notification);
                }
                break;
            case 3:
                if(timeNum>6&&timeNum<18) {
                    Notification notification = new NotificationCompat.Builder(this, "chat")
                            .setContentTitle("♥小晨提醒")
                            .setContentText(weather.getHeWeather6().get(0).getBasicX().getLocation() + " 现在" + weather.getHeWeather6().get(0).getNowX().getCond_txt() + "  " +
                                    weather.getHeWeather6().get(0).getNowX().getTmp() + "℃。高温预警，记得防晒")
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(getResources().getIdentifier("icon_" + weather.getHeWeather6().get(0).getNowX().getCond_code() + "d", "drawable", "com.example.ChenFengWeather"))
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("icon_" + weather.getHeWeather6().get(0).getNowX().getCond_code() + "d", "drawable", "com.example.ChenFengWeather")))
                            .setContentIntent(mainPendingIntent)
                            .setAutoCancel(true)
                            .setNumber(1)
                            .build();
                    manager.notify(1, notification);
                }else{
                    Notification notification = new NotificationCompat.Builder(this, "chat")
                            .setContentTitle("♥小晨提醒")
                            .setContentText(weather.getHeWeather6().get(0).getBasicX().getLocation() + " 现在" + weather.getHeWeather6().get(0).getNowX().getCond_txt() + "  " +
                                    weather.getHeWeather6().get(0).getNowX().getTmp() + "℃。高温预警，记得防晒")
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(getResources().getIdentifier("icon_" + weather.getHeWeather6().get(0).getNowX().getCond_code() + "n", "drawable", "com.example.ChenFengWeather"))
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("icon_" + weather.getHeWeather6().get(0).getNowX().getCond_code() + "n", "drawable", "com.example.ChenFengWeather")))
                            .setContentIntent(mainPendingIntent)
                            .setAutoCancel(true)
                            .setNumber(1)
                            .build();
                    manager.notify(1, notification);
                }
                break;
            case 4:
                if(timeNum>6&&timeNum<18) {
                    Notification notification = new NotificationCompat.Builder(this, "chat")
                            .setContentTitle("♥小晨提醒")
                            .setContentText(weather.getHeWeather6().get(0).getBasicX().getLocation() + " 现在" + weather.getHeWeather6().get(0).getNowX().getCond_txt() + "  " +
                                    weather.getHeWeather6().get(0).getNowX().getTmp() + "℃。低温预警，注意保暖")
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(getResources().getIdentifier("icon_" + weather.getHeWeather6().get(0).getNowX().getCond_code() + "d", "drawable", "com.example.ChenFengWeather"))
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("icon_" + weather.getHeWeather6().get(0).getNowX().getCond_code() + "d", "drawable", "com.example.ChenFengWeather")))
                            .setContentIntent(mainPendingIntent)
                            .setAutoCancel(true)
                            .setNumber(1)
                            .build();
                    manager.notify(1, notification);
                }else{
                    Notification notification = new NotificationCompat.Builder(this, "chat")
                            .setContentTitle("♥小晨提醒")
                            .setContentText(weather.getHeWeather6().get(0).getBasicX().getLocation() + " 现在" + weather.getHeWeather6().get(0).getNowX().getCond_txt() + "  " +
                                    weather.getHeWeather6().get(0).getNowX().getTmp() + "℃。低温预警，注意保暖")
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(getResources().getIdentifier("icon_" + weather.getHeWeather6().get(0).getNowX().getCond_code() + "n", "drawable", "com.example.ChenFengWeather"))
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("icon_" + weather.getHeWeather6().get(0).getNowX().getCond_code() + "n", "drawable", "com.example.ChenFengWeather")))
                            .setContentIntent(mainPendingIntent)
                            .setAutoCancel(true)
                            .setNumber(1)
                            .build();
                    manager.notify(1, notification);
                }
                break;
            case 5:
                if(timeNum>6&&timeNum<18) {
                    Notification notification = new NotificationCompat.Builder(this, "chat")
                            .setContentTitle("♥小晨提醒")
                            .setContentText(weather.getHeWeather6().get(0).getBasicX().getLocation() + " 现在" + weather.getHeWeather6().get(0).getNowX().getCond_txt() + "  " +
                                    weather.getHeWeather6().get(0).getNowX().getTmp() + "℃。风力"+weather.getHeWeather6().get(0).getNowX().getWind_spd()+"级。风较大，出门注意安全")
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(getResources().getIdentifier("icon_" + weather.getHeWeather6().get(0).getNowX().getCond_code() + "d", "drawable", "com.example.ChenFengWeather"))
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("icon_" + weather.getHeWeather6().get(0).getNowX().getCond_code() + "d", "drawable", "com.example.ChenFengWeather")))
                            .setContentIntent(mainPendingIntent)
                            .setAutoCancel(true)
                            .setNumber(1)
                            .build();
                    manager.notify(1, notification);
                }else{
                    Notification notification = new NotificationCompat.Builder(this, "chat")
                            .setContentTitle("♥小晨提醒")
                            .setContentText(weather.getHeWeather6().get(0).getBasicX().getLocation() + " 现在" + weather.getHeWeather6().get(0).getNowX().getCond_txt() + "  " +
                                    weather.getHeWeather6().get(0).getNowX().getTmp() + "℃。风较大，出门注意安全")
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(getResources().getIdentifier("icon_" + weather.getHeWeather6().get(0).getNowX().getCond_code() + "n", "drawable", "com.example.ChenFengWeather"))
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("icon_" + weather.getHeWeather6().get(0).getNowX().getCond_code() + "n", "drawable", "com.example.ChenFengWeather")))
                            .setContentIntent(mainPendingIntent)
                            .setAutoCancel(true)
                            .setNumber(1)
                            .build();
                    manager.notify(1, notification);
                }
                break;
        }
    }

    public void sendSubscribeMsg(Weather weather) {//基本天气通知
        Intent mainIntent = new Intent(this, WeatherActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int timeNum = Integer.valueOf(time).intValue();
        if(timeNum>6&&timeNum<18) {
        Notification notification = new NotificationCompat.Builder(this, "subscribe")
                .setContentTitle(weather.getHeWeather6().get(0).getBasicX().getLocation())
                .setContentText(weather.getHeWeather6().get(0).getNowX().getCond_txt()+"  "+
                        weather.getHeWeather6().get(0).getNowX().getTmp()+"℃")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(getResources().getIdentifier("icon_"+weather.getHeWeather6().get(0).getNowX().getCond_code()+"d","drawable","com.example.ChenFengWeather"))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("icon_"+weather.getHeWeather6().get(0).getNowX().getCond_code()+"d","drawable","com.example.ChenFengWeather")))
                .setContentIntent(mainPendingIntent)
                .setAutoCancel(true)
                .setNumber(2)
                .build();
        manager.notify(2, notification);
        }else{
            Notification notification = new NotificationCompat.Builder(this, "subscribe")
                    .setContentTitle(weather.getHeWeather6().get(0).getBasicX().getLocation())
                    .setContentText(weather.getHeWeather6().get(0).getNowX().getCond_txt()+"  "+
                            weather.getHeWeather6().get(0).getNowX().getTmp()+"℃")
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(getResources().getIdentifier("icon_"+weather.getHeWeather6().get(0).getNowX().getCond_code()+"n","drawable","com.example.ChenFengWeather"))
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("icon_"+weather.getHeWeather6().get(0).getNowX().getCond_code()+"n","drawable","com.example.ChenFengWeather")))
                    .setContentIntent(mainPendingIntent)
                    .setAutoCancel(true)
                    .setNumber(2)
                    .build();
            manager.notify(2, notification);
        }

    }
    class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if(location==null){
                return;
            }

            StringBuffer sb2 = new StringBuffer(256);
            StringBuffer sb3 = new StringBuffer(256);
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb3.append(location.getCity().substring(0,location.getCity().length()-1));//获取详细地址信息

                sb2.append(location.getLongitude());
                sb2.append(",");
                sb2.append(location.getLatitude());

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果

                sb3.append(location.getCity().substring(0,location.getCity().length()-1));//获取详细地址信息

                sb2.append(location.getLongitude());
                sb2.append(",");
                sb2.append(location.getLatitude());

            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb3.append(location.getCity().substring(0,location.getCity().length()-1));//获取详细地址信息

                sb2.append(location.getLongitude());
                sb2.append(",");
                sb2.append(location.getLatitude());
            }
            mWeatherId=sb2.toString();
            mAqiID=sb3.toString();
            requestWeather(mWeatherId,mAqiID);
        }
    }

    private void setOption(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 0;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(false);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        option.setNeedDeviceDirect(true); //返回的定位结果包含手机机头方向
        mLocationClient.setLocOption(option);
        mLocationClient.start(); //启动位置请求
        mLocationClient.requestLocation();//发送请求
    }
    public void initchart(Weather weather){
        axisValues.clear();
        pointValues.clear();
        axisValues2.clear();
        //设置x轴显示
        for(int i=0;i<6;i++){
            axisValues.add(new AxisValue(i).setLabel(weather.getHeWeather6().get(0).getHourly().get(i).getTime().substring(11)+" "+weather.getHeWeather6().get(0).getHourly().get(i).getCond_txt()));
        }
        //设置点显示
        for(int i=0;i<6;i++){
            pointValues.add(new PointValue(i,Integer.valueOf(weather.getHeWeather6().get(0).getHourly().get(i).getTmp())).setLabel(weather.getHeWeather6().get(0).getHourly().get(i).getTmp()+"℃"));
        }
        for(int i=1;i<=20;i++){
            axisValues2.add(new AxisValue(i).setValue(i));
        }

        //线的设置
        Line line=new Line(pointValues);
        List<Line> lines=new ArrayList<Line>();
        line.setColor(Color.parseColor("#FFFFFF"));//黑色线
        line.setShape(ValueShape.CIRCLE);//圆形点
        line.setCubic(true);//折线
        line.setFilled(false);//不填充曲线面积
        line.setHasLabels(true);//线上数据坐标加上备注。
        line.setHasLines(true);//显示曲线
        line.setHasPoints(true);//显示点
        line.setStrokeWidth(1);//设置线宽
        line.setPointRadius(1);//设置点半径
        lines.add(line);
        LineChartData linedata=new LineChartData();
        linedata.setLines(lines);//加载线
        linedata.setValueLabelsTextColor(Color.WHITE);// 设置数据文字颜色
        linedata.setValueLabelTextSize(10);// 设置数据文字大小
        linedata.setValueLabelBackgroundEnabled(false);// 设置是否有数据背景
        //X轴
        Axis axis=new Axis();
        axis.setHasTiltedLabels(false);//字体斜的显示
        axis.setTextColor(Color.WHITE);
        axis.setMaxLabelChars(7);//x轴最多显示7个坐标
        axis.setTextSize(10);//设置字体大小
        axis.setHasLines(true);//显示分割线
        axis.setValues(axisValues);//加载X轴数据
        linedata.setAxisXBottom(axis);//底部显示坐标轴

        //Y轴
        Axis axiy=new Axis();
        axiy.setMaxLabelChars(5);//显示5个字符
        axiy.setTextColor(Color.WHITE);
        axiy.setTextSize(9);//设置字体大小
        axiy.setValues(axisValues2);//加载Y轴数据
        linedata.setAxisYLeft(axiy);//linedata加载Y轴，左边显示
        //整体设置
        axiy.setHasLines(true);// 是否显示Y轴网格线
        linechart.setZoomEnabled(false);//能缩放
        //linechart.setZoomType(ZoomType.HORIZONTAL);
        linechart.setInteractive(true);//可以与用户互动
        linechart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);//可以水平平滑
        linechart.setLineChartData(linedata);//加载linedata
        linechart.setZoomType(ZoomType.HORIZONTAL);
        Viewport v=new Viewport(linechart.getMaximumViewport());
        v.bottom=Integer.valueOf(weather.getHeWeather6().get(0).getDaily_forecast().get(0).getTmp_min())-1;
        v.top=Integer.valueOf(weather.getHeWeather6().get(0).getDaily_forecast().get(0).getTmp_max())+1;
        linechart.setMaximumViewport(v);
        v.left=0;
        v.right=3;
        linechart.setCurrentViewport(v);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }
}



