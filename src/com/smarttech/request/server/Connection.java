package com.smarttech.request.server;


import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.smarttech.conf.Config;
import com.smarttech.request.json.exception.ServerErrorException;
import com.smarttech.request.json.exception.SmartTechNetworkException;
import com.smarttech.util.NetworkConnection;

/**
 * 통신 Thread Pooling
 * @author SmartTech (jungkyungjoo)
 * Copyright (c) 2014, SmartTech (jungkyungjoo)
 */
public class Connection {
	
    private static Connection gConnection = null;
    private LinkedList<ServerRequest> mQueue;
    
    
    private ServerRequest  mRequest;
    private HttpUriRequest mHttpRequest;
    
    public static HttpClient fConnection;
    private HttpResponse response;
    
    
    
    HttpURLConnection urlconnection = null;
    private class RequestThread extends Thread
    {
        RequestThread() {
            super("ServerThread");
        }
        
        
        @SuppressWarnings("unused")
		public void run() {
        	
//            for(;;) {
            while(true) {
                ServerRequest  req     = null;
                HttpUriRequest request = null;
                try {
                	
					synchronized (Connection.this) {
						while (mQueue.isEmpty()) {
							try {
								Connection.this.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
								// Ignore
							}
						}

						req = mQueue.removeFirst();
//						if(NetworkConnection.newInstance().isAirplaneMode()){
//							req.fireFailed(new PentaNetworkException("NET:405"));
//							continue;
//						}
//						
						if(NetworkConnection.newInstance().checkNetworkConnection()){
							Log.d(Config.TAG," <<<<< NETWORK ERROR NET:404 CALLED >>>>>");
							req.fireFailed(new SmartTechNetworkException("NET:404"));
							continue;
						}	

						
						Log.d(Config.TAG," <REQUEST_UPLOAD ["+req.REQUEST_UPLOAD+"] req.getRequestMode()["+req.getRequestMode()+"]");
						if(req.REQUEST_UPLOAD == req.getRequestMode()){
							urlconnection = (HttpURLConnection)req.generateRequest();
//							
						}else {
							request = (HttpUriRequest)req.generateRequest();
							if (request == null) {
								Log.d(Config.TAG," <<<<< REQUERST IS NULL >>>>>");
								req.fireFailed(new Exception("Unable to execute"));
								continue;
							}
//							fRequest = req;
							mHttpRequest = request;
						}
						mRequest = req;
					}
					
					if(req.REQUEST_UPLOAD != req.getRequestMode()){
						//file upload
//						requestUpload(req);
//					}else{
						requestData(req,request);
					}
					
				} catch (Throwable e) {
					e.printStackTrace();
					synchronized (Connection.this) {
						if (mRequest != null) {
							mRequest.fireFailed(e);
						}
					}

				} finally {
					synchronized (Connection.this) {
						mRequest = null;
						mHttpRequest = null;
						if( urlconnection != null ){
							urlconnection.disconnect();
							urlconnection = null;
						}
					}
				}
			}
        }
        
        
    }
    
    
    /**
     * 파일 업로드 요청
     * @param req
     */
    private void requestUpload(ServerRequest req){
    	
    	InputStream is = null;
    	try{
    		is = urlconnection.getInputStream();
    		if( is == null){
    			is = new NullInputStream();
    		}
    		
    		int resultCode = urlconnection.getResponseCode();
    		if (resultCode < 500 && resultCode >= 400) {

				StringWriter w = new StringWriter();
				InputStreamReader r = new InputStreamReader(is, Config.encode);
				char[] a = new char[256];
				int len;
				while (0 < (len = r.read(a)))
					w.write(a, 0, len);
				w.close();

				String err = "Server Error Result: Status=" + resultCode + " ('" + urlconnection.getResponseMessage() + "')";
				throw new Exception(err);
			} else if (resultCode == 503) {
				throw new ServerErrorException(Config.SERVER_MSG_503 + "(code:" + resultCode + ")");
			} else if (resultCode == 304) {

				String empty = "{}";
				ByteArrayInputStream bais = new ByteArrayInputStream(empty.getBytes(Config.encode));
				is = bais;
			}
    		
    		/*
			 * Handle the response
			 */
			synchronized (Connection.this) {
				req = mRequest;
			}

			if (urlconnection != null) {
				req.processResponse(is);
			}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    
  
    
    /**
     * json Data요청
     * 이미지 파일다운로드
     * @param req
     * @param request
     * @throws Exception
     */
	public void requestData(ServerRequest req, HttpUriRequest request) throws Exception {

		InputStream is = null;
		try {

			response = fConnection.execute(request);
			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				is = entity.getContent();
				/*
				 * Header[] heard =response.getHeaders("FILE_NAME"); for(Header
				 * h: heard){ Log.d(Config.TAG, h.toString()); }
				 */
			} else {
				// Dummy class represents zero byte buffer
				is = new NullInputStream();
			}

			StatusLine result = response.getStatusLine();
			int resultCode = result.getStatusCode();
			Log.d(Config.TAG, " result Code=[" + resultCode + "]");
			if (resultCode < 500 && resultCode >= 400) {

				StringWriter w = new StringWriter();
				InputStreamReader r = new InputStreamReader(is, Config.encode);
				char[] a = new char[256];
				int len;
				while (0 < (len = r.read(a)))
					w.write(a, 0, len);
				w.close();

				String err = "Server Error Result: Status=" + resultCode + " ('" + result.getReasonPhrase() + "')";
				throw new Exception(err);
			} else if (resultCode == 503) {
				throw new ServerErrorException(Config.SERVER_MSG_503 + "(code:" + resultCode + ")");
			} else if (resultCode == 304) {

				String empty = "{}";
				ByteArrayInputStream bais = new ByteArrayInputStream(empty.getBytes(Config.encode));
				is = bais;
			}

			/*
			 * Handle the response
			 */
			synchronized (Connection.this) {
				req = mRequest;
			}

			if (req != null) {
				req.setFileSize(entity.getContentLength());
				req.processResponse(is);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}

		if (is != null) {
			is.close();
		}
	}
    
    public static HttpClient getHttpClient(){
    	if(fConnection == null){
//    		fConnection = new DefaultHttpClient();
    		fConnection = HttpClientRequest();
    	}
    	
    	return fConnection;
    }
    
    
    
    /**
     * HttpClient 
     * https / http
     * @return HttpClient
     */
    public static HttpClient HttpClientRequest() {
    	
    	Log.d(Config.TAG,"getHttpClient  CURRENT_MODE = ["+Config.CURRENT_MODE +"]");
    	if(Config.CURRENT_MODE == Config.DEV_MODE){
    		return new DefaultHttpClient();
    	}
    	
    	
        try {
        	
            final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            final SSLSocketFactory sf = new SmartTechSSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            final HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            final SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, Config.HTTPS_PORT));
            params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
            params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
            params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);

            final ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
            return new DefaultHttpClient(ccm, params);
            
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
        
    }
    
    
    public void clearHttpClient(){
    	fConnection = null;
    	fConnection = getHttpClient();
    }
    
