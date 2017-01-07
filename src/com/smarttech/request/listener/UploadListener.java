package com.smarttech.request.listener;

import android.annotation.SuppressLint;
import android.util.Log;
import android.widget.Toast;

import com.example.libtest.BaseActivity;
import com.smarttech.common.SmtAlertDialogHelper;
import com.smarttech.common.ApplicationContext;
import com.smarttech.conf.Config;
import com.smarttech.request.server.Connection;
import com.smarttech.request.server.ServerRequest;
import com.smarttech.request.server.UploadServerRequest;

/**
 * FileUpload Listener 
 * @author SmartTech (jungkyungjoo)
 * Copyright (c) 2014, SmartTech (jungkyungjoo)
 */
public class UploadListener implements UploadServerRequest.FileCallBack {
	BaseActivity base = null;
	private static SmtAlertDialogHelper pdhelper = null;
	@Override
	public void requestCompleted(int message){
		Log.d(Config.TAG," 1 UPLOAD requestCompleted ["+message+"]");
	}
	@Override
	public void requestCompleted(int message ,int currentFileCount){
		Log.d(Config.TAG," 1 UPLOAD requestCompleted ["+message+"]");
	}
	
	
	@SuppressLint("NewApi")
	public void requestCompleted(ServerRequest req,int position) {
		
	}
	
	@SuppressLint("NewApi")
	public void requestCompleted(ServerRequest req) {
		
		Log.d(Config.TAG," 2 UPLOAD requestCompleted ====== 2");
		Connection.getInstance().dequeueRequest(req);
	}
	
	@Override
	public void requestFailed(ServerRequest req, Throwable ex) {
//		base.hideProgress();
		final String errMsg = ex.getMessage();
		Connection.getInstance().dequeueRequest(req);
		
		if( this.base == null ){
			Toast.makeText(ApplicationContext.getInstance(), "[LM]:"+errMsg, Toast.LENGTH_LONG).show();
			return ;
		}
		
		if(pdhelper != null && pdhelper.isShowing()){
			return;
		}
		
		pdhelper =  new SmtAlertDialogHelper(this.base
				 , "에러"
				 , "[LM]:"+errMsg
				 , SmtAlertDialogHelper.ALERT_OK);
		pdhelper.show();

	}
	
	@Override
	public void requestFailed(ServerRequest req, Throwable ex, byte[] data) {
		requestFailed(req, ex);
	}

}
