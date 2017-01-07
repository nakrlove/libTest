package com.example.libtest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.smarttech.common.SmtAlertDialogHelper;
import com.smarttech.common.ApplicationContext;
import com.smarttech.support.UIThread;

public class BaseActivity extends Activity {
	
	/**
	 * ArrayList<HashMap<String,Object>> 형식을 JSONArray형식으로 파싱
	 * @param array
	 * @return
	 */
	public JSONArray getParseJsonArray(ArrayList<HashMap<String,Object>> array){
		final JSONArray jarray = new JSONArray();
		try{
			for(HashMap<String,Object> map:array){
				final JSONObject json = new JSONObject();
				
				final Iterator iterator = map.entrySet().iterator();
				while (iterator.hasNext()) {
					final Entry<String,Object> entry = (Entry<String,Object>) iterator.next();
					json.put(entry.getKey(), entry.getValue());
				}
				
				jarray.put(json);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return jarray;
	}
	
	
	
	/**
	 * HashMap 값을 JSON형식으로 파싱
	 * @param map
	 * @return
	 */
	public JSONObject getJsonParam(HashMap map) {
		final JSONObject json = new JSONObject();
		if (map.isEmpty()) {
			return json;
		}

		try {

			final Iterator iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				final Entry<String,Object> entry = (Entry<String,Object>) iterator.next();
				final String key= entry.getKey();
				
				final Object obj = (Object)map.get(key);
				if(obj instanceof ArrayList){
					json.put(entry.getKey(), getParseJsonArray((ArrayList<HashMap<String,Object>>)obj));
				}else {
					json.put(entry.getKey(), entry.getValue());
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}
	
	private ProgressDialog fProgDlg;
	/**
	 * 다이얼로그 닫기
	 */
    public void hideProgress(){
    	if (fProgDlg != null) {
            fProgDlg.dismiss();
            fProgDlg = null;
        }
    }
       
    /**
     * 다이얼로그 호출하기
     * 
     * @param msg  다이얼로그에 출력할 메세지
     */
    public void showProgress(final String msg){
   	 	hideProgress();
   	
   	 	fProgDlg = new ProgressDialog(this);
        fProgDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        fProgDlg.setMessage(msg);
        fProgDlg.setCancelable(false);
        fProgDlg.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            	hideProgress();
                finish();
            }
        });
        fProgDlg.show();
    }
    
    
    
	/**
	 * 세션이 끊어졌을때 alert 다이얼록
	 */
	final private SmtAlertDialogHelper.CallBack callback = new SmtAlertDialogHelper.CallBack() {

		@Override
		public void onPressButton(int btnIndex) {
			// TODO Auto-generated method stub
			UIThread.newInstance().executeInUIThread(new Runnable(){
				@Override
				public void run(){
//					final Intent intent = new Intent(BaseActivity.this,LoginActivity.class);
//				    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//				    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			        intent.addFlags(0x8000); // e
//					startActivity(intent);
//					finish();
//					overridePendingTransition(0, 0);
				}
			},500);
		}
	};
    
	
	/**
	 * 세션이 끊어졌을때  
	 * 메세치 처리 Handler
	 */
    private SmtAlertDialogHelper dialog = null;
	final private Handler handler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			if(dialog == null || !dialog.isShowing()){
				dialog = new SmtAlertDialogHelper(BaseActivity.this
							,"알림"
							,msg.obj +""
							,SmtAlertDialogHelper.ALERT_OK,callback);
				dialog.show();
			}
		}
	};

	/**
	 * 세션이 끊어졌을때 통신단에서 호출 요청함
	 * main Thread단이 틀려서 따로 Handler를 호출함
	 * @param msg
	 */
	public void sendMsg(String msg){
		 hideProgress();
		 if("finish".equals(msg)){
			 ApplicationContext.getInstance().requestKillProcess();
			 return;
		 }
		 final Message mhandler = handler.obtainMessage();
		 mhandler.obj = msg;
         handler.sendMessage(mhandler);
	}
}