    private Connection() {
        mQueue = new LinkedList<ServerRequest>();
//        fConnection = new DefaultHttpClient();
        fConnection = Connection.getHttpClient();
        final RequestThread thread = new RequestThread();
        thread.setDaemon(true);
        thread.start();
    }
    
    
    public static Connection getInstance() {
    	if( gConnection == null ){
    		gConnection = new Connection();
    	}
        return gConnection;
    }
    
    
    public synchronized void enqueueRequest(ServerRequest request) {
        mQueue.addLast(request);
        notifyAll();
    }
  
    public synchronized void dequeueRequest(ServerRequest request) {
        mQueue.remove(request);
        if (mRequest == request) {
            mHttpRequest.abort();
            mRequest = null;
        }
    }
    
    public synchronized void dequeueRequestWithCallback(ServerRequest.CallBack reqCallback) {
        Iterator<ServerRequest> iter = mQueue.iterator();
        while (iter.hasNext()) {
            ServerRequest req = iter.next();
            if (req.getCallback() == reqCallback) {
                iter.remove();
            }
        }
        if (mRequest.getCallback() == reqCallback) {
            mHttpRequest.abort();
            mRequest = null;
        }
    }
    
    public synchronized void dequeueRequestRemove() {
        Iterator<ServerRequest> iter = mQueue.iterator();
        while (iter.hasNext()) {
            iter.next();
            iter.remove();
        }
    }
}