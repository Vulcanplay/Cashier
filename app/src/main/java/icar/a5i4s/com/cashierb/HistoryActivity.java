package icar.a5i4s.com.cashierb;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ums.AppHelper;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import icar.a5i4s.com.cashierb.adapter.HistoryAdapter;
import icar.a5i4s.com.cashierb.dao.HistoryDAO;
import icar.a5i4s.com.cashierb.dao.OrderCashierDAO;
import icar.a5i4s.com.cashierb.helper.Logg;
import icar.a5i4s.com.cashierb.helper.WebServicesTool;
import icar.a5i4s.com.cashierb.module.Order;
import icar.a5i4s.com.cashierb.module.ResultData;
import okhttp3.Call;

public class HistoryActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    protected ListView historyListView;
    protected TextView message;
    protected MaterialDialog dialog;
    protected HistoryAdapter historyAdapter;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.history, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        hDAO = new HistoryDAO(getApplicationContext());
        webServicesTool = new WebServicesTool(this);
        message = (TextView) findViewById(R.id.message);
        historyListView = (ListView) findViewById(R.id.history_list);
        historyListView.setOnItemClickListener(this);
        if (hDAO.getHistoryCount() == 0){
            message.setVisibility(View.VISIBLE);
            message.setText(getString(R.string.message_not_data));
        } else{
            rView();
        }
    }

    String onlyLabel,orderId;
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String msg = ((TextView) view.findViewById(R.id.status)).getText().toString();
        onlyLabel = ((TextView) view.findViewById(R.id.onlyLabel)).getText().toString();
        orderId = ((TextView) view.findViewById(R.id.orderId)).getText() == null ? "" : ((TextView) view.findViewById(R.id.orderId)).getText().toString();
        if (msg.equals(getString(R.string.pos_system_error))){
            try {
                AppHelper.callTrans(HistoryActivity.this, getString(R.string.bankCardPay), getString(R.string.bankCardTypeLastSelect), new JSONObject("{\"randomNo\":\""+onlyLabel+"\"}"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected void rView(){
        historyAdapter = new HistoryAdapter(getApplicationContext(), hDAO.getHistoryOrder());
        historyListView.setAdapter(historyAdapter);
    }

    Order order;
    WebServicesTool webServicesTool;
    HistoryDAO hDAO = new HistoryDAO(this);
    protected boolean isRevoke = false;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(AppHelper.TRANS_REQUEST_CODE == requestCode){
            Log.w("RESULT CODE", "" + resultCode);
            if (Activity.RESULT_OK == resultCode) {
                Map<String,String> map = AppHelper.filterTransResult(data);
                //创建一个order实例
                //解析 transData 数据
                Map<String, Object> transMap = new Gson().fromJson(map.get(AppHelper.TRANS_DATA), new TypeToken<Map<String, Object>>(){}.getType());
                Map<String, String> map2 = (Map<String, String>) transMap.get("transData");
                if ((transMap.get("resCode") == null && !map2.get("resCode").equals("00")) || (transMap.get("resCode") != null && !transMap.get("resCode").equals("00"))) {
                    /*2017-02-17*/
                    hDAO.updateHistoryOrderBeforeMsgText(onlyLabel, transMap.get("resDesc").toString());
                    new MaterialDialog.Builder(this)
                            .title(getString(R.string.alert))
                            .content(transMap.get("resDesc").toString())
                            .positiveText(getString(R.string.payResultClose))
                            .show();
                    rView();
                } else {
                    order = new Order(orderId, transMap.get("appName").toString(),
                            transMap.get("transId").toString(),
                            transMap.get("resultCode").toString(),
                            transMap.get("resultMsg").toString(),
                            new Gson().toJson(map2).toString(),
                            getSharedPreferences("userId", Activity.MODE_PRIVATE).getString("id", "").toString());
                    order.setOnlyLabel(onlyLabel);
                    dialog = new MaterialDialog.Builder(this)
                            .title(R.string.please_wait)
                            .content(R.string.please_wait_payed)
                            .progress(true, 0)
                            .show();
                    if (transMap.get("transId").toString().equals(getString(R.string.input_revoke_title))){
                        isRevoke = true;
                        order.setCancelTraceNo(map2.get("traceNo"));
                    } else{
                        order.setOrgTraceNo(map2.get("traceNo"));
                    }
                    order.setAmt(String.valueOf(map2.get("amt")));
                    webServicesTool.Connect("doAndroidBackstage.do")
                            .addParams("orderId", order.getOrderId())
                            .addParams("appName", order.getAppName())
                            .addParams("transId", order.getTransId())
                            .addParams("resultCode", order.getResultCode())
                            .addParams("resultMsg", order.getResultMsg())
                            .addParams("transData", order.getTransData())
                            .addParams("sign", order.getSign())
                            .addParams("userId", order.getUserId())
                            .build().execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            dialog.dismiss();
                            /*2017-02-17*/
                            hDAO.updateHistoryOrderBefore(order, getString(R.string.pay_success_pos_network_error));
                            /******************************************/
                            Logg.i("====请求失败", e.toString());
                            if (new OrderCashierDAO(HistoryActivity.this).setRepairOrder(order)){
                                new MaterialDialog.Builder(HistoryActivity.this)
                                        .title(getString(R.string.payResultAlert))
                                        .content(getString(R.string.payResultExceptionToRepair))
                                        .positiveText(getString(R.string.payResultClose))
                                        //.negativeText(R.string.disagree)
                                        .show();
                            }
                            List<Order> list = new OrderCashierDAO(HistoryActivity.this).getRepairOrder();
                            Logg.i("=====补送的总数据", String.valueOf(list.size()));
                            for (Order order:list
                                    ) {
                                Logg.i("=====getId", String.valueOf(order.getId()));
                                Logg.i("=====getOrderId", order.getOrderId());
                                Logg.i("=====getAppName", order.getAppName());
                                Logg.i("=====getTransId", order.getTransId());
                                Logg.i("=====getResultCode", order.getResultCode());
                                Logg.i("=====getResultMsg", order.getResultMsg());
                                Logg.i("=====getTransData", order.getTransData());
                            }
                            rView();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            ResultData resultData = new Gson().fromJson(response, ResultData.class);
                            /******************************************/
                            if (resultData.getSuccess()){
                                 /*2017-02-17*/
                                if (isRevoke){
                                    hDAO.updateHistoryOrderBeforeForRevoke(order, getString(R.string.revoke_success));
                                } else {
                                    hDAO.updateHistoryOrderBefore(order, getString(R.string.pay_success));
                                }
                                new MaterialDialog.Builder(HistoryActivity.this)
                                        .title(getString(R.string.payResultTitle))
                                        .content(getString(R.string.payResultSuccess))
                                        .positiveText(getString(R.string.payResultClose))
                                        .show();
                            }else{
                                if (new OrderCashierDAO(HistoryActivity.this).setRepairOrder(order)){
                                    /*2017-02-17*/
                                    if (isRevoke){
                                        hDAO.updateHistoryOrderBefore(order, getString(R.string.revoke_success_icar_network_error));
                                    } else {
                                        hDAO.updateHistoryOrderBefore(order, getString(R.string.pay_success_icar_network_error));
                                    }
                                    hDAO.updateHistoryOrderBefore(order, getString(R.string.pay_success_icar_network_error));
                                    new MaterialDialog.Builder(HistoryActivity.this)
                                            .title(getString(R.string.payResultAlert))
                                            .content(getString(R.string.payResultExceptionToRepairForICar))
                                            .positiveText(getString(R.string.payResultClose))
                                            .show();
                                }
                            }
                            dialog.dismiss();
                            rView();
                        }
                    });
                }
            }else{
                Logg.i("POS RESULT", "Intent is null");
            }
        }else{
            Logg.i("POS MESSAGE", "resultCode is not RESULT_OK");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
