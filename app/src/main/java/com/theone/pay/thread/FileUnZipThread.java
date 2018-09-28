package com.theone.pay.thread;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import com.theone.pay.MyApplication;
import com.theone.pay.db.DBUtils;
import com.theone.pay.model.TempObj;
import com.theone.pay.utils.MyRequests;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 对文件进行解密的线程
 */
public class FileUnZipThread extends Thread {
    private static final String TAG = "FileUnZipThread";
    public  boolean isEixt = false;//线程是否退出

    private String filepath;
    String ccode;//订单号哦
    Handler mHandler2;//用handler进行处理
    Context context;
    long lasttxtime = 0;//最后提醒时间(1秒只提醒一次)
    int unzipcount = 0;//处理文件数
    public boolean isFast = false;
    long minFileLen = 1024 * 1024 * 10;//最小的文件，不小于10M
    long startime=System.currentTimeMillis();
    String[] FileExts = new String[]{".MP4", ".AVI", ".MKV", ".DMP"};//文件后缀要求
    String sussFiles = "";//成功的文件名称放在这里



    Map<String, String> hasMap = new HashMap<String, String>();//记录处理过的

    public FileUnZipThread(Context context,String ccode, String filepath, Handler mHandler2) {
        this.context = context;
        this.ccode=ccode;
        this.filepath = filepath;
        this.mHandler2 = mHandler2;
    }

    /*
     * 继承重写Thread的方法
     * 实现异步处理
     * */
    @Override
    public void run() {
        MyApplication appcon = null;
        try {
            //全局变量
            appcon = MyApplication.getApplication();
            if (appcon.isUnzipRuning()) {
                mHandler2.obtainMessage(0, "正在进行解密操作，请稍候...").sendToTarget();
                return;
            }
            appcon.setUnzipRuning(true);
            //判断是否有权限进行操作
            if(!checkAuth()){
                return;
            }

            //1.判断是否有filepath，如果有，则直接执行
            if (filepath != null && filepath.length() > 2) {
                unzip(new File(filepath));
                return;
            }
            //2.如果没有filepath,则搜索符合条件的文件
            if(isFast){
                this.fastSearchUnzip();
            }else{
                searchUnzip();
            }

        } catch (Exception e) {

        } finally {
            if (appcon != null) {
                appcon.setUnzipRuning(false);
            }
        }
        long usetime = (System.currentTimeMillis()-startime)/1000;
        mHandler2.obtainMessage(2, "解密文件完成，成功解密文件数："+unzipcount+" ,耗时："+usetime+"秒\n"+sussFiles).sendToTarget();
    }

