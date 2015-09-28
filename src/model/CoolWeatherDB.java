package model;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import db.CoolWeatherOpenHelper;

public class CoolWeatherDB {
	// 数据库名
	public static final String DB_NAME = "cool_weather";
	// 数据库版本
	public static final int VERSION = 1;

	private static CoolWeatherDB coolWeatherDB;

	private SQLiteDatabase db;

	/*
	 * 构造方法私有化
	 */

	private CoolWeatherDB(Context context) {
		CoolWeatherOpenHelper dbHepler = new CoolWeatherOpenHelper(context, DB_NAME, null, VERSION);
		db = dbHepler.getWritableDatabase();
	}

	/*
	 * 获取CoolWeatherDB实例
	 */
	public synchronized static CoolWeatherDB getInstance(Context context) {
		if (coolWeatherDB == null) {
			coolWeatherDB = new CoolWeatherDB(context);

		}
		return coolWeatherDB;
	}

	/*
	 * 将Province实例储存到数据库
	 */
	public void saveProvince(Province province) {
		if (province != null) {
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			db.insert("Province", null, values);
			Log.i("saveProvince", values.toString());
		}
	}

	/*
	 * 从数据库读取全国省份数据
	 */
	public List<Province> loadProvinces() {
		List<Province> list = new ArrayList<Province>();
		Cursor cursor = db
				.query("Province", null, null, null, null, null, null);

		if (cursor.moveToFirst()) {
			do {
				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
				Log.i("loadProvinces", province.getProvinceName()+province.getProvinceCode());

				list.add(province);
			} while (cursor.moveToNext());
		}

		return list;

	}

	/*
	 * 将CITY实例储存到数据库
	 */

	public void saveCity(City city) {
		if (city != null) {
			ContentValues values = new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			values.put("province_id", city.getProvinceId());
			db.insert("City", null, values);
			Log.i("saveCity", values.toString());

		}
	}

	/*
	 * 从数据库读取某省所有城市数据
	 */
	public List<City> loadCities(int provinceId) {
		Log.i("loadCities",String.valueOf(provinceId) );
		List<City> list = new ArrayList<City>();
		Cursor cursor = db.query("City", null, "province_id= ?", new String[] { String.valueOf(provinceId) }, null,
				null, null);

		if (cursor.moveToFirst()) {
			do {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
				list.add(city);
				Log.i("loadCities", city.getCityName()+city.getCityCode());

			} while (cursor.moveToNext());
		}
		return list;

	}
	
	/*
	 * 将county实例储存到数据库
	 */

	public void saveCounty(County county) {
		if (county != null) {
			ContentValues values = new ContentValues();
			values.put("county_name", county.getCountyName());
			values.put("county_code", county.getCountyCode());
			values.put("city_id", county.getCityId());
			db.insert("County", null, values);
			Log.i("saveoCounty", values.toString());

		}
	}

	/*
	 * 从数据库读取某市所有县数据
	 */
	public List<County> loadCounties(int cityId) {
		List<County> list = new ArrayList<County>();
		Cursor cursor = db.query("County", null, "city_id= ?", new String[] { String.valueOf(cityId) }, null,
				null, null);

		if (cursor.moveToFirst()) {
			do {
				County county = new County();
				county.setId(cursor.getInt(cursor.getColumnIndex("id")));
				county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
				county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
				Log.i("loadCounties", county.getCountyName()+county.getCountyCode());

				list.add(county);
			} while (cursor.moveToNext());
		}
		return list;

	}
}
