package com.example.ChenFengWeather;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
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

    private TextView myTV;
    //private String LocationId;
    public LocationClient mLocationClient = null;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
   // private TextView popText;
    private ImageView weatherIcon;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
   // private TextView aqiText;
  //  private TextView pm25Text;
  //  private TextView qltyText;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private TextView dressText;
    private TextView fluText;
    private TextView uvText;
    private TextView airText;
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
        setContentView(R.layout.activity_weather);
        //初始化各控件
        swipeRefresh= findViewById(R.id.swipe_refresh);
        weatherLayout= findViewById(R.id.weather_layout);
        titleCity= findViewById(R.id.title_city);
        titleUpdateTime= findViewById(R.id.title_update_time);
        degreeText= findViewById(R.id.degree_text);
     //   popText=(TextView)findViewById(R.id.pop_text);
        weatherInfoText= findViewById(R.id.weather_info_text);
        weatherIcon= findViewById(R.id.weather_icon);
        forecastLayout= findViewById(R.id.forecast_layout);
      //  aqiText=(TextView)findViewById(R.id.aqi_text);
      //  pm25Text=(TextView)findViewById(R.id.pm25_text);
      //  qltyText=(TextView)findViewById(R.id.qlty_text);
        comfortText= findViewById(R.id.comfort_text);
        carWashText= findViewById(R.id.car_wash_text);
        sportText= findViewById(R.id.sport_text);
        dressText= findViewById(R.id.drsg_text);
        fluText= findViewById(R.id.flu_text);
        uvText= findViewById(R.id.uv_text);
        airText= findViewById(R.id.air_text);
        bingPicImg= findViewById(R.id.bing_pic_img);
        drawerLayout= findViewById(R.id.drawer_layout);
        navButton= findViewById(R.id.nav_button);
        myTV = findViewById(R.id.position_text_view);

        BDLocationListener listener = new MyLocationListener();

        mLocationClient = new LocationClient(getApplicationContext());

//注册监听器
        mLocationClient.registerLocationListener(listener);

        setOption();

        swipeRefresh.setColorSchemeColors(R.color.colorPrimary);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        /*String aqiString=prefs.getString("aqi",null);*/
        String M =getIntent().getStringExtra("weather_id");

        if(M.equals("CN101200101")){
            flag=false;
 /*           weatherLayout.setVisibility(View.INVISIBLE);
            //mWeatherId = LocationId;
            requestWeather(LocationId);*/
        }else{
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
        }}
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
        if(bingPic!=null)
        {
           Glide.with(this).load(bingPic).into(bingPicImg);
           // bingPicImg.setImageResource();
        }
        else
        {
            loadBingPic();
        }
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
                       Glide.with(WeatherActivity.this).load(bingpic).into(bingPicImg);
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

        String cityName=weather.getHeWeather6().get(0).getBasicX().getLocation();
        String updateTime=weather.getHeWeather6().get(0).getUpdate().getLoc();
        String degree=weather.getHeWeather6().get(0).getNowX().getTmp()+"℃";
        String weatherInfo=weather.getHeWeather6().get(0).getNowX().getCond_txt();
        String weatherCode=weather.getHeWeather6().get(0).getNowX().getCond_code();
       // String popRate=weather.getHeWeather6().get(0).getDaily_forecast().get(0).getPop()+"%";
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
       // popText.setText(popRate);
        weatherIcon.setImageResource(getResources().getIdentifier("i"+weatherCode,"drawable","com.example.ChenFengWeather"));
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        for(int i=0;i<3;i++ )
        {View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
        TextView dataText= view.findViewById(R.id.data_text);
        TextView infoText= view.findViewById(R.id.info_text);
        TextView maxText= view.findViewById(R.id.max_text);
        TextView minText= view.findViewById(R.id.min_text);

        dataText.setText(weather.getHeWeather6().get(0).getDaily_forecast().get(i).getDate());
        infoText.setText(weather.getHeWeather6().get(0).getDaily_forecast().get(i).getCond_txt_n());
        maxText.setText(weather.getHeWeather6().get(0).getDaily_forecast().get(i).getTmp_max());
        minText.setText(weather.getHeWeather6().get(0).getDaily_forecast().get(i).getTmp_min());
        forecastLayout.addView(view);
        }


            comfortText.setText("舒适度："+weather.getHeWeather6().get(0).getLifestyle().get(0).getBrf()+"。"+weather.getHeWeather6().get(0).getLifestyle().get(0).getTxt());
            carWashText.setText("洗车指数："+weather.getHeWeather6().get(0).getLifestyle().get(6).getBrf()+"。"+weather.getHeWeather6().get(0).getLifestyle().get(6).getTxt());
            sportText.setText("运动指数："+weather.getHeWeather6().get(0).getLifestyle().get(3).getBrf()+"。"+weather.getHeWeather6().get(0).getLifestyle().get(3).getTxt());
            dressText.setText("穿衣指数："+weather.getHeWeather6().get(0).getLifestyle().get(1).getBrf()+"。"+weather.getHeWeather6().get(0).getLifestyle().get(1).getTxt());
            fluText.setText("感冒指数："+weather.getHeWeather6().get(0).getLifestyle().get(2).getBrf()+"。"+weather.getHeWeather6().get(0).getLifestyle().get(2).getTxt());
            uvText.setText("紫外线指数："+weather.getHeWeather6().get(0).getLifestyle().get(5).getBrf()+"。"+weather.getHeWeather6().get(0).getLifestyle().get(5).getTxt());
            airText.setText("空气指数："+weather.getHeWeather6().get(0).getLifestyle().get(7).getBrf()+"。"+weather.getHeWeather6().get(0).getLifestyle().get(7).getTxt());

        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent=new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if(location==null){
                return;
            }

            StringBuffer sb = new StringBuffer(256);
            StringBuffer sb2 = new StringBuffer(256);
           /* sb.append("time : ");
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
            sb.append(location.getRadius());*/
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                /*sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());//gps定位结果时，获取gps锁定用的卫星数
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 行进角度*/
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());//获取详细地址信息
                /*sb.append("\ndescribe : ");
                sb.append("gps定位成功");*/

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\n ");
                sb.append(location.getAddrStr());//获取详细地址信息

                sb2.append(location.getLongitude());
                sb2.append(",");
                sb2.append(location.getLatitude());
                /*sb.append("\n城市 : ");
                sb.append(location.getCity().substring(0,location.getCity().length()-1));
                sb.append("\nADCode: ");
                sb.append(location.getAdCode());//获取详细地址信息
                sb.append("\nLocationId: ");
                sb.append(location.getLocationID());//获取详细地址信息*/

               /* sb.append("\noperationers : ");
                sb.append(location.getOperators());//获取运营商信息*/
               /* sb.append("\n");
                sb.append("网络定位成功");*/
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                /*sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");*/
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
            /*sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息*/
           /* List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }*/
            //LocationId = location.getCity().substring(0,location.getCity().length()-1);
            //LocationId = "武汉";
            Log.v("TGA","***************************************"+sb.toString());
            myTV.setText(sb.toString());
            if(flag==false){
            weatherLayout.setVisibility(View.INVISIBLE);
            //mWeatherId = LocationId;
            requestWeather(sb2.toString());}
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }
}



