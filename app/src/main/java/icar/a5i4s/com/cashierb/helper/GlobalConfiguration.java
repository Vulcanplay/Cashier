package icar.a5i4s.com.cashierb.helper;


import android.content.Context;

public class GlobalConfiguration extends Object {

	// private Map<String, String> mTable = new HashMap<String, String>();

	private static GlobalConfiguration mInstance = null;

	public static GlobalConfiguration getInstance() {
		if (mInstance == null) {
			mInstance = new GlobalConfiguration();
		}
		return mInstance;
	}

	public boolean isUseLocal() {
//		return PreferenceManager.getDefaultSharedPreferences(this.ctx)
//				.getBoolean(ctx.getString(R.string.key_use_local), true);
		return false;
	}
	public boolean isSaveLocal() {
//		return PreferenceManager.getDefaultSharedPreferences(this.ctx)
//				.getBoolean(ctx.getString(R.string.key_save_local), false);
		return false;
	}
	public String getWebServiceHost(){
//		int index = ctx.getResources().getInteger(R.integer.default_webservice_index);
//		return PreferenceManager
//		.getDefaultSharedPreferences(this.ctx)
//		.getString(
//				ctx.getString(R.string.key_webservice_host),
//				ctx.getResources().getStringArray(R.array.host_value)[index]);
		return "http://180.76.158.206:81";
	}
	

	Context ctx;

	public void init(Context ctx) {
		this.ctx = ctx;
	}

}
