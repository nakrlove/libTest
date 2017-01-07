package com.smarttech.request;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.libtest.BaseActivity;
import com.smarttech.conf.Config;
import com.smarttech.request.json.parser.JSONParseEngine;
import com.smarttech.request.server.ServerRequest;
import com.smarttech.support.UIThread;

import android.util.Log;

public class FileUploadRequest extends ServerRequest {

	String urlString =null;
	ArrayList<JSONObject> filesJsonObj = null;
	public FileCallBack callback;
	public interface FileCallBack extends ServerRequest.CallBack{
		public void requestCompleted(int percenter);
		public void requestCompleted(int percenter,int currentFileCount);
	}
	
	BaseActivity base = null;
	public FileUploadRequest( FileCallBack callback, ArrayList<JSONObject> filesJsonObj,JSONObject bodyJsonObj ,BaseActivity base ,String urlString ) {
		super(callback);
		this.callback = callback;
		this.base = base;
		this.filesJsonObj = filesJsonObj;
		this.bodyJsonObj = bodyJsonObj;
		this.urlString = urlString;
		setReqeustMode(REQUEST_UPLOAD);
	}
	
	@Override
	public HttpURLConnection generateRequest() {
		return requestUpload();
	}
	
	@Override
	protected HttpPost getRequest(HttpPost request) {
		return null;
	}
	
	
	private class UIRunnable implements Runnable {
		private Throwable fError;
		private byte[] fData;
		public static final int RUN_SIZE = 1;
		public static final int RUN_PROG = 2;
//		public static final int RUN_COMP = 3;
		
		private int RUN      = RUN_PROG;

		private int message = 0;
		private int currentFileCount = 0;

//		UIRunnable() {
//		}
//
//		UIRunnable(int message, int RUN) {
//			this.message = message;
//			this.RUN = RUN;
//		}
		
		/**
		 * 현재 진행중인 파일업로드 카운터값까지 받음
		 * @param message
		 * @param fRun
		 * @param currentFileCount
		 */
		UIRunnable(int RUN,int message, int currentFileCount) {
			this.RUN = RUN;
			this.message = message;
			this.currentFileCount = currentFileCount;
		}

//		UIRunnable(Throwable e) {
//			fError = e;
//		}
//
//		UIRunnable(Throwable e, byte[] d) {
//			fError = e;
//			fData = d;
//		}

