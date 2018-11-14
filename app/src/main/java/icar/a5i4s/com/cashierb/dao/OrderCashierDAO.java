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
public class OrderCashierDAO {
    private static DBHelper helper;

    public OrderCashierDAO(Context context) {
        helper = new DBHelper(context);
    }

    public List<Order> getRepairOrder(){
        SQLiteDatabase db = helper.getWritableDatabase();
        List<Order> orderList = new ArrayList<Order>();
        try {
            Cursor cursor = db.query("orders",new String[]{"id,orderId,appName,transId,resultCode,resultMsg,transData,userId"},null,null,null,null,null,null);
            while (cursor.moveToNext()) {
                orderList.add(new Order(cursor.getInt(cursor.getColumnIndex("id")),
                        cursor.getString(cursor.getColumnIndex("orderId")),
                        cursor.getString(cursor.getColumnIndex("appName")),
                        cursor.getString(cursor.getColumnIndex("transId")),
                        cursor.getString(cursor.getColumnIndex("resultCode")),
                        cursor.getString(cursor.getColumnIndex("resultMsg")),
                        cursor.getString(cursor.getColumnIndex("transData")),
                        cursor.getString(cursor.getColumnIndex("userId"))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return orderList;
    }
    //添加补送单
    public boolean setRepairOrder(Order order) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "insert into orders (orderId, appName, transId, resultCode, resultMsg, transData, userId, onlyLabel) values (?,?,?,?,?,?,?,?)";
        try {
            db.execSQL(sql, new Object[]{order.getOrderId(), order.getAppName(), order.getTransId(), order.getResultCode(), order.getResultMsg(), order.getTransData(), order.getUserId(), order.getOnlyLabel()});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }
    //查询总数
    public int getRepairCount() {
        int count = 0;
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            Cursor cs = db.query("orders",new String[]{"count(*)"},null,null,null,null,null,null);
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

    public boolean deleteRepairOrderForId(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "delete from orders where id=?";
        try {
            db.execSQL(sql, new Object[]{id});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }

    public String getRepairOnlyLabelForId(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String onlyLabel = "";
        try {
            String sql = "select onlyLabel from orders where id=?";
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(id)});
            while (cursor.moveToNext()) {
                onlyLabel = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return onlyLabel;
    }
}