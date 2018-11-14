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
public class ExceptionOrdersDAO {
    private static DBHelper helper;

    public ExceptionOrdersDAO(Context context) {
        helper = new DBHelper(context);
    }

    public List<Order> getOrder(){
        SQLiteDatabase db = helper.getWritableDatabase();
        List<Order> orderList = new ArrayList<Order>();
        try {
            Cursor cursor = db.query("exception_orders",new String[]{"id,orderId,orgTraceNo,onlyLabel"},null,null,null,null,null,null);
            while (cursor.moveToNext()) {
                orderList.add(new Order(cursor.getInt(cursor.getColumnIndex("id")),
                        cursor.getString(cursor.getColumnIndex("orderId")),
                        cursor.getString(cursor.getColumnIndex("orgTraceNo")),
                        cursor.getString(cursor.getColumnIndex("onlyLabel"))));
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            db.close();
        }
        return orderList;
    }
    //添加补送单
    public boolean setOrder(Order order) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "insert into exception_orders (orderId, orgTraceNo, onlyLabel) values (?,?,?)";
        try {
            db.execSQL(sql, new Object[]{order.getOrderId(), order.getOrgTraceNo(), order.getOnlyLabel()});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }
    //查询总数
    public int getCount() {
        int count = 0;
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            Cursor cursor = db.query("exception_orders",new String[]{"count(*)"},null,null,null,null,null,null);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return count;
    }

    public boolean deleteOrder() {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "delete from exception_orders";
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