		public void run() {
			if (fError == null) {
				if (RUN == RUN_SIZE) {
					callback.requestCompleted(message);
				} else if(RUN == RUN_PROG){
					callback.requestCompleted(message,currentFileCount);
				} else {
					callback.requestCompleted(FileUploadRequest.this);
				}

			} else if (fData == null) {
				callback.requestFailed(FileUploadRequest.this, fError);
			} else {
				callback.requestFailed(FileUploadRequest.this, fError, fData);
			}

		}
	}
	
	
	protected void fireComplete(int message, int currentFileCount) {
		if (fcallback != null) {
			UIThread.newInstance().executeInUIThread(new UIRunnable(UIRunnable.RUN_PROG, message,currentFileCount));
		}
	}	
	final static String boundary = "0xKhTmLbOuNdArY";
	public static HttpURLConnection getURLConnection(String urlString) {
		HttpURLConnection conn = null;
		try {
			final URL connectURL = new URL(Config.getServer() + urlString);
			// Open a HTTP connection to the URL
			conn = (HttpURLConnection) connectURL.openConnection();
			// Allow Inputs
			conn.setDoInput(true);
			// Allow Outputs
			conn.setDoOutput(true);
			// Don't use a cached copy.
			conn.setUseCaches(false);
			// Use a post method.
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type","multipart/form-data; boundary=" + boundary);
			conn.setChunkedStreamingMode(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	JSONObject bodyJsonObj;
	private HttpURLConnection requestUpload() {
		DataOutputStream dos = null;
	    FileInputStream fileInputStream = null;
		final HttpURLConnection conn = getURLConnection(urlString);
		try {
			dos = new DataOutputStream(conn.getOutputStream());
		    int fileCnt = 1;
		    
		    /*
		     * file 전송
		     */
		    for ( JSONObject fileJsonObj : filesJsonObj ) {
		    	final Iterator<?> filekey = fileJsonObj.keys();
		    	dos.write(("\r\n--" + boundary + "\r\n").getBytes(Config.encode));
		    	Log.i(Config.TAG,"\r\n--" + boundary + "\r\n");
				while(filekey.hasNext()){
					
					final String key = (String)filekey.next();
					Log.d("TEST", "type==["+key+"  filename=["+fileJsonObj.getString(key)+"]");
					dos.write(("Content-Disposition: form-data; name=\""+key+"\"; filename=\""+ fileJsonObj.getString(key) + "\";").getBytes(Config.encode));
					dos.write(("\r\nContent-Type: application/octet-stream\r\n\r\n").getBytes(Config.encode));
					dos.flush();
					
					Log.d(Config.TAG, "Content-Disposition: form-data; name=\""+key+"\"; filename=\""+ fileJsonObj.getString(key) + "\";" );
					Log.d(Config.TAG, "Content-Type: application/octet-stream\r\n\r\n");
					
					
					
					final File file = new File(fileJsonObj.getString(key));
					final int filesize = (int)file.length();
				
					fileInputStream =  new FileInputStream(file);
					// create a buffer of maximum size
					final int maxBufferSize = 1024;
					final byte[] buffer = new byte[maxBufferSize];
		
					// read file and write it into form...
					double uploadsize = 0;
					final DecimalFormat dformat = new DecimalFormat("###.##");
					
					double percenter = 0;
					
					
					int bytesRead = -1;
					while ((bytesRead = fileInputStream.read(buffer)) != -1) {
					
						dos.write(buffer, 0, bytesRead);
						uploadsize += bytesRead;
						percenter = uploadsize / filesize * 100;
						fireComplete((int)Double.parseDouble(dformat.format(percenter)),fileCnt);
					}
					fireComplete(100,fileCnt);
					fileCnt++;
					dos.write(("\r\n").getBytes(Config.encode));
					dos.flush();
//					fileInputStream.close();
				}
		    }
		    
		    
		    Log.d(Config.TAG, "########JSON DATA###############");
		    /*
		     * json data값 전송
		     */
		    if(bodyJsonObj != null ){
				final Iterator<?> datakey = bodyJsonObj.keys();
				while(datakey.hasNext()){
					final String key = (String)datakey.next();
					dos.write(("\r\n--" + boundary + "\r\n").getBytes(Config.encode));
					dos.write(("Content-Disposition: form-data; name=\""+key+"\"\r\n\r\n"+URLEncoder.encode((String)bodyJsonObj.get(key), "UTF-8")+ "\r\n").getBytes(Config.encode));
					
					
					Log.d(Config.TAG, ("--" + boundary + "\r\n"));
					Log.d(Config.TAG, ("Content-Disposition: form-data; name=\""+key+"\"\r\n\r\n"+URLEncoder.encode((String)bodyJsonObj.get(key), "UTF-8")+ "\r\n"));
					dos.flush();
				}
			}
		    
			
			Log.i(Config.TAG,"\r\n--" + boundary + "--" + "\r\n");
			dos.write(("\r\n--" + boundary + "--" + "\r\n").getBytes(Config.encode));
			
			dos.close();
		} catch (MalformedURLException ex) {
			Log.e(Config.TAG, "URL error: " + ex.getMessage(), ex);
			fireFailed(ex);
		} catch (IOException ioe) {
			Log.e(Config.TAG, "IO error: " + ioe.getMessage(), ioe);
			fireFailed(ioe);
		} catch (JSONException j){
			j.printStackTrace();
			fireFailed(j);
		} finally{
			try{
				if(dos != null ){dos.close();}
				if(fileInputStream != null){	fileInputStream.close();}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return conn;
	}


	/**
	 * 업로드 완료처리함
	 */
	@Override
	protected void processResponse(InputStream in) throws Exception{
		final JSONParseEngine engine = new JSONParseEngine(printLog(in,this.urlString));
		final HashMap<String,Object> result = (HashMap<String,Object>)engine.parse();
		this.setValue(result);
//		callback.requestCompleted(this);
		fireComplete();
	}
}
