package util;

import model.City;
import model.County;
import model.Province;
import db.CoolWeatherDB;
import android.text.TextUtils;

public class ParseUtil {

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
}
