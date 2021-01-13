package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

//这个是总的实体类，来引用另外5个实体类
public class Weather {
    //天气数据中包含的status数据，成功返回true，失败返回具体的原因
    public String status;

    public Basic basic;

    public AQI aqi;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
