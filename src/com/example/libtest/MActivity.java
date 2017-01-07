package com.example.libtest;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/*
 * 구글맵 샘플
 */
public class MActivity extends FragmentActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
//		marker();
	}

	final String coordinates[] = { "37.543073", "126.9737121" };
	double lat = Double.parseDouble(coordinates[0]);
	double lng = Double.parseDouble(coordinates[1]);
	final LatLng position = new LatLng(lat, lng);
	CameraPosition cp = new CameraPosition.Builder().target((position)).zoom(16).build();
	MarkerOptions marker = new MarkerOptions().position(position); // 구글맵에 기본마커 표시
	private void marker() {
		
//		final String coordinates[] = { "37.543073", "126.9737121" };
//		double lat = Double.parseDouble(coordinates[0]);
//		double lng = Double.parseDouble(coordinates[1]);

//		final LatLng position = new LatLng(lat, lng);
		GooglePlayServicesUtil.isGooglePlayServicesAvailable(MActivity.this);

		final GoogleMap mGoogleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map1)).getMap();
		//위성 사진 (http://siamdaarc.tistory.com/67)
//		mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		// 맵 위치이동.
		mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
		// 첫번째 마커 설정.
		MarkerOptions optFirst = new MarkerOptions();
		optFirst.position(position);// 위도 • 경도
		optFirst.title("지도 보기 테스트");// 제목 미리보기
		optFirst.snippet("Snippet");
		optFirst.draggable(true);
		optFirst.flat(true);
		optFirst.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
		mGoogleMap.addMarker(optFirst).showInfoWindow();

		// 두번째 마커 설정.
		MarkerOptions optSecond = new MarkerOptions();
		optSecond.position(new LatLng(37.543053, 127.041278));// 위도 • 경도
		optSecond.title("지도 보기 테스트2"); // 제목 미리보기
		optSecond.snippet("Snippet2");
		optSecond.draggable(true);
		optSecond.flat(true);
		optSecond.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
		mGoogleMap.addMarker(optSecond).showInfoWindow();

		// 마커 클릭 리스너
		mGoogleMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			public boolean onMarkerClick(Marker marker) {
//				marker.showInfoWindow();
				Toast.makeText(MActivity.this, "onMarker", Toast.LENGTH_LONG).show();
				return false;
			}
		});
		
		// 터치이벤트 설정
		mGoogleMap.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng latLng) {
//						mGoogleMap.clear();
				mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
			}
	    });
	}
	
	
	
//	 private Runnable mMapCheckRunnable = new Runnable() {
//		  public void run() {
//		   if(mRetryCount == MAX_RETRY_COUNT) {
//		    // 맵 로드 실패
//		    return;
//		   }
//
//		   mRetryCount++;
//		   mGoogleMap = mSupportMapFragment.getMap();
//		   if(mGoogleMap == null) {
//		    // google play service에 의해 map이 초기화가 되지 않은 상태
//		    mMapCheckHandler.postDelayed(mMapCheckRunnable, 500L);
//		   } else {
//		    // 맵 사용...
//		   }
//		  }
//	 };
}
