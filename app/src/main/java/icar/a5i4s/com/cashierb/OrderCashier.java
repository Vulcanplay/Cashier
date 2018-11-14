package icar.a5i4s.com.cashierb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ums.upos.sdk.exception.SdkException;
import com.ums.upos.sdk.printer.BoldEnum;
import com.ums.upos.sdk.printer.FontConfig;
import com.ums.upos.sdk.printer.FontSizeEnum;
import com.ums.upos.sdk.printer.OnPrintResultListener;
import com.ums.upos.sdk.printer.PrinterManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.ums.AppHelper;
import com.ums.upos.sdk.system.BaseSystemManager;
import com.ums.upos.sdk.system.OnServiceStatusListener;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
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

public class OrderCashier extends AppCompatActivity implements MaterialSearchBar.OnSearchActionListener, PopupMenu.OnMenuItemClickListener, AdapterView.OnItemClickListener {

    protected int colors[] = {0,
            R.color.array_1,
            R.color.array_2,
            R.color.array_3,
            R.color.array_4,
            R.color.array_5,
            R.color.array_6,
            R.color.array_7,
            R.color.array_8,
            R.color.array_9};
    protected SharedPreferences sf;
    protected String snCode;
    //Search View Bar
    private List<String> lastSearches;
    private MaterialSearchBar searchBar;

    private PullToRefreshListView pullToRefresh;
    private List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
    private List<Map<String, Object>> dataCopy = new ArrayList<Map<String, Object>>();
    MyAdapter adapter;
    protected MaterialDialog dialog;
    protected Order order;

    protected WebServicesTool webServicesTool;

