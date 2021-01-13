package com.example.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backBtn;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    //省列表
    private List<Province> provinceList;
    //市列表
    protected List<City> cityList;
    //县列表
    private List<County> countyList;
    //选中的省份
    private Province selectedProvince;
    //选中的城市
    private City selectedCity;
    //当前选中的级别
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //设置视图为choose_area
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = view.findViewById(R.id.choose_tv_title);
        backBtn = view.findViewById(R.id.choose_btn_back);
        listView = view.findViewById(R.id.choose_lv);
        //设置适配器
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;  
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //设置ListView上面的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //判断是省还是市
                if (currentLevel == LEVEL_PROVINCE){
                    //选择点击的省
                    selectedProvince = provinceList.get(position);
                    //查询市
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    //选择点击的市
                    selectedCity = cityList.get(position);
                    //查询县
                    queryCounties();
                }else if (currentLevel == LEVEL_COUNTY){
                    //从县列表中得到weatherId
                    String weatherId = countyList.get(position).getWeatherId();

                    /*
                    * instanceof判断一个对象是否属于一个类，这里判断碎片
                    * 是否在MainActivity
                    * */
                    if (getActivity() instanceof MainActivity){
                        //跳转到天气界面
                        Intent intent = new Intent(getActivity(),WeatherActivity.class);
                        //跳转过程中传递weatherId
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                        //如果碎片在WeatherActivity中
                    }else if (getActivity() instanceof WeatherActivity){
                        //获取WeatherActivity对象，并用来调用drawerLayout.closeDrawers()关闭菜单
                        //请求新城市的天气信息
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        //设置按钮的点击事件
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果当前页面是County
                if (currentLevel == LEVEL_COUNTY){
                    //查询市的页面(返回市的页面)
                    queryCities();
                    //如果当前页面是City
                }else if (currentLevel == LEVEL_CITY){
                    //查询省的页面(返回省的页面)
                    queryProvinces();
                }
            }
        });
        //一开始跳转到省页面
        queryProvinces();
    }

    //查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上面查询
    private void queryProvinces() {
        //设置标题为中国
        titleText.setText("中国");
        //当标题是中国时，则没有可返回的界面设置backBtn为不可见
        backBtn.setVisibility(View.GONE);
        //启用数据库查询
        provinceList = DataSupport.findAll(Province.class);
        //如果数据库中搜索到了
        if (provinceList.size()>0){
            dataList.clear();
            //把provinceList中的数据遍历到dataList中
            for (Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            //适配器更新数据
            adapter.notifyDataSetChanged();
            //设置当前listView选择的位置为第一行
            listView.setSelection(0);
            //设置currentLevel为Province
            currentLevel = LEVEL_PROVINCE;
            //否则从服务器上面查找
        }else {
            //定义服务器网址
            String address = "http://guolin.tech/api/china";
            //调用此方法便可访问服务器并查找省数据
            queryFromServer(address,"province");
        }
    }
    //查询全国所有的市，优先从数据库查询，如果没有查询到再去服务器上面查询
    private void queryCities() {
        //标题设置为所选中省的名字
        titleText.setText(selectedProvince.getProvinceName());
        //只是便可设置backBtn为可见
        backBtn.setVisibility(View.VISIBLE);
        //查找并显示出所选择省的市
        cityList = DataSupport.where("provinceid = ?",
                String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0){
            dataList.clear();
            //开始遍历City
            for (City city : cityList){
                dataList.add(city.getCityName());
            }
            //更新适配器中的内容
            adapter.notifyDataSetChanged();
            //设置默认选择在第一排
            listView.setSelection(0);
            //设置currentLevel为LEVEL_CITY
            currentLevel = LEVEL_CITY;
        }else {
            //定义所选择省的号码
            int provinceCode = selectedProvince.getProvinceCode();
            //定义所选择省的网址
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address,"city");
        }

    }
    //查询全国所有的县，优先从数据库查询，如果没有查询到再去服务器上面查询
    private void queryCounties() {
        //设置标题为所选市的名字
        titleText.setText(selectedCity.getCityName());
        //backBtn可见
        backBtn.setVisibility(View.VISIBLE);
        //查找并显示出所选择市的县
        countyList = DataSupport.where
                ("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0){
            dataList.clear();
            //遍历县
            for (County county : countyList){
                dataList.add(county.getCountyName());
            }
            //更新适配器
            adapter.notifyDataSetChanged();
            //默认选择第一行
            listView.setSelection(0);
            //设置currentLevel为LEVEL_COUNTY
            currentLevel = LEVEL_COUNTY;
        }else{
            //所选省的号码
            int provinceCode = selectedProvince.getProvinceCode();
            //所选市的号码
            int cityCode = selectedCity.getCityCode();
            //定义县所在的地址
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address,"county");
        }
    }

    //根据传入的地址和类型从服务器上查询省市县
    private void queryFromServer(String address,final String type){
        showProgressDialog();
        //调用HttpUtil中的sendOkHttpRequest()来向服务器发送请求
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            //表示请求返回结果的方法
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                //判断传入的类型
                if ("province".equals(type)){
                    //调用Utility.handleProvinceResponse()并传入responseText定义result结果
                    result = Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){
                    result = Utility.handleCityResponse
                            (responseText,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result = Utility.handleCountyResponse
                            (responseText,selectedCity.getId());
                }
                //如果传回来的result为true
                if (result){
                    /*
                    * 因为这里要重新调用 queryProvince() queryCities()queryCounties()更新数据
                    * 涉及到UI，而UI只能在主线程中更新，所以这一调用runOnUiThread()
                    * 当前子线程转换为主线程
                    * */
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            //表示请求失败的方法
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    //显示进度对话框
    private void showProgressDialog(){
        //如果没有对话框就重新实例化一个
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            //设置触摸对话框以外界面不能取消对话框
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    //关闭对话框
    private void closeProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
