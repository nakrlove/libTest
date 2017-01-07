package com.example.libtest;

import java.util.ArrayList;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.example.libtest.dialog.SmartTechProgressDialogHelper;
import com.smarttech.conf.Config;
import com.smarttech.request.ImageDownLoadRequest;
import com.smarttech.request.server.Connection;
import com.smarttech.request.server.ServerRequest;

/*
 * 이미지 로드 샘플
 * 파일 다운로드 
 */
public class ImageDownloadActivity extends BaseActivity {
	
	
	ImageView iv_image = null;
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_download);
		iv_image = (ImageView)findViewById(R.id.iv_image);
		//파일 다운로드
		requestFileDownload();
		//이미지로더 
//		requestImageLoad();
	}
	
	
	
	/**
	 * background 이미지 처리
	 */
	final protected class BackGroundImageCallBack implements ImageDownLoadRequest.FileCallBack {
		
		ImageView iv = null;
		
		public BackGroundImageCallBack(ImageView iv){
//			super(ImageDownloadActivity.this);
			this.iv = iv;
		}
		
		
		@Override
		public void requestCompleted(int size) {
		}
		
		/**
		 * 에러처리
		 */
		public void requestFailed(ServerRequest req, Throwable ex) {
			hideProgress();
		}

		public void requestFailed(ServerRequest req, Throwable ex, byte[] data) {
			requestFailed(req, ex);
		}
		
		
		@SuppressLint("NewApi") 
		@Override
		public void requestCompleted(ServerRequest req) {
			hideProgress();
			final ArrayList<Bitmap> mapList = req.getBitmaps();
			if(mapList != null && !mapList.isEmpty()){
				Bitmap bmap = mapList.get(0);
				if( bmap != null ){
					iv.setImageBitmap(bmap);
					iv.invalidate();
				}
				mapList.clear();
			}
			
			Connection.getInstance().dequeueRequest(req);
		}
	}
	
	
	
	private SmartTechProgressDialogHelper pop = null;
	/**
    * 파일업로드 UploadRequest에서 callback 메소요청으로 
    * 파일업로드 진행처리를 실시간으로 프로그레스바로 표시해준 
    */
	final private class DownloadCallBack implements ImageDownLoadRequest.FileCallBack {

		// 파일업로드 total갯수용

		public DownloadCallBack() {
			pop.setProgressValues(0);
		}

		/**
		 * 파일 업로드 진행바 callback method
		 */
		@Override
		public void requestCompleted(int size) {
			Log.d(Config.TAG,"size[" + size + "]");
			pop.setProgressValues(size);
		}

		/**
		 * 파일업로드후 서버에서 성공메세지를 받으면 호출되어짐
		 */
		@Override
		public void requestCompleted(ServerRequest req) {
			Log.d(Config.TAG, "Completed =========");
			pop.setProgressValues(100);
			if (pop != null && pop.isShowing()) {
				pop.dismiss();
			}

			// 다음 Activity화면으로 넘김
		}

		/**
		 * 에러처리
		 */
		public void requestFailed(ServerRequest req, Throwable ex) {
			Log.d(Config.TAG, "에러 처리중입니다.");
			hideProgress();
		}

		public void requestFailed(ServerRequest req, Throwable ex, byte[] data) {
			requestFailed(req, ex);
		}
	}
	
	
	ImageDownLoadRequest irequest = null;
	/**
	 * 파일다운로드  취소처리함
	 */
	final class CancelUpload implements SmartTechProgressDialogHelper.CallBack{
		@Override
		public void onPressButton(int btnIndex){
			if(SmartTechProgressDialogHelper.ALERT_CANCEL == btnIndex){
				Connection.getInstance().dequeueRequest(irequest);
			}
			
		}
	}
	
	/*
	 * 파일다운로드 요청
	 */
	public void requestFileDownload(){
//		showProgress("로딩중...");
		pop = new SmartTechProgressDialogHelper( this , "파일을 다운로드 중입니다.", SmartTechProgressDialogHelper.ALERT_OK,new CancelUpload());
		pop.show();
		String url = "/smarttech/images/test1.flv";
		irequest = new ImageDownLoadRequest(new DownloadCallBack(),url,true);
		Connection.getInstance().enqueueRequest(irequest);
	
	}
	
	/*
	 * 이미지 다운로드 요청
	 */
	public void requestImageLoad(){
		showProgress("로딩중...");
		String url = "/smarttech/images/03.png";
		irequest = new ImageDownLoadRequest(new BackGroundImageCallBack(iv_image),url,false);
		Connection.getInstance().enqueueRequest(irequest);
	
	}
}
