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
import com.baidu.location.Poi;
import com.bumptech.glide.Glide;
import com.example.ChenFengWeather.gson.AQI;
import com.example.ChenFengWeather.gson.Weather;
import com.example.ChenFengWeather.service.AutoUpdateService;
import com.example.ChenFengWeather.util.HttpUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
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
    private LinearLayout hourlyLayout;
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
    private String mAqiID;
    public DrawerLayout drawerLayout;
    private Button navButton;
    private Button locButton;
    private boolean flag = false;
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
         aqiText=(TextView)findViewById(R.id.aqi_text);
         pm25Text=(TextView)findViewById(R.id.pm25_text);
        qltyText=(TextView)findViewById(R.id.ql_text);
        mainText=(TextView)findViewById(R.id.main_text);
        soText=(TextView)findViewById(R.id.so_text);
        noText=(TextView)findViewById(R.id.no_text);
        minText=(TextView)findViewById(R.id.now_minT);
        maxText=(TextView)findViewById(R.id.now_maxT);
        rainText=(TextView)findViewById(R.id.now_rain);
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
        locButton= findViewById(R.id.location_button);
        //myTV = findViewById(R.id.position_text_view);

        linechart = (LineChartView) findViewById(R.id.line);


        swipeRefresh.setColorSchemeColors(R.color.colorPrimary);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        String aqiString=prefs.getString("aqi",null);
        //String aqiString=prefs.getString("aqi",null);
        //String M =getIntent().getStringExtra("weather_id");

        if(weatherString!=null&&aqiString!=null)
        {
            //有缓存时直接解析天气数据
            Weather weather= handleWeatherResponse(weatherString);
            AQI aqi=handleAQIResponse(aqiString);
            mWeatherId=weather.getHeWeather6().get(0).getBasicX().getCid();
            mAqiID=weather.getHeWeather6().get(0).getBasicX().getParent_city();
            initchart(weather);
            showWeatherInfo(weather);
            showAQIInfo(aqi);

        }else
        {   //无缓存时,去服务器查询天气
            //String weatherId=getIntent().getStringExtra("weather_id");
            mWeatherId=getIntent().getStringExtra("weather_id");
            mAqiID=getIntent().getStringExtra("aqi_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId,mAqiID);
            //requestAqi(aqiID);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId,mAqiID);
               // requestAqi(aqiID);
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

        locButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BDLocationListener listener = new MyLocationListener();

                mLocationClient = new LocationClient(getApplicationContext());

//注册监听器
                mLocationClient.registerLocationListener(listener);


                setOption();
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
    public void requestWeather(String weatherId,String aqiId)
    {
        final String weatherUrl="https://free-api.heweather.com/s6/weather?location="+ weatherId +"&key=4b97bbe3cc074b2f8ff4dc1c393f8e15";
        final String aqiUrl="https://free-api.heweather.com/s6/air/now?location="+ aqiId +"&key=4b97bbe3cc074b2f8ff4dc1c393f8e15";
      //  final String hourlyUrl="https://free-api.heweather.net/s6/weather/hourly?location="+weatherId+"&key=4b97bbe3cc074b2f8ff4dc1c393f8e15";
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
                            mAqiID=weather.getHeWeather6().get(0).getBasicX().getParent_city();
                            mWeatherId=weather.getHeWeather6().get(0).getBasicX().getCid();
                            initchart(weather);
                            showWeatherInfo(weather);

                            if(weather.getHeWeather6().get(0).getNowX().getCond_txt().contains("雨")||weather.getHeWeather6().get(0).getNowX().getCond_txt().contains("雪")){
                                sendChatMsg(weather);
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
                                                //mAqiID=aqi.getHeWeather6().get(0).getBasic().getCid();
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
        /*HttpUtil.sendOkHttpRequest(hourlyUrl, new Callback() {
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
                final Weather hourly=handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if((hourly != null) && "ok".equals(hourly.getHeWeather6().get(0).getStatusX()))
                        {
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();

                            editor.putString("aqi",responseText);
                            editor.apply();
                            //mAqiID=aqi.getHeWeather6().get(0).getBasic().getCid();
                            showAQIInfo(aqi);
                        }else
                        {
                            Toast.makeText(WeatherActivity.this, responseText, Toast.LENGTH_SHORT).show();

                        }
                        swipeRefresh.setRefreshing(false);
                    }

                });
            }
        });*/

    }
    /*public void requestAqi(String aqiID){

    }*/

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

        //Typeface typeFace =Typeface.createFromAsset(getAssets(),"fonts/ziti.ttf");

        String cityName=weather.getHeWeather6().get(0).getBasicX().getLocation();
        String updateTime=weather.getHeWeather6().get(0).getUpdate().getLoc();
        String degree=weather.getHeWeather6().get(0).getNowX().getTmp()+"℃";
        //String weatherInfo=weather.getHeWeather6().get(0).getNowX().getCond_txt();
        String weatherCode=weather.getHeWeather6().get(0).getNowX().getCond_code();
        String nowMin="最低："+weather.getHeWeather6().get(0).getDaily_forecast().get(0).getTmp_min()+"℃";
        String nowMax="最高："+weather.getHeWeather6().get(0).getDaily_forecast().get(0).getTmp_max()+"℃";
        String nowRain="降水："+weather.getHeWeather6().get(0).getDaily_forecast().get(0).getPop()+"%";
        //String winddir=weather.getHeWeather6().get(0).getDaily_forecast().get(0).get;
        //String windSC=weather.getHeWeather6().get(0).getNowX().getWind_sc()+"级";
        // String popRate=weather.getHeWeather6().get(0).getDaily_forecast().get(0).getPop()+"%";
        titleCity.setText(cityName);
        //titleCity.setTypeface(typeFace);
        titleUpdateTime.setText(updateTime);
        //titleUpdateTime.setTypeface(typeFace);
        degreeText.setText(degree);
        minText.setText(nowMin);
        maxText.setText(nowMax);
        rainText.setText(nowRain);
        //degreeText.setTypeface(typeFace);
        // popText.setText(popRate);
        //windDir.setText(winddir);
        //windSc.setText(windSC);
        weatherIcon.setImageResource(getResources().getIdentifier("i"+weatherCode,"drawable","com.example.ChenFengWeather"));
        // weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        //hourlyLayout.removeAllViews();
        lifestyleLayout.removeAllViews();
        //myTV.setText(getIntent().getStringExtra("weather_id"));
        /*for(int i=0;i<6;i++ )
        {View view = LayoutInflater.from(this).inflate(R.layout.hourly_item,hourlyLayout,false);
            TextView OdataText= view.findViewById(R.id.Odata_text);
            ImageView OweatherText= view.findViewById(R.id.Oweather_text);
            TextView OmaxText= view.findViewById(R.id.Omax_text);

            OdataText.setText(weather.getHeWeather6().get(0).getHourly().get(i).getTime());
            OweatherText.setImageResource(getResources().getIdentifier("i"+weather.getHeWeather6().get(0).getHourly().get(i).getCond_code(),"drawable","com.example.ChenFengWeather"));
            //infoText.setText(weather.getHeWeather6().get(0).getDaily_forecast().get(i).getCond_txt_n());
            OmaxText.setText(weather.getHeWeather6().get(0).getHourly().get(i).getTmp());
            hourlyLayout.addView(view);
        }*/
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
            //infoText.setText(weather.getHeWeather6().get(0).getDaily_forecast().get(i).getCond_txt_n());
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
        channel.setShowBadge(true);
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }
    public void sendChatMsg(Weather weather) {
        Intent mainIntent = new Intent(this, WeatherActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, "chat")
                .setContentTitle("♥小晨提醒您")
                .setContentText(weather.getHeWeather6().get(0).getBasicX().getLocation()+" 现在"+weather.getHeWeather6().get(0).getNowX().getCond_txt()+"  "+
                        weather.getHeWeather6().get(0).getNowX().getTmp()+"℃。记得带上伞( • ̀ω•́ )")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(getResources().getIdentifier("i"+weather.getHeWeather6().get(0).getNowX().getCond_code(),"drawable","com.example.ChenFengWeather"))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("i"+weather.getHeWeather6().get(0).getNowX().getCond_code(),"drawable","com.example.ChenFengWeather")))
                .setContentIntent(mainPendingIntent)
                .setAutoCancel(true)
                .setNumber(1)
                .build();
        manager.notify(1, notification);
    }

    public void sendSubscribeMsg(Weather weather) {
        Intent mainIntent = new Intent(this, WeatherActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, "subscribe")
                .setContentTitle(weather.getHeWeather6().get(0).getBasicX().getLocation())
                .setContentText(weather.getHeWeather6().get(0).getNowX().getCond_txt()+"  "+
                        weather.getHeWeather6().get(0).getNowX().getTmp()+"℃")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(getResources().getIdentifier("i"+weather.getHeWeather6().get(0).getNowX().getCond_code(),"drawable","com.example.ChenFengWeather"))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("i"+weather.getHeWeather6().get(0).getNowX().getCond_code(),"drawable","com.example.ChenFengWeather")))
                .setContentIntent(mainPendingIntent)
                .setAutoCancel(true)
                .setNumber(2)
                .build();
        manager.notify(2, notification);
    }
    class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if(location==null){
                return;
            }

            StringBuffer sb = new StringBuffer(256);
            StringBuffer sb2 = new StringBuffer(256);
            StringBuffer sb3 = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\ntype : ");
            sb.append(location.getLocType());
            sb.append("\nerror code : ");
            sb.append(location.getLocTypeDescription());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");//定位精度
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());//gps定位结果时，获取gps锁定用的卫星数
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 行进角度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());//获取详细地址信息
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果

                sb3.append(location.getCity().substring(0,location.getCity().length()-1));//获取详细地址信息

                sb2.append(location.getLongitude());
                sb2.append(",");
                sb2.append(location.getLatitude());
                sb.append("\n城市 : ");
                sb.append(location.getCity().substring(0,location.getCity().length()-1));
                sb.append("\nADCode: ");
                sb.append(location.getAdCode());//获取详细地址信息
                sb.append("\nLocationId: ");
                sb.append(location.getLocationID());//获取详细地址信息

                sb.append("\noperationers : ");
                sb.append(location.getOperators());//获取运营商信息
                sb.append("\n");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            mWeatherId=sb2.toString();
            mAqiID=sb3.toString();
            requestWeather(mWeatherId,mAqiID);
                //flag = true;

            //LocationId = location.getCity().substring(0,location.getCity().length()-1);
            //LocationId = "武汉";

        }
    }



    private void setOption(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 0; //5秒发送一次
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
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
        //line.setHasLabelsOnlyForSelected(true);
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
        //axis.setName("日期");
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

        /*//设置触摸事件
        linechart.setOnValueTouchListener(new LineChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
                Toast.makeText(MainActivity.this, ""+value.getY(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onValueDeselected() {

            }
        });*/
        Viewport v=new Viewport(linechart.getMaximumViewport());
        v.bottom=Integer.valueOf(weather.getHeWeather6().get(0).getDaily_forecast().get(0).getTmp_min());
        v.top=Integer.valueOf(weather.getHeWeather6().get(0).getDaily_forecast().get(0).getTmp_max());
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



