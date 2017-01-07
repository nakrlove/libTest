package com.smarttech.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.libtest.R;

public class SmtAlertDialogHelper extends Dialog {
	public static String btn_1 = "OK" ;       /* OK(확인) */
	public static String btn_2 = "Cancel";  /* Cancel(취소) */
	public static String btn_3 = "Back";     /* Back(뒤로) */
	public static String btn_4 = "Close";    /* Close(닫기) */
	public static String btn_5 = "Retray";   /* Retray(재시도) */
	public static String btn_6 = "Setting";  /* Setting(환경설정) */
	
	public static void setButtonText(int type, String btnText) {
		switch(type) {
		case ALERT_OK:
			btn_1 = btnText;
			break;
		case ALERT_CANCEL:
			btn_2 = btnText;
			break;
		}
	}
	

	
	final static public int ALERT_CANCEL = 0;
	final static public int ALERT_OK = 1;
	final static public int ALERT_OKANDCANCEL = 2;
	
	
	public static final int ALERT_CLOSE   = 4;
	public static final int ALERT_RETRAY  = 5;
	public static final int ALERT_SETTING = 6;
	public static final int ALERT_USERDEFINE  = 7;
	public static final int ALERT_BACK = 8;
	public static final int ALERT_CLOSEANDRETRAYANDSETTING = 9;
	
