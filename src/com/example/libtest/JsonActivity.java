package com.example.libtest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.smarttech.common.SmtAlertDialogHelper;
import com.smarttech.conf.Config;
import com.smarttech.request.JsonRequest;
import com.smarttech.request.listener.JsonDataListener;
import com.smarttech.request.server.Connection;
import com.smarttech.request.server.ServerRequest;

/*
 * JSON Data요청 샘플
 */
public class JsonActivity extends BaseActivity {

	ListView json = null;
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.json);
		json = (ListView)findViewById(R.id.json);
		workListAdapter = new SmtechAdapter();
		json.setAdapter(workListAdapter);
		
		
		//서버에 JSON형식으로 요청함
		requestJson();
	}
	
	
	private SmtechAdapter workListAdapter;
	/**
	 * 응답처리 CallBack
	 */
	final class ResponseCallback extends JsonDataListener {

        public ResponseCallback(){
            super(JsonActivity.this);
        }

        /**
         * 응답결과 처리함
         * 업무별로 처리로직 구현하는곳입니다.
         */
        public void requestCompleted(ServerRequest req) {
        	
        	/*
        	* 주의 (로딩중 다이얼로그 닫기 )
    		*/
        	hideProgress();
        	super.requestCompleted(req);
        	
        	/*
        	 * Json형식 응답을 모듈에서 HashMap으로(key=value)형식으로 자동 파싱처리를 합니다. 
        	 * Json형식을 따로 파싱할필요가 없으며 
        	 * HashMap에서 꺼집어 내어서 사용하시면 됩니다.
        	 */
        	final HashMap resultMap = req.getValue();
        	Log.d(Config.TAG,resultMap.toString());
        	
            if(resultMap == null || resultMap.isEmpty()){
            	
            	new SmtAlertDialogHelper(JsonActivity.this
						,"알림"
						,"에러 발생했습니다."
						,SmtAlertDialogHelper.ALERT_OK).show();
            	return;
            }
            
            
            /* json값을 꺼집어내어 사용하는 예제입니다. 
             * TEST_RESULT_CODE ,TEST_RESULT_MSG 서버에서 응답으로넘겨준 key값들 
             * 서버단과 상의가 필요한부분입니다.
             */
            final String RESULT_CODE = (String) resultMap.get("TEST_RESULT_CODE");
			final String RESULT_MSG  = (String) resultMap.get("TEST_RESULT_MSG");
			
			//로직처리 
			if("9999".equals(RESULT_CODE)){
				//생략 
			}
			
			//테스터 샘플 
			final HashMap<String, Object> memsStat = (HashMap<String, Object>) resultMap.get("TEST_KEY");
			
			//테스터 샘플 (ArrayList 리스터형식) 
			final ArrayList<HashMap> resultList = (ArrayList<HashMap>) resultMap.get("TEST_list");
           
        	//결과값으로 로직구현처리 해주세요 
        }
        
        /**
    	 * 실패 처리함 
    	 * 실패 처리는 공통으로 모듈안에 숨겨져 있습니다. 따로 메소드 선언해서 구현하지 않아도 되지만 
    	 * Alert UI처리때문에 약간 수정이 필요합니다.
    	 * 
    	 * 구현이 필요없으시면 메소드 자체를 제거해주세요! 
    	 */
        @Override
        public void requestFailed(ServerRequest req, Throwable ex) {
            /*
        	* 주의 (로딩중 다이얼로그 닫기 )
    		*/
        	hideProgress();
        	
        	new SmtAlertDialogHelper(JsonActivity.this
					,"알림"
					,"이론이론 에러입니다."
					,SmtAlertDialogHelper.ALERT_OKANDCANCEL,new ErrorCallBack()).show();
        }
    }
	
	
	
	class ErrorCallBack implements SmtAlertDialogHelper.CallBack
	{
		public void onPressButton(int btnIndex){
			if(SmtAlertDialogHelper.ALERT_OK == btnIndex)
			{
				finish();
			}
		}
	}
	/*
	 * 서버에 요청 호출함
	 */
	public void requestJson(){
		/*
		* 진행바를 노출함 
		* 주의: CallBack class에서는 응답을 받는메소드에서 열린 다이얼로그를 닫아주어야 합니다.
		* 닫지 않으면 App이 계속 Lock 된 상태여서 사용자가 보기에는 서버에서 응답이 없는것 처럼 느껴지며 앱이 멈춘걸로 인지하게 됩니다.
		*/
		showProgress("로딩중...");
		
		/*
		 * HashMap으로 서버에 던질 파라메터값을 담는다.
		 * 형식은 key=value
		 * getJsonParam 에서 JSON형식으로 변환처리함 
		 * 
		 * 주의: 
		 * 서버에서는 JSON형식으로 받을준비를 해야합니다.
		 * App 에서 JSON으로 던지고 서버단에서 JSON형식으로 받지 않으면 서버단에서 
		 * 파라메터 값을 구분하기가 어렵습니다.(참고바랍니다.)
		 */
		final HashMap<String, String>  params = new HashMap<String, String>();
	    params.put("USER_ID", "testId");
        params.put("USER_PASSWORD", "d8578edf8458ce06fbc5bb76a58c5ca4");
        
        
        
        /* ========================================================
         * 주의)
         * 파라메터중에 URL도메인 주소는 환경설정 class에 설정해야 합니다.
         * Config.java 소스참조 
         * "https" 통신할 경우  Config.java 소스중 PROTOCOL값을 변경 해 주셔야 합니다.
         * 서버담당장에게 물어보세요 
         * ========================================================
         */
		final JsonRequest request = new JsonRequest(new ResponseCallback(),getJsonParam(params),"/test/json.do");
        Connection.getInstance().enqueueRequest(request);
	}
	
	
	/*
	 * Adapter에서 사용할 ArrayList
	 */
	private ArrayList<HashMap<String,Object>> paymentInfo = new ArrayList<HashMap<String,Object>>();
	/*
	 * Adpater 처리함
	 */
	public class SmtechAdapter extends ArrayAdapter<HashMap<String,Object>> {

		private final static int id = R.layout.json_list;
	

		public SmtechAdapter() {
			super(JsonActivity.this, id, paymentInfo);
		}

		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			final HashMap<String,Object> map = paymentInfo.get(position);
			final ViewHolder holder;
			if (convertView == null) {
				
				final LayoutInflater layoutInflater = LayoutInflater.from(JsonActivity.this);
				convertView = layoutInflater.inflate(id, null);
				holder = new ViewHolder();
				holder.key      = (TextView) convertView.findViewById(R.id.key);
				holder.value      = (TextView) convertView.findViewById(R.id.value);
				
			
				convertView.setTag(holder);
				
			} else {
				
				holder = (ViewHolder) convertView.getTag();

			}
		
			final Iterator iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				final Entry<String,Object> entry = (Entry<String,Object>) iterator.next();
				final String key= entry.getKey();
				
				holder.key.setText(key);
				Object obj = map.get(key);
				if(obj  instanceof HashMap){
					HashMap fmap = (HashMap)obj;
					holder.value.setText(fmap.toString());
				}else {
					holder.value.setText((String)obj);
				}
				
			}
			
			return convertView;
		}

		class ViewHolder {
 
			TextView key;    //동적으로 추가되는 카드사목록
			TextView value;    //은행명
			
		}
	}
	
}
