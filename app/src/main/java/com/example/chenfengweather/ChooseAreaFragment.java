package com.example.ChenFengWeather;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.ChenFengWeather.db.City;
import com.example.ChenFengWeather.db.County;
import com.example.ChenFengWeather.db.Province;
import com.example.ChenFengWeather.util.HttpUtil;
import com.example.ChenFengWeather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;



public class ChooseAreaFragment extends Fragment {

    public LocationClient mLocationClient = null;
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList=new ArrayList<>();

   private List<Province> provincesList;
   private List<City> cityList;
   private List<County> countyList;

   private boolean flag = false;

   /**
    选中的省份
    */
   private Province selectedProvince;
   /**
   选中的城市
    */
   private City selectedCity;
    /**
     * 当前选中的级别
     */
   private int currentLevel;

    @RequiresApi(api = Build.VERSION_CODES.M)

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

       View view=inflater.inflate(R.layout.choose_area,container,false);
       titleText=(TextView)view.findViewById(R.id.title_text);
       backButton=(Button)view.findViewById(R.id.back_button);
       listView=(ListView)view.findViewById(R.id.list_view);
       adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
       listView.setAdapter(adapter);
       return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provincesList.get(position);
                    queryCities();
                }else if(currentLevel==LEVEL_CITY)
                {
                    selectedCity=cityList.get(position);
                    queryCounties();
                }else if(currentLevel==LEVEL_COUNTY)
                {
                    String weatherId=countyList.get(position).getweatherId();
                    if(getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                        //flag = true;
                    }
                    else if(getActivity() instanceof WeatherActivity)
                    {

                        WeatherActivity activity=(WeatherActivity)getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                        //flag = true;
                    }
                }
            }

        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel==LEVEL_COUNTY)
                {
                    queryCities();

                }else if(currentLevel==LEVEL_CITY)
                {
                    queryProvinces();
                }else if(currentLevel==LEVEL_PROVINCE)
                    /*Intent intent = new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("weather_id","null");
                    startActivity(intent);
                    //new AlertDialog.Builder(this).setTitle("自动定位").show();
                    getActivity().finish();*/
                    {
                        BDLocationListener listener = new ChooseAreaFragment.MyLocationListener();

                        mLocationClient = new LocationClient(getActivity().getApplicationContext());

//注册监听器
                        mLocationClient.registerLocationListener(listener);

                        setOption();
                    }


            }
        });
        queryProvinces();
    }

    /**
     * 查询中国所有的省,优先从数据库查询,如果没有查询到再到服务器上去查
     */
    private void queryProvinces()
    {
        titleText.setText("中国");
        backButton.setVisibility(View.VISIBLE);
        provincesList= DataSupport.findAll(Province.class);
        if(provincesList.size()>0)
        {
            dataList.clear();
            for(Province province:provincesList)
            {
                dataList.add(province.getProvinceName());

            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else
        {
            String address="http://guolin.tech/api/china";
            queryFromSever(address,"province");

        }
    }
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);

        cityList = DataSupport.where("provinceid=?", String.valueOf(selectedProvince.getId())).find(City.class);

        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else{
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromSever(address,"city");
        }
    }

    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);

        countyList = DataSupport.where("cityid=?", String.valueOf(selectedCity.getId())).find(County.class);

        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else{
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromSever(address,"county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     * @param address
     * @param type
     */
    private void queryFromSever(String address, final String type)
    {showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                if("province".equals(type))
                {
                 result= Utility.handleProvinceResponse(responseText);
                }else  if("city".equals(type))
                {
                    result= Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else  if("county".equals(type))
                {
                    result= Utility.handleCountyResponse(responseText,selectedCity.getId());
                }

                if(result)
                {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type))
                            {
                                queryProvinces();
                            }else if("city".equals(type))
                            {
                                queryCities();
                            }
                            else if("county".equals(type))
                            {
                                queryCounties();
                            }
                        }
                    });
                }

            }
        });

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
            String AutoweatherId=sb2.toString();
            if(getActivity() instanceof MainActivity) {
                Intent intent = new Intent(getActivity(), WeatherActivity.class);
                intent.putExtra("weather_id", AutoweatherId);
                startActivity(intent);
                getActivity().finish();
                //flag = true;
            }
            else if(getActivity() instanceof WeatherActivity)
            {

                WeatherActivity activity=(WeatherActivity)getActivity();
                activity.drawerLayout.closeDrawers();
                activity.swipeRefresh.setRefreshing(true);
                activity.requestWeather(AutoweatherId);
                //flag = true;
            }
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
    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if(progressDialog==null)
        {
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载....");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度条
     */
    private void closeProgressDialog()
    {
        if(progressDialog!=null)
            progressDialog.dismiss();
    }
}
