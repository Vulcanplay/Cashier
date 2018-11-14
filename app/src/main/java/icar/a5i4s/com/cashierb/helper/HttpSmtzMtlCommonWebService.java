package icar.a5i4s.com.cashierb.helper;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HttpSmtzMtlCommonWebService extends WebServiceBase {

	public HttpSmtzMtlCommonWebService() {
		super(null);
	}

	public HttpSmtzMtlCommonWebService(IHttpJSON2Event eventHandler) {
		super(eventHandler, GlobalConfiguration.getInstance()
				.getWebServiceHost());
	}

	public HttpSmtzMtlCommonWebService(IHttpJSON2Event eventHandler,
			String hostName) {
		super(eventHandler, hostName, 6000);
	}

	public HttpSmtzMtlCommonWebService(IHttpJSON2Event eventHandler,
									   String hostName, int timeOutInSeconds) {
		super(eventHandler, hostName, timeOutInSeconds);
	}

	@Override
	protected String getWebServcieName() {
		return "CommonWebService.asmx";
	}

	@Override
	protected String getWebMethodName() {

		return "doInsertTempChartNew1";
	}

	@Override
	protected List<NameValuePair> getNameValuePairSendData(
			JSONObject jsonParameters) {
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("data", jsonParameters
					.getString("tzData")));
			return nameValuePairs;
		} catch (Exception e) {
			return null;
		}
	}

}