	protected CallBack fcallback;
	private int ALERT = ALERT_OK;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();    
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.8f;
		getWindow().setAttributes(lpWindow);
		getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		getWindow().setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER);
		setContentView(R.layout.common_pop);
		setLayout();
		setTitle(mTitle);
		setContent(mContent);
	}
	
	public SmtAlertDialogHelper(Context context) {
		// Dialog 배경을 투명 처리 해준다.
		super(context , android.R.style.Theme_Translucent_NoTitleBar);
	}
	
	public SmtAlertDialogHelper(Context context , String title , String content , int ALERT) {
		super(context , android.R.style.Theme_Translucent_NoTitleBar);
		this.mTitle = title;
		this.mContent = content;
		this.ALERT = ALERT;
	}
	
	public SmtAlertDialogHelper(Context context , String title , String content , int ALERT ,CallBack fcallback ) {
		super(context , android.R.style.Theme_Translucent_NoTitleBar);
		this.mTitle = title;
		this.mContent = content;
		this.ALERT = ALERT;
		this.fcallback = fcallback;
	}
	
	
	
	
	private void setTitle(String title){
		tv_title.setText(title);
	}
	
	private void setContent(String content){
		tv_contents.setText(content);
	}
	
	
	/*
	 * Layout
	 */
	private TextView tv_title; //타이틀
	private TextView tv_contents;        //내용
	private Button btn_cancel;  //취소
	private Button btn_ok; //확인
	private String mTitle;
	private String mContent;
	
	
	/*
	 * Layout
	 */
	private void setLayout(){
		
		
		tv_title     = (TextView) findViewById(R.id.tv_title);
		tv_contents  = (TextView) findViewById(R.id.tv_contents);
		btn_cancel  = (Button) findViewById(R.id.btn_cancel);
		btn_ok  = (Button) findViewById(R.id.btn_ok);
		
		
		if( ALERT == ALERT_CANCEL ){
			btn_ok.setVisibility(View.GONE);
//			btn_dialog.setText("취소");
		}
		
		if( ALERT == ALERT_OK ){
			btn_cancel.setVisibility(View.GONE);
//			btn_dialog.setText("확인");
		}
		
		if( ALERT == ALERT_OKANDCANCEL ){
			btn_ok.setVisibility(View.VISIBLE);
			btn_cancel.setVisibility(View.VISIBLE);
		}
		
		btn_cancel.setOnClickListener(onclick);
		btn_ok.setOnClickListener(onclick);
	}
	
	
	private View.OnClickListener onclick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_cancel:
				dismiss();
				if(fcallback != null){
					fcallback.onPressButton(ALERT_CANCEL);
				}
				break;
			case R.id.btn_ok:
				dismiss();
				if(fcallback != null){
					fcallback.onPressButton(ALERT_OK);
				}
				break;
			}
		}
	};
	
	
	@Override
	protected void onStop() {
 		// TODO Auto-generated method stub
		super.onStop();
		dismiss();
	}
	
	public interface CallBack {
		public void onPressButton(int btnIndex);
	}
	
	
	
	
	
	private static AlertDialog dialog =  null;
	/**
	 * 
	 * @param mContext
	 * @param title     팝어타이틀
	 * @param items     팝업에 출력될 리스트
	 * @param callback  Event 처리 CallBack
	 * @param cancel    하드백버턴으로 팝업창 닫기
	 */
	public static void showPop(Activity mContext, String title, String[] items,  final CallBack callback,boolean cancel) {
		showPop(mContext,title,items,-1,callback,cancel);
	}
	public static void showPop(Activity mContext, String title, String[] items,  final CallBack callback) {
		showPop(mContext,title,items,-1,callback,false);
	}
	
	
	/**
	 * 
	 * @param mContext
	 * 
	 * @param title     팝어타이틀
	 * @param items     팝업에 출력될 리스트
	 * @param type      Event 처리 index
	 * @param callback  Event 처리 CallBack
	 * @param cancel    하드백버턴으로 팝업창 닫기
	 */
	public static void showPop(Activity mContext, String title, String[] items, int type, final CallBack callback,boolean cancel) {
		
		if(mContext instanceof Activity) {
			if(((Activity) mContext).isFinishing()) {
				return;
			}
		}
		
		final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
		alert.setTitle(title);
		alert.setCancelable(cancel);
		alert.setItems(items,new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				callback.onPressButton(which);
				dialog.dismiss();
			}
		});
		
		EventListener(alert,callback,type);
	}
	
	
	public static void EventListener(final AlertDialog.Builder alert,final CallBack callback,int type){
		
//		alert.setMessage(msg);
		switch (type) {
		case ALERT_OKANDCANCEL:
			alert.setPositiveButton(btn_1, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface d, int w) {
					if(callback != null) {
						callback.onPressButton(ALERT_OK);
					}
					d.dismiss();
				}
			});
			alert.setNegativeButton(btn_2, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface d, int w) {
					if(callback != null) {
						callback.onPressButton(ALERT_CANCEL);
					}
					d.dismiss();
				}
			});
			break;
		case ALERT_USERDEFINE:
			if(!TextUtils.isEmpty(btn_1)) {
				alert.setPositiveButton(btn_1, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int w) {
						if(callback != null) {
							callback.onPressButton(ALERT_OK);
						}
						d.dismiss();
					}
				});
			}
			if(!TextUtils.isEmpty(btn_2)) {
				alert.setNegativeButton(btn_2, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int w) {
						if(callback != null) {
							callback.onPressButton(ALERT_CANCEL);
						}
						d.dismiss();
					}
				});
			}
			break;
			
		case ALERT_CLOSEANDRETRAYANDSETTING:
			
			if(!TextUtils.isEmpty(btn_5)) {
				alert.setPositiveButton(btn_5, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int w) {
						if(callback != null) {
							callback.onPressButton(ALERT_RETRAY);
						}
						d.dismiss();
					}
				});
			}
			if(!TextUtils.isEmpty(btn_6)) {
				alert.setNeutralButton(btn_6, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int w) {
						if(callback != null) {
							callback.onPressButton(ALERT_SETTING);
						}
						d.dismiss();
					}
				});
			}
			if(!TextUtils.isEmpty(btn_4)) {
				alert.setNegativeButton(btn_4, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int w) {
						if(callback != null) {
							callback.onPressButton(ALERT_CLOSE);
						}
						d.dismiss();
					}
				});
			}
			break;	
		case ALERT_OK:
			alert.setPositiveButton(btn_1, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface d, int w) {
					if(callback != null) {
						callback.onPressButton(ALERT_OK);
					}
					d.dismiss();
				}
			});
			break;
		case ALERT_CANCEL:
			alert.setPositiveButton(btn_2, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface d, int w) {
					if(callback != null) {
						callback.onPressButton(ALERT_CANCEL);
					}
					d.dismiss();
				}
			});
			break;
		case ALERT_BACK:
			alert.setPositiveButton(btn_3, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface d, int w) {
					if(callback != null) {
						callback.onPressButton(ALERT_BACK);
					}
					d.dismiss();
				}
			});
			break;
		default:
			break;
		}
		dialog = alert.create();
		dialog.show();
	}
	
}
