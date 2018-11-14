package icar.a5i4s.com.cashierb.helper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by light on 2016/11/17.
 */

public class Tools {
    public static String FLASH = "4S店销售/售后服务";

    public static String getOnlyLabel(Context context){
        return context.getSharedPreferences("userId", Activity.MODE_PRIVATE).getString("loginShopId", "") + System.currentTimeMillis() + getUserId(context);
    }
    public static String getAppName(Context context){
        return context.getSharedPreferences("userId", Activity.MODE_PRIVATE).getString("appName", "");
    }
    public static String getShopName(Context context){
        return context.getSharedPreferences("userId", Activity.MODE_PRIVATE).getString("shopName", "");
    }
    public static String getUserId(Context context){
        return context.getSharedPreferences("userId", Activity.MODE_PRIVATE).getString("id", "");
    }
    public static String getScanUrl(Context context){
        return context.getSharedPreferences("userId", Activity.MODE_PRIVATE).getString("scanUrl", "").toString();
    }
    public static String getPayTopAmt(Context context){
        return context.getSharedPreferences("userId", 0).getString("payTopAmt", "").toString();
    }
    public static String getNoLimit(){
        return "900000000";
    }
    public static String getLoginShopId(Context context){
        return context.getSharedPreferences("userId", 0).getString("loginShopId", "").toString();
    }
    public static final String ORDER_PAY = "APOS1";
    public static final String AMT_PAY = "APOS2";

    public static final String ALI_ORDER_PAY = "ALIPAY1";
    public static final String ALI_AMT_PAY = "ALIPAY2";

    public static String getDate(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
    }
    public static String getDateYMD() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
    }
    public static String getVersion(Context context) {
            try {
                PackageManager manager = context.getPackageManager();
                PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
                String version = info.versionName;
                return "V" + version;
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
    }
    public static String getTime(){
        Date date=new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }
    public static String getAppId() {
        return "b2838fee6f194d4d844d2c4aa25dfb50";
    }
}
