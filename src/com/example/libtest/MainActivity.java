package com.example.libtest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.smarttech.conf.Config;
import com.smarttech.util.GeoUtil;

/**
 * 통신 샘플
 * @author jungkyungjoo
 *
 */
public class MainActivity extends BaseActivity {
	private Button request = null;

	
	private void init(){
//		String ad = "서울시 용산구 남영동 48-4";
		String ad = "서울특별시 용산구 한강대로80길";
		//37.543073, 경도 : 126.9737121
		GeoUtil.newInstance().findGeoPoint(ad);
		Log.d(Config.TAG," ======================== ");
		Log.d(Config.TAG,"["+GeoUtil.newInstance().findAddress(37.543073,126.9737121)+"]");
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.json:
			jsonRequest();
			break;
		case R.id.fileUpload:
			FileUploadRequest();
			break;
		case R.id.filedown:
			imageRequest();
			break;
		case R.id.map:
			doGeoMap();
			break;
		}
	}

	/**
	 * Json Data값 요청
	 */
	private void jsonRequest() {
		Intent intent = new Intent(MainActivity.this, JsonActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);

	}

	/**
	 * 이미지 다운로드
	 */
	private void imageRequest() {
		Intent intent = new Intent(MainActivity.this,ImageDownloadActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}
	
	/**
	 * 파일 업로드 요청
	 */
	private void FileUploadRequest() {
		Intent intent = new Intent(MainActivity.this,FileUploadActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}
	/**
	 * map view
	 */
	private void doGeoMap() {
		Intent intent = new Intent(MainActivity.this,MActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}
    
  
}
