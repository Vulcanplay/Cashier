package icar.a5i4s.com.cashierb.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by hxsd on 2015/7/6.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final int VERSION = 3;//數據庫版本
    private static final String NAME = "orderRepair.db";//數據庫名稱

    public DBHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.execSQL("create table if not exists orders (" +
                "id integer primary key autoincrement," +
                "orderId varchar(20)," +
                "appName varchar(20)," +
                "transId varchar(20)," +
                "resultCode varchar(5)," +
                "resultMsg varchar(20)," +
                "transData varchar(255)," +
                "userId varchar(20)," +
                "onlyLabel varchar(255))");
        db.execSQL("create table if not exists user (" +
                "id integer primary key autoincrement," +
                "userId varchar(20)," +
                "roleName varchar(20)," +
                "account varchar(40)," +
                "password varchar(40))");
        db.execSQL("create table if not exists success_orders (" +
                "id integer primary key autoincrement," +
                "orderId varchar(20)," +
                "appName varchar(20)," +
                "transId varchar(20)," +
                "resultCode varchar(5)," +
                "resultMsg varchar(20)," +
                "transData varchar(255)," +
                "userId varchar(20))");
        db.execSQL("create table if not exists exception_orders (" +
                "id integer primary key autoincrement," +
                "orderId varchar(50)," +
                "orgTraceNo varchar(50)," +
                "onlyLabel varchar(255))");
        db.execSQL("create table if not exists history_orders (" +
                "id integer primary key autoincrement," +
                "orderId varchar(20)," +
                "appName varchar(20)," +
                "transId varchar(20)," +
                "resultCode varchar(5)," +
                "resultMsg varchar(20)," +
                "transData varchar(255)," +
                "userId varchar(20)," +
                "orgTraceNo varchar(50)," +
                "cancelTraceNo varchar(50)," +
                "onlyLabel varchar(255)," +
                "msgText varchar(50)," +
                "time varchar(50)," +
                "amt varchar(50))");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists orders (" +
                "id integer primary key autoincrement," +
                "orderId varchar(20)," +
                "appName varchar(20)," +
                "transId varchar(20)," +
                "resultCode varchar(5)," +
                "resultMsg varchar(20)," +
                "transData varchar(255)," +
                "userId varchar(20))");
        db.execSQL("create table if not exists user (" +
                "id integer primary key autoincrement," +
                "userId varchar(20)," +
                "roleName varchar(20)," +
                "account varchar(40)," +
                "password varchar(40))");
        db.execSQL("create table if not exists success_orders (" +
                "id integer primary key autoincrement," +
                "orderId varchar(20)," +
                "appName varchar(20)," +
                "transId varchar(20)," +
                "resultCode varchar(5)," +
                "resultMsg varchar(20)," +
                "transData varchar(255)," +
                "userId varchar(20))");
        db.execSQL("create table if not exists exception_orders (" +
                "id integer primary key autoincrement," +
                "orderId varchar(50)," +
                "orgTraceNo varchar(50)," +
                "onlyLabel varchar(255))");

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == oldVersion) {
            return;
        }
        db.execSQL("ALTER TABLE orders ADD onlyLabel varchar(255)");
    }
}
