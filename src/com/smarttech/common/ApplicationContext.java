package com.smarttech.common;


import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

public class ApplicationContext extends Application {
	private static ApplicationContext instance = null;
	public ApplicationContext()
	{
		instance = this;
	}
	
	public static ApplicationContext getInstance() {

		if (null == instance) {
			instance = new ApplicationContext();
		}

		return instance;
	}
	
	/**
	 * App 종료
	 */
	@SuppressLint("NewApi")
	public void requestKillProcess() {
		// finish();
		int sdkVersion = Build.VERSION.SDK_INT;
		final ActivityManager am = (ActivityManager) getInstance().getSystemService(Context.ACTIVITY_SERVICE);
		
//		if( this.activity  != null ){
//			this.activity.finish();
//			this.activity = null;
//		}
		
		// android version 2.2 (SDK code : 8)
		if (sdkVersion < 8) {
			am.restartPackage(getPackageName());

		} else {
		
			String packageName = getPackageName();
			am.killBackgroundProcesses(packageName);
			android.os.Process.killProcess(android.os.Process.myPid());
			
		}
	}
	
	/**
	 * 네트워크 설정화면으로 이동함
	 */
	public void doNetWorkSetting(){
		final Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
		startActivity(intent);
	}
}
