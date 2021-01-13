package com.example.coolweather.gson;


import com.google.gson.annotations.SerializedName;

/*
* 第三阶段风和天气返回的JSON数据复杂，这里用GSON对天气信息解析
* 前提是要将数据对应实体类创建好，这里筛选了一些重要数据(数据太多)
* */
public class Basic {


    /*
    * 由于JSON中一些字段可能不太适合直接作为Java字段来命名，
    * 这里使用@SerializedName注解的方式来让JSON字段和Java字段之间建立映射关系
    * */
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
