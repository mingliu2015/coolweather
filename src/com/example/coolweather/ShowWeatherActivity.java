package com.example.coolweather;

import reciever.AutoUpdateReceiver;
import service.AutoUpdateService;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.ParseUtil;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShowWeatherActivity extends Activity {
	
	private LinearLayout weatherInfoLayout;
	private TextView cityNameText;
	/**
	* ������ʾ����ʱ��
	*/
	private TextView publishText;
	/**
	* ������ʾ����������Ϣ
	*/
	private TextView weatherDespText;
	/**
	* ������ʾ����1
	*/
	private TextView temp1Text;
	/**
	* ������ʾ����2
	*/
	private TextView temp2Text;
	/**
	* ������ʾ��ǰ����
	*/
	private TextView currentDateText;
	/**
	* �л����а�ť
	*/
	private Button switchCity;
	/**
	* ����������ť
	*/
	private Button refreshWeather;
	
	private String type;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		currentDateText = (TextView) findViewById(R.id.current_date);
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			// ���ؼ�����ʱ��ȥ��ѯ����
			publishText.setText("ͬ����...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			// û���ؼ�����ʱ��ֱ����ʾ��������
			showWeather();
		}
		switchCity.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ShowWeatherActivity.this,
						ChooseAreaActivity.class);
				intent.putExtra("from_weather_activity", true);
				startActivity(intent);
				finish();
			}
		});
		refreshWeather.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				publishText.setText("ͬ����...");
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(ShowWeatherActivity.this);
				String weatherCode = prefs.getString("weather_code", "");
				if (!TextUtils.isEmpty(weatherCode)) {
					queryWeatherInfo(weatherCode);
				}
			}
		});
	}
	/*
	 * ��ѯ�ؼ����Ŷ�Ӧ����������
	 */
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city" +
		countyCode + ".xml";
		type = "countyCode";
		queryFromServer(address);
		}
	
	public void showWeather() {
		SharedPreferences prefs = PreferenceManager.
				getDefaultSharedPreferences(this);
				cityNameText.setText( prefs.getString("city_name", ""));
				temp1Text.setText(prefs.getString("temp1", ""));
				temp2Text.setText(prefs.getString("temp2", ""));
				weatherDespText.setText(prefs.getString("weather_desp", ""));
				publishText.setText("����" + prefs.getString("publish_time", "") + "����");
				currentDateText.setText(prefs.getString("current_date", ""));
				weatherInfoLayout.setVisibility(View.VISIBLE);
				cityNameText.setVisibility(View.VISIBLE);
				Intent intent = new Intent(this,AutoUpdateService.class);
				startService(intent);
	}
	/*
	 * ��ѯ������������Ӧ������
	 */
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/" +
		weatherCode + ".html";
		type = "weatherCode";
		queryFromServer(address);
		}
	private void queryFromServer(String address) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
							}
					}
				} else if ("weatherCode".equals(type)) {
					ParseUtil.handleWeatherResponse(response,ShowWeatherActivity.this);
					runOnUiThread(new Runnable() {
						public void run() {
							showWeather();
						}
					});
				}
 					
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					public void run() {
						publishText.setText("on Error");
					}
				});

			}
		});
	}
}
