package com.theone.pay.receiver;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.theone.pay.LoginActivity;
import com.theone.pay.MainActivity;
import com.theone.pay.R;
import com.theone.pay.service.MyService;

/**
 * 广播接收者,静态注册的广播，动态广播不能放这里，
 * 部分广播需要动态注册，例如每分钟通知的广播，必须动态注册
 * 接收到下拉广播进行相关处理
 * 1.
 */
public class MyReceiver extends BroadcastReceiver {
    public static final String TAG = "BroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.i(TAG, "手机开机了...");
            Intent i = new Intent(context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
        if (intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            Log.i(TAG, "用户解锁了...");
        }
        //这个无法执行，每分钟的广播必须动态注册，在系统启动的代码里注册
        if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
            try{
                Log.i(TAG, "每分钟执行一次的广播...");
                //在这里写重新启动service的相关操作
                Intent i = new Intent(context,MyService.class);
                i.putExtra("data", "sendAllNotify");
                context.startService(i);
                Log.i(TAG, "每分钟执行一次的广播2...");
                //发送通知，激活
                sendTestNotify(context);
                Log.i(TAG, "每分钟执行一次的广播3...");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (intent.getAction().equals("com.theone.pay.service.destroy")) {
            //TODO
            Log.i(TAG, "重启service...");
            //在这里写重新启动service的相关操作
            Intent i = new Intent(context,MyService.class);
            context.startService(i);
        }
        Log.w(TAG, "in onCreate");
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
                new Intent(context, MainActivity.class),
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        //2.通过Notification.Builder来创建通知
        Notification.Builder myBuilder = new Notification.Builder(context);
        myBuilder.setContentTitle("请保持黑米支付开启")
                .setContentText("请保持黑米支付开启，并处于锁定状态，避免被系统回收")
                //.setSubText("黑米支付测试补充小行内容")
                .setTicker("请保持黑米支付开启")
                .setSmallIcon(R.mipmap.ic_launcher)
                //.setLargeIcon(R.mipmap.ic_launcher)
                .setDefaults(Notification.FLAG_ONLY_ALERT_ONCE)
                .setAutoCancel(true)//点击后取消
                .setWhen(System.currentTimeMillis())//设置通知时间
                .setPriority(Notification.PRIORITY_HIGH)//高优先级
                .setVisibility(Notification.VISIBILITY_PUBLIC);
                //.setContentIntent(pi);  //3.关联PendingIntent
        Notification  myNotification = myBuilder.build();
        //4.通过通知管理器来发起通知，ID区分通知
        myManager.notify(2, myNotification);
        Log.i(TAG,"send_Notification2");
    }
}
