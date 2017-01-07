package com.smarttech.request.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.util.Log;

import com.smarttech.conf.Config;
import com.smarttech.request.json.parser.JSONParseEngine;


/**
 * JSON 통신모듈
 * @author SmartTech (jungkyungjoo)
 * Copyright (c) 2014, SmartTech (jungkyungjoo)
 */
public class JsonServerRequest extends ServerRequest {
	
	static boolean isRequest = true;
	protected JSONObject json = null;
	protected String urlTemp  = null;
	public JsonServerRequest(CallBack callback){
		super(callback);
	}
	
	public JsonServerRequest(CallBack callback,JSONObject json){
		super(callback);
		this.json = json;
	}

	@Override
	protected HttpPost getRequest(HttpPost request) {
		
//		Log.i(Config.TAG,"======isRequest ["+isRequest+"]");
		try {
			Log.d(Config.TAG,"============= Request URL & getParam ====================");
			urlTemp = request.getURI()+"";
			Log.d(Config.TAG,urlTemp);
			String jsonData = "{}";
			if(json != null){
				jsonData = json.toString();
			}
			Log.d(Config.TAG,jsonData);
			
			final StringEntity stringEntity = new StringEntity(jsonData,Config.encode);
			stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
			stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
			request.setHeader("Accept", "application/json");
			request.setHeader("Content-type", "application/json; charset=utf-8");
			request.setHeader("Auth-Token", " ");
			request.setEntity(stringEntity);
			Log.d(Config.TAG,"============= Request URL & getParam ====================");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return request;
	}

	@Override
	protected void processResponse(InputStream in) throws Exception {
		// TODO Auto-generated method stub

		try {
			Log.d(Config.TAG,"=== processResponse ===");
			internalProcessResponse(in);
			fireComplete();
			Log.d(Config.TAG,"=== processResponse ===");
		} catch (IOException ex) {
			fireFailed(ex);
			throw ex;
		} catch (Exception e) {
			fireFailed(e);
			e.printStackTrace();
		}


	}

	
	@SuppressWarnings("unchecked")
	public void internalProcessResponse(InputStream in)	throws IOException {
		
		final JSONParseEngine engine = new JSONParseEngine(printLog(in,urlTemp));
		this.setValue((HashMap<String,Object>) engine.parse());

	}

	@Override
	public HttpUriRequest generateRequest() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
