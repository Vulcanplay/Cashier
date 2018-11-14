package icar.a5i4s.com.cashierb;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ums.AppHelper;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import icar.a5i4s.com.cashierb.dao.ExceptionOrdersDAO;
import icar.a5i4s.com.cashierb.dao.HistoryDAO;
import icar.a5i4s.com.cashierb.dao.OrderCashierDAO;
import icar.a5i4s.com.cashierb.dao.SuccessOrdersDAO;
import icar.a5i4s.com.cashierb.helper.Logg;
import icar.a5i4s.com.cashierb.helper.Tools;
import icar.a5i4s.com.cashierb.helper.WebServicesTool;
import icar.a5i4s.com.cashierb.module.Order;
import icar.a5i4s.com.cashierb.module.ResultData;
import okhttp3.Call;

public class Navigation extends Activity implements View.OnClickListener {

    //获取本地存储
    protected SharedPreferences sf;

    protected ImageButton iconOrder;
    protected ImageButton iconRevoke;
    protected ImageButton iconHistory;
    protected ImageButton iconCheckout;
    protected ImageButton iconPay;
    protected ImageButton iconRemainder;
    protected ImageButton iconRepair;
    protected ImageButton iconSetting;
    protected ImageButton iconPlus;
    protected ImageButton iconRePrint;
    protected TextView orderRepairCount;

