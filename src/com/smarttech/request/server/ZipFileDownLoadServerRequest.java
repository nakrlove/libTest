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
import com.smarttech.util.ZipUtils;

/**
 * zip으로 압축된 파일을 다운로드후 압축해제 모듈
 * @author SmartTech (jungkyungjoo)
 * Copyright (c) 2014, SmartTech (jungkyungjoo)
 */
public abstract class ZipFileDownLoadServerRequest extends ServerRequest {

	protected ZipCallBack zcallback;
	protected String fileName = null;
	public interface ZipCallBack extends ServerRequest.CallBack{

		int runType = 0;
		/*
		 * 압축 풀기위한 구분자
		 */
		int getRunType();

		void requestCompleted(int message);

	}

	private class UIRunnable implements Runnable {
		private Throwable fError;
		private byte[] fData;
		private boolean fRun = false;
		private int message = 0;

		private int runType = 1;

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
					zcallback.requestCompleted(message);
				} else {
					zcallback.requestCompleted(ZipFileDownLoadServerRequest.this);
				}

			} else if (fData == null) {
				Log.d(Config.TAG," >>>> requestFailed Zip Data is null ");
				zcallback.requestFailed(ZipFileDownLoadServerRequest.this, fError);
			} else {
				Log.d(Config.TAG," >>>> requestFailed Zip Data failed ");
				zcallback.requestFailed(ZipFileDownLoadServerRequest.this, fError, fData);
			}

		}
	}

	public ZipFileDownLoadServerRequest(ZipCallBack zcallback) {
		super(zcallback);
		this.zcallback = zcallback;
	}


	/**
	 * zip file type
	 * 
	 * @return
	 */
	protected abstract int getUnZip();

	/**
	 * Internal support routine for getting the base URL
	 * 
	 * @return
	 */
	protected String getBaseURL() {
		return Config.getServer();
	}

	protected void fireFailed(Throwable e) {
		if (zcallback != null) {
			UIThread.newInstance().executeInUIThread(new UIRunnable(e));
		}
	}

	protected void fireUnZipFailed(Exception e, byte[] data) {
		if (zcallback != null) {
			UIThread.newInstance().executeInUIThread(new UIRunnable(e, data));
		}
	}

	protected void fireComplete() {
		if (zcallback != null) {
			UIThread.newInstance().executeInUIThread(new UIRunnable());
		}
	}

	protected void fireComplete(int message) {
		if (zcallback != null) {
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
	public void processResponse(InputStream in, String time) throws Exception {

		BufferedInputStream is = null;
		BufferedOutputStream fos = null;
		File parent;
		int bytes = 0;

		final int type = getUnZip();
		try {
			parent = new File(Config.getPackageURL());
			if (!parent.exists()) {
				if (!parent.mkdirs()) {
					throw new Exception("Could not create directories, "
							+ Config.getPackageURL());
				}
			}

			final byte[] buffer = new byte[Config.DOWN_LOAD_BUFFER];
			int read = 0;

			is = new BufferedInputStream(in);
			fos = new BufferedOutputStream(new FileOutputStream(Config.getInstallURL(fileName)));
			while ((read = is.read(buffer)) != -1) {
				bytes += read;
				processResponseObject(bytes);
				fos.write(buffer, 0, read);
				fos.flush();
			}

			/* 마지막 작업이 끝난후 압축파일을 풀기 위한 please wait ... 메세지 출력 함 */
			// if( type == FileManager.TOTAL_FILE_COUNT){
			processResponseObject(-999);
			// }

			/* 압축 해제및 압축파일 삭제 처리 */
			unZip(type);
			processResponseObject();

			// } catch(Exception e) {
			// e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
					is = null;
				}
				if (fos != null) {
					fos.close();
					fos = null;
				}

			} catch (Exception ec) {
				fireUnZipFailed(ec, null);
				// fDelegate.requestFailed(this, ec);
				// ec.printStackTrace();
			}

		}

	}

	public void unZip(int type) throws Exception {
		ZipUtils.unzip(
				new File(Config.getInstallURL(fileName)),
				new File(Config.getPackageURL()), true);

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
