package com.theone.pay.notify;

import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.theone.pay.MyConfigFragment;
import com.theone.pay.httpservice.PayService;
import com.theone.pay.model.MyNotification;
import com.theone.pay.model.SysConfig;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Android在4.3的版本中(即API 18)加入了NotificationListenerService，根据SDK的介绍可以知道，
 * 当系统收到新的通知或者通知被删除时，会触发NotificationListenerService的回调方法。
 * 同时在Android 4.4 中新增了Notification.extras 字段，也就是说可以使用NotificationListenerService获取系统通知具体信息，
 * 这在以前是需要用反射来实现的
 *
 * 经过测试，发现是可以实现的
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MyNotificationListenerService extends NotificationListenerService {

    private static final String TAG="NotificationListener";
    private static Map<String,String> hasMap = new HashMap<String,String>();

    /**
     * 接收到系统消息
     * @param sbn
     */
    @Override
    public void onNotificationPosted(final StatusBarNotification sbn) {
        Log.i(TAG,"onNotificationPosted PackageName " + sbn.getPackageName());
        if("com.theone.pay".equals(sbn.getPackageName())){
            MyConfigFragment.lastPostTime = sbn.getPostTime();
        }
        if(hasMap==null){
            hasMap = new HashMap<String,String>();
        }
        if(SysConfig.getCurrSysConfig().listenerPay!=1){
            Log.i(TAG,"非通知监控，不处理 " );
            return;
        }
        ////开启线程处理
        ////线程处理
        try{
            new Thread(){
                @Override
                public void run() {
                    try {
                        doNotification(sbn,true);
                        //返回当前系统的所有的通知
                        StatusBarNotification[] sbns = getActiveNotifications();
                        for(StatusBarNotification s:sbns){
                            doNotification(s,false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 处理单个通知
     * @param sbn
     */
    private void doNotification(StatusBarNotification sbn,boolean isnew){
        String skey = sbn.getId()+"_"+sbn.getPackageName()+"_"+sbn.getPostTime();
        if(hasMap.containsKey(skey)){
            return;
        }
        hasMap.put(skey,null);
        MyNotification myn = new MyNotification(sbn);
        //如果是新的通知，时间和手机时间差异超过10秒，则使用手机的时间，避免时间出错
        if(isnew){
            Log.i(TAG, "notification ：" + myn.toString());
            if(Math.abs(new Date().getTime()-myn.getPostTime())>1000*10){
                Log.w(TAG,"time error " + sbn.getPackageName());
                myn.setPostTime(new Date().getTime());
            }
        }
        //2天以内
        //Log.i(TAG, "notification ：" + myn.toString());
        //发送并保存
        new PayService().saveAndSendNotify(myn);
    }


    /**
     * 系统消息被删除
     * @param sbn
     */
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG,"Notification removed");
    }


}
