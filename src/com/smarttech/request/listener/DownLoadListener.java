package com.smarttech.request.listener;

import android.annotation.SuppressLint;

import com.smarttech.request.server.Connection;
import com.smarttech.request.server.ServerRequest;
import com.smarttech.request.server.DownLoadServerRequest;



/**
 * 일반 파일 다운로드 callback Listener 
 * @author SmartTech (jungkyungjoo)
 * Copyright (c) 2014, SmartTech (jungkyungjoo)
 */
public class DownLoadListener implements  DownLoadServerRequest.FileCallBack{
	
	@Override
	public void requestCompleted(int message){
		
	}
	
	@SuppressLint("NewApi")
	public void requestCompleted(ServerRequest req) {
		Connection.getInstance().dequeueRequest(req);
	}
	@SuppressLint("NewApi")
	public void requestCompleted(ServerRequest req,int position) {
		Connection.getInstance().dequeueRequest(req);
	}
	
	public void requestFailed(ServerRequest req, Throwable ex) {
//		base.hideProgress();
		final String errMsg = ex.getMessage();
		Connection.getInstance().dequeueRequest(req);

	}
	public void requestFailed(ServerRequest req, Throwable ex, byte[] data) {
		requestFailed(req, ex);
	}

}
