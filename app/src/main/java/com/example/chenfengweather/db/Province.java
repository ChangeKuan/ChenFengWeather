package com.example.chenfengweather.db;

import org.litepal.crud.DataSupport;

public class Province extends DataSupport{
    private int id; //必备字段
    private String provinceName;//省份名
    private int provinceCode;//省份代号
    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }
    public String getProvinceName(){
        return provinceName;
    }
    public void setProvinceName(String provinceName){
        this.provinceName = provinceName;
    }
    public int getProvinceCode(){
        return provinceCode;
    }
    public void setProvinceCode(int provinceCode){
        this.provinceCode = provinceCode;
    }
}
