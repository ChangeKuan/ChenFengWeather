package com.example.chenfengweather.db;

import org.litepal.crud.DataSupport;

public class City extends DataSupport {
    private int id; //必备字段
    private String cityName;//市名
    private int cityCode;//市代号
    private int provinceId;
    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }
    public String getCityName(){
        return cityName;
    }
    public void setCityName(String cityName){
        this.cityName = cityName;
    }
    public int getCityCode(){
        return cityCode;
    }
    public void setCityCode(int cityeCode){
        this.cityCode = cityCode;
    }
    public void setProvinceId(int provinceId){
        this.provinceId = provinceId;
    }
}
