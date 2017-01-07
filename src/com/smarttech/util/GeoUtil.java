package com.smarttech.util;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.smarttech.common.ApplicationContext;
import com.smarttech.conf.Config;

public class GeoUtil {
	
	//1)google map API v2 에서 지도에 마커설정하기
	//http://mainia.tistory.com/732
	
	//2)주소로 위도,경도 얻기 참조할것
    //http://ondestroy.tistory.com/45
	
    //3)참조
	//http://areumwing.blogspot.kr/2012/05/blog-post.html
	private static GeoUtil geo;

	public static GeoUtil newInstance() {
		synchronized (GeoUtil.class) {
			if (geo == null) {
				geo = new GeoUtil();
			}
			return geo;
		}
	}

	/**
	 * 위도,경도로 주소취득
	 * 
	 * @param lat
	 * @param lng
	 * @return 주소
	 */
	public String findAddress(double lat, double lng) {
		
		final StringBuffer bf = new StringBuffer();
		final Geocoder geocoder = new Geocoder(ApplicationContext.getInstance(), Locale.KOREA);
		List<Address> address;
		try {
			if (geocoder != null) {
				// 세번째 인수는 최대결과값인데 하나만 리턴받도록 설정했다
				address = geocoder.getFromLocation(lat, lng, 1);
				// 설정한 데이터로 주소가 리턴된 데이터가 있으면
				if (address != null && address.size() > 0) {
					// 주소
					final String currentLocationAddress = address.get(0).getAddressLine(0).toString();

					// 전송할 주소 데이터 (위도/경도 포함 편집)
					bf.append(currentLocationAddress).append("#");
					bf.append(lat).append("#");
					bf.append(lng);
				}
			}

		} catch (IOException e) {
			Toast.makeText(ApplicationContext.getInstance(), "주소취득 실패",Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		return bf.toString();
	}

	/**
	 * 주소로부터 위치정보 취득
	 * 
	 * @param address 주소
	 */
	public GeoPoint findGeoPoint(String address) {
		
		Geocoder geocoder = new Geocoder(ApplicationContext.getInstance());
		Address addr;
		GeoPoint location = null;
		try {
			List<Address> listAddress = geocoder.getFromLocationName(address, 1);
			if (listAddress.size() > 0) { // 주소값이 존재 하면
				addr = listAddress.get(0); // Address형태로
				final int lat = (int) (addr.getLatitude() * 1E6);
				final int lng = (int) (addr.getLongitude() * 1E6);
				location = new GeoPoint(lat, lng);
				Log.d(Config.TAG, "1 주소로부터 취득한 위도 : " + addr.getLatitude() + ", 경도 : " + addr.getLongitude());
				Log.d(Config.TAG, "2 주소로부터 취득한 위도 : " + lat + ", 경도 : " + lng);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return location;
	}

}
