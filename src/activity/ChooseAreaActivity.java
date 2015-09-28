package activity;

import java.util.ArrayList;
import java.util.List;

import com.example.coolweather.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import model.City;
import model.CoolWeatherDB;
import model.County;
import model.Province;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

public class ChooseAreaActivity extends Activity {

	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;

	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList = new ArrayList<String>();

	private List<Province> provinceList;// ʡ�б�
	private List<City> cityList;// ���б�
	private List<County> countyList;// ���б�

	private Province selectedProvince;// ѡ�е�ʡ
	private City selectedCity;// ѡ�е���
	private int currectLevel;// ѡ�еļ���

	@Override

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(prefs.getBoolean("city_selected", false)){
			Intent intent = new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		Log.i("ChooseAreaActivity", "layout.choose_area");

		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
				if (currectLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(index);
					queryCities();
					Log.i("ChooseAreaActivity", "queryCities");


				} else if (currectLevel == LEVEL_CITY) {
					selectedCity = cityList.get(index);
					queryCounties();
					Log.i("ChooseAreaActivity", "queryCounties");


				}
				else if(currectLevel == LEVEL_COUNTY){
					String countyCode = countyList.get(index).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("county_code",countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvinces();
	}

	/*
	 * ��ѯȫ�����е�ʡ���Ȳ����ݿ⣬û���ٲ������
	 */
	private void queryProvinces() {
		provinceList = coolWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());

			}

			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("�й�");
			currectLevel = LEVEL_PROVINCE;

		} else {
			queryFromServer(null, "province");
		}
	}

	protected void queryCities() {
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());

		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());

			}

			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currectLevel = LEVEL_CITY;
			

		} else {
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}

	protected void queryCounties() {
		countyList = coolWeatherDB.loadCounties(selectedCity.getId());

		if (countyList.size() > 0) {
			dataList.clear();
			for (County county : countyList) {
				dataList.add(county.getCountyName());
			}

			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currectLevel = LEVEL_COUNTY;

		} else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}

	}

	private void queryFromServer(final String code, final String type) {
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
			Log.i("queryFromServer1", address);
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
			Log.i("queryFromServer2", address);

		}
		showProgressDialog();// ��ʾ���ȶԻ���
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvincesResponse(coolWeatherDB, response);

				} else if ("city".equals(type)) {
					result = Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());

				} else if ("county".equals(type)) {
					result = Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());

				}
				if (result) {
					// ͨ��runOnUiTHread�����ص����̴߳����߼�
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("City".equals(type)) {
								queryCities();
							} else if ("County".equals(type)) {
								queryCounties();
							}

						}

					});
				}

			}

			@Override
			public void onError(Exception e) {
				
				runOnUiThread(new Runnable(){
					@Override
					public void run(){
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
						
					}
				});
			}
		});
	}

	/*
	 * ��ʾ����
	 */
	private void showProgressDialog() {
		if (progressDialog == null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();

	}
	/*
	 * �رս���
	 */
	private void closeProgressDialog() {
		if(progressDialog!=null){
			progressDialog.dismiss();
		}

	}
	
	/*
	 * ����back�������ݵ�ǰ�ļ������жϣ����ص��б�
	 * 
	 */
	@Override
	public void onBackPressed(){
		if (currectLevel == LEVEL_COUNTY){
			queryCities();
			
		}
		else if (currectLevel == LEVEL_CITY){
			queryProvinces();
			
		}

		else {
			finish();
			
		}
	}
	
}
