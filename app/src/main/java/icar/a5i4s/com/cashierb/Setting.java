package icar.a5i4s.com.cashierb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ums.AppHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import icar.a5i4s.com.cashierb.dao.HistoryDAO;
import icar.a5i4s.com.cashierb.helper.Logg;
import icar.a5i4s.com.cashierb.helper.WebServicesTool;

public class Setting extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {


    protected Switch debug;
    protected Switch simulate;
    protected Button logout;
    protected TextView exception, wechatReprint,alipayReprint, sign, history;
    protected SharedPreferences sf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.input_setting_title));
        super.setContentView(R.layout.activity_setting);
        initView();
    }

    private void initView() {
        webServicesTool = new WebServicesTool(getApplicationContext());
        debug = (Switch) findViewById(R.id.debug);
        debug.setOnCheckedChangeListener(this);
//        simulate = (Switch) findViewById(R.id.simulate);
//        simulate.setOnCheckedChangeListener(this);
        logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(this);
        exception = (TextView) findViewById(R.id.exception);
        exception.setOnClickListener(this);
        wechatReprint = (TextView) findViewById(R.id.wechat_reprint);
        wechatReprint.setOnClickListener(this);
        alipayReprint = (TextView) findViewById(R.id.alipay_reprint);
        alipayReprint.setOnClickListener(this);
        sign = (TextView) findViewById(R.id.sign);
        sign.setOnClickListener(this);
        history = (TextView) findViewById(R.id.history);
        history.setOnClickListener(this);

//        new WebServicesTool(this);
//        sf = getSharedPreferences("setting", Activity.MODE_PRIVATE);
//
//        if (sf.getBoolean("debugPower", false)){
//            debug.setChecked(false);
//        }
//
//        if(sf.getBoolean("releasePower", false)) {
//            debug.setChecked(true);
//        }
//
//        if (debug.isChecked()){
//            debug.setText("正式环境");
//        } else {
//            debug.setText("测试环境");
//        }
//
//        if(sf.getBoolean("repairPower", false)) {
//            debug.setEnabled(false);
//            simulate.setChecked(true);
//        } else {
//            debug.setEnabled(true);
//            simulate.setChecked(false);
//        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        //存储到 XML
        sp = getSharedPreferences("setting", 0);
        editor = sp.edit();
        switch (buttonView.getId()){
            case R.id.debug:
                if (isChecked){
                    editor.putBoolean("debugPower", false);
                    editor.putBoolean("releasePower", true);
                    editor.putBoolean("repairPower", false);
                    debug.setText("正式环境");
                } else {
                    editor.putBoolean("debugPower", true);
                    editor.putBoolean("releasePower", false);
                    editor.putBoolean("repairPower", false);
                    debug.setText("测试环境");
                }
                Log.d("debugChange", String.valueOf(isChecked));
                break;
//            case R.id.simulate:
//                if (isChecked){
//                    editor.putBoolean("debugPower", false);
//                    editor.putBoolean("releasePower", false);
//                    editor.putBoolean("repairPower", true);
//                    debug.setEnabled(false);
//                }else {
//                    if (debug.isChecked()){
//                        editor.putBoolean("releasePower", true);
//                        editor.putBoolean("debugPower", false);
//                    } else {
//                        editor.putBoolean("releasePower", false);
//                        editor.putBoolean("debugPower", true);
//                    }
//                    editor.putBoolean("repairPower", false);
//                    debug.setEnabled(true);
//                }
//                Log.d("simulateChange", String.valueOf(isChecked));
//                break;
            default:
                break;
        }
        editor.commit();
        if (sf.getBoolean("debugPower", false)){
            Toast.makeText(Setting.this, "====测试环境开启" + sf.getString("typeDebug", ""), Toast.LENGTH_SHORT).show();
            Logg.i("====测试环境开启", sf.getString("typeDebug", ""));
        } else if(sf.getBoolean("releasePower", false)) {
            Toast.makeText(Setting.this, "====正式环境开启" + sf.getString("typeRelease", ""), Toast.LENGTH_SHORT).show();
            Logg.i("====正式环境开启", sf.getString("typeRelease", ""));
        }
    }

    HistoryDAO hDAO = new HistoryDAO(this);

    public void removeException(){
        //1.判断是否有异常数据
        if (hDAO.getErrorCount() != 0){
            //2.取pos机 类型
            //新大陆
            //if (Vendor.equals(getString(R.string.new_load))){
                try {
                    //调用系统接口
                    AppHelper.callTrans(this, getString(R.string.bankCardPay), getString(R.string.bankCardTypeRePrint), new JSONObject("{\"traceNo\":\"000000\"}"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
           //} else {
                //联迪
            //}
        } else {
            //暂无异常数据
            Toast.makeText(this, "暂无异常数据", Toast.LENGTH_LONG).show();
        }

        //3.调用重打印，接受返回结果，匹配本地最后成功数据表，一致：清空异常记录。 不一致：将记录推送icar。更新成功数据表
    }

    MaterialDialog dialog;
    WebServicesTool webServicesTool;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(AppHelper.TRANS_REQUEST_CODE == requestCode){
            Logg.i("RESULT CODE", "" + resultCode);
            if (null != data) {
                Map<String,String> map = AppHelper.filterTransResult(data); //拿到 result的 TransData
                Map<String, String> requestTDMaps = new GsonBuilder().create().fromJson(map.get(AppHelper.TRANS_DATA), new TypeToken<Map<String, String>>() {}.getType());
                if (getString(R.string.bankCardTypeSign).equals(map.get(AppHelper.TRANS_BIZ_ID))){
                    new MaterialDialog.Builder(this)
                            .positiveText(R.string.confirm)
                            .title(R.string.alert)
                            .content(requestTDMaps.get("resDesc"))
                            .show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.logout:
                getSharedPreferences("userId",0).edit().clear().commit();
                startActivity(new Intent(this,LoginActivity.class));
                this.finish();
                break;
            case R.id.exception:
                removeException();
                break;
            case R.id.wechat_reprint:
                startActivity(new Intent(this, WeChatRePrint.class));
                break;
            case R.id.alipay_reprint:
                startActivity(new Intent(this, AliPayRePrint.class));
                break;
            case R.id.sign:
                try {
                    AppHelper.callTrans(Setting.this, getString(R.string.bankCardPay), getString(R.string.bankCardTypeSign),  new JSONObject().put("amt", "1"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.history:
                startActivity(new Intent(this, HistoryActivity.class));
                break;
        }
    }
}
