package com.theone.pay.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.theone.pay.MyApplication;
import com.theone.pay.model.TempObj;

import java.util.ArrayList;
import java.util.List;

public class DBUtils {
    private static final String TAG = "DBUtils";
    /**
     * 根据key查询记录
     * @param tempkey
     * @return
     */
    public static TempObj getByKey(String tempkey){
        DatabaseHelper dbHelper =  new DatabaseHelper(MyApplication.getApplication());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try{
            Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, null, "tempkey=?", new String[]{tempkey}, null, null, null);
            if (cursor.moveToNext()) {
                TempObj t = new TempObj();
                t.setTempkey(cursor.getString(cursor.getColumnIndex("tempkey")));
                t.setTemptype(cursor.getString(cursor.getColumnIndex("temptype")));
                t.setTempvalue(cursor.getString(cursor.getColumnIndex("tempvalue")));
                t.setCreatetime(cursor.getString(cursor.getColumnIndex("createtime")));
                cursor.close();
                return t;
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }finally {
            //db.close();
            //dbHelper.close();
            Log.i(TAG,"getByKey---------");
        }
        return null;
    }

    /**
     * 保存记录
     * @param obj
     */
    public static void save(TempObj obj){
        TempObj _obj  = getByKey(obj.getTempkey());
        DatabaseHelper dbHelper =  new DatabaseHelper(MyApplication.getApplication());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try{
            if(_obj==null){
                ContentValues contentValues = new ContentValues();
                contentValues.put("tempkey",obj.getTempkey());
                contentValues.put("temptype",obj.getTemptype());
                contentValues.put("tempvalue",obj.getTempvalue());
                contentValues.put("createtime",obj.getCreatetime());
                db.insert(DatabaseHelper.TABLE_NAME,null,contentValues);
            }else{
                ContentValues contentValues = new ContentValues();
                //contentValues.put("tempkey",obj.getTempkey());
                contentValues.put("temptype",obj.getTemptype());
                contentValues.put("tempvalue",obj.getTempvalue());
                contentValues.put("createtime",obj.getCreatetime());
                db.update(DatabaseHelper.TABLE_NAME,contentValues,"tempkey=?",new String[]{obj.getTempkey()});
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }finally {
            //db.close();
            //dbHelper.close();
            Log.i(TAG,"save---------");
        }
        //db.close();
    }

    /**
     * 删除记录
     * @param tempkey
     */
    public static void delete(String tempkey){
        DatabaseHelper dbHelper =  new DatabaseHelper(MyApplication.getApplication());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try{
            db.delete(DatabaseHelper.TABLE_NAME ,"tempkey=?",new String[]{tempkey});

        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }finally {
            //db.close();
            //dbHelper.close();
            Log.i(TAG,"delete---------");
        }
    }

    /**
     * 查询某类的记录
     * @param temptype
     * @return
     */
    public static List<TempObj> listObj(String temptype){
        DatabaseHelper dbHelper =  new DatabaseHelper(MyApplication.getApplication());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<TempObj> list= new ArrayList<TempObj>();
        try{
            Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, null, "temptype=?", new String[]{temptype}, null, null, null);
            while (cursor.moveToNext()) {
                TempObj t = new TempObj();
                t.setTempkey(cursor.getString(cursor.getColumnIndex("tempkey")));
                t.setTemptype(cursor.getString(cursor.getColumnIndex("temptype")));
                t.setTempvalue(cursor.getString(cursor.getColumnIndex("tempvalue")));
                t.setCreatetime(cursor.getString(cursor.getColumnIndex("createtime")));
                list.add(t);
            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }finally {
            //db.close();
            //dbHelper.close();
            Log.i(TAG,"listObj---------");
        }
        return list;
    }
}
