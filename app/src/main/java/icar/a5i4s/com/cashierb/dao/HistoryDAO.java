package icar.a5i4s.com.cashierb.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import icar.a5i4s.com.cashierb.helper.DBHelper;
import icar.a5i4s.com.cashierb.helper.Tools;
import icar.a5i4s.com.cashierb.module.Order;

/**
 * Created by light on 2016/9/3.
 */
public class HistoryDAO {
    private static DBHelper helper;

    public HistoryDAO(Context context) {
        helper = new DBHelper(context);
    }

    public List<Order> getHistoryOrder(){
        SQLiteDatabase db = helper.getWritableDatabase();
        List<Order> orderList = new ArrayList<Order>();
        Order o;
        try{
            Cursor cursor = db.query("history_orders",new String[]{"id,orderId,appName,transId,resultCode,resultMsg,transData,userId,orgTraceNo,onlyLabel,msgText,time,amt,cancelTraceNo"},null,null,null,null,null,null);
            while (cursor.moveToNext()) {
                o = new Order(cursor.getInt(cursor.getColumnIndex("id")),
                        cursor.getString(cursor.getColumnIndex("orderId")),
                        cursor.getString(cursor.getColumnIndex("appName")),
                        cursor.getString(cursor.getColumnIndex("transId")),
                        cursor.getString(cursor.getColumnIndex("resultCode")),
                        cursor.getString(cursor.getColumnIndex("resultMsg")),
                        cursor.getString(cursor.getColumnIndex("transData")),
                        cursor.getString(cursor.getColumnIndex("userId")),
                        cursor.getString(cursor.getColumnIndex("orgTraceNo")),
                        cursor.getString(cursor.getColumnIndex("onlyLabel")),
                        cursor.getString(cursor.getColumnIndex("msgText")),
                        cursor.getString(cursor.getColumnIndex("time")),
                        cursor.getString(cursor.getColumnIndex("amt")));
                o.setCancelTraceNo(cursor.getString(cursor.getColumnIndex("cancelTraceNo")));
                orderList.add(o);
            }

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            db.close();
        }
        return orderList;
    }
    //添加
    public boolean setHistoryOrderBefore(Order order) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "insert into history_orders (orderId, userId, onlyLabel, time, amt, orgTraceNo, msgText) values (?,?,?,?,?,?,?)";
        try {
            db.execSQL(sql, new Object[]{order.getOrderId(), order.getUserId(), order.getOnlyLabel(), Tools.getDate(),order.getAmt(),order.getOrgTraceNo() == null ? "" : order.getOrgTraceNo(),order.getMsgText()});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }
    //更新
    public int updateHistoryOrderBeforeMsgText(String onlyLabel, String msgText) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int i = 0;
        try {
            ContentValues cv = new ContentValues();
            cv.put("msgText", msgText);
            String[] args = {String.valueOf(onlyLabel)};
            i = db.update("history_orders", cv, "onlyLabel=?",args);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            db.close();
        }
        return i;
    }
    //更新
    public int updateHistoryOrderBefore(Order order, String msgText) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int i = 0;
        try {
            ContentValues cv = new ContentValues();
            cv.put("appName", order.getAppName());
            cv.put("transId", order.getTransId());
            cv.put("resultCode", order.getResultCode());
            cv.put("resultMsg", order.getResultMsg());
            cv.put("transData", order.getTransData());
            cv.put("orgTraceNo", order.getOrgTraceNo());
            cv.put("cancelTraceNo", order.getCancelTraceNo() == null ? "" : order.getCancelTraceNo());
            cv.put("msgText", msgText);
            String[] args = {String.valueOf(order.getOnlyLabel())};
            i = db.update("history_orders", cv, "onlyLabel=?",args);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            db.close();
        }
        return i;
    }
    //更新
    public int updateHistoryOrderBeforeForRevoke(Order order, String msgText) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int i = 0;
        try {
            ContentValues cv = new ContentValues();

            cv.put("appName", order.getAppName());
            cv.put("transId", order.getTransId());
            cv.put("resultCode", order.getResultCode());
            cv.put("resultMsg", order.getResultMsg());
            cv.put("transData", order.getTransData());
            cv.put("cancelTraceNo", order.getCancelTraceNo() == null ? "" : order.getCancelTraceNo());
            cv.put("amt", order.getAmt());
            cv.put("msgText", msgText);
            String[] args = {String.valueOf(order.getOnlyLabel())};
            i = db.update("history_orders", cv, "onlyLabel=?",args);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            db.close();
        }
        return i;
    }
    public int updateHistoryOrderBeforeForRepair(String onlyLabel, String msgText) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int i = 0;
        try {
            ContentValues cv = new ContentValues();
            cv.put("msgText", msgText);
            String[] args = {String.valueOf(onlyLabel)};
            i = db.update("history_orders", cv, "onlyLabel=?",args);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            db.close();
        }
        return i;
    }
    //查询总数
    public int getHistoryCount() {
        int count = 0;
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cs = db.query("history_orders",new String[]{"count(*)"},null,null,null,null,null,null);
        if (cs.moveToFirst()) {
            count = cs.getInt(0);
        }
        return count;
    }
    public String getAmtForOrg(String org){
        SQLiteDatabase db = helper.getWritableDatabase();
        String amt = "";
        String sql = "select amt from history_orders where orgTraceNo=?";
        try {
            Cursor cursor = db.rawQuery(sql, new String[]{org});
            amt = cursor.getString(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return amt;
    }
    public boolean deleteHistoryOrder() {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "delete from history_orders";
        try {
            db.execSQL(sql, new Object[]{});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }

    public int getErrorCount() {
        int count = 0;
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            Cursor cs = db.rawQuery("select count(*) from history_orders where msgText='POS系统异常'", null);
            if (cs.moveToFirst()) {
                count = cs.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return count;
    }

    public String getOrderIdForTraceNo(String orgTraceNoInput) {
        String orderId = "";
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            Cursor cs = db.rawQuery("select orderId from history_orders where orgTraceNo='"+orgTraceNoInput+"'", null);
            if (cs.moveToFirst()) {
                orderId = cs.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return orderId;
    }
}