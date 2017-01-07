package com.example.libtest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import com.example.libtest.dialog.SmartTechProgressDialogHelper;
import com.smarttech.conf.Config;
import com.smarttech.request.FileUploadRequest;
import com.smarttech.request.UploadRequest;
import com.smarttech.request.UploadRequest2;
import com.smarttech.request.server.Connection;
import com.smarttech.request.server.ServerRequest;
import com.smarttech.support.UIThread;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

public class FileUploadActivity extends BaseActivity {

	
	private SmartTechProgressDialogHelper ppdhelper = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_upload);

	}
	
	boolean threadStop = true;
	final Runnable progress = new Runnable() {
		
		int runCnt = 0;
		
		@Override
		public void run() {
			while(threadStop){
				
				//UI처리용 Thread
				UIThread.newInstance().executeInUIThread(new Runnable() {
					@Override
					public void run() {
						ppdhelper.setProgressValues(runCnt,threadStop);
					}
				});
				
				try{
					Thread.sleep(10);
					if( runCnt == 100 ){
						runCnt = 0;
					}else {
						runCnt++;
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	};
	
	 /**
    * 파일업로드 UploadRequest에서 callback 메소요청으로 
    * 파일업로드 진행처리를 실시간으로 프로그레스바로 표시해준 
    */
	final private class UpLoadCallBack implements FileUploadRequest.FileCallBack {
//	final private class UpLoadCallBack implements UploadRequest.FileCallBack {
//	final private class UpLoadCallBack implements UploadRequest2.FileCallBack {

		// 파일업로드 total갯수용
		int totalFile = -1;

		public UpLoadCallBack() {
//			super(this);
		}

		/**
		 * @param totalFile 업로드할 파일카운터
		 */
		public UpLoadCallBack(int totalFile) {
			this.totalFile = totalFile;
		}

		/**
		 * 파일 업로드 진행바 callback method
		 */
		@Override
		public void requestCompleted(int size) {
			Log.d(Config.TAG,"size[" + size + "]");
			if (totalFile == -1) {
				hideProgress();
				return;
			}
			ppdhelper.setProgressValues(size);
		}

		/**
		 * 파일 업로드 진행바 callback method 
		 *  ex) 1. [totalFile  > 0] 메세지 출력형식 "[1/8] 100%" 
		 *      2. [totalFile <= 0] 메세지 출력형식 "100%"
		 */
		@Override
		public void requestCompleted(int size, int currentFileCount) {
//			Log.d(Config.TAG,"[" + currentFileCount + "/" + totalFile + "] size["+size+"]");
			if (totalFile != -1) {
				ppdhelper.setProgressValues(size, "[" + currentFileCount + "/" + totalFile + "]");
			} else {
				ppdhelper.setProgressValues(size);
			}
		}

		/**
		 * 파일업로드후 서버에서 성공메세지를 받으면 호출되어짐
		 */
		@Override
		public void requestCompleted(ServerRequest req) {
			Log.d(Config.TAG, "Completed =========");
			
			HashMap<String,Object> result = req.getValue();
			if(result == null ){
				return;
			}
			String RT = (String)result.get("RT");
			String RT_MSG = (String)result.get("RT_MSG");
			Log.d(Config.TAG,"RT =["+RT+"]");
			Log.d(Config.TAG,"RT_MSG =["+RT_MSG+"]");
			
			threadStop = false;
			if (totalFile == -1) {
				hideProgress();
				return;
			}

			ppdhelper.setProgressValues(100);
			if (ppdhelper != null && ppdhelper.isShowing()) {
				ppdhelper.dismiss();
			}

			// 다음 Activity화면으로 넘김
		}

		/**
		 * 에러처리
		 */
		public void requestFailed(ServerRequest req, Throwable ex) {
			Log.d(Config.TAG, "에러 처리중입니다.");
			threadStop = false;
			hideProgress();
			final HashMap resultMap = req.getValue();
			if (resultMap == null) {
				return;
			}

			// 재전송버턴 노출처리함
			ppdhelper.setFailLayout();

		}

		public void requestFailed(ServerRequest req, Throwable ex, byte[] data) {
			requestFailed(req, ex);
		}
	}
	
	/**
	 * 업로드할 파일을 압축/암호화 처리후 JSONObject에 값을 담아서 ArrayList에 JSONObject를 추가한다
	 * @return
	 */
	private ArrayList<JSONObject> getFileUpload()
	{
		
		/* sdcard  path */
		final  String sdcardURL = Environment.getExternalStorageDirectory().getPath() + File.separator;
//		Log.d(Config.TAG,sdcardURL+"test1.flv");
		final ArrayList<JSONObject> arrayJson = new ArrayList<JSONObject>();
		try{
//			final JSONObject json = new JSONObject();
//			json.put("file", sdcardURL+"log.txt");
//			json.put("file", sdcardURL+"test1.flv");
			arrayJson.add(new JSONObject().put("file", sdcardURL+"config.json"));
			arrayJson.add(new JSONObject().put("file", sdcardURL+"dm.cfg"));
//			arrayJson.add(new JSONObject().put("file", sdcardURL+"Pictures/Screenshots/Screenshot_2013-10-16-10-35-27.png"));
//			arrayJson.add(new JSONObject().put("file", sdcardURL+"DRM_logfile.txt"));
//			arrayJson.add(new JSONObject().put("file", sdcardURL+"Download/상속자들.E15.131127.HDTV.X264.720p-BarosG.avi"));
//			arrayJson.add(new JSONObject().put("file", sdcardURL+"Download/TWorld_asis_40.zip"));
//			arrayJson.add(new JSONObject().put("file", sdcardURL+"DCIM/Camera/1377267021949.jpg"));
//			arrayJson.add(new JSONObject().put("file", sdcardURL+"msDownload/saveUriFile/pushmessage.txt"));
//			arrayJson.add(new JSONObject().put("file", sdcardURL+"dm.cfg"));
		}catch(Exception e){
			e.printStackTrace();
		}
		return arrayJson;
	}
	
	
	/**
	 * 파일업로드시 JSON Data정보를 동시에 첨부한다.
	 * @return
	 */
	private JSONObject requestRegisterData(){
		
		
		final JSONObject jdata = new JSONObject();
		//사업자번호
		try{
			
//			jdata.put(Bizfast.BR_NUMBER, brNumber);
			jdata.put("SYM_KEY1", "TEST1");
			jdata.put("USER_ID1", "ptojung1");
//			jdata.put("USER_ID2", "ptojung2");
			jdata.put("USER_ID3", "ptojung3");
			jdata.put("SYM_KEY2", "TEST2");
			jdata.put("SYM_KEY3", "TEST3");
			
			/*
			final ArrayList<JSONObject> imageInfoList = new ArrayList<JSONObject>();
			for (HashMap<String, Object> i : documentaryInfoMap) {
	
				final JSONObject imageInfo = new JSONObject();
				imageInfo.put(Bizfast.TARGET_CARD,(String) i.get(Bizfast.TARGET_CARD));
				imageInfo.put(Bizfast.FORM_ID, (String) i.get(Bizfast.FORM_ID));
				imageInfo.put(Bizfast.FORM_SEQNO,(String) i.get(Bizfast.FORM_ORDER));
				imageInfo.put(Bizfast.FILE_NAME, (String) i.get(Bizfast.FILE_NAME));
				imageInfo.put(Bizfast.FORM_NAME, (String) i.get(Bizfast.FORM_NAME));
				imageInfoList.add(imageInfo);
			
			}// for
			jdata.put(Bizfast.IMAGE_INFO, imageInfoList);
			 */
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return jdata;
	}
	
	/**
	 * 파일업로드도중에 취소처리함
	 * 업로드중에 서버에 취소요청처리함
	 */
	final class CancelUpload implements SmartTechProgressDialogHelper.CallBack{
		@Override
		public void onPressButton(int btnIndex){
			
			if(SmartTechProgressDialogHelper.ALERT_OK == btnIndex){
				//재전송요청 
				ppdhelper.setSendLayout();
				final ArrayList<JSONObject> arrayJson = getFileUpload();
				UpLoadCallBack callback = null;
				if(arrayJson.isEmpty()){
					callback = new UpLoadCallBack();
				}else {
					final int fcnt = arrayJson.size();
					callback = new UpLoadCallBack(fcnt);
				}
				
//				frequest = new UploadRequest(callback,arrayJson,requestRegisterData(),FileUploadActivity.this,"/SaveNewTempImage");
//				frequest2 = new UploadRequest2(callback,arrayJson,requestRegisterData(),FileUploadActivity.this,"/SaveNewTempImage");
				filerequest = new FileUploadRequest(callback,arrayJson,requestRegisterData(),FileUploadActivity.this,"/smarttech/MultipartFile.do");
				Connection.getInstance().enqueueRequest(filerequest);
			}
			
		}
	}
	
	private UploadRequest2 frequest2 = null;
	private UploadRequest frequest = null;
	private FileUploadRequest filerequest = null;
	public void requestUpload(){
		
		
		final ArrayList<JSONObject> arrayJson = getFileUpload();
		if(arrayJson != null && !arrayJson.isEmpty()){
			ppdhelper = new SmartTechProgressDialogHelper( this , "파일을 전송중입니다.", SmartTechProgressDialogHelper.ALERT_OK,new CancelUpload());
			ppdhelper.show();
		}else {
			showProgress("작업중입니다..");
		}
		
		UpLoadCallBack callback = null;
		if(arrayJson.isEmpty()){
			callback = new UpLoadCallBack();
		}else {
			final int fcnt = arrayJson.size();
			callback = new UpLoadCallBack(fcnt);
		}
		
		filerequest = new FileUploadRequest(callback  //파일업로드시 화면단에서 처리하기 위한 CallBack
		                           , arrayJson              //파일경로+파일명
		                           , requestRegisterData()  //파일과같이 전송할 JSON Data값
		                           , this                   //android Context 
//		                           ,"/spring/uploadMultipleFile");   //전송할 URL주소
		,"/test/TESTUP");   //전송할 URL주소
//		,"/test/fileUpload.do");   //전송할 URL주소
		
		
		Connection.getInstance().enqueueRequest(filerequest);
		
		/*
		final ArrayList<JSONObject> arrayJson = getFileUpload();
		if(arrayJson != null && !arrayJson.isEmpty()){
			ppdhelper = new PentaProgressDialogHelper( this , "파일을 전송중입니다.", PentaProgressDialogHelper.ALERT_OK,new CancelUpload());
			ppdhelper.show();
		}else {
			showProgress("작업중입니다..");
		}
		
		UpLoadCallBack callback = null;
		if(arrayJson.isEmpty()){
			callback = new UpLoadCallBack();
		}else {
			final int fcnt = arrayJson.size();
			callback = new UpLoadCallBack(fcnt);
		}
		
		
		frequest = new UploadRequest(callback  //파일업로드시 화면단에서 처리하기 위한 CallBack
		                           , arrayJson              //파일경로+파일명
		                           , requestRegisterData()  //파일과같이 전송할 JSON Data값
		                           , this                   //android Context 
		                           ,"/smarttech/MultipartFile.do");   //전송할 URL주소
		ServerConnection.getInstance().enqueueRequest(frequest);
		
		frequest2 = new UploadRequest2(callback  //파일업로드시 화면단에서 처리하기 위한 CallBack
		                           , arrayJson              //파일경로+파일명
		                           , requestRegisterData()  //파일과같이 전송할 JSON Data값
		                           , this                   //android Context 
		                           ,"/smarttech/MultipartFile.do");   //전송할 URL주소
		ServerConnection.getInstance().enqueueRequest(frequest2);
		*/
	}
	
	
	
	
	
	public void onClick(View v){
		switch(v.getId()){
		case R.id.btnfileUpload:
			requestUpload();
			break;
		}
	}
	
	
	
}
