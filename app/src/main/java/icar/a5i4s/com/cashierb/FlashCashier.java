package icar.a5i4s.com.cashierb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ums.AppHelper;
import com.ums.upos.sdk.exception.SdkException;
import com.ums.upos.sdk.printer.BoldEnum;
import com.ums.upos.sdk.printer.FontConfig;
import com.ums.upos.sdk.printer.FontSizeEnum;
import com.ums.upos.sdk.printer.OnPrintResultListener;
import com.ums.upos.sdk.printer.PrinterManager;
import com.ums.upos.sdk.system.BaseSystemManager;
import com.ums.upos.sdk.system.OnServiceStatusListener;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import icar.a5i4s.com.cashierb.dao.HistoryDAO;
import icar.a5i4s.com.cashierb.dao.OrderCashierDAO;
import icar.a5i4s.com.cashierb.helper.Logg;
import icar.a5i4s.com.cashierb.helper.QRCodeUtil;
import icar.a5i4s.com.cashierb.helper.Tools;
import icar.a5i4s.com.cashierb.helper.WebServicesTool;
import icar.a5i4s.com.cashierb.module.Order;
import icar.a5i4s.com.cashierb.module.ResultData;
import icar.a5i4s.com.cashierb.module.WXTicket;
import okhttp3.Call;

public class FlashCashier extends AppCompatActivity implements View.OnClickListener {

    protected TextView flashCashTotal;
    protected TextView flashCashCalc;
    protected LinearLayout topPanel;
    protected Button panelNum7;
    protected Button panelNum4;
    protected Button panelNum1;
    protected Button panelNum0;
    protected Button panelNum8;
    protected Button panelNum5;
    protected Button panelNum2;
    protected Button panelDot;
    protected Button panelNum9;
    protected Button panelNum6;
    protected Button panelNum3;
    protected Button panelPlus;
    protected Button panelBackspace;
    protected Button panelEmpty;
    protected Button panelPay;

