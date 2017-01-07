package com.smarttech.request;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONObject;

import com.smarttech.request.server.JsonServerRequest;
/**
 * 
 * Copyright (c) 2014, SmartTech (jungkyungjoo)
 */
public class JsonRequest extends JsonServerRequest {

	private String url = null;
	
	public JsonRequest(String url){
		super(null,null);
		this.url = url;
	}

	/**
	 * request 
	 * @param callback Class
	 * @param json     parameter
	 * @param url      서버주소(호출하는 url 주소)
	 */
	public JsonRequest(CallBack callback,JSONObject json,String url){
		super(callback,json);
		this.url = url;
	}

	
	@Override
	public HttpUriRequest generateRequest() {
		return getRequest( new HttpPost(getBaseURL()+this.url));
	}
}
