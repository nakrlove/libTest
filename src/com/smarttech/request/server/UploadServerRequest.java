package com.smarttech.request.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.json.JSONObject;

import com.smarttech.conf.Config;
import com.smarttech.request.json.parser.JSONParseEngine;
import com.smarttech.support.UIThread;

/**
 * 파일 업로드 통신모듈
 * @author SmartTech (jungkyungjoo)
 * Copyright (c) 2014, SmartTech (jungkyungjoo)
 */
public abstract class UploadServerRequest extends ServerRequest {

	protected String urlTemp  = null;
	protected JSONObject json = null;
	public FileCallBack callback;
	public interface FileCallBack extends ServerRequest.CallBack{
		void requestCompleted(int percenter);
		void requestCompleted(int percenter,int currentFileCount);
	}

	private class UIRunnable implements Runnable {
		private Throwable fError;
		private byte[] fData;
		private boolean fRun = false;
		private int message = 0;
		private int currentFileCount = 0;

		UIRunnable() {
		}

		UIRunnable(int message, boolean fRun) {
			this.message = message;
			this.fRun = fRun;
		}
		
		/**
		 * 현재 진행중인 파일업로드 카운터값까지 받음
		 * @param message
		 * @param fRun
		 * @param currentFileCount
		 */
		UIRunnable(int message, boolean fRun,int currentFileCount) {
			this.message = message;
			this.currentFileCount = currentFileCount;
			this.fRun = fRun;
		}

		UIRunnable(Throwable e) {
			fError = e;
		}

		UIRunnable(Throwable e, byte[] d) {
			fError = e;
			fData = d;
		}

		public void run() {
			if (fError == null) {
				// fDelegate.requestCompleted(ServerRequest.this);
				if (fRun) {
					callback.requestCompleted(message);
				} else {
					callback.requestCompleted(UploadServerRequest.this);
				}

			} else if (fData == null) {
				callback.requestFailed(UploadServerRequest.this, fError);
			} else {
				callback.requestFailed(UploadServerRequest.this, fError, fData);
			}

		}
	}

	public UploadServerRequest(FileCallBack callback,JSONObject json) {
		super(callback);
		this.callback = callback;
		this.json = json;
	}


	/**
	 * Internal support routine for getting the base URL
	 * 
	 * @return
	 */
	protected String getBaseURL() {
		return Config.getServer();
	}

	protected void fireFailed(Throwable e) {
		if (callback != null) {
			UIThread.newInstance().executeInUIThread(new UIRunnable(e));
		}
	}


	protected void fireComplete() {
		if (callback != null) {
			UIThread.newInstance().executeInUIThread(new UIRunnable());
		}
	}

	protected void fireComplete(int message) {
		if (callback != null) {
			UIThread.newInstance().executeInUIThread(new UIRunnable(message, true));
		}
	}
	protected void fireComplete(int message ,int currentFileCount) {
		if (callback != null) {
			UIThread.newInstance().executeInUIThread(new UIRunnable(message, true,currentFileCount));
		}
	}

	
//	@Override
//	protected HttpPost getRequest(HttpPost request) {
//		try {
//			Log.d(Config.TAG,"============= Request URL & getParam ====================");
//			urlTemp = request.getURI()+"";
//			Log.d(Config.TAG,urlTemp);
//			Log.d(Config.TAG,json.toString());
//			final StringEntity stringEntity = new StringEntity(json.toString(),Config.encode);
//			stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
//			stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
//			
//			final String session = ApplicationContext.getInstance().getSharedInfo().getSessionId();
//			request.setHeader("SESSION_ID", session);
//			request.setHeader("Accept", "application/json");
//			request.setHeader("Content-type", "application/json; charset=utf-8");
//			request.setHeader("Auth-Token", " ");
//			request.setEntity(stringEntity);
//			Log.d(Config.TAG,"============= Request URL & getParam ====================");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		  return request;
//	}
	
	
	/**
	 * Default operation is to convert using the JSON libraries. Note that this
	 * is handled in a thread separate from the UI thread.
	 * 
	 * @param data
	 * @throws IOException
	 */
	@Override
	public void processResponse(InputStream in) throws Exception {
		
		//업로드 결과 처리함
		final JSONParseEngine engine = new JSONParseEngine(printLog(in,urlTemp));
		final HashMap<String,Object> result = (HashMap<String,Object>)engine.parse();
		this.setValue(result);
		final String rt = (String)result.get("RT");
		//업로드 실패처리
		if(!"00000".equals(rt)){
			final String rt_mst = (String)result.get("RT_MSG");
			fireFailed(new Throwable(rt_mst));
			return;
		}
		
		//업로드 성공처리
		processResponseObject();


	}

	protected abstract void processResponseObject() throws Exception;

}
