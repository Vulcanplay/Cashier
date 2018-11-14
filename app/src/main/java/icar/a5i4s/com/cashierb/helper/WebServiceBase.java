package icar.a5i4s.com.cashierb.helper;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;


public abstract class WebServiceBase {
	private String hostName = "";
	private int timeOut = 60000;
	public IHttpJSON2Event eventHandler;

	protected WebServiceBase() {
		this(null);
	}

	protected WebServiceBase(IHttpJSON2Event eventHandler) {
		this(eventHandler, GlobalConfiguration.getInstance()
				.getWebServiceHost());
	}

	protected WebServiceBase(IHttpJSON2Event eventHandler,
			String hostName) {
		this(eventHandler, hostName, 6000);
	}

	protected WebServiceBase(IHttpJSON2Event eventHandler,
							 String hostName, int timeOutInSeconds) {
		this.eventHandler = eventHandler;
		this.hostName = hostName;
		this.setTimeOut(timeOutInSeconds);
	}

	public void setHttpJSON2Event(IHttpJSON2Event eventHandler) {
		this.eventHandler = eventHandler;
	}

	public void setTimeOut(int seconds) {
		this.timeOut = seconds * 1000;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/***
	 * 得到WEB服务的名称
	 * @return WEB服务的名称
	 */
	protected  abstract String getWebServcieName();
	/***
	 * 得到WEB服务的方法名称 
	 * @return WEB服务的方法名称
	 */
	protected  abstract String getWebMethodName();
	/***
	 * 生成数据参数（键值对的形式）
	 * @param jsonParameters 可使用的JSON发数数据
	 * @return 数据参数（键值对的形式）
	 */
	protected abstract List<NameValuePair> getNameValuePairSendData(JSONObject jsonParameters);
	
	protected JSONObject parseResultData(String result)
			throws JSONException {
		// int inndex
		// =result.indexOf("<string xmlns=\"http://Sunway.Mobile.WebService.App/\">")+"<string xmlns=\"http://Sunway.Mobile.WebService.App/\">".length();
		// 前93位不要
		return new JSONObject(result);// .getJSONObject("d");
		// resultJSON = new JSONObject(result).getJSONObject("d");
	}
	/***
	 * 取得指定业务功能的数据
	 * 
	 * @param jsonParameters
	 *            参数JSON
	 * @param opCode
	 *            业务操作码
	 * @return JSON数据
	 */
	public JSONObject ProcessPadOperationByJson(JSONObject jsonParameters,
												String opCode) {
		JSONObject resultJSON = null;
		try {
		//	resultJSON =readLocalData(opCode, jsonParameters);
			if(resultJSON==null){
				String strUrl = hostName + "/"+getWebServcieName()+"/"+getWebMethodName();
				HttpPost request = new HttpPost(strUrl);
				request.setHeader("Accept", "application/json");
			    request.setHeader("Content-type", "application/json");

				// 修改成用原始方式，解决数据太多没法转成JSON,StatusCode=500的问题
				// JSON 方式
				// request.addHeader("Content-Type",
				// "application/json; charset=utf-8");
				// JSONObject entity = new JSONObject();
				// entity.put("jsonData", jsonParameters.toString());
				// HttpEntity bodyEntity = new StringEntity(entity.toString(),
				// "utf8");
				// request.setEntity(bodyEntity);
				// 原始方式（XML） 参数列表 方式
				List<NameValuePair> nameValuePairs = getNameValuePairSendData(jsonParameters);
				//request.setEntity(new UrlEncodedFormEntity(nameValuePairs, "utf8"));
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, timeOut);
				HttpConnectionParams.setSoTimeout(httpParameters, timeOut);
				JSONObject s = new JSONObject();
				s.put("userId", "xiaotao");
				s.put("password", "12345678");
				HttpEntity bodyEntity = new StringEntity(s.toString(),"utf8");
				request.setEntity(bodyEntity);
				HttpResponse httpResponse = new DefaultHttpClient(httpParameters)
						.execute(request);
				if (httpResponse.getStatusLine().getStatusCode() != 404) {
					String result = EntityUtils.toString(httpResponse.getEntity());
					// int inndex
					// =result.indexOf("<string xmlns=\"http://Sunway.Mobile.WebService.App/\">")+"<string xmlns=\"http://Sunway.Mobile.WebService.App/\">".length();
					// 前93位不要
//					resultJSON = new JSONObject(result.substring(93,
//							result.length() - 9));// .getJSONObject("d");
					// resultJSON = new JSONObject(result).getJSONObject("d");
					resultJSON = parseResultData(result);
					writeDataToLocalFile(opCode, jsonParameters, resultJSON);
				}
			}
			
		} catch (Exception e) {
			if (eventHandler != null) {
				eventHandler.finishedWithException(opCode, e);
			}
		}
		return resultJSON;
	}
	/***
	 * 取得指定业务功能的数据 ，业务操作码和业务功能编码一致
	 * @param jsonParameters 参数JSON
	 * @return JSON数据
	 */
	public JSONObject ProcessPadOperationByJson(JSONObject jsonParameters) {
		JSONObject resultJSON = null;
		String opCode="";
		try {
			opCode =jsonParameters.getString("OperatorCode");
			resultJSON=ProcessPadOperationByJson(jsonParameters,opCode);
		} catch (Exception e) {
			if (eventHandler != null) {
				eventHandler.finishedWithException(opCode, e);
			}
		}
		return resultJSON;
	}
	public void ProcessPadOperationByJsonAsync(final JSONObject jsonParameters)
	throws Exception {
		if (this.eventHandler == null)
			throw new Exception("Async Methods Requires IHttpJSON2Event");
		final String OpCode = jsonParameters.getString("OperatorCode");
		ProcessPadOperationByJsonAsync(jsonParameters,OpCode);
	}
	public void ProcessPadOperationByJsonAsync(final JSONObject jsonParameters,
			final String opCode) throws Exception {
		if (this.eventHandler == null)
			throw new Exception("Async Methods Requires IHttpJSON2Event");
		final String OpCode = opCode;
		new AsyncTask<Void, Void, JSONObject>() {
			@Override
			protected void onPreExecute() {
				eventHandler.startedRequest();
			};

			@Override
			protected JSONObject doInBackground(Void... params) {
				return ProcessPadOperationByJson(jsonParameters, OpCode);
			}

			@Override
			protected void onPostExecute(JSONObject result) {
				int code = -9999;
				if (result != null) {
					try {
						JSONObject processCode = result
								.getJSONObject("ProcessCodeInfo");
						code = processCode.getInt("Code");
						String message = processCode.getString("Message");
						if (code == 0) {
							eventHandler.processNormalCode(OpCode, code,
									message);
							JSONArray dataArr = result
									.optJSONArray("ProcessDataResult");
							if (dataArr == null || dataArr.length() == 0) {
								eventHandler.finishedNoData(OpCode);
							} else {
								eventHandler.finishedProcessData(OpCode,
										dataArr);
							}
						} else {
							eventHandler.processWarnCode(OpCode, code, message);
						}

					} catch (Exception ex) {
						code = -9999;
						eventHandler.finishedWithException(OpCode, ex);
					}
				}
				eventHandler.endedRequest(OpCode, code);
			}
		}.execute();
	}
	private String getLocalFileName(String opCode, JSONObject jsonParameters){
		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(jsonParameters.toString().getBytes());
			byte messageDigest[] = algorithm.digest();
			StringBuffer hex = new StringBuffer();
			//hex.append(NurseApp.LOCAL_PATH);
			hex.append("/");
			hex.append(opCode);
			hex.append("_");
			for (int i = 0; i < messageDigest.length; i++) {
				hex.append(Integer.toHexString(0xFF & messageDigest[i]));
			}			
			return hex.toString();
		} catch (NoSuchAlgorithmException nsae) {
			nsae.printStackTrace();
		}
		return null;
	}
	private JSONObject readLocalData(String opCode, JSONObject jsonParameters){
		// 配置文件指定不使用本地数据
		if (!GlobalConfiguration.getInstance().isUseLocal()){
			return null;
		}			
		String fileName =getLocalFileName(opCode,jsonParameters);
		if(fileName==null){
			return null;
		}
		File f = new File(fileName);
		if(f.exists()==false){
			return null;
		}
		
		int len = 0;
		byte[] data = new byte[1024 * 100];
		StringBuffer buffer = new StringBuffer();
		try {
			InputStream in = new FileInputStream(fileName);
			while ((len = in.read(data)) != -1) {
				buffer.append(new String(data, 0, len));
			}
			return new JSONObject(buffer.toString());
		} catch (FileNotFoundException e) {
			Log.v("VDoctor", "VDoctor: File not found");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch(JSONException e){
			e.printStackTrace();
		}
		return null;
	}
	private void writeDataToLocalFile(String opCode, JSONObject jsonParameters, JSONObject data){
		// 配置文件指定不缓存网络数据
		if (!GlobalConfiguration.getInstance().isSaveLocal()){
			return;
		}
		String fileName =getLocalFileName(opCode,jsonParameters);
		try {
			FileOutputStream out = new FileOutputStream(fileName);
			out.write(data.toString().getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