    protected OrderCashierDAO ocDAO;
    protected WebServicesTool webServicesTool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_navigation);
        //检查版本 参数
        webServicesTool = new WebServicesTool(getApplicationContext());
        initView();
        setRepairOrderCountView();
    }

    @Override
    public void onClick(View view) {
        Class clazz = null;
        if (view.getId() == R.id.icon_order) {
            //加载XML数据
            sf = getSharedPreferences("snCode", Activity.MODE_PRIVATE);
            if(sf.getString("code", "").equals("")){
                String string = AppHelper.getBaseSysInfo(this);

                Map<String, Object> map = new GsonBuilder().create().fromJson(string, new TypeToken<Map<String, Object>>() {}.getType());
                if(!map.get("SN").equals("")){
                    SharedPreferences sp;
                    SharedPreferences.Editor editor;
                    //存储到 XML
                    sp = getApplicationContext().getSharedPreferences("snCode", 0);
                    editor = sp.edit();
                    editor.putString("code", map.get("SN").toString());
                    editor.commit();
                    clazz = OrderCashier.class;
                }
                Log.w("=====SN :" , map.get("SN").toString());
            } else {
                //单据收银
                clazz = OrderCashier.class;
            }

        } else if (view.getId() == R.id.icon_pay) {
            //普通收银
            clazz = FlashCashier.class;
        } else if (view.getId() == R.id.icon_revoke) {
            //撤销
            sendRevoke();
        } else if (view.getId() == R.id.icon_history) {
            //流水查询
            AppHelper.callTrans(Navigation.this, getString(R.string.bankCardPay), getString(R.string.bankCardTypeHistory), new JSONObject());
        } else if (view.getId() == R.id.icon_checkout) {
            //结算
            if (new HistoryDAO(this).getErrorCount() != 0){
                new MaterialDialog.Builder(this)
                        .title(getString(R.string.alert))
                        .content(R.string.have_exception_pay)
                        .neutralText(getString(R.string.payResultClose))
                        .positiveText(getString(R.string.go_setting))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                startActivity(new Intent().setClass(getApplicationContext(), Setting.class));
                                dialog.dismiss();
                            }
                        })
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        }).show();
            } else {
                new MaterialDialog.Builder(this)
                        .title(getString(R.string.alert))
                        .content(R.string.confirm_js)
                        .neutralText(getString(R.string.payResultClose))
                        .positiveText(getString(R.string.confirm))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                AppHelper.callTrans(Navigation.this, "公共资源", "换班", new JSONObject());
                                dialog.dismiss();
                            }
                        })
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        } else if (view.getId() == R.id.icon_remainder) {
            //余额查询
            AppHelper.callTrans(Navigation.this, getString(R.string.bankCardPay), getString(R.string.bankCardTypeRemainder), new JSONObject());

        } else if (view.getId() == R.id.icon_repair) {
            //补送
            sendRepair();
        } else if(view.getId() == R.id.plus){
            //clazz = TActivity.class;
            Toast.makeText(Navigation.this, "期待更多…", Toast.LENGTH_SHORT).show();
        } else if(view.getId() == R.id.reprint){
            rePrint();
        } else if (view.getId() == R.id.icon_setting) {
            Intent intent = new Intent();
            intent.setClass(Navigation.this, Setting.class);
            startActivity(intent);
//            //设置
//            new MaterialDialog.Builder(this)
//                    .title(R.string.input_setting_title)
//                    .content(R.string.input_setting_content)
//                    .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
//                    .widgetColorRes(R.color.gray)
//                    .input(R.string.empty, R.string.empty, new MaterialDialog.InputCallback() {
//                        @Override
//                        public void onInput(MaterialDialog dialog, CharSequence input) {
//                            String string = String.valueOf(input);
//                            if(string.equals("962464")){
//                                Intent intent = new Intent();
//                                intent.setClass(Navigation.this, Setting.class);
//                                startActivity(intent);
//                            } else {
//                                Toast.makeText(Navigation.this, "密码错误", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    }).show();
        }
        if (clazz == null) {

        } else {
            Intent intent = new Intent();
            intent.setClass(Navigation.this, clazz);
            startActivity(intent);
        }
        //finish();//停止当前的Activity,如果不写,则按返回键会跳转回原来的Activity
    }
    private void rePrint() {
        if (new ExceptionOrdersDAO(this).getCount() != 0){
            new MaterialDialog.Builder(this)
                    .title(getString(R.string.alert))
                    .content(R.string.have_exception_pay)
                    .neutralText(getString(R.string.payResultClose))
                    .positiveText(getString(R.string.go_setting))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            startActivity(new Intent().setClass(getApplicationContext(), Setting.class));
                            dialog.dismiss();
                        }
                    })
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    }).show();
        } else {
            new MaterialDialog.Builder(this)
                    .title(R.string.input_print_title)
                    .content(R.string.input_print_content)
                    .inputType(InputType.TYPE_CLASS_NUMBER)
                    .positiveText(R.string.reprintButton)
                    .neutralText(R.string.rePrintPrev)
                    .negativeText(R.string.payResultClose)
                    .widgetColorRes(R.color.gray)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            try {
                                orgTraceNoInput = "000000";
                                JSONObject json = new JSONObject("{\"traceNo\":\""+ orgTraceNoInput +"\"}");
                                Log.w("Reprint", json.toString());
                                //调用系统接口
                                AppHelper.callTrans(Navigation.this, getString(R.string.bankCardPay), getString(R.string.bankCardTypeRePrint), json);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .input(R.string.empty, R.string.empty, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {
                            orgTraceNoInput = String.valueOf(input);
                            if(!orgTraceNoInput.equals("")){
                                JSONObject json = null;
                                try {
                                    json = new JSONObject("{\"traceNo\":\""+ orgTraceNoInput +"\"}");
                                    Log.w("Reprint", json.toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                //调用系统接口
                                AppHelper.callTrans(Navigation.this, getString(R.string.bankCardPay), getString(R.string.bankCardTypeRePrint), json);
                            }
                        }
                    }).show();
        }
    }

    //交易凭证号
    private String orgTraceNoInput = "";
    private String onlyLabel = "",orderId;
    protected SuccessOrdersDAO soDAO = new SuccessOrdersDAO(this);
    protected ExceptionOrdersDAO exoDAO = new ExceptionOrdersDAO(this);
    protected HistoryDAO hDAO = new HistoryDAO(this);
    Order o;
    //发送撤销请求
    private void sendRevoke() {
        if (new HistoryDAO(this).getErrorCount() != 0){
            new MaterialDialog.Builder(this)
                    .title(getString(R.string.alert))
                    .content(R.string.have_exception_pay)
                    .neutralText(getString(R.string.payResultClose))
                    .positiveText(getString(R.string.go_setting))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            startActivity(new Intent().setClass(getApplicationContext(), Setting.class));
                            dialog.dismiss();
                        }
                    })
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    }).show();
        } else {
            onlyLabel = Tools.getOnlyLabel(getApplicationContext());
            new MaterialDialog.Builder(this)
                    .title(R.string.input_revoke_title)
                    .content(R.string.input_revoke_content)
                    .inputType(InputType.TYPE_CLASS_NUMBER)
                    .widgetColorRes(R.color.gray)
                    .input(R.string.empty, R.string.empty, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {
                            orgTraceNoInput = ("000000"+input.toString()).substring(input.length());
                            if(!orgTraceNoInput.equals("")){
                                orderId = hDAO.getOrderIdForTraceNo(orgTraceNoInput);
                                Log.i("撤销 order id" , orderId);
                                o = new Order(orderId, orgTraceNoInput, onlyLabel, getSharedPreferences("userId", Activity.MODE_PRIVATE).getString("id", "").toString());
                                o.setAmt("0");
                                o.setMsgText(getString(R.string.pos_system_error));
                                if (hDAO.setHistoryOrderBefore(o)) {
                                    //调用系统接口
                                    try {
                                        JSONObject jsonObject = new JSONObject(
                                                "{\"orgTraceNo\":\""+ orgTraceNoInput +"\"," +
                                                        " \"orderNo\": \""+orderId+"\"," +
                                                        " \"randomNo\": \""+onlyLabel+"\"," +
                                                        " \"prnInfo\":\""+getString(R.string.techSupport) + String.format(getString(R.string.merchantName), Tools.getShopName(getApplicationContext()))+"\"}");
                                        AppHelper.callTrans(Navigation.this, getString(R.string.bankCardPay), getString(R.string.bankCardTypeRevoke), jsonObject);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }).show();
        }
    }

    //发送补送
    protected int successCount = 0;
    protected int errorCount = 0;
    protected int repairIng = 1;
    protected int count = 0;
    MaterialDialog dialog;
    private void sendRepair() {
        ocDAO = new OrderCashierDAO(this);
        count = ocDAO.getRepairCount();
        errorCount = 0;
        successCount = 0;
        repairIng = 1;
        if(count != 0) {
            dialog = new MaterialDialog.Builder(this)
                    .title(R.string.please_wait_repair)
                    .content("正在补送第" + repairIng + "单，总单数" + count + "张")
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
            List<Order> list = ocDAO.getRepairOrder();
            for (final Order order : list) {
                webServicesTool.Connect("doAndroidBackstage.do")
                        .addParams("orderId", order.getOrderId())
                        .addParams("appName", order.getAppName())
                        .addParams("transId", order.getTransId())
                        .addParams("resultCode", order.getResultCode())
                        .addParams("resultMsg", order.getResultMsg())
                        .addParams("transData", order.getTransData())
                        .addParams("sign", order.getSign())
                        .build().execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        errorCount++;
                        dialog.dismiss();
                        new MaterialDialog.Builder(Navigation.this)
                                .title(getString(R.string.alert))
                                .content("失败" + errorCount + "张" + "\n\n" + "成功" + successCount + "张")
                                .positiveText(getString(R.string.payResultClose))
                                //.negativeText(R.string.disagree)
                                .show();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        ResultData resultData = new Gson().fromJson(response, ResultData.class);
                        if (resultData.getSuccess()){
                            //删除本地数据库 此条数据
                            if (hDAO.updateHistoryOrderBeforeForRepair(ocDAO.getRepairOnlyLabelForId(order.getId()), getString(R.string.repair_success)) == 1 && ocDAO.deleteRepairOrderForId(order.getId())) {
                                //成功张数+1
                                successCount++;
                                //设置进度条
                                dialog.setContent("正在补送第" + repairIng + "单，总单数" + count + "张");
                                Logg.i("=====补送进度", "正在补送第" + repairIng + "单，总单数" + count + "张");
                                //UI修改
                                orderRepairCount.setText(String.valueOf(count));
                            } else {
                                //本地数据库删除失败
                                errorCount++;
                            }
                        } else {
                            //ICar 删除失败
                            errorCount++;
                        }
                        if (successCount + errorCount == count) {
                            //关闭进度条
                            dialog.dismiss();
                            //隐藏补送
                            orderRepairCount.setVisibility(View.GONE);
                            new MaterialDialog.Builder(Navigation.this)
                                    .title(getString(R.string.alert))
                                    .content("失败" + errorCount + "张" + "\n\n" + "成功" + successCount + "张")
                                    .positiveText(getString(R.string.payResultClose))
                                    //.negativeText(R.string.disagree)
                                    .show();
                            setRepairOrderCountView();
                        } else {
                            //补送张数+1
                            repairIng++;
                        }
                    }
                });
            }
        } else {
            new MaterialDialog.Builder(Navigation.this)
                    .title(getString(R.string.alert))
                    .content(getString(R.string.notRepairOrder))
                    .positiveText(getString(R.string.payResultClose))
                    //.negativeText(R.string.disagree)
                    .show();
        }
    }

    private void initView() {
        iconOrder = (ImageButton) findViewById(R.id.icon_order);
        iconOrder.setOnClickListener(Navigation.this);
        iconRevoke = (ImageButton) findViewById(R.id.icon_revoke);
        iconRevoke.setOnClickListener(Navigation.this);
        iconHistory = (ImageButton) findViewById(R.id.icon_history);
        iconHistory.setOnClickListener(Navigation.this);
        iconCheckout = (ImageButton) findViewById(R.id.icon_checkout);
        iconCheckout.setOnClickListener(Navigation.this);
        iconPay = (ImageButton) findViewById(R.id.icon_pay);
        iconPay.setOnClickListener(Navigation.this);
        iconRemainder = (ImageButton) findViewById(R.id.icon_remainder);
        iconRemainder.setOnClickListener(Navigation.this);
        iconRepair = (ImageButton) findViewById(R.id.icon_repair);
        iconRepair.setOnClickListener(Navigation.this);
        iconSetting = (ImageButton) findViewById(R.id.icon_setting);
        iconSetting.setOnClickListener(Navigation.this);
        iconPlus = (ImageButton) findViewById(R.id.plus);
        iconPlus.setOnClickListener(this);

        iconRePrint = (ImageButton) findViewById(R.id.reprint);
        iconRePrint.setOnClickListener(this);
        orderRepairCount = (TextView) findViewById(R.id.orderRepairCount);

    }

    //更新补送数
    protected void setRepairOrderCountView(){
        int count = new OrderCashierDAO(this).getRepairCount();
        Logg.i("=====补送数", String.valueOf(count));
        if (count == 0) {
            orderRepairCount.setVisibility(View.GONE);
        } else {
            orderRepairCount.setVisibility(View.VISIBLE);
            orderRepairCount.setText(String.valueOf(count));
        }
    }

    protected Order order;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setRepairOrderCountView();
        if(AppHelper.TRANS_REQUEST_CODE == requestCode){
            Logg.i("RESULT CODE", "" + resultCode);
            Logg.i("=====交易凭证号", "" + orgTraceNoInput);
            if (null != data) {
                final StringBuilder result = new StringBuilder();
                Map<String,String> map = AppHelper.filterTransResult(data);
                //创建一个order实例
                order = new Order("", map.get(AppHelper.TRANS_APP_NAME),
                        map.get(AppHelper.TRANS_BIZ_ID),
                        map.get(AppHelper.RESULT_CODE),
                        map.get(AppHelper.RESULT_MSG),
                        map.get(AppHelper.TRANS_DATA),
                        getSharedPreferences("userId", Activity.MODE_PRIVATE).getString("id", "").toString());
                result.append("orderId" + ":" + order.getOrderId() + "\r\n");
                result.append(AppHelper.TRANS_APP_NAME + ":" + order.getAppName() + "\r\n");
                result.append(AppHelper.TRANS_BIZ_ID + ":" + order.getTransId() + "\r\n");
                result.append(AppHelper.RESULT_CODE + ":" + order.getResultCode() + "\r\n");
                result.append(AppHelper.RESULT_MSG + ":" + order.getResultMsg() + "\r\n");
                result.append(AppHelper.TRANS_DATA + ":" + order.getTransData() + "\r\n");
                result.append("sign" + ":" + order.getSign() + "\r\n");
                result.append("revoke" + ":" + orgTraceNoInput);
                result.append("userId" + ":" + order.getUserId() + "\r\n");
                Logg.i("sign", order.getSign());
                Logg.i("POS RESULT", "" + result);
                Logg.i("userId", order.getUserId());
                order.setOnlyLabel(onlyLabel);
                //如果是撤销才做处理
                if("结算".equals(map.get(AppHelper.TRANS_BIZ_ID))){//解析 transData 数据
                    Map<String, Object> transMap = new Gson().fromJson(order.getTransData(), new TypeToken<Map<String, Object>>(){}.getType());
                    if (transMap.get("resCode").toString().equals("00")) {
                        hDAO.deleteHistoryOrder();
                    }
                    new MaterialDialog.Builder(this)
                            .title(getString(R.string.payResultTitle))
                            .content(transMap.get("resDesc").toString())
                            .positiveText(getString(R.string.payResultClose))
                            //.negativeText(R.string.disagree)
                            .show();
                }
                if (getString(R.string.bankCardTypeRevoke).equals(map.get(AppHelper.TRANS_BIZ_ID))){
                    //解析 transData 数据
                    Map<String, String> transMap = new Gson().fromJson(order.getTransData(), new TypeToken<Map<String, String>>(){}.getType());
                    order.setAmt(transMap.get("amt"));
                    order.setCancelTraceNo(transMap.get("traceNo"));
                    //支付失败
                    if (!transMap.get("resCode").equals("00")) {
                        /**
                         * DB操作
                         * ************清空异常表数据*************
                         * */
                        exoDAO.deleteOrder();
                        /******************************************/
                        int c = hDAO.updateHistoryOrderBeforeForRevoke(order, transMap.get("resDesc"));
                        Logg.i("XXXXXXXXXXXXXXXX", c+"");
                        new MaterialDialog.Builder(this)
                                .title(getString(R.string.payResultTitle))
                                .content(getString(R.string.revokeCode) + orgTraceNoInput + "\n\n" + transMap.get("resDesc"))
                                .positiveText(getString(R.string.payResultClose))
                                //.negativeText(R.string.disagree)
                                .show();
                    } else {
                        dialog = new MaterialDialog.Builder(this)
                                .title(R.string.please_wait)
                                .content(R.string.please_wait_payed)
                                .progress(true, 0)
                                .show();
                        webServicesTool.Connect("doAndroidBackstage.do")
                                .addParams("orderId", order.getOrderId())
                                .addParams("appName", order.getAppName())
                                .addParams("transId", order.getTransId())
                                .addParams("resultCode", order.getResultCode())
                                .addParams("resultMsg", order.getResultMsg())
                                .addParams("transData", order.getTransData())
                                .addParams("sign", order.getSign())
                                .addParams("orgTraceNo", orgTraceNoInput)
                                .addParams("userId", order.getUserId())
                                .build().execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                dialog.dismiss();
                                /**
                                 * DB操作
                                 * ************清空异常表数据*************
                                 * */
                                exoDAO.deleteOrder();
                                /******************************************/
                                Logg.i("====请求失败", e.toString());
                                if (new OrderCashierDAO(Navigation.this).setRepairOrder(order)) {
                                    hDAO.updateHistoryOrderBeforeForRevoke(order, getString(R.string.revoke_success_pos_network_error));
                                    new MaterialDialog.Builder(Navigation.this)
                                            .title(getString(R.string.payResultAlert))
                                            .content(getString(R.string.revokeResultExceptionToRepair))
                                            .positiveText(getString(R.string.payResultClose))
                                            //.negativeText(R.string.disagree)
                                            .show();
                                }
                                List<Order> list = new OrderCashierDAO(Navigation.this).getRepairOrder();
                                Logg.i("=====补送的总数据", String.valueOf(list.size()));
                                for (Order order : list
                                        ) {
                                    Logg.i("=====getId", String.valueOf(order.getId()));
                                    Logg.i("=====getOrderId", order.getOrderId());
                                    Logg.i("=====getAppName", order.getAppName());
                                    Logg.i("=====getTransId", order.getTransId());
                                    Logg.i("=====getResultCode", order.getResultCode());
                                    Logg.i("=====getResultMsg", order.getResultMsg());
                                    Logg.i("=====getTransData", order.getTransData());
                                    Logg.i("=====getUserId", order.getUserId());
                                }
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                ResultData resultData = new Gson().fromJson(response, ResultData.class);
                                if (resultData.getSuccess()) {
                                    /**
                                     * DB操作
                                     * ************清空异常表数据*************
                                     * */
                                    exoDAO.deleteOrder();
                                    /******************************************/
                                    /**
                                     * DB操作
                                     * ***********成功数据表***********
                                     * ************1.清空*************
                                     * ************2.新增*************
                                     * */
                                    if (soDAO.deleteOrder()){
                                        soDAO.setOrder(order);
                                    }
                                    hDAO.updateHistoryOrderBeforeForRevoke(order, getString(R.string.revoke_success));
                                    new MaterialDialog.Builder(Navigation.this)
                                            .title(getString(R.string.revokeResultTitle))
                                            .content(getString(R.string.revokeResultSuccess))
                                            .positiveText(getString(R.string.payResultClose))
                                            //.negativeText(R.string.disagree)
                                            .show();
                                } else {
                                    if (new OrderCashierDAO(Navigation.this).setRepairOrder(order)) {
                                        hDAO.updateHistoryOrderBeforeForRevoke(order, getString(R.string.revoke_success_icar_network_error));
                                        new MaterialDialog.Builder(Navigation.this)
                                                .title(getString(R.string.payResultAlert))
                                                .content(getString(R.string.revokeResultExceptionToRepairForICar))
                                                .positiveText(getString(R.string.payResultClose))
                                                //.negativeText(R.string.disagree)
                                                .show();
                                    }
                                }
                                dialog.dismiss();
                                Logg.i("====response", response);
                            }
                        });
                    }
                } else if(getString(R.string.bankCardTypeRemainder).equals(map.get(AppHelper.TRANS_BIZ_ID))){
                    Map<String, String> transMap = new Gson().fromJson(order.getTransData(), new TypeToken<Map<String, String>>(){}.getType());
                    if (!transMap.get("resCode").equals("00")) {
                        new MaterialDialog.Builder(this)
                                .title(getString(R.string.paySearchTitle))
                                .content(getString(R.string.revokeCode) + orgTraceNoInput + "\n\n" + transMap.get("resDesc"))
                                .positiveText(getString(R.string.payResultClose))
                                //.negativeText(R.string.disagree)
                                .show();
                    } else {
                        NumberFormat nf = new java.text.DecimalFormat("#,##0.00");
                        String bankNumber = transMap.get("cardNo");
                        int number = bankNumber.indexOf("/");
                        if(number > 0)
                            bankNumber = bankNumber.substring(0, number);
                        new MaterialDialog.Builder(this)
                                .title(getString(R.string.paySearchTitle))
                                .content(getString(R.string.bankName) + transMap.get("cardIssuerCode") + "\n\n" +
                                        getString(R.string.bankNo) + bankNumber + "\n\n" +
                                        getString(R.string.bankMoney) + nf.format(Double.valueOf(transMap.get("amt")) / 100) + getString(R.string.YUAN))
                                .positiveText(getString(R.string.payResultClose))
                                //.negativeText(R.string.disagree)
                                .show();
                    }
                }else{
                    Logg.i("POS RESULT", "Intent is null");
                }
            }else{
                Logg.i("POS MESSAGE", "resultCode is not RESULT_OK");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        setRepairOrderCountView();
        super.onResume();
    }

    @Override
    protected void onStart() {
        setRepairOrderCountView();
        super.onStart();
    }


}
