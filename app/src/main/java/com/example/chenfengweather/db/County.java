package com.example.chenfengweather.db;

import org.litepal.crud.DataSupport;

public class County extends DataSupport {
    private int id; //必备字段
    private String countyName;//县名
    private String weatherId;//对应天气代号
    private int cityId;
    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }
    public String getCountyName(){
        return countyName;
    }
    public void setCountyName(String countyName){
        this.countyName = countyName;
    }
    public String getWeatherId(){
        return weatherId;
    }
    public void setWeatherId(String weatherId){
        this.weatherId = weatherId;
    }
    public void setCityId(int cityId){
        this.cityId = cityId;
    }
}
