package com.theone.pay;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.theone.pay.model.PayBus;
import com.theone.pay.model.SysConfig;
import com.theone.pay.service.MyService;

/**
 * app上下文
 */
public class MyApplication extends Application {
    private static  final  String TAG = "MyApplication";
    private boolean unzipRuning  =false;//unzip是否正在进行解密操作

    public boolean isUnzipRuning() {
        return unzipRuning;
    }

    public void setUnzipRuning(boolean unzipRuning) {
        this.unzipRuning = unzipRuning;
    }

    private static MyApplication myApplication = null;

    public static MyApplication getApplication() {
        if(myApplication==null){
            myApplication = new MyApplication();
        }
        return myApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        //注册动态广播
        registerOtherReceiver();
        //注册监听
        toggleNotificationListenerService();
    }


    /**
     * 注册广播，部分无法静态注册的广播放在这里进行注册
     *
     */
    private void registerOtherReceiver(){
        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                //每分钟执行的广播
                if(action.equals(Intent.ACTION_TIME_TICK)) {
                    try{
                        Log.i(TAG, "每分钟执行一次的广播...");
                        //在这里写重新启动service的相关操作
                        Intent i = new Intent(context,MyService.class);
                        i.putExtra("data", "sendAllNotify");
                        context.startService(i);
                        Log.i(TAG, "每分钟执行一次的广播2...");
                        //发送通知，激活
                        SysConfig config = SysConfig.getCurrSysConfig();
                        if(PayBus.getCurrPayBus()!=null && config!=null && config.getKeepNotifyTime()==1){
                            sendTestNotify(context);
                            //打开微信
                            //startWX();
                            //打开支付宝

                        }
                        Log.i(TAG, "每分钟执行一次的广播3...");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        };
        IntentFilter filter=new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(receiver,filter);
    }

    /**
     * 重新注册监听
     */
    private  void toggleNotificationListenerService() {
        Log.e(TAG,"toggleNLS");
        try{
            PackageManager pm = this.getPackageManager();
            pm.setComponentEnabledSetting(
                    new ComponentName(this, com.theone.pay.notify.MyNotificationListenerService.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

            pm.setComponentEnabledSetting(
                    new ComponentName(this, com.theone.pay.notify.MyNotificationListenerService.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }catch (Exception e){

        }
    }

    /**
     * 发送测试通知
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void sendTestNotify(Context context){
        //1.获取系统的通知服务：
        NotificationManager myManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //3.定义一个PendingIntent，点击Notification后启动一个Activity
        PendingIntent pi = PendingIntent.getActivity(
                context,
                200,
                new Intent(context, WelcomeActivity.class),
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        //2.通过Notification.Builder来创建通知
        Notification.Builder myBuilder = new Notification.Builder(context);
        myBuilder.setContentTitle("请保持黑米支付开启")
                .setContentText("请保持黑米支付开启，并处于锁定状态，避免被系统回收")
                //.setSubText("黑米支付测试补充小行内容")
                .setTicker("请保持黑米支付开启")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setDefaults(Notification.FLAG_ONLY_ALERT_ONCE)
                .setAutoCancel(true)//点击后取消
                .setWhen(System.currentTimeMillis())//设置通知时间
                .setPriority(Notification.PRIORITY_HIGH)//高优先级
                .setVisibility(Notification.VISIBILITY_PUBLIC)
         .setContentIntent(pi);  //3.关联PendingIntent
        Notification  myNotification = myBuilder.build();
        //4.通过通知管理器来发起通知，ID区分通知
        myManager.notify(2, myNotification);
        Log.i(TAG,"send_Notification2");
    }

    /**
     * 打开微信
     */
    private void startWX(){
        try{

            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI"));
            //intent.putExtra("LauncherUI.From.Scaner.Shortcut", true);
            //intent.setFlags(335544320);
            intent.setAction("android.intent.action.VIEW");
            startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 打开支付宝
     */
    private void startZFB(){
        try{
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI"));
            //intent.putExtra("LauncherUI.From.Scaner.Shortcut", true);
            //intent.setFlags(335544320);
            intent.setAction("android.intent.action.VIEW");
            startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 获取IMEI号
     */
    public String getImei(){
        try{
            TelephonyManager TelephonyMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
            String szImei = TelephonyMgr.getDeviceId();
            return szImei;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 获取IMEI号
     */
    private String getImei2(){
        try{

            //android.os.SystemProperties.get(android.telephony.TelephonyProperties.PROPERTY_IMEI);
            //String szImei = TelephonyMgr.getDeviceId();
            //return szImei;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


}


