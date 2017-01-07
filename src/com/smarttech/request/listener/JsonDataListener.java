package com.smarttech.request.listener;


import java.util.HashMap;

import android.annotation.SuppressLint;
import android.util.Log;
import android.widget.Toast;

import com.example.libtest.BaseActivity;
import com.smarttech.common.SmtAlertDialogHelper;
import com.smarttech.common.ApplicationContext;
import com.smarttech.conf.Config;
import com.smarttech.request.server.Connection;
import com.smarttech.request.server.ServerRequest;


/**
 * Json Data callback Listener 
 * @author SmartTech (jungkyungjoo)
 * Copyright (c) 2014, SmartTech (jungkyungjoo)
 */
public class JsonDataListener implements ServerRequest.CallBack{
	
	BaseActivity base = null;
	private static SmtAlertDialogHelper pdhelper = null;
	protected HashMap<String,Object> resultMap  = null;
	
	public JsonDataListener(){
//		this.base = base;
	}
	public JsonDataListener(BaseActivity base){
		this.base = base;
	}
	
	
	/**
	 * 로그인 화면으로 Activity전환처리
	 * @author jungkyungjoo
	 *
	 */
	final class LoginViewRequest implements SmtAlertDialogHelper.CallBack {
		
		public LoginViewRequest(){
		}
		
		public void onPressButton(int btnIndex){
			
			Toast.makeText(base, "로그인 화면으로 이동해야 합니다.", Toast.LENGTH_LONG).show();
//			final Intent intent = new Intent();
//			intent.setClass(base, LoginActivity.class);
//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			base.startActivity(intent);
//			base.finish();
		}
		
	}
	
	@SuppressLint("NewApi")
	public void requestCompleted(ServerRequest req) {
		
		Log.d(Config.TAG ,"<<<< JSON DATA CALL >>>>");
		if( this.base != null ){
			this.base.hideProgress();
		}
		
		resultMap  = req.getValue();
		if( resultMap == null ){
			return;
		}
		Connection.getInstance().dequeueRequest(req);
		
		/*
		final String RT = (String)resultMap.get("RT");
		
		//00005 :데이터존재 하지 않음
		//00000 :정상적인 데이터
		//50006 :세션이 끊어진상태
		if( !"00000".equals(RT) && !"00005".equals(RT)){
			
			final String RT_MSG =  (String)resultMap.get("RT_MSG");
			//데이터가 존재 하지않음 처리함
			if( this.base == null ){
				Toast.makeText(ApplicationContext.getInstance(), "[SC:"+RT+"]"+RT_MSG, Toast.LENGTH_LONG).show();
				return ;
			}
			
			//세션연결이 끊어진상태
			if("50006".equals(RT)){
				this.base.sendMsg(RT_MSG);
				return;
			}
			
			if(pdhelper != null && pdhelper.isShowing()){
				return;
			}
			
			pdhelper = new SmtAlertDialogHelper(this.base, "알림", "[SC:"+RT+"]"+RT_MSG , SmtAlertDialogHelper.ALERT_OK);
			pdhelper.show();
			return ;
		}
		*/
	}
	
	
	public void requestFailed(ServerRequest req, Throwable ex) {
		
		if(this.base != null){
			this.base.hideProgress();
		}

		Connection.getInstance().dequeueRequest(req);
		final String errMsg = ex.getMessage();
		//네트워크망 불량 체크
		/*
		if("NET:404".equals(errMsg)){
			
			if(pdhelper != null && pdhelper.isShowing()){
				return;
			}
			
			pdhelper =  new SmtAlertDialogHelper(this.base
					,"알림"
					,"3g 또는 4g,LTE 네트워크 설정이 필요합니다.!"
					,SmtAlertDialogHelper.ALERT_OKANDCANCEL,new NetCallBack());
			pdhelper.show();
			return;
		}
		*/
		if( this.base == null ){
			Toast.makeText(ApplicationContext.getInstance(), "[LM]:"+errMsg, Toast.LENGTH_LONG).show();
			return ;
		}
		
		if(pdhelper != null && pdhelper.isShowing()){
			return;
		}
		
		pdhelper =  new SmtAlertDialogHelper(this.base
				 , "에러"
				 , "[LM]:"+errMsg
				 , SmtAlertDialogHelper.ALERT_OK);
		pdhelper.show();

	}
	
	public void requestFailed(ServerRequest req, Throwable ex, byte[] data) {
		requestFailed(req, ex);
	}
	
}