    //校验是否有权限哦
    private boolean checkAuth(){
        //1.查询唯一的id;
        TempObj tempObj = null;
        List<TempObj> list  = DBUtils.listObj(TempObj.TYPE_PHONE_PID);
        if(list.isEmpty()){
            tempObj = new TempObj();
            tempObj.setTemptype(TempObj.TYPE_PHONE_PID);
            DBUtils.save(tempObj);
        }else{
            tempObj=list.get(0);
        }
        try{
            String url = "http://47.106.70.111/movie/checkUnMarkZip.action?ccode=" + ccode + "&mac=" + tempObj.getTempkey();
            MyRequests req = new MyRequests();
            String body = req.get(url);
            Log.i(TAG,"http请求数据返回:"+body);
            if(body.indexOf("ok")!=-1){
                //如果失败，则返回失败的提示语
                mHandler2.obtainMessage(1, body).sendToTarget();
                //2.网络查询是否有权限
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            mHandler2.obtainMessage(1, "网络请求异常，请稍候再试...").sendToTarget();
        }
        return false;
    }



    /**
     * 对文件进行解密
     * 解密单个文件
     */
    private void unzip(File fp) {
        sendTXMessage(1, "正在解密文件:" + fp);
        try {
            //文件后缀判断
            if(fp.getName().toUpperCase().indexOf(".DMP")==-1){
                mHandler2.obtainMessage(1, "解密文件格式不对，请确认解密文件的格式是否正确").sendToTarget();
                return;
            }
            //文件大小判断
            if (fp.length() < minFileLen){
                mHandler2.obtainMessage(1, "解密文件格式不对，请确认解密文件的格式是否正确").sendToTarget();
                return;
            }
            UnzipFile(fp);

        } catch (Exception e) {

        }
    }

    public void checkAndUnzipFile(File fp) throws IOException, JSONException {
        sendTXMessage(1,"正在搜索文件:"+fp.getPath());
        //文件后缀判断
        if(fp.getName().toUpperCase().indexOf(".DMP")==-1){
            return;
        }
        //文件大小判断
        if (fp.length() < minFileLen){
            return;
        }
        UnzipFile(fp);
    }

    /**
     * 对文件进行解密
     *
     * @param fp
     */
    public void UnzipFile(File fp) throws IOException, JSONException {
        FileInputStream inputStream=null;
        FileOutputStream outputStream=null;
        try{
            //sendTXMessage(1, "正在搜索文件：" + fp.getAbsolutePath());
            inputStream = new FileInputStream(fp);
            //读取前面4位做头部长度
            byte[] hlenb= new byte[4];
            inputStream.read(hlenb);
            int hlen = byteArrayToInt(hlenb);
            //头部长度判断，头部长度如果不符合，直接返回
            if(hlen>1024*10||hlen<10){
                return;
            }
            //头部长度符合，则读取头部
            byte[] hb= new byte[hlen];
            inputStream.read(hb);
            for(int i=0;i<hb.length;i++){
                hb[i]=(byte)(hb[i] -0x05);
                //hb[i]=(byte)(hb[i] & 0xFF -0x05);
            }
            String hstr = new String(hb,"utf-8");
            if(hstr.indexOf("Byteadd")==-1){
                return;
            }
            JSONObject jsonObject = new JSONObject(hstr);
            byte Byteadd = (byte)jsonObject.getInt("Byteadd");
            String Sname = jsonObject.getString("Sname");
            String Smd532 = jsonObject.getString("Smd532");

            mHandler2.obtainMessage(1, "正在解密文件:" + fp.getAbsolutePath()).sendToTarget();
            //int Hreadlen = jsonObject.getInt("Hreadlen");
            //输出流
            //System.out.println(fp.getPath());
            String outFile = fp.getParent()+"/"+Sname;
            outputStream = new FileOutputStream(outFile);
            byte[] temp = new byte[1024*32];
            int rn=0;
            while((rn=inputStream.read(temp))>0){
                byte[] temp2 = new byte[rn];
                for(int i=0;i<rn;i++){
                    temp2[i]=(byte)(temp[i]-Byteadd);
                }
                outputStream.write(temp2);
            }
            inputStream.close();
            //删除源文件
            fp.delete();
            //保存成功记录
            TempObj tempObj = new TempObj();
            tempObj.setTempkey(Smd532);
            tempObj.setTempvalue(outFile);
            DBUtils.save(tempObj);
            unzipcount++;
            sussFiles=outFile+";";
        }catch(Exception e){
            mHandler2.obtainMessage(1, "解密文件出错:" + fp.getAbsolutePath()+"错误信息："+e.toString()).sendToTarget();
            return;
        }finally {
            if(outputStream!=null){
                outputStream.close();
            }
            if(inputStream!=null){
                inputStream.close();
            }
        }
        mHandler2.obtainMessage(1, "解密完成:" + fp.getAbsolutePath()).sendToTarget();
        return;
    }

    private   int byteArrayToInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    /**
     * what 0,1
     * 发送提醒消息
     */
    private void sendTXMessage(int what, String msg) {
        long currTime = System.currentTimeMillis();
        if (currTime - lasttxtime > 1000) {
            lasttxtime = currTime;
            mHandler2.obtainMessage(what, msg).sendToTarget();
        }
    }

    /**
     * 搜索，并解密文件
     * 搜索到一个就解密一个
     */
    private void searchUnzip() {
        //
        searchUnzip(new File("/sdcard" ));
        List<String> listp = listAllPath();
        for (String p : listp) {
            if (p == null || p.length() < 2) {
                continue;
            }
            searchUnzip(new File("/" + p));
        }
    }

    /**
     * 快速搜索
     */
    private void fastSearchUnzip() {
        try{
            List<String> list = searchKeyWord(".dmp");
            for(String p:list){
                checkAndUnzipFile(new File( p));
            }

        }catch (Exception e){
            //如果不支持快速，则进行慢速搜索
            searchUnzip();
            //mHandler2.obtainMessage(1, "快速搜索失败，当前手机不支持快速搜索:" ).sendToTarget();
        }
    }

    //快速搜索
    private List<String> searchKeyWord(String keyword) {
        List<String> fileList = new ArrayList<String>();
        ContentResolver resolver = context.getContentResolver();

        Uri uri = MediaStore.Files.getContentUri("external");
        Cursor cursor = resolver.query(uri,
                new String[]{MediaStore.Files.FileColumns.DATA},
                MediaStore.Files.FileColumns.TITLE + " LIKE '%" + keyword + "%'",
                null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
                fileList.add(path);
            }
        }
        cursor.close();
        return fileList;
    }

    private void searchUnzip(File path) {
        if (hasMap.containsKey("p_" + path.getPath())) {
            return;
        }
        if(isEixt){
            return;
        }
        hasMap.put("p_" + path.getPath(), null);
        sendTXMessage(1, "正在搜索目录：" + path.getPath());
        //Log.i("MyActivity","正在搜索目录:"+path.getPath());
        File fs[] = path.listFiles();
        for (File f : fs) {
            if(isEixt){
                return;
            }
            if (f.isDirectory()) {
                searchUnzip(f);
            } else {
                try {
                    //Log.i(TAG,"正在搜索文件:"+f.getPath());
                    checkAndUnzipFile(f);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }
    }


    /**
     * 查询所有可能的目录
     * /sdcard/
     * /storage
     * /storage
     */
    private List<String> listAllPath() {
        List<String> listp = new ArrayList<String>();
        try {
            listp.add(getRootPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)));
        } catch (Exception e) {

        }
        try {
            listp.add(getRootPath(Environment.getExternalStorageDirectory()));
        } catch (Exception e) {

        }
        try {
            listp.add(getRootPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)));
        } catch (Exception e) {

        }
        try {
            listp.add(getRootPath(Environment.getExternalStorageDirectory()));
        } catch (Exception e) {

        }
        try {
            listp.add(getRootPath(context.getExternalFilesDir(null)));
        } catch (Exception e) {

        }
        try {
            listp.add(getRootPath(context.getExternalCacheDir()));
        } catch (Exception e) {

        }
        try {
            listp.add(getRootPath(context.getFilesDir()));
        } catch (Exception e) {

        }
        try {
            listp.add(getRootPath(context.getCacheDir()));
        } catch (Exception e) {

        }
        return listp;
    }

    private String getRootPath(File file) {
        if (file.exists()) {
            String ap = file.getAbsolutePath();
            String aps[] = ap.split("/");
            if (aps.length > 1) {
                return aps[1];
                //return ap;
            }
        }
        return null;
    }
}
