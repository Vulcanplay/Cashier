package icar.a5i4s.com.cashierb;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.reflect.TypeToken;
import com.ums.upos.sdk.exception.SdkException;
import com.ums.upos.sdk.printer.BoldEnum;
import com.ums.upos.sdk.printer.FontConfig;
import com.ums.upos.sdk.printer.FontSizeEnum;
import com.ums.upos.sdk.printer.OnPrintResultListener;
import com.ums.upos.sdk.printer.PrinterManager;
import com.ums.upos.sdk.system.BaseSystemManager;
import com.ums.upos.sdk.system.OnServiceStatusListener;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import icar.a5i4s.com.cashierb.adapter.WeChatRePrintAdapter;
import icar.a5i4s.com.cashierb.helper.Logg;
import icar.a5i4s.com.cashierb.helper.Tools;
import icar.a5i4s.com.cashierb.helper.WebServicesTool;
import icar.a5i4s.com.cashierb.module.ResultData;
import icar.a5i4s.com.cashierb.module.WXTicket;
import okhttp3.Call;

public class WeChatRePrint extends AppCompatActivity implements AdapterView.OnItemClickListener {
    protected ListView weChatListView;
    protected TextView message;
    protected MaterialDialog dialog;
    protected WebServicesTool webServicesTool;
    protected WeChatRePrintAdapter adapter;
    private List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.wx_re_print));
        setContentView(R.layout.activity_we_chat_re_print);
        webServicesTool = new WebServicesTool(getApplicationContext());
        message = (TextView) findViewById(R.id.message);
        weChatListView = (ListView) findViewById(R.id.wechat_reprint_list);
        weChatListView.setOnItemClickListener(this);

        dialog = new MaterialDialog.Builder(this)
                .title(R.string.please_wait)
                .content(R.string.please_wait)
                .progress(true, 0)
                .cancelable(false)
                .show();
        webServicesTool.Connect("queryPosPayResult.do")
                .addParams("transDate", Tools.getDateYMD())
                .addParams("shopId", Tools.getLoginShopId(this))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                message.setVisibility(View.VISIBLE);
                message.setText(getString(R.string.message_network));
                dialog.dismiss();
            }

            @Override
            public void onResponse(String response, int id) {
                dialog.dismiss();
                List<WXTicket> lwx = new ArrayList<WXTicket>();
                Map<String, Object> wt = null;
                ResultData resultData = WebServicesTool.getOrderGson().fromJson(response, new TypeToken<ResultData>(){}.getType());
                data = (List<Map<String, Object>>) resultData.getDataList();
                Logg.i("print data", data.toString());
                Logg.i("print size", data.size() + "");
                if (data.size() == 0){
                    message.setVisibility(View.VISIBLE);
                    message.setText(getString(R.string.message_not_data));
                } else if(resultData.getSuccess()){
                    for (int i = 0; i < data.size(); i++){
                        wt = data.get(i);
                        lwx.add(new WXTicket(
                                getString(R.string.rmb) + wt.get("total_fee").toString(),
                                wt.get("update_time").toString().replace('T', ' '),
                                wt.get("transaction_id").toString(),
                                wt.get("biz_order_id").toString()));
                    }
                    adapter = new WeChatRePrintAdapter(getApplicationContext(), lwx);
                    weChatListView.setAdapter(adapter);
                } else {
                    message.setVisibility(View.VISIBLE);
                    message.setText(resultData.getErrorMassge());
                }
            }
        });
    }

    String amt, orderId, transId, time;
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        amt = ((TextView) view.findViewById(R.id.rp_amt)).getText().toString();
        orderId = ((TextView) view.findViewById(R.id.rp_orderId)).getText().toString();
        transId = ((TextView) view.findViewById(R.id.rp_transId)).getText().toString();
        time = ((TextView) view.findViewById(R.id.rp_time)).getText().toString();
        printWXTicket(new WXTicket(amt, Tools.getShopName(this), Tools.getAppName(this), getString(R.string.payResultSuccess), time, transId));
    }

    //打印微信支付小票
    protected void printWXTicket(final WXTicket weChatTicket) {
        try {
            BaseSystemManager.getInstance().deviceServiceLogin(
                    this, null, "99999998",
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
                                    printer.setPrnText(getString(R.string.remark_reprint), fontConfig);
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
}
