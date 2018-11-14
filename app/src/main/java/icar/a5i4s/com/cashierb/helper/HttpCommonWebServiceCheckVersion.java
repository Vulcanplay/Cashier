package icar.a5i4s.com.cashierb.helper;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class HttpCommonWebServiceCheckVersion extends WebServiceBase {

	public HttpCommonWebServiceCheckVersion() {
		super(null);
	}

	public HttpCommonWebServiceCheckVersion(IHttpJSON2Event eventHandler) {
		super(eventHandler, GlobalConfiguration.getInstance()
				.getWebServiceHost());
	}

	public HttpCommonWebServiceCheckVersion(IHttpJSON2Event eventHandler,
			String hostName) {
		super(eventHandler, hostName, 6000);
	}

	public HttpCommonWebServiceCheckVersion(IHttpJSON2Event eventHandler,
											String hostName, int timeOutInSeconds) {
		super(eventHandler,hostName,timeOutInSeconds);
	}
	@Override
	protected String getWebServcieName() {
		return "CommonWebService.asmx";
	}

	@Override
	protected String getWebMethodName() {
		return "GetApkVersion";
	}

	@Override
	protected List<NameValuePair> getNameValuePairSendData(
			JSONObject jsonParameters) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		try{
			nameValuePairs.add(new BasicNameValuePair("appType", jsonParameters
					.getString("appType")));
		}catch(Exception e){
			
		}		
		return nameValuePairs;
	}
}
