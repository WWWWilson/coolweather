package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;


/*
*daily_forecast中包含的是一个数组，详细看书512，数组中的每一项
*都代表着未来一天的天气信息，对于这种情况我们只需要定义出单日天气的实体类
*然后在声明实体类引用的时候使用集合类型来声明
 */
public class Forecast {
    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature{
        public String max;
        public String min;
    }

    public class More{

        @SerializedName("txt_d")
        public String info;
    }
}
