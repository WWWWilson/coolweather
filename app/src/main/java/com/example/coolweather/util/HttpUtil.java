package com.example.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        //创建OkHttpClient实例
        OkHttpClient client = new OkHttpClient();
        //创建request对象，调用url()并传入address并调用build()代表可以发送请求
        Request request = new Request.Builder()
                .url(address)
                .build();
        /*调用newCall()来创建Call对象，
        调用enqueue()来发送请求，
        并获得服务器返回的数据（用Response对象来接收返回的数据，这里没有写出来）
        * */
        client.newCall(request).enqueue(callback);
    }
}
