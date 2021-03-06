package com.theone.pay.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.theone.pay.MyApplication;
import com.theone.pay.db.wxDBHandle;
import com.theone.pay.httpservice.PayService;
import com.theone.pay.model.MyNotification;
import com.theone.pay.model.RetObject;
import com.theone.pay.model.SysConfig;
import com.theone.pay.utils.MyRequests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 后台服务，用于各种定时，非定时启动的后台服务
 * 这个Service有可能被系统回收了，还是用通知好点，靠
 */
public class MyService extends Service {
    public static final String TAG = "MyService";
    public static long lastRunTime = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "in onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(TAG, "in onStartCommand");
        Log.w(TAG, "MyService:" + this);
        String data = intent.getStringExtra("data");
        //发送服务
        if("sendAllNotify".equals(data)){
            try{
                runThread();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        Log.w(TAG, data );
        return START_REDELIVER_INTENT;
        //START_STICKY：如果service进程被kill掉，保留service的状态为开始状态，但不保留递送的intent对象。随后系统会尝试重新创建service，由于服务状态为开始状态，所以创建服务后一定会调用onStartCommand(Intent,int,int)方法。如果在此期间没有任何启动命令被传递到service，那么参数Intent将为null。
        //START_NOT_STICKY：“非粘性的”。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统不会自动重启该服务。
        //START_REDELIVER_INTENT：重传Intent。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统会自动重启该服务，并将Intent的值传入。
    }

    @Override
    public void onDestroy() {
        //销毁时发出广播，接收到广播后重启service
        //取消前台运行
        //stopForeground(true);
        //消耗的时候，重启，死循环

        Intent intent = new Intent("com.theone.pay.service.destroy");
        sendBroadcast(intent);
        super.onDestroy();
        Log.w(TAG, "in onDestroy");
    }

    /**
     * 定时执行的后台任务
     * 每分钟执行一次
     */
    public static void runThread(){
        if(System.currentTimeMillis()-lastRunTime<1000*30){
            Log.w(TAG, "重复执行");
            return;
        }
        Log.w(TAG, "执行定时任务");
        lastRunTime = System.currentTimeMillis();
        try{
            //开启线程处理
            new Thread(){
                @Override
                public void run() {
                    //1.请求测试网络
                    try{
                        MyRequests req =new MyRequests();
                        req.setConnectTimeout(2000);
                        req.setReadTimeout(3000);
                        req.get("http://www.baidu.com");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    //2.发送
                    try {
                        new PayService().sendAllNotify();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //加载微信数据
                    try {
                        if(SysConfig.getCurrSysConfig().listenerPay==1||SysConfig.getCurrSysConfig().listenerPay==2){
                            wxDBHandle.loadWXData();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //加载Xposed重发数据
                    try {
                        reLoadXposedData();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }catch(Exception e){

        }
    }

    public static void reLoadXposedData(){
        String path = Environment.getExternalStoragePublicDirectory("") + "/theonepay/";
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        //遍历文件进行读取
        File[] fs = file.listFiles();
        long currTime = System.currentTimeMillis();
        for(File f:fs){
            //大于5秒的文件才处理
            if(f.isFile()&&f.getName().startsWith("wxm_")&&(currTime-f.lastModified()>1000*5)){
                String coutent = "";
                try{
                    InputStream instream = new FileInputStream(f);
                    InputStreamReader inputreader
                            = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);

                    String line = "";
                    //分行读取
                    while ((line = buffreader.readLine()) != null) {
                        coutent += line;
                    }
                    instream.close();//关闭输入流
                    buffreader.close();
                    f.delete();
                }catch (Exception e){

                }
                try{
                    //
                    if(coutent!=null &&coutent.length()>3){
                        Gson gson = new Gson();
                        MyNotification msg = gson.fromJson(coutent,MyNotification.class);
                        if(msg!=null){
                            new PayService().saveAndSendNotify(msg);
                        }
                    }
                }catch (Exception e){

                }
            }
        }

    }

}
