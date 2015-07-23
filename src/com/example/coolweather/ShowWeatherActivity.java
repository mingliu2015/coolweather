package com.example.coolweather;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.view.MovingPictureView;

import reciever.AutoUpdateReceiver;
import service.AutoUpdateService;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.ParseUtil;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ShowWeatherActivity extends Activity {
	
	private LinearLayout weatherInfoLayout;
	private TextView cityNameText;
	private RelativeLayout weather_bg;
	private MovingPictureView mMove1,mMove2,mMove3;
	/**
	* 用于显示发布时间
	*/
	private TextView publishText;
	/**
	* 用于显示天气描述信息
	*/
	private TextView weatherDespText;
	/**
	* 用于显示气温1
	*/
	private TextView temp1Text;
	/**
	* 用于显示气温2
	*/
	private TextView temp2Text;
	/**
	* 用于显示当前日期
	*/
	private TextView currentDateText;
	/**
	* 切换城市按钮
	*/
	private Button switchCity;
	/**
	* 更新天气按钮
	*/
	private Button refreshWeather;
	/*
	 * s 
	 */
	private String type;
	
	//private static String[] weather_description = new String[]{"晴","雨"};
	
	public static int imgIndex;
	
	public static Timer weather_timer;
	
	private List<MovingPictureView> movingList = new ArrayList<MovingPictureView>();
	
	private ImageView rainImage1;
	private ImageView rainImage2;
	private ImageView rainImage3;
	
	/*
	 * 定时任务调用此handler进行对UI操作
	 */
	private RainHanlder rainHanlder = new RainHanlder(){
		@Override
		public void handleMessage(Message msg) {
			if (weather_bg.indexOfChild(rainImage1) != -1) {
				weather_bg.removeViewAt(weather_bg.indexOfChild(rainImage1));
			}
			if (weather_bg.indexOfChild(rainImage2) != -1) {
				weather_bg.removeViewAt(weather_bg.indexOfChild(rainImage2));
			}
			
			int number = (Integer) msg.obj;
			switch(number) {
			case 0:
				weather_bg.addView(rainImage1);
				break;
			case 1:
				weather_bg.addView(rainImage2);
				break;
			default:break;
			}	
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		rainImage1 = new ImageView(this);
		rainImage2 = new ImageView(this);
		rainImage3 = new ImageView(this);
		weather_bg = (RelativeLayout) findViewById(R.id.weather_background);
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		currentDateText = (TextView) findViewById(R.id.current_date);
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		weather_bg.removeView(mMove1);
		weather_bg.removeView(mMove2);
		weather_bg.removeView(mMove3);
		
		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			// 有县级代号时就去查询天气
			publishText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			// 没有县级代号时就直接显示本地天气
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
				publishText.setText("同步中...");
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(ShowWeatherActivity.this);
				String weatherCode = prefs.getString("weather_code", "");
				if (!TextUtils.isEmpty(weatherCode)) {
					queryWeatherInfo(weatherCode);
				}
			}
		});
	}
	
	private void loadMovePic(){
		mMove1 = new MovingPictureView(this, R.drawable.yjjc_h_a3,-300,10,40);
		mMove2 = new MovingPictureView(this, R.drawable.yjjc_h_a3,250,10,40);
		mMove3 = new MovingPictureView(this, R.drawable.yjjc_h_a4,480,40,40);
		weather_bg.addView(mMove1);
		weather_bg.addView(mMove2);
		weather_bg.addView(mMove3);
	}
	
	/*
	 * 查询县级代号对应的天气代号
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
				String weather = prefs.getString("weather_desp", "");
				if (weather.contains("晴")) {
					loadMovePic();
					weather_bg.setBackgroundResource(R.drawable.yjjc_h_a1);
					if(!mMove1.isstarted){
						new Thread(mMove1).start();//每一个移动的图片都是一个线程
						new Thread(mMove2).start();
						new Thread(mMove3).start();
					}
				} else if (weather.contains("雨")) {
					/*
					 * 添加两张下雨图片
					 */
					weather_bg.setBackgroundResource(R.drawable.yjjc_h_e1);
					rainImage1.setImageResource(R.drawable.yjjc_h_e2);
					rainImage1.setId(100);
					rainImage2.setImageResource(R.drawable.yjjc_h_e3);
					rainImage2.setId(110);
					/*
					 * 开启图片切换的定时任务
					 */
					RainTimer timer = new RainTimer();
					Thread rainThread = new Thread(timer);
					rainThread.start();
				} else if (weather.contains("多云")) {
					loadMovePic();
					weather_bg.setBackgroundResource(R.drawable.yjjc_h_c1);
					if(!mMove1.isstarted){
						new Thread(mMove1).start();//每一个移动的图片都是一个线程
						new Thread(mMove2).start();
						new Thread(mMove3).start();
					}
				}
				cityNameText.setText(prefs.getString("city_name", ""));
				temp1Text.setText(prefs.getString("temp1", ""));
				temp2Text.setText(prefs.getString("temp2", ""));
				//String weather = prefs.getString("weather_desp", "");
				weatherDespText.setText(prefs.getString("weather_desp", ""));	
				//weatherDespText.setTextColor(Color.parseColor("#234578"));
				publishText.setText("今天" + prefs.getString("publish_time", "") + "发布");
				currentDateText.setText(prefs.getString("current_date", ""));
				weatherInfoLayout.setVisibility(View.VISIBLE);
				cityNameText.setVisibility(View.VISIBLE);
				Intent intent = new Intent(this,AutoUpdateService.class);
				startService(intent);
	}
	/*
	 * 查询天气代号所对应的天气
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
	
	class RainTimer implements Runnable{

		@Override
		public void run() {
			ShowWeatherActivity.weather_timer = new Timer();
			TimerTask t = new TimerTask(){
				@Override
				public void run() {
					int number = (int) (Math.random()*2);				
					Message message = new Message();
					message.obj = number;
					rainHanlder.sendMessage(message);									
				}
				
			};
			ShowWeatherActivity.weather_timer.schedule(t, 0, 300); 
		}
		
		
	}
	
	class RainHanlder extends Handler {
		
		public RainHanlder() {
			
		}

		
	}
}
