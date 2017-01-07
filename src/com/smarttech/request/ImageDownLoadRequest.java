package com.smarttech.request;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.smarttech.conf.Config;
import com.smarttech.request.server.ServerRequest;

/**
 * 이미지 다운로드 모듈
 * @author SmartTech (jungkyungjoo)
 * Copyright (c) 2014, SmartTech (jungkyungjoo)
 */
public class ImageDownLoadRequest extends ServerRequest {

	protected String photoURL = null;
	protected JSONObject json = null;

	protected boolean flag = false;
	public ImageDownLoadRequest(FileCallBack callback) {
		super(callback);
		this.callback = callback;
	}

	public ImageDownLoadRequest(FileCallBack callback, String photoURL,boolean flag) {
		super(callback);
		this.callback = callback;
		this.photoURL = photoURL;
		this.flag = flag;
	}

	public ImageDownLoadRequest(FileCallBack callback, JSONObject json,String photoURL,boolean flag) {
		super(callback);
		this.callback = callback;
		this.json = json;
		this.photoURL = photoURL;
		this.flag = flag;
	}

	
	public FileCallBack callback;
	public interface FileCallBack extends ServerRequest.CallBack{
		public void requestCompleted(int percenter);
//		public void requestCompleted(int percenter,int currentFileCount);
	}
	
	@Override
	protected HttpPost getRequest(HttpPost request) {
		
		try {
			photoURL = request.getURI()+"";
			String jsonData = "{}";
			if(json != null){
				jsonData = json.toString();
			}
			
			if(Config.debug){
				Log.d(Config.TAG,"============= Request URL & getParam ====================");
				Log.d(Config.TAG,photoURL);
				Log.d(Config.TAG,jsonData);
				Log.d(Config.TAG,"============= Request URL & getParam ====================");
			}
			
			final StringEntity stringEntity = new StringEntity(jsonData,Config.encode);
			stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
			stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
			request.setHeader("Accept", "application/json");
			request.setHeader("Content-type", "application/json; charset=utf-8");
			request.setHeader("Auth-Token", " ");
			request.setEntity(stringEntity);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return request;
	}
	
	@Override
	public HttpUriRequest generateRequest() {
		if( json == null ){
			/* 이미지 다운로드 요청 */
			if(Config.debug){
				Log.d(Config.TAG,"============= Request URL & getParam ====================");
				Log.d(Config.TAG,getBaseURL()+this.photoURL);
				Log.d(Config.TAG,"============= Request URL & getParam ====================");
			}
			return new HttpGet(getBaseURL()+this.photoURL);
		}
		/* json data요청 파라메터가 포함됨 */
		return getRequest( new HttpPost(getBaseURL()+this.photoURL));
	}
	
	@Override
	protected void processResponse(InputStream in) throws IOException {
		// TODO Auto-generated method stub
		try {
			
			//다운로드 진행바 표시처리히
			if(flag){
				FileDownloadResponse(in);
			}else {
				internalProcessResponse(in);
			}
			
			fireComplete();
		} catch (IOException ex) {
			fireFailed(ex);
			throw ex;
		} catch (Exception e) {
			fireFailed(e);
			e.printStackTrace();
//		} finally{
//			
//			ServerConnection.getInstance().dequeueRequest(this);
		}
		
	}
	
	/**
	 * 파일다운로드
	 * @param in
	 * @throws IOException
	 */
	public void FileDownloadResponse(InputStream in)throws IOException {
		final double filesize = getFileSize();
		final DecimalFormat dformat = new DecimalFormat("###.##");
		final byte[] buffer = new byte[1024];
		
		int read = 0;
		double total = 0;
		while( (read = in.read(buffer)) > 0) {
			total += read;
			double percenter = (total / filesize) * 100;
			callback.requestCompleted((int)Double.parseDouble(dformat.format(percenter)));
		}
	}
	
	/**
	 * 이미지 다운로드
	 * @param in
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void internalProcessResponse(InputStream in) throws IOException {
		try {
//			callback.requestCompleted(this);
			final Bitmap bm = BitmapFactory.decodeStream(in);
			if (bm != null) {
				setBitmap(bm);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				in.close();
			}
		}

	}
	
}
