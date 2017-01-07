package com.smarttech.request.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import android.graphics.Bitmap;
import android.util.Log;

import com.smarttech.conf.Config;
import com.smarttech.support.UIThread;


/**
 * 통신 모듈
 * @author SmartTech (jungkyungjoo)
 * Copyright (c) 2014, SmartTech (jungkyungjoo)
 */
public abstract class ServerRequest
{
//	private final static Locale sLocale = Locale.getDefault();
//	private final static Object sLockForLocaleSettings = new Object();
	
	
	public final static int REQUEST_JSON     = 1;
	public final static int REQUEST_DOWNLOAD = 2;
	public final static int REQUEST_UPLOAD   = 3;
	public int REQUEST_MODE = REQUEST_JSON;
	
	
	protected long filesize = 0;
    protected CallBack fcallback;
    protected HashMap<String,Object> menu = null;
    private final ArrayList<Bitmap> bmap = new ArrayList<Bitmap>();
    
    /*
     * 요청 형식
     */
    public int getRequestMode(){
    	return REQUEST_MODE;
    }
    
    public void setReqeustMode(int REQUEST_MODE){
    	this.REQUEST_MODE = REQUEST_MODE;
    }
    
    public interface CallBack
    {
        public void requestCompleted(ServerRequest req);
        public void requestFailed(ServerRequest req, Throwable ex);
        public void requestFailed(ServerRequest req, Throwable ex, byte[] data);
    }
    
    private class UIRunnable implements Runnable
    {
        private Throwable fError;
        private byte[] fData;
        
        UIRunnable() {
        }
        
        UIRunnable(Throwable e) {
            fError = e;
        }
        
        UIRunnable(Throwable e, byte[] d) {
            fError = e;
            fData = d;
        }
        
        public void run() {
        	
        	if( fcallback == null ){
        		return;
        	}
        	
            if (fError == null) {
            	fcallback.requestCompleted(ServerRequest.this);
            } else if (fData == null) {
            	fcallback.requestFailed(ServerRequest.this, fError);
            } else {
            	fcallback.requestFailed(ServerRequest.this, fError, fData);
            }
        }
    }
    
    public ServerRequest(CallBack callback) {
    	UIThread.newInstance();
    	fcallback = callback;
    }
    
//    protected abstract HttpUriRequest generateRequest();
    protected abstract Object generateRequest();
    protected abstract HttpPost getRequest(HttpPost request);
//    protected abstract void requestUpload();
    
    
    protected String getBaseURL() {
        return Config.getServer();
    }
    
    protected void fireFailed(Throwable e) {
        if (fcallback != null) {
            UIThread.newInstance().executeInUIThread(new UIRunnable(e));
        }
    }
    
    protected void fireFailed(Exception e, byte[] data) {
        if (fcallback != null) {
            UIThread.newInstance().executeInUIThread(new UIRunnable(e, data));
        }
    }
    
    protected void fireComplete() {
        if (fcallback != null) {
            UIThread.newInstance().executeInUIThread(new UIRunnable());
        }
    }
    
    
    public HttpGet generateBaseRequest(String urlPath) {
    	final HttpGet get = new HttpGet(getBaseURL()+urlPath);
    	return get;
    }
    
   
    public void setBitmap(Bitmap map){
    	if(bmap != null && !bmap.isEmpty()){
    		bmap.clear();
    	}
    	bmap.add(map);
    }
    
    public ArrayList<Bitmap> getBitmaps(){
    	return bmap;
    }
    public void setValue(HashMap<String,Object> menu){
    	this.menu = menu;
    }
    
    public HashMap<String,Object> getValue(){
    	return this.menu;
    }
    
    public CallBack getCallback() {
        return fcallback;
    }
    
    /**
     * file download size 
     */
    protected void setFileSize(long filesize){
    	this.filesize = filesize;
    }
    
    public long getFileSize(){
    	return this.filesize;
    }
    
    protected abstract void processResponse(InputStream in) throws Exception;
    
    /**
	 *  JSON Data 로그출력
	 * @param in
	 * @throws Exception
	 */
	public InputStream printLog( InputStream in,final String url ) throws IOException{
		//############# debug start ##############
         final  ByteArrayOutputStream baos = new ByteArrayOutputStream();
         byte[] data = new byte[1024];
         int i;
         while (0 < (i = in.read(data))) {
        	 baos.write(data,0,i);
         }
         String str = new String(baos.toByteArray(),Config.encode);
         Log.d(Config.TAG,"=============== [response Data] ===============");
         Log.d(Config.TAG,str);
         Log.d(Config.TAG,"=============== [response Data] ===============");
         return new ByteArrayInputStream(baos.toByteArray());
             
		//############# debug end ##############
	}
}
