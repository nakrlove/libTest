package com.smarttech.request;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;

import android.util.Log;

import com.smarttech.conf.Config;
import com.smarttech.request.server.DownLoadServerRequest;
import com.smarttech.request.server.Connection;

/**
 * 파일 다운로드 
 * @author SmartTech (jungkyungjoo)
 * 
 * Copyright (c) 2014, SmartTech (jungkyungjoo)
 */
public class DownLoadRequest extends DownLoadServerRequest {

	 
	 public DownLoadRequest(FileCallBack callback) {
	       super(callback);
	 }
	 
	 /**
	  * 
	  * @param callback
	  * @param fileName 다운로드받을 파일명 및 저장될 파일명 
	  */
	 public DownLoadRequest(FileCallBack callback,String fileName) {
		 super(callback);
		 this.fileName = fileName;
	 }
	
	
	@Override
	protected HttpPost getRequest(HttpPost request) {
		  return request;
	}
	
	
	@Override
	public HttpUriRequest generateRequest() {
		// TODO Auto-generated method stub
		final HttpPost request = new HttpPost(getBaseURL()+fileName);
	
		final ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		if(Config.debug){
			Log.d(Config.TAG," #Request URL ======================================== ");
			Log.d(Config.TAG,request.getURI()+"");
			Log.d(Config.TAG," #Request URL ======================================== ");
		}
		try{
		  final UrlEncodedFormEntity entityRequest = new UrlEncodedFormEntity(nameValuePairs, Config.encode);  
		  request.setEntity(entityRequest); 
		}catch(Exception e){
			e.printStackTrace();
		}
        return request;
	}
	
	
	
	@Override
	protected void processResponseObject(int message)  {
		// TODO Auto-generated method stub
		try {
			/* 다운로드 진행값 출력해줌 */
			fireComplete(message);
		} catch (Exception e) {
			fireFailed(e);
			e.printStackTrace();
		}
	}
	
	@Override
	protected void processResponseObject() {
		// TODO Auto-generated method stub
		try {
			fireComplete();
		} catch (Exception e) {
			fireFailed(e);
			e.printStackTrace();
		}
		Connection.getInstance().dequeueRequest(this);
//		Log.d(Config.TAG,"3 ########################################");
	}
}
