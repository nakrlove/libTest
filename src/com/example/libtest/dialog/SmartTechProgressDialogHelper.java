package com.example.libtest.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.libtest.R;
import com.smarttech.conf.Config;
import com.smarttech.support.UIThread;

public class SmartTechProgressDialogHelper extends Dialog {
	  public static String btn_1 = "OK";     /* OK(확인) */
	    public static String btn_2 = "Cancel"; /* Cancel(취소) */

	    public static void setButtonText(int type, String btnText) {
	        switch (type) {
	            case ALERT_OK:
	                btn_1 = btnText;
	                break;
	            case ALERT_CANCEL:
	                btn_2 = btnText;
	                break;
	        }
	    }

	    final static public int ALERT_DEFAULT     = -1;
	    final static public int ALERT_CANCEL      = 0;
	    final static public int ALERT_OK          = 1;
	    final static public int ALERT_OKANDCANCEL = 2;

	    /*
	     * Layout
	     */
	    private TextView tv_title;    // 타이틀
	    private Button cancel;    // 취소
	    private Button ok;        // 확인
	    private String mTitle;
	    private SmartTechProgressBar pb = null;
	    
	    protected CallBack fcallback;
	    private int ALERT = ALERT_DEFAULT;

	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
	        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
	        lpWindow.dimAmount = 0.8f;
	        getWindow().setAttributes(lpWindow);

	        getWindow().setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER);
	        setContentView(R.layout.progress_dialog);
	        setLayout();
	        setTitle(mTitle);
	    }

	    public SmartTechProgressDialogHelper(Context context) {
	        // Dialog 배경을 투명처리해준다.
	        super(context, android.R.style.Theme_Translucent_NoTitleBar);
	    }

//	    public PentaProgressDialogHelper(Context context, String title) {
//	        super(context, android.R.style.Theme_DeviceDefault_Light);
//	        this.mTitle = title;
//	    }
	    public SmartTechProgressDialogHelper(Context context, String title, int ALERT) {
	    	super(context, android.R.style.Theme_Translucent_NoTitleBar);
	    	this.mTitle = title;
	    	this.ALERT = ALERT;
	    }

	    public SmartTechProgressDialogHelper(Context context, String title,int ALERT, CallBack fcallback) {
	        super(context, android.R.style.Theme_Translucent_NoTitleBar);
	        this.mTitle = title;
	        this.ALERT = ALERT;
	        this.fcallback = fcallback;
	    }

	    private void setTitle(String title) {
	        tv_title.setText(title);
	    }

	    
	    
	    private RelativeLayout sending = null;
	    private RelativeLayout sendFail = null;
	    
	    /**
	     * 전송실패 화면전환
	     */
	    public void setFailLayout(){
	    	sending  = (RelativeLayout) findViewById(R.id.sending);
	    	sendFail = (RelativeLayout) findViewById(R.id.sendFail);
	    	sending.setVisibility(View.GONE);
	    	sendFail.setVisibility(View.VISIBLE);
	    	ok.setVisibility(View.VISIBLE);
	    	cancel.setVisibility(View.VISIBLE);
	        cancel.setOnClickListener(onclick);
	        ok.setOnClickListener(onclick);
	    }
	    
	    /**
	     * 전송중인 화면전환
	     */
	    public void setSendLayout(){
	    	
	    	sending  = (RelativeLayout) findViewById(R.id.sending);
	    	sendFail = (RelativeLayout) findViewById(R.id.sendFail);
	    	sending.setVisibility(View.VISIBLE);
	    	sendFail.setVisibility(View.GONE);
	    	cancel.setVisibility(View.VISIBLE);
	    	ok.setVisibility(View.GONE);
	        cancel.setOnClickListener(onclick);
	    	
	    }
	    

	 
	    /*
	     * Layout
	     */
	    private void setLayout() {

	        pb       = (SmartTechProgressBar) findViewById(R.id.pb);
	        tv_title = (TextView) findViewById(R.id.sendingText);
	        cancel   = (Button) findViewById(R.id.cancel);
	        ok       = (Button) findViewById(R.id.ok);

	        if (ALERT == ALERT_DEFAULT) {
	        	ok.setVisibility(View.GONE);
	        	cancel.setVisibility(View.GONE);
	        	return;
	        }

	        if (ALERT == ALERT_CANCEL) {
	        	ok.setVisibility(View.GONE);
	        }

	        if (ALERT == ALERT_OK) {
	        	cancel.setVisibility(View.VISIBLE);
	        }

	        
	        if (ALERT == ALERT_OKANDCANCEL) {
	        	ok.setVisibility(View.VISIBLE);
	        	cancel.setVisibility(View.VISIBLE);
	        }

	        cancel.setOnClickListener(onclick);
	        ok.setOnClickListener(onclick);
	    }

	    /**
	     * Progress 진행처리함
	     * @param myProgress
	     */
	    public void setProgressValues(int myProgress){
	        pb.setProgress(myProgress);
	        pb.setText(myProgress + "%");
	    }
	    
	    /**
	     * Progress 진행처리함
	     * @param myProgress
	     */
	    public void setProgressValues(int myProgress,boolean run){
	    	pb.setProgress(myProgress);
	    }
	    
	    /**
	     *  Progress 진행처리함
	     * @param myProgress
	     * @param msg
	     */
	    public void setProgressValues(int myProgress,String msg){
	    	pb.setProgress(myProgress);
	    	pb.setText(msg+"  "+myProgress + "%");
//	    	setTitle(msg);
	    }


		private View.OnClickListener onclick = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.cancel:
					
					dismiss();
					if (fcallback != null) {
						fcallback.onPressButton(ALERT_CANCEL);
					}
					break;
				case R.id.ok:
					UIThread.newInstance().executeInUIThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if (fcallback != null) {
								fcallback.onPressButton(ALERT_OK);
							}
						}
					},10);
					
					if(sending.isShown()){
						dismiss();
					}
					break;
				}
			}
		};


	    public interface CallBack {
	        public void onPressButton(int btnIndex);
	    }
}
