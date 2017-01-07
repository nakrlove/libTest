package com.smarttech.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

import com.smarttech.common.ApplicationContext;


public class NetworkConnection {
	private static NetworkConnection gConnection;

	public static NetworkConnection newInstance() {
		synchronized (NetworkConnection.class) {
			if (gConnection == null) {
				gConnection = new NetworkConnection();
			}
			return gConnection;
		}
	}

	/**
	 * 3g,4g 네트워크로 연결체크함
	 * 
	 * @return 네트워크 상태값
	 */
	public boolean is3GAnd4GNetworkConnection() {

		final ConnectivityManager conManager = (ConnectivityManager) ApplicationContext.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo mobile = conManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE); // 3g
		final NetworkInfo wifi   = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI); // wifi
		final NetworkInfo wimax  = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX); // 4g

		boolean network = false;
		if ((mobile != null && mobile.isConnected())
		|| (wifi != null && wifi.isConnected())
		|| (wimax != null && wimax.isConnected())) {
			
			if (wifi != null && wifi.isConnected()) {
				network = true;
			}
		}
		return network;

	}

	/**
	 * network 상태 체크
	 * 
	 * @return 네트워크 상태값
	 */
	public boolean checkNetworkConnection() {

		final ConnectivityManager conManager = (ConnectivityManager) ApplicationContext.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo mobile = conManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE); // 3g
		final NetworkInfo wifi = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI); // wifi
		final NetworkInfo wimax = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX); // 4g

		boolean network = true;

		if ((mobile != null && mobile.isConnected())
		|| (wifi != null && wifi.isConnected())
		|| (wimax != null && wimax.isConnected())) {
			network = false;
		}
	
		return network;

	}

	/**
	 * 비행기 모드 체크함
	 * 
	 * @return
	 */
	public boolean isAirplaneMode() {
		try {
			return Settings.System.getInt(ApplicationContext.getInstance().getContentResolver(),
					Settings.System.AIRPLANE_MODE_ON, 0) != 0;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

	// public void TelephonyAidl(Context context, boolean dataconnectivity)
	// throws Exception {
	// TelephonyManager tm = (TelephonyManager)
	// NearKidsApplication.sharedApplication().getSystemService(Context.TELEPHONY_SERVICE);
	// @SuppressWarnings("rawtypes")
	// Class c = Class.forName(tm.getClass().getName());
	// Method m = c.getDeclaredMethod("getITelephony");
	// m.setAccessible(true);
	// ITelephony telephonyService = (ITelephony) m.invoke(tm);
	//
	// boolean dataConnection = telephonyService.isDataConnectivityPossible();
	// telephonyService.getNetworkType();
	// if (dataconnectivity) {
	// telephonyService.enableDataConnectivity();
	// } else {
	// telephonyService.disableDataConnectivity();
	// }
	// }

	/**
	 * Returns the state of the 3G module.
	 * 
	 * @param context
	 *            Context of the application component
	 * @return State of the 3G module
	 * @throws Exception
	 *             if the device doesn't have the access to the 3G module
	 */
	public boolean is3GEnabled() {

		boolean is3g = false;
		try {
			final ConnectivityManager connectivityManager = (ConnectivityManager) ApplicationContext.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
			final Class<?> connectivityManagerClass = connectivityManager.getClass();
			final Field connectivityManagerField = connectivityManagerClass.getDeclaredField("mService");
			connectivityManagerField.setAccessible(true);

			final Object iConnectivityManager = connectivityManagerField.get(connectivityManager);
			final Class<?> iConnectivityManagerClass = iConnectivityManager.getClass();
			final Method getMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
			getMobileDataEnabledMethod.setAccessible(true);
			is3g = (Boolean) getMobileDataEnabledMethod.invoke(iConnectivityManager);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return is3g;
	}
}
