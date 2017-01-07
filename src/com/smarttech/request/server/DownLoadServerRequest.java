package com.smarttech.request.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

import com.smarttech.conf.Config;
import com.smarttech.support.UIThread;


/**
 * 파일 다운로드
 * @author SmartTech (jungkyungjoo)
 * Copyright (c) 2014, SmartTech (jungkyungjoo)
 */
public abstract class DownLoadServerRequest extends ServerRequest {

	public FileCallBack callback;
	protected String fileName = null;
	public interface FileCallBack extends ServerRequest.CallBack{
		void requestCompleted(int message);
	}

	private class UIRunnable implements Runnable {
		private Throwable fError;
		private byte[] fData;
		private boolean fRun = false;
		private int message = 0;

		UIRunnable() {
		}

		UIRunnable(int message, boolean fRun) {
			this.message = message;
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
					callback.requestCompleted(DownLoadServerRequest.this);
				}

			} else if (fData == null) {
				callback.requestFailed(DownLoadServerRequest.this, fError);
			} else {
				callback.requestFailed(DownLoadServerRequest.this, fError, fData);
			}

		}
	}

	public DownLoadServerRequest(FileCallBack callback) {
		super(callback);
		this.callback = callback;
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


	/**
	 * Default operation is to convert using the JSON libraries. Note that this
	 * is handled in a thread separate from the UI thread.
	 * 
	 * @param data
	 * @throws IOException
	 */
	@Override
	public void processResponse(InputStream in) throws Exception {

		BufferedInputStream is = null;
		BufferedOutputStream fos = null;
		File parent;
		int bytes = 0;

		try {
			Log.d(Config.TAG,"processResponse called ===================");
			Log.d(Config.TAG,"Config.getPackageURL()" +Config.getPackageURL());
			Log.d(Config.TAG,"processResponse called ===================");
			parent = new File(Config.getPackageURL());

			if (!parent.exists()) {
				if (!parent.mkdirs()) {
					throw new Exception("Could not create directories, "
							+ Config.getPackageURL());
				}
			}
			Log.d(Config.TAG,"processResponse called ===================");
			Log.d(Config.TAG,"Config.getInstallURL()" +Config.getInstallURL(fileName));
			Log.d(Config.TAG,"processResponse called ===================");
			final byte[] buffer = new byte[Config.DOWN_LOAD_BUFFER];
			int read = 0;

			is  = new BufferedInputStream(in);
			fos = new BufferedOutputStream(new FileOutputStream(Config.getInstallURL(fileName)));
			final long downloadfilesize = getFileSize();
			
			while ((read = is.read(buffer)) != -1) {
//				bytes += 1024;
				bytes += read;
				double percenter = bytes / downloadfilesize * 100;
				processResponseObject(bytes);
				fos.write(buffer, 0, read);
				fos.flush();
			}
			

		} finally {
			processResponseObject();
			try {
				
			
				if (fos != null ) {
					fos.close();
					fos = null;
				}

				if (is != null) {
					is.close();
					is = null;
				}
				
				if( in != null ){
					in.close();
					in = null;
				}
			} catch (Exception ec) {
//				fireFailed(ec, null);
			}
			
			
		}

	}


	/**
	 * This is used to process the response object. This object must be
	 * overridden to handle the response
	 * 
	 * @param object
	 * @throws DioTeckParserException
	 */
	protected abstract void processResponseObject(int message) throws Exception;

	protected abstract void processResponseObject() throws Exception;

}
