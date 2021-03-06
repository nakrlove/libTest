package com.smarttech.request;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.example.libtest.BaseActivity;
import com.smarttech.conf.Config;
import com.smarttech.request.server.UploadServerRequest;

/**
 * file첨부 업로드 모듈
 * @author SmartTech (jungkyungjoo)
 * Copyright (c) 2014, SmartTech (jungkyungjoo)
 */
public class UploadRequest extends UploadServerRequest {

	private static final String boundary = "0xKhTmLbOuNdArY";

	BaseActivity base = null;
	String url = null;
	ArrayList<JSONObject> filesJsonObj = null;
	boolean fResult = false;
	JSONObject bodyJsonObj;

	public UploadRequest( FileCallBack callback, BaseActivity base ) {
		super(callback,null);
		this.base = base;
	}
	
	/**
	 * 
	 * @param callback
	 * @param filesJsonObj upload할 파일리스트
	 * @param bodyJsonObj  jsondata값을 file과 같이 전송함
	 * @param base         전송실패시 UI단에 메세지 처리를 위한 Activity
	 * @param url          전송할주소
	 */
	public UploadRequest( FileCallBack callback,  ArrayList<JSONObject> filesJsonObj, JSONObject bodyJsonObj,  BaseActivity base,String url ) {
		super(callback,bodyJsonObj);
		this.base = base;
		this.filesJsonObj = filesJsonObj;
		this.bodyJsonObj = bodyJsonObj;
		this.url = url;
	}

	@Override
	protected HttpPost getRequest( HttpPost request ) {
		return request;
	}

	
	@Override
	public HttpUriRequest generateRequest() {

		if(Config.debug){
			Log.d(Config.TAG, "============= Request URL & getParam Start====================");
			Log.d(Config.TAG, getBaseURL() + url);
			Log.d(Config.TAG, "============= Request URL & getParam End ====================");
		}
		final HttpPost request = new HttpPost(getBaseURL() + url);
		request.addHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		StringBuilder postBody;
		String postBodyString = "";
		
		try {
			// Add body part
			postBody = new StringBuilder();
			// Add file part
			if ( filesJsonObj != null && !filesJsonObj.isEmpty()) {
				
				int fileCnt = 1;
				final int uploadfileTotal = filesJsonObj.size();
				for ( JSONObject fileJsonObj : filesJsonObj ) {
					postBody = new StringBuilder();
					postBody.append("\r\n--" + boundary + "\r\n");
					
					final Iterator<?> filekey = fileJsonObj.keys();
					while(filekey.hasNext()){

						final String key = (String)filekey.next();
						postBody.append("Content-Disposition: form-data; name=\""+key+"\"; ");
						postBody.append("filename=\"" + fileJsonObj.getString(key) + "\";");
						postBody.append("\r\nContent-Type: application/octet-stream\r\n\r\n");
						postBodyString += postBody.toString() + "[Binary Data...]\r\n";
						baos.write(postBody.toString().getBytes(Config.encode));
						baos.flush();
						final String filepath = (String)fileJsonObj.get(key);
						if ( fileJsonObj.has(key) && filepath != null && !"".equals(filepath)) {
							
							//파일 사이즈구함
							final File file = new File(filepath);
							final int filesize = (int)file.length();
							
							int bufferSize = 2000*1024;
							
							final FileInputStream fStream = new FileInputStream(file);
							int maxBufferSize=20*1024;
							int bytesAvailable = fStream.available();
							bufferSize = Math.min(bytesAvailable, maxBufferSize);
							byte[] buffer = new byte[bufferSize];
							int length = -1;
							//현재 upload사이즈
							double uploadsize = 0;
							final DecimalFormat dformat = new DecimalFormat("###.##");
							
							int bytesRead = fStream.read(buffer, 0, bufferSize);
//							while((length = fStream.read(buffer)) != -1) {
							while (bytesRead > 0){
//								baos.write(buffer, 0, length);
								
						      	baos.write(buffer, 0, bufferSize);
                                bytesAvailable = fStream.available();
                                bufferSize = Math.min(bytesAvailable,maxBufferSize);
                                bytesRead = fStream.read(buffer, 0,bufferSize);
								uploadsize += bufferSize;
								//퍼센트 계산처리함
								double percenter = uploadsize / filesize * 100;
								callback.requestCompleted((int)Double.parseDouble(dformat.format(percenter)),fileCnt);
							}
							
							//baos.write(postBody.toString().getBytes(Config.encode));
//							baos.flush();
							//파일카운터
							fileCnt++;
						}
					}
				}
			}
			
			if(bodyJsonObj != null ){
				final Iterator<?> datakey = bodyJsonObj.keys();
				while(datakey.hasNext()){
					final String key = (String)datakey.next();
					postBody.append("\r\n--" + boundary + "\r\n");
					postBody.append("Content-Disposition: form-data; name=\""+key+"\"\r\n\r\n"+bodyJsonObj.get(key)+ "\r\n");
					baos.write(postBody.toString().getBytes(Config.encode));
				}
				//http://darksilber.tistory.com/42   참조
				//http://blog.debug.so/m/post/view/id/138
				postBodyString += postBody.toString() ;
				Log.d(Config.TAG, "JSON ["+postBodyString+"]");
			}
			
			
			postBody = new StringBuilder();
			postBody.append("\r\n--" + boundary + "--\r\n");
			postBodyString += postBody.toString();
			
			baos.write(postBody.toString().getBytes(Config.encode));
			baos.flush();
			final HttpEntity httpBody = new ByteArrayEntity(baos.toByteArray());
			request.setEntity(httpBody);
			if(Config.debug){
				Log.d(Config.TAG, "===========file data=====");
//				Log.d(Config.TAG, getBaseURL() + url);
				Log.d(Config.TAG, postBodyString);
				Log.d(Config.TAG, "===========file data=====");
			}
			
		} catch (JSONException e) {
			callback.requestFailed(this,e);
		} catch (UnsupportedEncodingException e) {
			callback.requestFailed(this,e);
		} catch (IOException e) {
			callback.requestFailed(this,e);
		} catch (Exception e){
			callback.requestFailed(this,e);
		}finally{
			try{ if(baos != null) baos.close();}catch(Exception e){}
		}
		
		return request;
	}

	public boolean getResult() {
		return fResult;
	}

	
//	protected void processResponseObject(int message) throws Exception{
//	}

	/**
	 * 업로드 완료처리함
	 */
	protected void processResponseObject() throws Exception{
		callback.requestCompleted(this);
	}
	
}
