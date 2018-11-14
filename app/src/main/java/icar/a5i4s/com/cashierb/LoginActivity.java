package icar.a5i4s.com.cashierb;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ums.AppHelper;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import icar.a5i4s.com.cashierb.helper.Logg;
import icar.a5i4s.com.cashierb.helper.Tools;
import icar.a5i4s.com.cashierb.helper.WebServicesTool;
import icar.a5i4s.com.cashierb.module.ResultData;
import okhttp3.Call;

public class LoginActivity extends Activity implements View.OnClickListener {

    //获取本地存储
    protected SharedPreferences sf;
    protected WebServicesTool webServicesTool;
    protected EditText userName;
    protected EditText password;
    protected Button signInButton;
    protected ImageView boLogo;
    protected TextView version;
    Intent intent;

    private List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        intent = new Intent();
        intent.setClass(LoginActivity.this, Navigation.class);
        sf = getSharedPreferences("userId", Activity.MODE_PRIVATE);
        if(!sf.getString("id", "").equals("") &&
           !sf.getString("loginShopId", "").equals("")&&
           !sf.getString("payTopAmt", "").equals("")){
            startActivity(intent);
        } else {
            init();
        }
    }

    protected void init(){
        webServicesTool = new WebServicesTool(this);
        userName = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        boLogo = (ImageView) findViewById(R.id.bo_logo);
        version = (TextView) findViewById(R.id.version);
        signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);
        boLogo.setOnClickListener(this);
        version.setText(Tools.getVersion(getApplicationContext()));
    }
    //表单验证
    protected boolean checkForm(String userName, String password){
        if (userName.equals("")){
            this.userName.requestFocus();
            return false;
        } else if(password.equals("")){
            this.password.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    //获取SN号
    protected String getSN(){
        SharedPreferences sf = getSharedPreferences("snCode", Activity.MODE_PRIVATE);
        if(sf.getString("code", "").equals("")){
            String string = AppHelper.getBaseSysInfo(this);
            Logg.i("=====getBaseSysInfo", string);
            Map<String, Object> map = new GsonBuilder().create().fromJson(string, new TypeToken<Map<String, Object>>() {}.getType());
            if(!map.get("SN").equals("")){
                SharedPreferences sp;
                SharedPreferences.Editor editor;
                //存储到 XML
                sp = getApplicationContext().getSharedPreferences("snCode", 0);
                editor = sp.edit();
                editor.putString("code", map.get("SN").toString());
                editor.commit();
            }
            Logg.i("=====SN :" , map.get("SN").toString());
        }
        return sf.getString("code", "");
    }

    protected MaterialDialog dialog;
    String us = "";
    String pw = "";
    int debugOrRelease;
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sign_in_button:
                us = userName.getText().toString();
                pw = password.getText().toString();
                if (checkForm(userName.getText().toString(), password.getText().toString())){
                    dialog = new MaterialDialog.Builder(this)
                            .content(R.string.please_wait)
                            .progress(true, 0)
                            .show();
                    webServicesTool.Connect("doAndroidPayLogin.do")
                            .addParams("posSn", getSN())
                            .addParams("userName", us)
                            .addParams("pw", pw)
                            .build().execute(new StringCallback() {

                        @Override
                        public void onError(Call call, Exception e, int id) {
                            dialog.dismiss();
                            new MaterialDialog.Builder(getApplicationContext())
                                    .title(getString(R.string.alert))
                                    .content(getString(R.string.message_network))
                                    .positiveText(getString(R.string.payResultClose));
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Logg.i("====登录成功", response);
                            ResultData resultData = new Gson().fromJson(response, ResultData.class);
                            if (resultData.getSuccess()){
                                data = (List<Map<String, Object>>) resultData.getDataList();
                                Map<String, String> map = new Gson().fromJson(resultData.getCode(), new TypeToken<Map<String, String>>() {}.getType());
                                String userId = map.get("userId");
                                String payTopAmt = map.get("payTopAmt");
                                String loginShopId = map.get("loginShopId");
                                String scanUrl = map.get("scanUrl");
                                String appName = map.get("appName");
                                String shopName = data.get(0).get("shopName").toString();
                                Log.i("====Login ", userId + "\n"+
                                        payTopAmt + "\n"+
                                        loginShopId + "\n"+
                                        scanUrl + "\n");
                                SharedPreferences sp;
                                SharedPreferences.Editor editor;
                                //存储到 XML
                                sp = getApplicationContext().getSharedPreferences("userId", 0);
                                editor = sp.edit();
                                editor.putString("id", userId);
                                editor.putString("payTopAmt", payTopAmt);
                                editor.putString("loginShopId", loginShopId);
                                editor.putString("shopName", shopName);
                                editor.putString("scanUrl", scanUrl);
                                editor.putString("appName", appName);
                                editor.commit();
                                startActivity(intent);
                            }else{
                                new MaterialDialog.Builder(LoginActivity.this)
                                        .title(getString(R.string.alert))
                                        .content(resultData.getErrorMassge())
                                        .positiveText(getString(R.string.payResultClose))
                                        .show();
                            }
                            dialog.dismiss();
                        }
                    });
                } else {
                    new MaterialDialog.Builder(LoginActivity.this)
                            .title(getString(R.string.alert))
                            .content(getString(R.string.sign_not_null))
                            .positiveText(getString(R.string.payResultClose))
                            .show();
                }
                break;
            case R.id.bo_logo:
                sf = getSharedPreferences("setting", Activity.MODE_PRIVATE);

                if (sf.getBoolean("debugPower", false)){
                    debugOrRelease = 0;
                }else if(sf.getBoolean("releasePower", false)) {
                    debugOrRelease = 1;
                }
                //将mHints数组内的所有元素左移一个位置
                System.arraycopy(mHints, 1, mHints, 0, mHints.length - 1);
                //获得当前系统已经启动的时间
                mHints[mHints.length - 1] = SystemClock.uptimeMillis();
                if (SystemClock.uptimeMillis() - mHints[0] <= 1000){
                        dialog = new MaterialDialog.Builder(this)
                                .title(R.string.change_dorr)
                                .items(R.array.change)
                                .cancelable(false)
                                .itemsCallbackSingleChoice(debugOrRelease, new MaterialDialog.ListCallbackSingleChoice() {

                                    @Override
                                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                        SharedPreferences.Editor editor = sf.edit();
                                        if (text.toString().equals(getString(R.string.debug))){
                                            editor.putBoolean("debugPower", true);
                                            editor.putBoolean("releasePower", false);
                                            debugOrRelease = 0;
                                        } else if (text.toString().equals(getString(R.string.release))){
                                            editor.putBoolean("debugPower", false);
                                            editor.putBoolean("releasePower", true);
                                            //修改变量
                                            debugOrRelease = 1;
                                        }
                                        editor.commit();
                                        if (sf.getBoolean("debugPower", false)){
                                            Toast.makeText(getApplicationContext(), "====DEBUG====\n" + sf.getString("typeDebug", ""), Toast.LENGTH_SHORT).show();
                                            Logg.i("====测试环境开启", sf.getString("typeDebug", ""));
                                        } else if(sf.getBoolean("releasePower", false)) {
                                            Toast.makeText(getApplicationContext(), "====RELEASE====\n" + sf.getString("typeRelease", ""), Toast.LENGTH_SHORT).show();
                                            Logg.i("====正式环境开启", sf.getString("typeRelease", ""));
                                        }
                                        new WebServicesTool(getApplicationContext());
                                        return false;
                                    }
                                })
                                .positiveText(getString(R.string.confirm))
                                .show();
                }
                break;
        }
    }
    //需要监听几次点击事件数组的长度就为几
    //如果要监听双击事件则数组长度为2，如果要监听3次连续点击事件则数组长度为3...
    long[] mHints = new long[3];//初始全部为0

    @Override
    protected void onStop() {
        super.onStop();
        this.finish();
    }
}

