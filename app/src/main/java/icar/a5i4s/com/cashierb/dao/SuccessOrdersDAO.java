package icar.a5i4s.com.cashierb.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import icar.a5i4s.com.cashierb.helper.DBHelper;
import icar.a5i4s.com.cashierb.module.Order;

/**
 * Created by light on 2016/9/3.
 */
public class SuccessOrdersDAO {
    private static DBHelper helper;

    public SuccessOrdersDAO(Context context) {
        helper = new DBHelper(context);
    }

    public List<Order> getOrder(){
        SQLiteDatabase db = helper.getWritableDatabase();
        List<Order> orderList = new ArrayList<Order>();
        try {
            Cursor cursor = db.query("success_orders",new String[]{"id,orderId,appName,transId,resultCode,resultMsg,transData"},null,null,null,null,null,null);
            while (cursor.moveToNext()) {
                orderList.add(new Order(cursor.getInt(cursor.getColumnIndex("id")),
                        cursor.getString(cursor.getColumnIndex("orderId")),
                        cursor.getString(cursor.getColumnIndex("appName")),
                        cursor.getString(cursor.getColumnIndex("transId")),
                        cursor.getString(cursor.getColumnIndex("resultCode")),
                        cursor.getString(cursor.getColumnIndex("resultMsg")),
                        cursor.getString(cursor.getColumnIndex("transData"))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return orderList;
    }
    //添加补送单
    public boolean setOrder(Order order) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "insert into success_orders (orderId, appName, transId, resultCode, resultMsg, transData, userId) values (?,?,?,?,?,?,?)";
        try {
            db.execSQL(sql, new Object[]{order.getOrderId(), order.getAppName(), order.getTransId(), order.getResultCode(), order.getResultMsg(), order.getTransData(), order.getUserId()});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }

    public boolean deleteOrder() {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "delete from success_orders";
        try {
            db.execSQL(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }
}