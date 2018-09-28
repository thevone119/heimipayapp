package com.theone.pay;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.theone.pay.service.MyService;
import com.theone.pay.utils.BaseUrlTools;

import java.util.Set;

/**
 * 欢迎页面，做一些广告，放一张图片
 */
public class WelcomeActivity extends AppCompatActivity {
    private static final String TAG="WelcomeActivity";
    private ProgressDialog loading;
    private boolean isshow = false;

    /**
     * 创建
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_welcome);
        //如果监听权限没有打开，则去到设置页面
        if(!isNotificationListenerEnabled()){
            openNotificationListenSettings();
        }

    }

    /**
     * 显示
     */
    @Override
    protected void onResume() {
        super.onResume();
        isshow = true;
        init();
    }

    @Override
    public void onPause() {
        super.onPause();
        isshow = false;
    }



    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            hideLoading();
            getHome();
            super.handleMessage(msg);
        }
    };

    /**
     * 初始化一些必要的数据
     */
    private void init(){
        loading = new ProgressDialog(this);
        //这里初始化服务器的url,采用线程的方式进行初始化
        BaseUrlTools.init();
        //showLoading();
        //多线程判断是否已经初始化完成
        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    for(int i=0;i<300;i++){
                        Thread.sleep(1000*1);
                        //如果已完成初始化，并且过去了2秒
                        if(BaseUrlTools.BASE_URL!=null && i>2 && isshow){
                            handler.obtainMessage(0, "已完成url初始化").sendToTarget();
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        //等待3秒
        //handler.sendEmptyMessageDelayed(0,3000);
    }


    public void getHome(){
        loading.dismiss();
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    //加载中
    private void showLoading(){
        //loading.setProgressStyle(ProgressDialog.STYLE_SPINNER); //默认就是小圆圈的那种形式
        loading.setMessage("正在初始化相关网络信息，请稍候...");
        //loading.setCancelable(true);//默认true
        loading.setCanceledOnTouchOutside(false);//默认true
        loading.show();
    }

    //加载结束
    private void hideLoading(){
        loading.hide();
    }

    //检查监听是否有权限
    public boolean isNotificationListenerEnabled() {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(this);
        if (packageNames.contains(this.getPackageName())) {
            return true;
        }
        return false;
    }

    /**
     * 打开监听设置页面
     */
    public void openNotificationListenSettings() {
        try {
            Intent intent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            } else {
                intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            }
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
