package com.theone.pay.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    //类没有实例化,是不能用作父类构造器的参数,必须声明为静态
    private static final String DB_NAME = "com.theone01.db";//数据存储的文件名
    public static final String TABLE_NAME = "tempobj";//数据库表名
    private static final int version = 1; //数据库版本



    public DatabaseHelper(Context context) {
        //第三个参数CursorFactory指定在执行查询时获得一个游标实例的工厂类,设置为null,代表使用系统默认的工厂类
        super(context, DB_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" (tempkey varchar(64) primary key ,temptype varchar(32), tempvalue text,createtime varchar(20))");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("ALTER TABLE person ADD phone VARCHAR(12)"); //往表中增加一列
    }

    //锁住
    private void lock(){

    }

    //释放锁
    private void unlock(){

    }



}