    protected TextView message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.order_cashier));
        webServicesTool = new WebServicesTool(this);
        setContentView(R.layout.activity_order_cashier);
        message = (TextView) findViewById(R.id.message);
        sf = getSharedPreferences("snCode", Activity.MODE_PRIVATE);
        snCode = sf.getString("code", "");
        searchBar = (MaterialSearchBar) findViewById(R.id.searchBar);
        searchBar.setHint("工单号/订单号/车牌号/姓名");

        searchBar.setSpeechMode(true);
        searchBar.setNavButtonEnabled(false);
        //set searchBar callBack
        searchBar.setOnSearchActionListener(this);
        pullToRefresh = (PullToRefreshListView) findViewById(R.id.pullToRefresh);
        adapter = new MyAdapter(this);
        pullToRefresh.setOnItemClickListener(this);
        getData("");
        /*
         * Mode.BOTH：同时支持上拉下拉
         * Mode.PULL_FROM_START：只支持下拉Pulling Down
         * Mode.PULL_FROM_END：只支持上拉Pulling Up
         */
        /*
         * 如果Mode设置成Mode.BOTH，需要设置刷新Listener为OnRefreshListener2，并实现onPullDownToRefresh()、onPullUpToRefresh()两个方法。
         * 如果Mode设置成Mode.PULL_FROM_START或Mode.PULL_FROM_END，需要设置刷新Listener为OnRefreshListener，同时实现onRefresh()方法。
         * 当然也可以设置为OnRefreshListener2，但是Mode.PULL_FROM_START的时候只调用onPullDownToRefresh()方法，
         * Mode.PULL_FROM的时候只调用onPullUpToRefresh()方法.
         */
        pullToRefresh.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        init();

        /*
         * setOnRefreshListener(OnRefreshListener listener):设置刷新监听器；
         * setOnLastItemVisibleListener(OnLastItemVisibleListener listener):设置是否到底部监听器；
         * setOnPullEventListener(OnPullEventListener listener);设置事件监听器；
         * onRefreshComplete()：设置刷新完成
         */
        /*
         * pulltorefresh.setOnScrollListener()
         */
        // SCROLL_STATE_TOUCH_SCROLL(1) 正在滚动
        // SCROLL_STATE_FLING(2) 手指做了抛的动作（手指离开屏幕前，用力滑了一下）
        // SCROLL_STATE_IDLE(0) 停止滚动
        /*
         * setOnLastItemVisibleListener
         * 当用户拉到底时调用
         */
        /*
         * setOnTouchListener是监控从点下鼠标 （可能拖动鼠标）到放开鼠标（鼠标可以换成手指）的整个过程 ，他的回调函数是onTouchEvent（MotionEvent event）,
         * 然后通过判断event.getAction()是MotionEvent.ACTION_UP还是ACTION_DOWN还是ACTION_MOVE分别作不同行为。
         * setOnClickListener的监控时间只监控到手指ACTION_DOWN时发生的行为
         */
        pullToRefresh.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>(){
            @Override
            public void onPullDownToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                // TODO Auto-generated method stub
                new FinishRefresh().execute();
                getData("");
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onPullUpToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                // TODO Auto-generated method stub
            }
        });
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        if (!enabled){
            onSearchConfirmed("");
        }
    }


    @Override
    public void onSearchConfirmed(CharSequence charSequence) {
        if (dataCopy != null){
            if (TextUtils.isEmpty(charSequence)){//当过滤的关键字为空的时候，我们则显示所有的数据
                data.clear();
                data.addAll(dataCopy);
            }else {//否则把符合条件的数据对象添加到集合中
                data.clear();
                for (Map<String, Object> map : dataCopy){
                    if ((map.get("orderId") == null ? false : map.get("orderId").toString().contains(charSequence)) ||
                            (map.get("licenseplates") == null ? false : map.get("licenseplates").toString().contains(charSequence)) ||
                            (map.get("customerName") == null ? false : map.get("customerName").toString().contains(charSequence)) ||
                            (map.get("qtOrderId") == null ? false : map.get("qtOrderId").toString().contains(charSequence))){
                        data.add(map);
                    }
                }
            }
            adapter.notifyDataSetChanged();
        } else {
            return;
        }
    }

    @Override
    public void onButtonClicked(int i) {
        switch (i){
            case MaterialSearchBar.BUTTON_NAVIGATION:
                //导航
                break;
            case MaterialSearchBar.BUTTON_SPEECH:
                //话筒
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }
    //初始化
    private void init() {
        ILoadingLayout startLabels = pullToRefresh
                .getLoadingLayoutProxy(true, false);
        startLabels.setPullLabel("下拉刷新...");// 刚下拉时，显示的提示
        startLabels.setRefreshingLabel("正在载入...");// 刷新时
        startLabels.setReleaseLabel("放开刷新...");// 下来达到一定距离时，显示的提示

//      // 设置上拉刷新文本
//      pullToRefresh.getLoadingLayoutProxy(true, false)
//              .setPullLabel("下拉刷新...");
//      pullToRefresh.getLoadingLayoutProxy(true, false).setReleaseLabel(
//              "放开刷新...");
//      pullToRefresh.getLoadingLayoutProxy(true, false).setRefreshingLabel(
//              "正在加载...");
    }
    //设置提示信息
    private void setMessage(String msg){
        message.setVisibility(View.VISIBLE);
        message.setText(msg);
    }
    //获取数据 condition搜索参数
    private void getData(String condition){
        message.setVisibility(View.GONE);

        dialog = new MaterialDialog.Builder(this)
                .title(R.string.please_wait)
                .content(R.string.please_wait)
                .progress(true, 0)
                .cancelable(false)
                .show();
        webServicesTool.Connect("getAndroidPosCollectCashs.do")
                .addParams("posSn", snCode)
                .addParams("condition", condition)
                .addParams("loginShopId", Tools.getLoginShopId(this))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        setMessage(getString(R.string.message_network));
                        dialog.dismiss();
                        Log.w("Exception", String.valueOf(e));
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        ResultData resultData = WebServicesTool.getOrderGson().fromJson(response, new TypeToken<ResultData>(){}.getType());
                        if (resultData.getSuccess()){
                            data = (List<Map<String, Object>>) resultData.getDataList();
                            dataCopy.clear();
                            dataCopy.addAll((List<Map<String, Object>>) resultData.getDataList());
                            Log.w("size", data.size() + "");
                            if (data.size() != 0) {
                                pullToRefresh.setAdapter(adapter);
                            } else {
                                //UI操作，显示无数据文本提示
                                setMessage(getString(R.string.message_not_data));
                            }
                        }else{
                            //没数据
                        }
                        dialog.dismiss();
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    protected String orderId = "", onlyLabel = "", userId="", price , priceValue, qrCodeUrl, itemName, aliPayOutTradeNo, aliPayQrCode;
    ImageView qrCode;
    protected HistoryDAO hDAO = new HistoryDAO(this);

    View payChoiseView;
    ImageView payWeChat, unionPay, unionPayNoLimit, alipay;
    MaterialDialog payChoiseDialog;
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (hDAO.getErrorCount() != 0){
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
            /** 实例化选择支付视图* */
            //通过系统提供的实例获得一个LayoutInflater对象
            LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //第一个参数为xml文件中view的id，第二个参数为此view的父组件，可以为null，android会自动寻找它是否拥有父组件
            payChoiseView = inflater.inflate(R.layout.pay_type, null);
            payWeChat = (ImageView) payChoiseView.findViewById(R.id.pay_we_chat);
            unionPay = (ImageView) payChoiseView.findViewById(R.id.unionpay);
            unionPayNoLimit = (ImageView) payChoiseView.findViewById(R.id.unionpay_no_limit);
            userId = getSharedPreferences("userId", Activity.MODE_PRIVATE).getString("id", "").toString();
            /*
            * @orderId
            * @getSN()
            * */
            orderId = ((TextView) view.findViewById(R.id.orderId)).getText().toString();
            itemName = ((TextView) view.findViewById(R.id.type)).getText().toString();
            onlyLabel = Tools.getOnlyLabel(this);
            priceValue = ((TextView) view.findViewById(R.id.price_value)).getText().toString();
            price = String.valueOf(Math.round(Double.valueOf(priceValue) * 100));
            qrCodeUrl = java.text.MessageFormat.format(
                    Tools.getScanUrl(getApplicationContext()),
                    Tools.getLoginShopId(this),
                    Tools.ORDER_PAY, priceValue,
                    onlyLabel,
                    itemName,
                    orderId);
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
            alipay = (ImageView) payChoiseView.findViewById(R.id.alipay);
            alipay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog = new MaterialDialog.Builder(OrderCashier.this)
                            .title(R.string.please_wait)
                            .content(R.string.please_wait_qrcode)
                            .progress(true, 0)
                            .show();
                    webServicesTool.Connect("tradeCreate.do")
                            .addParams("payType", Tools.ALI_ORDER_PAY)
                            .addParams("shopId", Tools.getLoginShopId(getApplicationContext()))
                            .addParams("totalFee", price)
                            .addParams("operatorId", Tools.getUserId(getApplicationContext()))
                            .addParams("orderId", orderId)
                            .build().execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            dialog.dismiss();
                            new MaterialDialog.Builder(OrderCashier.this)
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
                                new MaterialDialog.Builder(OrderCashier.this)
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
                    /*2017-02-17*/
                    Order o = new Order(orderId , "", onlyLabel, userId);
                    o.setAmt(priceValue);
                    //预制信息（默认为异常状态）
                    o.setMsgText(getString(R.string.pos_system_error));
                    if (hDAO.setHistoryOrderBefore(o)) {
                        try {
                            payChoiseDialog.dismiss();
                            AppHelper.callTrans(OrderCashier.this, getString(R.string.bankCardPay), getString(R.string.bankCardType), new JSONObject(
                                    "{\"amt\":"+ price +"," +
                                            "\"quota\":"+Tools.getPayTopAmt(getApplicationContext())+"," +
                                            " \"randomNo\": "+onlyLabel+"," +
                                            " \"orderNo\": "+orderId+"," +
                                            " \"techSupport\":\""+getString(R.string.techSupport)+"\"," +
                                            "\"merchantName\":\""+String.format(getString(R.string.merchantName), Tools.getShopName(getApplicationContext()))+"\"}"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            unionPayNoLimit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*2017-02-17*/
                    Order o = new Order(orderId , "", onlyLabel, userId);
                    o.setAmt(priceValue);
                    //预制信息（默认为异常状态）
                    o.setMsgText(getString(R.string.pos_system_error));
                    if (hDAO.setHistoryOrderBefore(o)) {
                        try {
                            payChoiseDialog.dismiss();
                            AppHelper.callTrans(OrderCashier.this, getString(R.string.bankCardPay), getString(R.string.bankCardType), new JSONObject(
                                    "{\"amt\":"+ price +"," +
                                            "\"quota\":"+Tools.getNoLimit()+"," +
                                            " \"randomNo\": "+onlyLabel+"," +
                                            " \"orderNo\": "+orderId+"," +
                                            " \"techSupport\":\""+getString(R.string.techSupport)+"\"," +
                                            "\"merchantName\":\""+String.format(getString(R.string.merchantName), Tools.getShopName(getApplicationContext()))+"\"}"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onRestart() {
        getData("");
        super.onRestart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        isResult = true;
        if(AppHelper.TRANS_REQUEST_CODE == requestCode){
            Logg.i("RESULT CODE", "" + resultCode);
            Logg.i("ORDER ID", "" + orderId);
            if (Activity.RESULT_OK == resultCode) {
                if (null != data) {
                    final StringBuilder result = new StringBuilder();
                    final Map<String,String> map = AppHelper.filterTransResult(data);
                    //创建一个order实例
                    order = new Order(orderId, map.get(AppHelper.TRANS_APP_NAME),
                            map.get(AppHelper.TRANS_BIZ_ID),
                            map.get(AppHelper.RESULT_CODE),
                            map.get(AppHelper.RESULT_MSG),
                            map.get(AppHelper.TRANS_DATA),
                            userId);
                    order.setOnlyLabel(onlyLabel);
                    result.append("orderId" + ":" + order.getOrderId() + "\r\n");
                    result.append(AppHelper.TRANS_APP_NAME + ":" + order.getAppName() + "\r\n");
                    result.append(AppHelper.TRANS_BIZ_ID + ":" + order.getTransId() + "\r\n");
                    result.append(AppHelper.RESULT_CODE + ":" + order.getResultCode() + "\r\n");
                    result.append(AppHelper.RESULT_MSG + ":" + order.getResultMsg() + "\r\n");
                    result.append(AppHelper.TRANS_DATA + ":" + order.getTransData() + "\r\n");
                    result.append("sign" + ":" + order.getSign() + "\r\n");
                    result.append("userId" + ":" + order.getUserId() + "\r\n");
                    Logg.i("sign", order.getSign());
                    Logg.i("orderId", order.getOrderId());
                    Logg.i("userId", order.getUserId());

                    //解析 transData 数据
                    Map<String, String> transMap = new Gson().fromJson(order.getTransData(), new TypeToken<Map<String, String>>(){}.getType());
                    //用户取消交易
                    if (!transMap.get("resCode").equals("00")) {
                        /*2017-02-17*/
                        int c = hDAO.updateHistoryOrderBefore(order, transMap.get("resDesc"));
                        Logg.i("XXXXXXXXXXXXXXXX", c+"");
                        /******************************************/
                        new MaterialDialog.Builder(this)
                                .title(getString(R.string.payResultTitle))
                                .content(getString(R.string.orderId) + orderId + "\n\n" + transMap.get("resDesc"))
                                .positiveText(getString(R.string.payResultClose))
                                //.negativeText(R.string.disagree)
                                .show();
                        Logg.i("POS RESULT", "" + result);
                        isResult = false;
                    } else {
                        dialog = new MaterialDialog.Builder(this)
                                .title(R.string.please_wait)
                                .content(R.string.please_wait_payed)
                                .cancelable(false)
                                .progress(true, 0)
                                .show();
                        order.setOrgTraceNo(transMap.get("traceNo"));

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
                                /******************************************/
                                Logg.i("====请求失败", e.toString());
                                if (new OrderCashierDAO(OrderCashier.this).setRepairOrder(order)){
                                    new MaterialDialog.Builder(OrderCashier.this)
                                            .title(getString(R.string.payResultAlert))
                                            .content(getString(R.string.payResultExceptionToRepair))
                                            .positiveText(getString(R.string.payResultClose))
                                            //.negativeText(R.string.disagree)
                                            .show();
                                }
                                List<Order> list = new OrderCashierDAO(OrderCashier.this).getRepairOrder();
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

                            @Override
                            public void onResponse(String response, int id) {
                                ResultData resultData = new Gson().fromJson(response, ResultData.class);
                                /******************************************/
                                if (resultData.getSuccess()){
                                            /*2017-02-17*/
                                    hDAO.updateHistoryOrderBefore(order, getString(R.string.pay_success));
                                    new MaterialDialog.Builder(OrderCashier.this)
                                            .title(getString(R.string.payResultTitle))
                                            .content(getString(R.string.payResultSuccess))
                                            .positiveText(getString(R.string.payResultClose))
                                            //.negativeText(R.string.disagree)
                                            .show();
                                }else{
                                    if (new OrderCashierDAO(OrderCashier.this).setRepairOrder(order)){
                                                /*2017-02-17*/
                                        hDAO.updateHistoryOrderBefore(order, getString(R.string.pay_success_icar_network_error));
                                        new MaterialDialog.Builder(OrderCashier.this)
                                                .title(getString(R.string.payResultAlert))
                                                .content(getString(R.string.payResultExceptionToRepairForICar))
                                                .positiveText(getString(R.string.payResultClose))
                                                //.negativeText(R.string.disagree)
                                                .show();
                                    }
                                }
                                dialog.dismiss();
                                isResult = false;
                                getData("");
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
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private class FinishRefresh extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            //刷新完成
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
//          adapter.notifyDataSetChanged();
            pullToRefresh.onRefreshComplete();
        }
    }
    Handler handler = new Handler();
    Runnable runnable;
    MaterialDialog qrCodeDialog;

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
                            .addParams("payType", Tools.ORDER_PAY)
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
                                Logg.i("print data", data.toString());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        qrCodeDialog.dismiss();
                                        date = Tools.getDate();
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
            Toast.makeText(getApplicationContext(), e.toString() + "", Toast.LENGTH_LONG).show();
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
                    Log.i("aliPayOutTradeNo",aliPayOutTradeNo);
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
                            final ResultData resultData = WebServicesTool.getOrderGson().fromJson(response, new TypeToken<ResultData>(){}.getType());
                            if (resultData.getSuccess()){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Map<String, String> map = new Gson().fromJson(resultData.getCode(), new TypeToken<Map<String, String>>() {}.getType());
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

    protected String transId, date;

    protected void payResultShow(){
        printWXTicket(new WXTicket(getString(R.string.rmb) + priceValue, Tools.getShopName(this), Tools.getAppName(this), getString(R.string.payResultSuccess), date, transId));
        new MaterialDialog.Builder(this)
                .title(R.string.payResultTitle)
                .content(R.string.payResultSuccess)
                .positiveText(R.string.payResultClose)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        getData("");
                    }
                })
                .show();
    }

    protected void aliPayResultShow(){
        printAliTicket(new WXTicket(getString(R.string.rmb) + priceValue, Tools.getShopName(this), Tools.getAppName(this), getString(R.string.payResultSuccess), date, transId));
        new MaterialDialog.Builder(this)
                .title(R.string.payResultTitle)
                .content(R.string.payResultSuccess)
                .positiveText(R.string.payResultClose)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        getData("");
                    }
                })
                .show();
    }

    //打印支付宝支付小票
    protected void printAliTicket(final WXTicket weChatTicket) {
        try {
            BaseSystemManager.getInstance().deviceServiceLogin(
                    OrderCashier.this, null, "99999998",
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

    //打印微信支付小票
    protected void printWXTicket(final WXTicket weChatTicket) {
        try {
            BaseSystemManager.getInstance().deviceServiceLogin(
                    OrderCashier.this, null, "99999998",
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

    private class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public MyAdapter(Context context) {
            // TODO Auto-generated constructor stub
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder viewHolder = null;
            Map<String, Object> map = data.get(position);
            if(convertView == null){
                viewHolder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.order_cashier_item, null);
                viewHolder.type = (TextView) convertView.findViewById(R.id.type);
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.orderId = (TextView) convertView.findViewById(R.id.orderId);
                viewHolder.price = (TextView) convertView.findViewById(R.id.price);
                viewHolder.priceValue = (TextView) convertView.findViewById(R.id.price_value);

                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.type.setText(map.get("orderTypeName").toString());
            viewHolder.type.setBackgroundResource(colors[Integer.parseInt(map.get("orderType").toString()) % 10]);
            viewHolder.name.setText(map.get("customerName") == null ? getString(R.string.anonymous) : map.get("customerName").toString());
            Logg.i("====TEST", String.valueOf(map.get("id")));
            viewHolder.orderId.setText(map.get("orderId").toString());
            viewHolder.price.setText(map.get("actualAmount").toString() + getString(R.string.YUAN));
            viewHolder.priceValue.setText(map.get("actualAmount").toString());

            return convertView;
        }

        class ViewHolder{
            TextView type;
            TextView name;
            TextView orderId;
            TextView price;
            TextView priceValue;
        }
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
