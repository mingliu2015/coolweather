package com.example.coolweather;

import java.util.ArrayList;
import java.util.List;

import util.HttpCallbackListener;
import util.HttpUtil;
import util.ParseUtil;

import model.City;
import model.County;
import model.Province;

import db.CoolWeatherDB;
import android.app.Activity;
import android.app.DownloadManager.Query;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.support.v7.internal.widget.ProgressBarICS;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {

	private static final int LEVEL_PROVINCE = 0;
	private static final int LEVEL_CICY = 1;
	private static final int LEVEL_COUNTY = 2;
	private TextView textView; 
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private ArrayList<String> dataList = new ArrayList<String>();
	private ProgressDialog pDialog;
	private ArrayList<Province> listProvince;
	private ArrayList<City> listCity;
	private ArrayList<County> listCounty;
	/*
	 * for the selected
	 */
	private Province selectedProvince;
	private City selectedCity;
	private County selectedCounty;
	private String type;
	private boolean isFromWeatherActivity;
	/*
	 * current selected level
	 */
	private int currentLevel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		/*
		 * 判断已经选择过城市并且判断是从哪种方法进到本活动中，如果是从ShowWeather进来，则不做跳转
		 */
		if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity) {
			Intent intent = new Intent(this, ShowWeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		setContentView(R.layout.choose_area);		
		listView = (ListView) findViewById(R.id.list_view);
		textView = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, dataList);
		listView.setAdapter(adapter);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = listProvince.get(position);
					queryCities();
				} else if (currentLevel == LEVEL_CICY) {
					selectedCity = listCity.get(position);
					queryCounties();
				} else if (currentLevel == LEVEL_COUNTY) {
					/*
					 * 选择county后，则跳转至weather activity显示当前气象
					 */
					String countyCode = listCounty.get(position).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this,ShowWeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvinces();
	}
	
	public void queryProvinces(){
		listProvince = (ArrayList<Province>) coolWeatherDB.getAllProvince();
		if (listProvince != null && listProvince.size() > 0) {
			dataList.clear();
			for (Province p : listProvince) {
				dataList.add(p.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			textView.setText("China");
			currentLevel = LEVEL_PROVINCE;
		} else {
			type = "province";
			queryFromServer(null);
		}
	}
	
	public void queryCities() {
		int provinceCode = Integer.parseInt(selectedProvince.getProvinceCode());
		listCity = (ArrayList<City>) coolWeatherDB.loadCities(provinceCode);
		dataList.clear();
		if (listCity != null && listCity.size() > 0) {
			for (City city : listCity) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			textView.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CICY;
		} else {
			type = "city";
			queryFromServer(selectedProvince.getProvinceCode());
		}
	}
	
	private void queryCounties() {
		int cityCode = Integer.parseInt(selectedCity.getCityCode());
		listCounty = (ArrayList<County>) coolWeatherDB.loadCounties(cityCode);
		if (listCounty.size() > 0 && listCounty != null) {
			dataList.clear();
			for (County county : listCounty) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			textView.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			type = "county";
			queryFromServer(selectedCity.getCityCode());
		}
	}
	
	private void queryFromServer(String code) {
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code +
					".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				boolean result = false;
				if ("province".equals(type)) {
					result = ParseUtil.handleProvinceResponse(coolWeatherDB,
							response);
				} else if ("city".equals(type)) {
					result = ParseUtil.handleCityResponse(coolWeatherDB, response,Integer.parseInt(selectedProvince.getProvinceCode()));
				} else if ("county".equals(type)) {
					result = ParseUtil.handleCountyResponse(coolWeatherDB, response,Integer.parseInt(selectedCity.getCityCode()));
				}
				if (result) {
					runOnUiThread(new Runnable() {
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this,
								"加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	private void showProgressDialog() {
		if (pDialog == null) {
			pDialog = new ProgressDialog(this);
			pDialog.setMessage("正在加载...");
			pDialog.setCanceledOnTouchOutside(false);
			}
		pDialog.show();
	}
	
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CICY) {
			queryProvinces();
		} else {
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this, ShowWeatherActivity.class);
				startActivity(intent);
				}
			finish();
		}
	}

	private void closeProgressDialog() {
		if (pDialog != null) {
			pDialog.dismiss();
		}
	}
}
