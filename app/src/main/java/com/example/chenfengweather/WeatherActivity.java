package com.example.ChenFengWeather;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
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
import com.bumptech.glide.Glide;
import com.example.ChenFengWeather.gson.AQI;
import com.example.ChenFengWeather.gson.Weather;
import com.example.ChenFengWeather.service.AutoUpdateService;
import com.example.ChenFengWeather.util.HttpUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.example.ChenFengWeather.util.Utility.handleAQIResponse;
import static com.example.ChenFengWeather.util.Utility.handleWeatherResponse;


public class WeatherActivity extends AppCompatActivity {

    //private TextView myTV;
    //private String LocationId;
    public LocationClient mLocationClient = null;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    // private TextView popText;
    private ImageView weatherIcon;
    private ImageView weatherText;
    // private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private LinearLayout lifestyleLayout;
    // private TextView aqiText;
    //  private TextView pm25Text;
    //  private TextView qltyText;
    /*private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private TextView dressText;
    private TextView fluText;
    private TextView uvText;
    private TextView airText;*/
    // private TextView windDir;
    // private TextView windSc;
    private ImageView bingPicImg;

    public SwipeRefreshLayout swipeRefresh;
    private String mWeatherId;

    public DrawerLayout drawerLayout;
    private Button navButton;
    private boolean flag = true;

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
            String channelName = "聊天消息";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);

            channelId = "subscribe";
            channelName = "订阅消息";
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
        //   popText=(TextView)findViewById(R.id.pop_text);
        // weatherInfoText= findViewById(R.id.weather_info_text);
        weatherIcon= findViewById(R.id.weather_icon);
        weatherText= findViewById(R.id.weather_text);
        forecastLayout= findViewById(R.id.forecast_layout);
        lifestyleLayout= findViewById(R.id.lifestyle_layout);
        //  aqiText=(TextView)findViewById(R.id.aqi_text);
        //  pm25Text=(TextView)findViewById(R.id.pm25_text);
        //  qltyText=(TextView)findViewById(R.id.qlty_text);
        /*comfortText= findViewById(R.id.comfort_text);
        carWashText= findViewById(R.id.car_wash_text);
        sportText= findViewById(R.id.sport_text);
        dressText= findViewById(R.id.drsg_text);
        fluText= findViewById(R.id.flu_text);
        uvText= findViewById(R.id.uv_text);
        airText= findViewById(R.id.air_text);*/
        //windDir=findViewById(R.id.wind_dir);
        //windSc=findViewById(R.id.wind_sc);
        bingPicImg= findViewById(R.id.bing_pic_img);
        drawerLayout= findViewById(R.id.drawer_layout);
        navButton= findViewById(R.id.nav_button);
        //myTV = findViewById(R.id.position_text_view);



        swipeRefresh.setColorSchemeColors(R.color.colorPrimary);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        /*String aqiString=prefs.getString("aqi",null);*/
        //String M =getIntent().getStringExtra("weather_id");

        if(weatherString!=null)
        {
            //有缓存时直接解析天气数据
            Weather weather= handleWeatherResponse(weatherString);
            mWeatherId=weather.getHeWeather6().get(0).getBasicX().getCid();

            showWeatherInfo(weather);

        }else
        {   //无缓存时,去服务器查询天气
            //String weatherId=getIntent().getStringExtra("weather_id");
            mWeatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        /*
         * 读取缓存在SharedPreference的pic数据,
         * */
        String bingPic=prefs.getString("bing_pic",null);
        loadBingPic();
        /*if(bingPic!=null)
        {
           Glide.with(this).load(bingPic).into(bingPicImg);
           // bingPicImg.setImageResource();
        }
        else
        {
            loadBingPic();
        }*/
//yt
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

    }

    /*
     * 加载必应每日一图
     */

    private void loadBingPic() {
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingpic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingpic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Glide.with(WeatherActivity.this).load(bingpic).into(bingPicImg);
                        bingPicImg.setImageResource(getResources().getIdentifier("wallpaper","drawable","com.example.ChenFengWeather"));
                    }
                });
            }
        });
    }

    /**
     * 根据天气id请求城市天气信息
     */
    public void requestWeather(String weatherId)
    {
        final String weatherUrl="https://free-api.heweather.com/s6/weather?location="+ weatherId +"&key=4b97bbe3cc074b2f8ff4dc1c393f8e15";
        final String aqiUrl="https://free-api.heweather.com/s6/air/now?location="+ weatherId +"&key=4b97bbe3cc074b2f8ff4dc1c393f8e15";
        // final String lifeUrl="https://free-api.heweather.net/s6/weather/lifestyle?location="+weatherId.toString()+"&key=4b97bbe3cc074b2f8ff4dc1c393f8e15";


        /**
         * 这是对基本天气的访问,但是缺了aqi这一项
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
                            mWeatherId=weather.getHeWeather6().get(0).getBasicX().getCid();
                            showWeatherInfo(weather);

                            if(weather.getHeWeather6().get(0).getNowX().getCond_txt().contains("雨")||weather.getHeWeather6().get(0).getNowX().getCond_txt().contains("雪")){
                                sendChatMsg(weather);
                            }else sendSubscribeMsg(weather);

                        }else
                        {
                            Toast.makeText(WeatherActivity.this, responseText, Toast.LENGTH_SHORT).show();

                        }
                        swipeRefresh.setRefreshing(false);
                    }


                });

            }
        });

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

                            editor.putString("weather",responseText);
                            editor.apply();
                            mWeatherId=aqi.getHeWeather6().get(0).getBasic().getCid();
                            //  showAQIInfo(aqi);
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
/*
    private void showAQIInfo(AQI aqi) {

    if(aqi!=null)
    {
        aqiText.setText(aqi.getHeWeather6().get(0).getAir_now_city().getAqi());
        pm25Text.setText(aqi.getHeWeather6().get(0).getAir_now_city().getPm25());
        qltyText.setText(aqi.getHeWeather6().get(0).getAir_now_city().getQlty());
    }

    }*/


    private void showWeatherInfo(Weather weather) {

        //Typeface typeFace =Typeface.createFromAsset(getAssets(),"fonts/ziti.ttf");

        String cityName=weather.getHeWeather6().get(0).getBasicX().getLocation();
        String updateTime=weather.getHeWeather6().get(0).getUpdate().getLoc();
        String degree=weather.getHeWeather6().get(0).getNowX().getTmp()+"℃";
        String weatherInfo=weather.getHeWeather6().get(0).getNowX().getCond_txt();
        String weatherCode=weather.getHeWeather6().get(0).getNowX().getCond_code();
        //String winddir=weather.getHeWeather6().get(0).getDaily_forecast().get(0).get;
        //String windSC=weather.getHeWeather6().get(0).getNowX().getWind_sc()+"级";
        // String popRate=weather.getHeWeather6().get(0).getDaily_forecast().get(0).getPop()+"%";
        titleCity.setText(cityName);
        //titleCity.setTypeface(typeFace);
        titleUpdateTime.setText(updateTime);
        //titleUpdateTime.setTypeface(typeFace);
        degreeText.setText(degree);
        //degreeText.setTypeface(typeFace);
        // popText.setText(popRate);
        //windDir.setText(winddir);
        //windSc.setText(windSC);
        weatherIcon.setImageResource(getResources().getIdentifier("i"+weatherCode,"drawable","com.example.ChenFengWeather"));
        // weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        lifestyleLayout.removeAllViews();
        //myTV.setText(getIntent().getStringExtra("weather_id"));
        for(int i=0;i<7;i++ )
        {View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dataText= view.findViewById(R.id.data_text);
            ImageView weatherText= view.findViewById(R.id.weather_text);
            TextView maxText= view.findViewById(R.id.max_text);
            TextView minText= view.findViewById(R.id.min_text);

            dataText.setText(weather.getHeWeather6().get(0).getDaily_forecast().get(i).getDate());
            weatherText.setImageResource(getResources().getIdentifier("i"+weather.getHeWeather6().get(0).getDaily_forecast().get(i).getCond_code_d(),"drawable","com.example.ChenFengWeather"));
            //infoText.setText(weather.getHeWeather6().get(0).getDaily_forecast().get(i).getCond_txt_n());
            maxText.setText(weather.getHeWeather6().get(0).getDaily_forecast().get(i).getTmp_max());
            minText.setText(weather.getHeWeather6().get(0).getDaily_forecast().get(i).getTmp_min());
            forecastLayout.addView(view);
        }

        for(int i=0;i<8;i++ ) {
            View view1 = LayoutInflater.from(this).inflate(R.layout.lifestyle_item, lifestyleLayout, false);
            TextView sugTitle = view1.findViewById(R.id.life_title);
            TextView sugText = view1.findViewById(R.id.life_text);
            switch (i){
                case 0:
                    sugTitle.setText("舒适度");
                    break;
                case 1:
                    sugTitle.setText("穿衣指数");
                    break;
                case 2:
                    sugTitle.setText("感冒指数");
                break;
                case 3:
                    sugTitle.setText("运动指数");
                break;
                case 4:
                    sugTitle.setText("风力指数");
                break;
                case 5:
                    sugTitle.setText("紫外线");
                break;
                case 6:
                    sugTitle.setText("洗车指数");
                break;
                case 7:
                    sugTitle.setText("空气指数");
                break;
            }
            sugText.setText(weather.getHeWeather6().get(0).getLifestyle().get(i).getBrf());
            lifestyleLayout.addView(view1);
        }
        /*comfortText.setText("舒适度："+weather.getHeWeather6().get(0).getLifestyle().get(0).getBrf()+"。"+weather.getHeWeather6().get(0).getLifestyle().get(0).getTxt());
        carWashText.setText("洗车指数："+weather.getHeWeather6().get(0).getLifestyle().get(6).getBrf()+"。"+weather.getHeWeather6().get(0).getLifestyle().get(6).getTxt());
        sportText.setText("运动指数："+weather.getHeWeather6().get(0).getLifestyle().get(3).getBrf()+"。"+weather.getHeWeather6().get(0).getLifestyle().get(3).getTxt());
        dressText.setText("穿衣指数："+weather.getHeWeather6().get(0).getLifestyle().get(1).getBrf()+"。"+weather.getHeWeather6().get(0).getLifestyle().get(1).getTxt());
        fluText.setText("感冒指数："+weather.getHeWeather6().get(0).getLifestyle().get(2).getBrf()+"。"+weather.getHeWeather6().get(0).getLifestyle().get(2).getTxt());
        uvText.setText("紫外线指数："+weather.getHeWeather6().get(0).getLifestyle().get(5).getBrf()+"。"+weather.getHeWeather6().get(0).getLifestyle().get(5).getTxt());
        airText.setText("空气指数："+weather.getHeWeather6().get(0).getLifestyle().get(7).getBrf()+"。"+weather.getHeWeather6().get(0).getLifestyle().get(7).getTxt());*/

        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent=new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }
    public void sendChatMsg(Weather weather) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, "chat")
                .setContentTitle("♥小晨提醒您")
                .setContentText(weather.getHeWeather6().get(0).getBasicX().getLocation()+" 现在"+weather.getHeWeather6().get(0).getNowX().getCond_txt()+"  "+
                        weather.getHeWeather6().get(0).getNowX().getTmp()+"℃。记得带上伞( • ̀ω•́ )")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(getResources().getIdentifier("i"+weather.getHeWeather6().get(0).getNowX().getCond_code(),"drawable","com.example.ChenFengWeather"))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("i"+weather.getHeWeather6().get(0).getNowX().getCond_code(),"drawable","com.example.ChenFengWeather")))
                .setAutoCancel(true)
                .build();
        manager.notify(1, notification);
    }

    public void sendSubscribeMsg(Weather weather) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, "subscribe")
                .setContentTitle(weather.getHeWeather6().get(0).getBasicX().getLocation())
                .setContentText(weather.getHeWeather6().get(0).getNowX().getCond_txt()+"  "+
                        weather.getHeWeather6().get(0).getNowX().getTmp()+"℃")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(getResources().getIdentifier("i"+weather.getHeWeather6().get(0).getNowX().getCond_code(),"drawable","com.example.ChenFengWeather"))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("i"+weather.getHeWeather6().get(0).getNowX().getCond_code(),"drawable","com.example.ChenFengWeather")))
                .setAutoCancel(true)
                .build();
        manager.notify(2, notification);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }
}



