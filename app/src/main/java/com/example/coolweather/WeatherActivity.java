package com.example.coolweather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.service.AutoUpdateService;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView weatherIv;

    public SmartRefreshLayout refreshLayout;
    private String mWeatherId;

    public DrawerLayout drawerLayout;
    private Button chooseBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //当版本大于21也就是5.0才会执行下面if中的代码
        if (Build.VERSION.SDK_INT >= 21){
            //调用getWindow()和getDecorView()拿到当前活动的DecorView
            View decorView = getWindow().getDecorView();
            /*
            * 再调用它的setSystemUiVisibility()来改变系统UI显示
            *这里传入View.SYSTEM_UI_FLAG_FULLSCREEN和View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            * 表示活动的布局会显示在动态栏上面，最后调用setStatusBarColor()将状态栏设置成透明色
            * */
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);
        initView();
        //这里用PreferenceManager中的getDefaultSharedPreferences()得到SharedPreferences对象
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //调用SharedPreferences总的getString()读取数据并定义给weatherString
        String weatherString = prefs.getString("weather",null);
        if(weatherString != null){
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else{
            //无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                requestWeather(mWeatherId);
            }
        });


        //从SharedPreferences中获取必应图片
        String background = prefs.getString("bing_pic",null);
        //判断是否有
        if (background != null){
            //用Glide加载图片
            Glide.with(this).load(background).into(weatherIv);
        }else{
            //调用此方法加载图片
            loadBingPic();
        }
        //设置按钮点击事件，点击可打开DrawerLayout
        chooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    //根据天气Id请求城市天气信息
    public void requestWeather(final String weatherId){
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //判断weather是否为空并且返回值是"ok"
                        if (weather != null && "ok".equals(weather.status)){
                            //通过PreferenceManager中的getDefaultSharedPreferences()方法获得Editor对象
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this)
                                    .edit();
                            //通过Editor对象来传递数据
                            editor.putString("weather",responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                loadBingPic();
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    //处理并展示Weather实体类中的数据
    private void showWeatherInfo(Weather weather) {
        //通过Weather类中Basic类中的cityName的定义市名字
        String cityName = weather.basic.cityName;
        //通过weather类中Basic类中的update类中的updateTime的split方法定义更新时间
        String upDateTime = weather.basic.update.updateTime.split(" ")[1];
        //通过weather类中的Now类中的temperature来定义度数
        String degree = weather.now.temperature + "℃";
        //通过weather类中的Now类中的More类中的info定义天气信息
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(upDateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        //遍历weather类中forecastList的数据，forecastList是未来几天天气的数据
        for (Forecast forecast : weather.forecastList){
            //连接forecast_item
            View view = LayoutInflater.from(this).inflate
                    (R.layout.forecast_item,forecastLayout,false);
            TextView dateText = view.findViewById(R.id.forecast_item_tv_date);
            TextView infoText = view.findViewById(R.id.forecast_item_tv_info);
            TextView maxText = view.findViewById(R.id.forecast_item_tv_max);
            TextView minText = view.findViewById(R.id.forecast_item_tv_min);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        //aqi有数据则添加到视图中
        if (weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        //启动服务
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic,new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(weatherIv);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void initView() {
        weatherLayout = findViewById(R.id.weather_scroll_view);
        titleCity = findViewById(R.id.title_tv_city);
        titleUpdateTime = findViewById(R.id.title_tv_update_time);
        degreeText = findViewById(R.id.now_tv_degree);
        weatherInfoText = findViewById(R.id.now_tv_weather_info);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_tv);
        pm25Text = findViewById(R.id.aqi_tv_pm25);
        comfortText = findViewById(R.id.suggestion_tv_comfort);
        carWashText = findViewById(R.id.suggestion_tv_car_wash);
        sportText = findViewById(R.id.suggestion_tv_sport);
        weatherIv = findViewById(R.id.weather_iv);
        refreshLayout = findViewById(R.id.weather_refresh_layout);
        drawerLayout = findViewById(R.id.weather_drawer_layout);
        chooseBtn = findViewById(R.id.title_btn);
    }
}