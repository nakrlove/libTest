package com.smarttech.conf;

import java.io.File;

import android.os.Environment;



public class Config {
	
	// SSL(https)통신 Protocal
	public static final String PROTOCOL = "TLSv1";
	public static final int HTTPS_PORT = 8443;
	// file다운로드 버퍼 사이즈
	public static final int DOWN_LOAD_BUFFER = 2048;
	public static final boolean debug = true; // 디버거로 사용여부
	public static final String encode = "UTF-8";

	/**
	 * 서버 정리함 (개발서버,운영서버)
	 */
	public static final int REAL_MODE = 1; // 운영모드
	public static final int DEV_MODE = 2; // 개발모드
	public static final int DEMO_MODE = 3; // 데모모드

	// /////////////////////////////////////////////
	public static int CURRENT_MODE = DEV_MODE;
	public static final String SERVER_MSG_503 = "서비스를 사용할수 없습니다.";

	/* sdcard path */
	final public static String sdcardURL = Environment
			.getExternalStorageDirectory().getPath() + File.separator;

	// 서버url
	// final public static String DEV_SERVER_URL =
	// "http://106.240.252.138:8443"; //개발서버
	// final public static String DEV_SERVER_URL = "http://192.168.0.18:8080";
	// //개발서버

//	final public static String DEMO_SERVER_URL = "http://www.bizfast.co.kr:8443"; // 데모서버
//	final public static String DEV_SERVER_URL = "http://www.bizfast.co.kr:8543"; // 개발서버
	final public static String DEV_SERVER_URL = "http://192.168.0.16:8080"; // 개발서버
	// final public static String DEV_SERVER_URL = "http://192.168.0.18:8080";
	// //개발서버
//	final public static String REAL_SERVER_URL = "https://www.bizfast.co.kr:8543"; // 운영서버

	/**
	 * URL서버 주소
	 * 
	 * @return
	 */
	public static String getServer() {

		String server = null;
	
			server = DEV_SERVER_URL;

		return server;
	}

	public static String getPackageURL() {
		return sdcardURL;
	}

	// 삭제예정임 통신모듈 Zip파일 사용여부 테스트를 위해 임시로 사용함
	@Deprecated
	public static String getInstallURL(String fileName) {
		return getPackageURL() + fileName;
	}

	public static final String TAG = "SMTLOG";
//	//SSL(https)통신을 위한 상수를 미리 만들어줌
//	public static final String PROTOCOL = "protocol";
//	//file다운로드 버퍼 사이즈 
//	public static final int    DOWN_LOAD_BUFFER = 2048;
//	public static final boolean debug = true;
//	public static final String encode = "UTF-8";
//	
//	public static final String SERVER_MSG_503 = "서비스를 사용할수 없습니다.";
//	final public static String assetsPATH = "file:///android_asset" + File.separator;
//	/* database path 
//	 * (/data/data/packagename/databases/)
//	 */
//	final public static String targetPackage = File.separator
//										     + "data"
//										     + File.separator
//										     + "data"
//										     + File.separator
//										     + "com.smlib"
//										     + File.separator
//										     + "databases" 
//										     + File.separator;
//	
//	/* sdcard  path */
//	final public static String sdcardURL = Environment.getExternalStorageDirectory().getPath()
//	                                     + File.separator;
//	
//	//서버주
//	final public static String serverURL = "http://192.168.0.16:8080/";
//	
//	public static String getServer(){
//		return serverURL;
//	}
//	
//	
//	/**
//	 * 파일이 저장될 폴더가 존재하는지 확인용
//	 * @return
//	 */
//	public static String getPackageURL(){
////		return targetPackage;
//		return sdcardURL;
//	}
//	
//	
//	/**
//	 * 다운로드받은 파일을 저장할 위치와 파일명
//	 * @return
//	 */
//	public static String getInstallURL(){
//		return getPackageURL()+"test1.flv";
//	}
}