    protected WebServicesTool webServicesTool;
    protected String transId, date;
    private List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置ActionBar
        setTitle(getString(R.string.flash_cashier));
        webServicesTool = new WebServicesTool(this);
        super.setContentView(R.layout.activity_flash_cashier);
        initView();
    }

    //数字
    private final int NUMBER = 0;
    //运算符
    private final int FUNCTION = 1;
    //操作
    private final int ARITHMETIC = 2;
    //跳转行为
    private final int ACTION = 3;

    //合计计数板 单独变量‘+’运算符之后清空
    protected String lastStr = "";
    //‘.’的次数
    protected int dotCount = 0;
    //运算符'+'次数
    protected int plusCount = 0;
    //金钱合计
    protected double total = 0.00;
    String onlyLabel ,qrCodeUrl, price, userId,aliPayOutTradeNo, aliPayQrCode;;
    protected boolean boo = false;
    String arr[] = null;
    DecimalFormat df = new DecimalFormat("######0.00");
    View payChoiseView;
    ImageView payWeChat, unionPay, unionPayNoLimit, alipay;;
    MaterialDialog payChoiseDialog;

    protected HistoryDAO hDAO = new HistoryDAO(this);

    protected void ArithmeticTotal(String str, int type){
        switch (type){
            case NUMBER:
                //如果第一个数已经是0 ，再输入0
                if (lastStr.equals(getString(R.string.panel_num0)) && str.equals(getString(R.string.panel_num0))) {
                    break;
                } //非空并且输入的是‘.’
                else if (flashCashCalc.getText().toString().equals("") && str.equals(getString(R.string.panel_dot))){
                    break;
                } else {
                    //如果已经输入过‘.’并且又输入了‘.’，无效
                    if (dotCount == 1 && str.equals(getString(R.string.panel_dot))) {
                        break;
                    } //如果输入了‘.’，进行计数
                    else if (str.equals(getString(R.string.panel_dot))) {
                        dotCount = 1;
                    }
                    //判断是否已经是小数点后面有两位数字了。
                    if (lastStr.matches("\\d+\\.\\d{2}")) {
                        break;
                    } else {
                        onTextViewChanged(str);
                    }
                    //清空‘+’ 计数
                    plusCount = 0;
                    lastStr = lastStr + str;
                    //Log.w("lastStr", lastStr);
                }
                break;
            case FUNCTION:
                if (plusCount == 1) {
                    break;
                } else {
                    boo = true;
                    plusCount = 1; //正在进行加法运算
                    flashCashCalc.setText(flashCashCalc.getText().toString() + "+");
                    lastStr = "";
                }
                dotCount = 0;
                break;
            case ARITHMETIC:
                if (str.equals(getString(R.string.panel_backspace))) {
                    //退格算法
                    Double total = 0.00;//reset
                    String st = flashCashCalc.getText().toString();
                    if(!st.isEmpty()) {
                        //总字符打散
                        String strs[] = st.split("\\+");
                        if(strs[strs.length - 1].indexOf(".") != -1){
                            dotCount = 0;
                        }
                        arr = st.substring(0, st.length() - 1).split("\\+");
                        //把最后一个数位 给lastStr
                        lastStr = arr[arr.length - 1];
                        for (int i = 0; i < arr.length; i++){
                            total += arr[i].isEmpty() ? 0.00 : Double.valueOf(arr[i]);
                        }
                        this.total = total;
                        flashCashCalc.setText(st.substring(0, st.length() - 1));
                        flashCashTotal.setText(getString(R.string.rmb) + df.format(total));
                    }
                } else if(str.equals(getString(R.string.panel_empty))) {
                    dotCount = 0;
                    plusCount = 0;
                    lastStr = "";
                    total = 0.00;
                    arr = null;
                    flashCashTotal.setText(getString(R.string.flash_cash_total));
                    flashCashCalc.setText("");
                }
                break;
            case ACTION:
                //金额为0
                if (total == 0.00){
                    Toast.makeText(FlashCashier.this, getString(R.string.totalZeroMsg), Toast.LENGTH_SHORT).show();
                    break;
                } else if (hDAO.getErrorCount() != 0) {
                    //去设置
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
                    /*
                    * 实例化选择支付视图
                    * */
                    //通过系统提供的实例获得一个LayoutInflater对象
                    LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    //第一个参数为xml文件中view的id，第二个参数为此view的父组件，可以为null，android会自动寻找它是否拥有父组件
                    payChoiseView = inflater.inflate(R.layout.pay_type, null);
                    payWeChat = (ImageView) payChoiseView.findViewById(R.id.pay_we_chat);
                    unionPay = (ImageView) payChoiseView.findViewById(R.id.unionpay);
                    unionPayNoLimit = (ImageView) payChoiseView.findViewById(R.id.unionpay_no_limit);
                    userId = getSharedPreferences("userId", Activity.MODE_PRIVATE).getString("id", "").toString();
                    alipay = (ImageView) payChoiseView.findViewById(R.id.alipay);
                    alipay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog = new MaterialDialog.Builder(FlashCashier.this)
                                    .title(R.string.please_wait)
                                    .content(R.string.please_wait_qrcode)
                                    .progress(true, 0)
                                    .show();
                            webServicesTool.Connect("tradeCreate.do")
                                    .addParams("payType", Tools.ALI_AMT_PAY)
                                    .addParams("shopId", Tools.getLoginShopId(getApplicationContext()))
                                    .addParams("totalFee", price)
                                    .addParams("operatorId", Tools.getUserId(getApplicationContext()))
                                    .addParams("orderId ", "")
                                    .build().execute(new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e, int id) {
                                    dialog.dismiss();
                                    new MaterialDialog.Builder(FlashCashier.this)
                                            .title(getString(R.string.alert))
                                            .content(getString(R.string.message_network))
                                            .positiveText(getString(R.string.payResultClose))
                                            .show();
                                }

                                @Override
                                public void onResponse(String response, int id) {
                                    ResultData resultData = new Gson().fromJson(response, ResultData.class);
                                    if (resultData.getSuccess()){
                                        Map<String, String> map = new Gson().fromJson(resultData.getCode(), new TypeToken<Map<String, String>>() {}.getType());
                                        aliPayOutTradeNo = map.get("outTradeNo");
                                        aliPayQrCode = map.get("qrCode");
                                        final String filePath = getFileRoot(getApplicationContext()) + File.separator
                                                + "qr_" + System.currentTimeMillis() + ".jpg";

                                        //二维码图片较大时，生成图片、保存文件的时间可能较长，因此放在新线程中
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                boolean success = QRCodeUtil.createQRImage(aliPayQrCode, 600, 600, null,filePath);

                                                if (success) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            qrCode = new ImageView(getApplicationContext());
                                                            qrCode.setImageBitmap(BitmapFactory.decodeFile(filePath));
                                                            payChoiseDialog.dismiss();
                                                            showAlipayQRCode();
                                                        }
                                                    });
                                                }
                                            }
                                        }).start();
                                    }else{
                                        new MaterialDialog.Builder(FlashCashier.this)
                                                .title(getString(R.string.payResultAlert))
                                                .content(resultData.getErrorMassge())
                                                .positiveText(getString(R.string.payResultClose))
                                                .show();
                                    }
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
                    payWeChat.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                final String filePath = getFileRoot(getApplicationContext()) + File.separator
                                        + "qr_" + System.currentTimeMillis() + ".jpg";

                                //二维码图片较大时，生成图片、保存文件的时间可能较长，因此放在新线程中
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        boolean success = QRCodeUtil.createQRImage(qrCodeUrl, 600, 600, null,filePath);

                                        if (success) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    qrCode = new ImageView(getApplicationContext());
                                                    qrCode.setImageBitmap(BitmapFactory.decodeFile(filePath));
                                                    payChoiseDialog.dismiss();
                                                    showQRCode();
                                                }
                                            });
                                        }
                                    }
                                }).start();
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                    unionPay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Order o = new Order("" , "", onlyLabel, userId);
                            o.setAmt(String.valueOf(Double.valueOf(total)));
                            //预制信息(默认为异常)
                            o.setMsgText(getString(R.string.pos_system_error));
                            //向表中存入即将打开的信息
                            if (hDAO.setHistoryOrderBefore(o)) {
                                try {
                                    payChoiseDialog.dismiss();
                                    AppHelper.callTrans(FlashCashier.this, getString(R.string.bankCardPay), getString(R.string.bankCardType), new JSONObject("{\"amt\":"+ price +",\"quota\":"+Tools.getPayTopAmt(getApplicationContext())+", \"randomNo\": "+onlyLabel+", \"orderNo\": \"\", \"techSupport\":\""+getString(R.string.techSupport)+"\",\"merchantName\":\""+String.format(getString(R.string.merchantName), Tools.getShopName(getApplicationContext()))+"\"}"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    unionPayNoLimit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Order o = new Order("" , "", onlyLabel, userId);
                            o.setAmt(String.valueOf(Double.valueOf(total)));
                            //预制信息(默认为异常)
                            o.setMsgText(getString(R.string.pos_system_error));
                            //向表中存入即将打开的信息
                            if (hDAO.setHistoryOrderBefore(o)) {
                                try {
                                    payChoiseDialog.dismiss();
                                    AppHelper.callTrans(FlashCashier.this, getString(R.string.bankCardPay), getString(R.string.bankCardType), new JSONObject("{\"amt\":"+ price +",\"quota\":"+Tools.getNoLimit()+", \"randomNo\": "+onlyLabel+", \"orderNo\": \"\", \"techSupport\":\""+getString(R.string.techSupport)+"\",\"merchantName\":\""+String.format(getString(R.string.merchantName), Tools.getShopName(getApplicationContext()))+"\"}"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    onlyLabel = Tools.getOnlyLabel(this);
                    price = String.valueOf(Math.round(Double.valueOf(total) * 100));
                    qrCodeUrl = java.text.MessageFormat.format(Tools.getScanUrl(getApplicationContext()), Tools.getLoginShopId(this), Tools.AMT_PAY, total, onlyLabel, Tools.FLASH, "");
                    payChoiseDialog = new MaterialDialog.Builder(this)
                            .title(R.string.please_choice_pay_type)
                            .positiveText(R.string.payResultClose)
                            .customView(payChoiseView, false)
                            .cancelable(false)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
                break;
        }

    }
    //如果有结果运算 刷新视图
    public void onTextViewChanged(String str){
        double totalOld = total;
        String reString = flashCashCalc.getText().toString();
        flashCashCalc.setText(reString + str);
        String arr[] = flashCashCalc.getText().toString().split("\\+");
        total = 0.00;
        for (int i = 0; i < arr.length; i++){
            total += arr[i].isEmpty() ? 0.00 : Double.valueOf(arr[i]);
        }
        //是否超出范围
        if (total > 9000000.00){
            Toast.makeText(FlashCashier.this, "超出最大范围", Toast.LENGTH_SHORT).show();
            flashCashCalc.setText(reString);
            total = totalOld;
        }else{
            flashCashTotal.setText(getString(R.string.rmb) + df.format(total));
        }
        Logg.i("total", String.valueOf(total));
    }
    @Override
    public void onClick(View view) {
        String str = "";
        int type = NUMBER;
        if (view.getId() == R.id.panel_num7) {
            str = getString(R.string.panel_num7);
        } else if (view.getId() == R.id.panel_num4) {
            str = getString(R.string.panel_num4);
        } else if (view.getId() == R.id.panel_num1) {
            str = getString(R.string.panel_num1);
        } else if (view.getId() == R.id.panel_num0) {
            str = getString(R.string.panel_num0);
        } else if (view.getId() == R.id.panel_num8) {
            str = getString(R.string.panel_num8);
        } else if (view.getId() == R.id.panel_num5) {
            str = getString(R.string.panel_num5);
        } else if (view.getId() == R.id.panel_num2) {
            str = getString(R.string.panel_num2);
        } else if (view.getId() == R.id.panel_dot) {
            str = getString(R.string.panel_dot);
        } else if (view.getId() == R.id.panel_num9) {
            str = getString(R.string.panel_num9);
        } else if (view.getId() == R.id.panel_num6) {
            str = getString(R.string.panel_num6);
        } else if (view.getId() == R.id.panel_num9) {
            str = getString(R.string.panel_num9);
        } else if (view.getId() == R.id.panel_num3) {
            str = getString(R.string.panel_num3);
        } else if (view.getId() == R.id.panel_plus) {
            str = getString(R.string.panel_plus);
            type = FUNCTION;
        } else if (view.getId() == R.id.panel_backspace) {
            str = getString(R.string.panel_backspace);
            type = ARITHMETIC;
        } else if (view.getId() == R.id.panel_empty) {
            str = getString(R.string.panel_empty);
            type = ARITHMETIC;
        } else if (view.getId() == R.id.panel_pay) {
            type = ACTION;
        }
        ArithmeticTotal(str ,type);
    }

    private void initView() {
        flashCashTotal = (TextView) findViewById(R.id.flash_cash_total);
        flashCashTotal.setOnClickListener(FlashCashier.this);
        flashCashCalc = (TextView) findViewById(R.id.flash_cash_calc);
        flashCashCalc.setOnClickListener(FlashCashier.this);
        panelNum7 = (Button) findViewById(R.id.panel_num7);
        panelNum7.setOnClickListener(FlashCashier.this);
        panelNum4 = (Button) findViewById(R.id.panel_num4);
        panelNum4.setOnClickListener(FlashCashier.this);
        panelNum1 = (Button) findViewById(R.id.panel_num1);
        panelNum1.setOnClickListener(FlashCashier.this);
        panelNum0 = (Button) findViewById(R.id.panel_num0);
        panelNum0.setOnClickListener(FlashCashier.this);
        panelNum8 = (Button) findViewById(R.id.panel_num8);
        panelNum8.setOnClickListener(FlashCashier.this);
        panelNum5 = (Button) findViewById(R.id.panel_num5);
        panelNum5.setOnClickListener(FlashCashier.this);
        panelNum2 = (Button) findViewById(R.id.panel_num2);
        panelNum2.setOnClickListener(FlashCashier.this);
        panelDot = (Button) findViewById(R.id.panel_dot);
        panelDot.setOnClickListener(FlashCashier.this);
        panelNum9 = (Button) findViewById(R.id.panel_num9);
        panelNum9.setOnClickListener(FlashCashier.this);
        panelNum6 = (Button) findViewById(R.id.panel_num6);
        panelNum6.setOnClickListener(FlashCashier.this);
        panelNum3 = (Button) findViewById(R.id.panel_num3);
        panelNum3.setOnClickListener(FlashCashier.this);
        panelPlus = (Button) findViewById(R.id.panel_plus);
        panelPlus.setOnClickListener(FlashCashier.this);
        panelBackspace = (Button) findViewById(R.id.panel_backspace);
        panelBackspace.setOnClickListener(FlashCashier.this);
        panelEmpty = (Button) findViewById(R.id.panel_empty);
        panelEmpty.setOnClickListener(FlashCashier.this);
        panelPay = (Button) findViewById(R.id.panel_pay);
        panelPay.setOnClickListener(FlashCashier.this);
    }

    protected MaterialDialog dialog;
    protected Order order;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        isResult = true;
        if(AppHelper.TRANS_REQUEST_CODE == requestCode){
            Logg.i("RESULT CODE", "" + resultCode);
            if (Activity.RESULT_OK == resultCode) {
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
                result.append("userId" + ":" + order.getUserId() + "\r\n");
                Logg.i("userId", order.getUserId());
                order.setOnlyLabel(onlyLabel);
                //解析 transData 数据
                Map<String, String> transMap = new Gson().fromJson(order.getTransData(), new TypeToken<Map<String, String>>(){}.getType());
                order.setOrgTraceNo(transMap.get("traceNo"));
                //支付失败
                if (!transMap.get("resCode").equals("00")) {
                    /******************************************
                     * //*2017-02-17*/
                    int c = hDAO.updateHistoryOrderBefore(order, transMap.get("resDesc"));
                    Logg.i("XXXXXXXXXXXXXXXX", c+"");
                    new MaterialDialog.Builder(this)
                            .title(getString(R.string.payResultTitle))
                            .content("\n" + transMap.get("resDesc"))
                            .positiveText(getString(R.string.payResultClose))
                            //.negativeText(R.string.disagree)
                            .show();
                    Logg.i("POS RESULT", "" + result);
                    isResult = false;
                } else {
                    dialog = new MaterialDialog.Builder(this)
                            .title(R.string.please_wait)
                            .content(R.string.please_wait_payed)
                            .progress(true, 0)
                            .cancelable(false)
                            .show();
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
                                /*2017-02-17*/
                            hDAO.updateHistoryOrderBefore(order, getString(R.string.pay_success_pos_network_error));
                            Logg.i("====请求失败", e.toString());
                            if (new OrderCashierDAO(FlashCashier.this).setRepairOrder(order)){
                                new MaterialDialog.Builder(FlashCashier.this)
                                        .title(getString(R.string.payResultAlert))
                                        .content(getString(R.string.payResultExceptionToRepair))
                                        .positiveText(getString(R.string.payResultClose))
                                        //.negativeText(R.string.disagree)
                                        .show();
                                List<Order> list = new OrderCashierDAO(FlashCashier.this).getRepairOrder();
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
                                dialog.dismiss();
                                isResult = false;
                            }
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            ResultData resultData = new Gson().fromJson(response, ResultData.class);
                            if (resultData.getSuccess()){
                                    /*2017-02-17*/
                                hDAO.updateHistoryOrderBefore(order, getString(R.string.pay_success));
                                new MaterialDialog.Builder(FlashCashier.this)
                                        .title(getString(R.string.payResultTitle))
                                        .content(getString(R.string.payResultSuccess))
                                        .positiveText(getString(R.string.payResultClose))
                                        //.negativeText(R.string.disagree)
                                        .show();
                            }else{
                                if (new OrderCashierDAO(FlashCashier.this).setRepairOrder(order)){
                                        /*2017-02-17*/
                                    hDAO.updateHistoryOrderBefore(order, getString(R.string.pay_success_icar_network_error));
                                    new MaterialDialog.Builder(FlashCashier.this)
                                            .title(getString(R.string.payResultAlert))
                                            .content(getString(R.string.payResultExceptionToRepairForICar))
                                            .positiveText(getString(R.string.payResultClose))
                                            //.negativeText(R.string.disagree)
                                            .show();
                                }
                            }
                            dialog.dismiss();
                            isResult = false;
                        }
                    });
                }
            }else{
                isResult = false;
                Logg.i("POS RESULT", "Intent is null");
            }
        }else{
            isResult = false;
            Logg.i("POS MESSAGE", "resultCode is not RESULT_OK");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    Handler handler = new Handler();
    Runnable runnable;
    MaterialDialog qrCodeDialog;
    ImageView qrCode;

    protected void showQRCode(){
        try{
            qrCodeDialog = new MaterialDialog.Builder(this)
                    .title(R.string.pay_type_wechat_qrcode)
                    .positiveText(R.string.payResultClose)
                    .customView(qrCode, false)
                    .cancelable(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            handler.removeCallbacks(runnable);
                            runnable = null;
                        }
                    })
                    .show();
            runnable = new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    webServicesTool.Connect("queryPosPayResult.do")
                            .addParams("payType", Tools.AMT_PAY)
                            .addParams("orderId", onlyLabel)
                            .build().execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            //要做的事情
                            handler.postDelayed(runnable, 1000);
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            ResultData resultData = WebServicesTool.getOrderGson().fromJson(response, new TypeToken<ResultData>(){}.getType());
                            if (resultData.getSuccess()){
                                data = (List<Map<String, Object>>) resultData.getDataList();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        qrCodeDialog.dismiss();
                                        date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
                                        transId = data.get(0).get("transaction_id").toString();
                                        payResultShow();
                                        handler.removeCallbacks(runnable);
                                    }
                                });
                            } else {
                                handler.postDelayed(runnable, 1000);
                            }
                        }
                    });
                }
            };
            handler.postDelayed(runnable, 0);
        } catch (Exception e){
            e.printStackTrace();
        } finally {

        }
    }
    protected void showAlipayQRCode(){
        try{
            qrCodeDialog = new MaterialDialog.Builder(this)
                    .title(R.string.pay_type_alipay_qrcode)
                    .positiveText(R.string.payResultClose)
                    .customView(qrCode, false)
                    .cancelable(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            handler.removeCallbacks(runnable);
                            runnable = null;
                        }
                    })
                    .show();
            runnable = new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    webServicesTool.Connect("apPosOrderstatus.do")
                            .addParams("outTradeNo", aliPayOutTradeNo)
                            .build().execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            //要做的事情
                            handler.postDelayed(runnable, 1000);
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            ResultData resultData = WebServicesTool.getOrderGson().fromJson(response, new TypeToken<ResultData>(){}.getType());
                            if (resultData.getSuccess()){
                                final Map<String, String> map = new Gson().fromJson(resultData.getCode(), new TypeToken<Map<String, String>>() {}.getType());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        qrCodeDialog.dismiss();
                                        date = Tools.getDate();
                                        transId = map.get("trade_no").toString();
                                        aliPayResultShow();
                                        handler.removeCallbacks(runnable);
                                    }
                                });
                            } else {
                                handler.postDelayed(runnable, 1000);
                            }
                        }
                    });
                }
            };
            handler.postDelayed(runnable, 0);
        } catch (Exception e){
            Toast.makeText(getApplicationContext(), e.toString() + "", Toast.LENGTH_LONG).show();
        } finally {

        }
    }
    protected void aliPayResultShow(){
        printAliTicket(new WXTicket(getString(R.string.rmb) + total, Tools.getShopName(this), Tools.getAppName(this), getString(R.string.payResultSuccess), date, transId));
        new MaterialDialog.Builder(this)
                .title(R.string.payResultTitle)
                .content(R.string.payResultSuccess)
                .positiveText(R.string.payResultClose)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
    protected void payResultShow(){
        printWXTicket(new WXTicket(getString(R.string.rmb) + total, Tools.getShopName(this), Tools.getAppName(this), getString(R.string.payResultSuccess), date, transId));
        new MaterialDialog.Builder(this)
                .title(R.string.payResultTitle)
                .content(R.string.payResultSuccess)
                .positiveText(R.string.payResultClose)
                .show();
    }
    //打印微信支付小票
    protected void printWXTicket(final WXTicket weChatTicket) {
        try {
            BaseSystemManager.getInstance().deviceServiceLogin(
                    FlashCashier.this, null, "99999998",
                    new OnServiceStatusListener() {
                        @Override
                        public void onStatus(int arg0) {
                            if (0 == arg0 || 2 == arg0 || 100 == arg0) {
                                //TODO:登录成功，设备操作
                                try {
                                    PrinterManager printer = new PrinterManager();
                                    printer.initPrinter();
                                    FontConfig fontConfig = new FontConfig();
                                    fontConfig.setBold(BoldEnum.NOT_BOLD);
                                    fontConfig.setSize(FontSizeEnum.SMALL);
                                    FontConfig fontConfig1 = new FontConfig();
                                    fontConfig1.setBold(BoldEnum.NOT_BOLD);
                                    fontConfig1.setSize(FontSizeEnum.MIDDLE);
                                    FontConfig fontConfig2 = new FontConfig();
                                    fontConfig2.setBold(BoldEnum.BOLD);
                                    fontConfig2.setSize(FontSizeEnum.BIG);

                                    printer.setPrnText("        " + getString(R.string.print_title) + "\n", fontConfig2);
                                    printer.setPrnText(getString(R.string.shop_name_title), fontConfig2);
                                    printer.setPrnText(weChatTicket.getShopName() + "\n", fontConfig1);
                                    printer.setPrnText(getString(R.string.app_name_title), fontConfig2);
                                    printer.setPrnText(weChatTicket.getAppName() + "\n", fontConfig1);
                                    printer.setPrnText(getString(R.string.status_title), fontConfig2);
                                    printer.setPrnText(weChatTicket.getStatus() + "\n", fontConfig1);
                                    printer.setPrnText(getString(R.string.date_title), fontConfig2);
                                    printer.setPrnText(weChatTicket.getDate() + "\n", fontConfig1);
                                    printer.setPrnText(getString(R.string.transId_title), fontConfig2);
                                    printer.setPrnText(weChatTicket.getTransId() + "\n", fontConfig1);
                                    printer.setPrnText(getString(R.string.amt_title), fontConfig2);
                                    printer.setPrnText(weChatTicket.getAmt() + "\n", fontConfig2);
                                    printer.setPrnText(getString(R.string.sign_statement), fontConfig1);
                                    printer.setPrnText(getString(R.string.custom_sign), fontConfig2);
                                    printer.setPrnText("\n\n\n\n", fontConfig);

                                    printer.startPrint(new OnPrintResultListener() {

                                        @Override
                                        public void onPrintResult(int arg0) {
                                            // TODO Auto-generated method stub
                                            //TODO：打印结果
                                            if(arg0 == 0){
                                                //成功
                                            }else{
                                                //失败
                                                Toast.makeText(getApplicationContext(), getErrorDescriptionUms(arg0), Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    });
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }

                        }
                    });
        } catch (SdkException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//        //通过系统提供的实例获得一个LayoutInflater对象
//        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        //第一个参数为xml文件中view的id，第二个参数为此view的父组件，可以为null，android会自动寻找它是否拥有父组件
//        View view = inflater.inflate(R.layout.print_layout, null);
//
//        TextView shopName = (TextView) view.findViewById(R.id.shopName);
//        TextView appName = (TextView) view.findViewById(R.id.appName);
//        TextView status = (TextView) view.findViewById(R.id.status);
//        TextView date = (TextView) view.findViewById(R.id.date);
//        TextView transId = (TextView) view.findViewById(R.id.transId);
//        TextView amt = (TextView) view.findViewById(R.id.amt);
//        shopName.setText(weChatTicket.getShopName());
//        appName.setText(weChatTicket.getAppName());
//        date.setText(weChatTicket.getDate());
//        status.setText(weChatTicket.getStatus());
//        transId.setText(weChatTicket.getTransId());
//        amt.setText(weChatTicket.getAmt());
//
//        view.setDrawingCacheEnabled(true);
//        view.buildDrawingCache();
//        Bitmap bitmap = view.getDrawingCache();
//        if(bitmap == null){
//            Log.d("", "bitmap is null");
//            return;
//        }
//
//        String fname = "/sdcard/wechat_print.png";
//        try {
//            FileOutputStream out = new FileOutputStream(fname);
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//            Log.d("", "file" + fname + "output done.");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return;
//        }
//
//        AppHelper.callPrint(this, fname);
    }
    //打印支付宝支付小票
    protected void printAliTicket(final WXTicket weChatTicket) {
        try {
            BaseSystemManager.getInstance().deviceServiceLogin(
                    FlashCashier.this, null, "99999998",
                    new OnServiceStatusListener() {
                        @Override
                        public void onStatus(int arg0) {
                            if (0 == arg0 || 2 == arg0 || 100 == arg0) {
                                //TODO:登录成功，设备操作
                                try {
                                    PrinterManager printer = new PrinterManager();
                                    printer.initPrinter();
                                    FontConfig fontConfig = new FontConfig();
                                    fontConfig.setBold(BoldEnum.NOT_BOLD);
                                    fontConfig.setSize(FontSizeEnum.SMALL);
                                    FontConfig fontConfig1 = new FontConfig();
                                    fontConfig1.setBold(BoldEnum.NOT_BOLD);
                                    fontConfig1.setSize(FontSizeEnum.MIDDLE);
                                    FontConfig fontConfig2 = new FontConfig();
                                    fontConfig2.setBold(BoldEnum.BOLD);
                                    fontConfig2.setSize(FontSizeEnum.BIG);

                                    printer.setPrnText("        " + getString(R.string.print_ali_title) + "\n", fontConfig2);
                                    printer.setPrnText(getString(R.string.shop_name_title), fontConfig2);
                                    printer.setPrnText(weChatTicket.getShopName() + "\n", fontConfig1);
                                    printer.setPrnText(getString(R.string.app_name_title), fontConfig2);
                                    printer.setPrnText(weChatTicket.getAppName() + "\n", fontConfig1);
                                    printer.setPrnText(getString(R.string.status_title), fontConfig2);
                                    printer.setPrnText(weChatTicket.getStatus() + "\n", fontConfig1);
                                    printer.setPrnText(getString(R.string.date_title), fontConfig2);
                                    printer.setPrnText(weChatTicket.getDate() + "\n", fontConfig1);
                                    printer.setPrnText(getString(R.string.transId_title), fontConfig2);
                                    printer.setPrnText(weChatTicket.getTransId() + "\n", fontConfig1);
                                    printer.setPrnText(getString(R.string.amt_title), fontConfig2);
                                    printer.setPrnText(weChatTicket.getAmt() + "\n", fontConfig2);
                                    printer.setPrnText(getString(R.string.sign_statement_ali), fontConfig1);
                                    printer.setPrnText(getString(R.string.custom_sign), fontConfig2);
                                    printer.setPrnText("\n\n\n\n", fontConfig);


                                    printer.startPrint(new OnPrintResultListener() {

                                        @Override
                                        public void onPrintResult(int arg0) {
                                            // TODO Auto-generated method stub
                                            //TODO：打印结果
                                            if(arg0 == 0){
                                                //成功
                                            }else{
                                                //失败
                                                Toast.makeText(getApplicationContext(), getErrorDescriptionUms(arg0), Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    });
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }

                        }
                    });
        } catch (SdkException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    //文件存储根目录
    private String getFileRoot(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File external = context.getExternalFilesDir(null);
            if (external != null) {
                return external.getAbsolutePath();
            }
        }
        return context.getFilesDir().getAbsolutePath();
    }

    public String getErrorDescriptionUms(int code) {
        switch (code) {
            case -1001:
                return "打印失败";
            case -1002:
                return "设置字符串缓冲失败";
            case -1003:
                return "设置图片缓冲失败";
            case -1004:
                return "打印机忙";
            case -1005:
                return "打印机缺纸";
            case -1006:
                return "打印数据包格式错";
            case -1007:
                return "打印机故障";
            case -1008:
                return "打印机过热";
            case -1009:
                return "打印未完成";
            case -1010:
                return "打印机未装字库";
            case -1011:
                return "数据包过长";
            case -1999:
                return "其他异常错误";
            default:
                return "打印失败";

        }
    }

    protected boolean isResult = false;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if ((dialog == null || !dialog.isShowing()) && !isResult){
                return super.onKeyDown(keyCode, event);
            } else {
                return true;
            }
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
