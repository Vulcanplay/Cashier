package icar.a5i4s.com.cashierb.helper;

import android.app.Activity;
import android.app.ProgressDialog;

import org.json.JSONArray;
import org.json.JSONObject;


public class HttpUtil {

	public final static String NOT_DATA ="NOTDATA";
	public static void ReqDataFromWebServer(Activity curract, JSONObject data, HttpUtilEvent event){
		
		try{
			String OpCode = data.getString("OperatorCode");
			ReqDataFromWebServer(curract,data,event,OpCode);
		}catch(Exception e){
			if(event != null){
				event.finishedWithErrorOrMsg("ERROR:",e.getMessage());
			}
		}
	}
	public static void ReqDataFromWebServer(Activity curract, JSONObject data, HttpUtilEvent event, String opCode){
		HttpJSON2CommonWebService webServer = new HttpJSON2CommonWebService();
		callWebServer(curract,data,event,opCode,webServer);
	}
	
	public static void checkVerFromWebServer(Activity curract, JSONObject data, HttpUtilEvent event, String opCode){
		HttpCommonWebServiceCheckVersion webServer = new HttpCommonWebServiceCheckVersion();
		callWebServer(curract,data,event,opCode,webServer);
	}
	
	public static void insertTempChartNew1(Activity curract, JSONObject data, HttpUtilEvent event, String opCode){
		HttpSmtzMtlCommonWebService webServer = new HttpSmtzMtlCommonWebService();
		callWebServer(curract,data,event,opCode,webServer);
	}
	
	private static void callWebServer(Activity curract, JSONObject data, HttpUtilEvent event, String opCode, WebServiceBase webServer){
		final Activity currActivity = curract;
		final HttpUtilEvent httpEvent = event;
		final ProgressDialog pd = new ProgressDialog(currActivity);
		pd.setTitle("请稍候");
		pd.setMessage("数据正在处理...");
		pd.setCancelable(true);
		try{			
			webServer.setHttpJSON2Event(new IHttpJSON2Event() {
				@Override
				public void startedRequest() {
					pd.show();
				}
				
				@Override
				public void processWarnCode(String methodName, int code, String message) {
					if(httpEvent != null){
						httpEvent.finishedWithErrorOrMsg(methodName,message);
					}
				}
				
				@Override
				public void processNormalCode(String methodName, int code, String message) {

				}
				
				@Override
				public void finishedWithException(String methodName, Exception ex) {
					if(httpEvent != null){
						httpEvent.finishedWithErrorOrMsg(methodName,ex.getMessage());
					}	
				}
				
				@Override
				public void finishedProcessData(String methodName, JSONArray data) {
					final String name=methodName;
					final JSONArray result =data;
					if(httpEvent != null){
						currActivity.runOnUiThread(new Runnable(){
							@Override
							public void run() {								
								httpEvent.finishedProcessData(name, result);
							}});
					}
				}
				
				@Override
				public void finishedNoData(String methodName) {
					final String name=methodName;
					if(httpEvent != null){
						currActivity.runOnUiThread(new Runnable(){
							@Override
							public void run() {								
								httpEvent.finishedWithErrorOrMsg(name, NOT_DATA);
							}});
					}
				}
				
				@Override
				public void endedRequest(String methodName, int code) {
					pd.dismiss();
				}
			});
			webServer.ProcessPadOperationByJsonAsync(data,opCode);
		}catch(Exception e){
			if(httpEvent != null){
				httpEvent.finishedWithErrorOrMsg("ERROR:",e.getMessage());
			}
		}
	}
	
}