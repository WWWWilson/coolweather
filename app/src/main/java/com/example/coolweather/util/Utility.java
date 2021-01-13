package com.example.coolweather.util;

import android.text.TextUtils;
import android.widget.TextView;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//由于服务器返回的省市县数据都是JSON格式，所以提供此工具来解析和处理这种数据
public class Utility {
    //解析和处理服务器返回的省级数据
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                //创建一个JSON类型数组来存放各个省
                JSONArray allProvinces = new JSONArray(response);
                //开始遍历各个省
                for (int i=0;i<allProvinces.length();i++){
                    //得到各个省的对象
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    //创建Province实例
                    Province province = new Province();
                    //设置省名字
                    province.setProvinceName(provinceObject.getString("name"));
                    //设置省代号
                    province.setProvinceCode(provinceObject.getInt("id"));
                    //保存数据到数据库
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    /*
    * 解析和处理服务器返回的市级数据
    * */
    public static boolean handleCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            try {
                //创建一个JSON数组来存放各个市
                JSONArray allCities = new JSONArray(response);
                //开始遍历
                for (int i=0;i<allCities.length();i++){
                    //得到各个市的对象
                    JSONObject cityObject = allCities.getJSONObject(i);
                    //创建City对象
                    City city = new City();
                    //设置市名字
                    city.setCityName(cityObject.getString("name"));
                    //设置市代号
                    city.setCityCode(cityObject.getInt("id"));
                    //设置市所属的省id
                    city.setProvinceId(provinceId);
                    //保存数据到数据库
                    city.save();
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    /*
    * 解析和处理服务器返回的县级数据
    * */
    public static boolean handleCountyResponse(String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            try {
                //创建JSON数组存放各个县
                JSONArray allCounties = new JSONArray(response);
                for (int i=0;i<allCounties.length();i++){
                    //得到各个县的对象
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    //创建县对象
                    County county = new County();
                    //设置县名字
                    county.setCountyName(countyObject.getString("name"));
                    //设置天气代号
                    county.setWeatherId(countyObject.getString("weather_id"));
                    //设置县所在的市id
                    county.setCityId(cityId);
                    //保存数据到数据库
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    //需要添加一个用于解析天气JSON数据的方法
    //下面则就是将返回的JSON数据解析成Weather实体类
    public static Weather handleWeatherResponse(String response){
        try {
            //通过response参数得到天气对象
            JSONObject jsonObject = new JSONObject(response);
            //创建JSONArray数组并通过jsonObject的getJSONArray得到服务器返回的数据
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            //把数组中的内容转化成String
            String weatherContent = jsonArray.getJSONObject(0).toString();
            /*
            * 之前已经按照JSON数据格式定义好了相应的Weather的GSON实体类，
            * 这里只需要通过fromJson()就能直接将JSON数据转换成Weather对象
            * */
            return new Gson().fromJson(weatherContent,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
