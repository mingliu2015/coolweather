package util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import model.City;
import model.County;
import model.Province;
import db.CoolWeatherDB;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class ParseUtil {

	/*
	 * 解析各省市县返回的字符串
	 */
	public synchronized static boolean handleProvinceResponse(CoolWeatherDB db, String response) {
		if (!TextUtils.isEmpty(response)) {
			String [] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String [] arr = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(arr[0]);
					province.setProvinceName(arr[1]);
					db.saveProvince(province);
				}
			}
			return true;
			
		}
		return false;
	}
	public synchronized static boolean handleCityResponse(CoolWeatherDB db, String response,int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			String [] allCites = response.split(",");
			if (allCites != null && allCites.length > 0) {
				for (String c : allCites) {
					String [] arr = c.split("\\|");
					City city = new City();
					city.setCityCode(arr[0]);
					city.setCityName(arr[1]);
					city.setProvinceId(provinceId);
					db.saveCity(city);
				}
			}
			return true;
		}
		return false;
	}
	
	public synchronized static boolean handleCountyResponse(CoolWeatherDB db, String response ,int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String [] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0) {
				for (String c : allCounties) {
					String [] arr = c.split("\\|");
					County county = new County();
					county.setCountyCode(arr[0]);
					county.setCountyName(arr[1]);
					county.setCityId(cityId);
					db.saveCounty(county);
				}
				return true;
			}
			
		}
		return false;
	}
	/*
	 * 处理气象预报数据返回的json格式数据
	 */
	public static void handleWeatherResponse(String response, Context context) {
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
			String cityName = weatherInfo.getString("city");
			String weatherCode = weatherInfo.getString("cityid");
			String temp1 = weatherInfo.getString("temp1");
			String temp2 = weatherInfo.getString("temp2");
			String weatherDesp = weatherInfo.getString("weather");
			String publishTime = weatherInfo.getString("ptime");
			saveWeatherInfo(context, cityName, weatherCode, temp1, temp2,
					weatherDesp, publishTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveWeatherInfo(Context context, String cityName,
			String weatherCode, String temp1, String temp2, String weatherDesp,
			String publishTime) {
		SharedPreferences.Editor editor = (Editor) PreferenceManager
				.getDefaultSharedPreferences(context).edit();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d",
				Locale.CHINA);
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));
		editor.commit();
	}
}
