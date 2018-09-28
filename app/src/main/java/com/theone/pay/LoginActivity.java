package com.theone.pay;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.theone.pay.httpservice.PayService;
import com.theone.pay.model.PayBus;
import com.theone.pay.model.RetObject;
import com.theone.pay.utils.EditTextClearTools;


/**
 * 登录页面
 */
public class LoginActivity extends AppCompatActivity {
    private EditText etUserName;
    private EditText etUserPassword;
    private Button btnLogin;
    private ProgressDialog loading;
    private Handler mHandler;//用handler进行处理

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_login);
        init();
        //如果已经有商户，则直接完成登录
        if(PayBus.getCurrPayBus()!=null){
            mHandler.obtainMessage(0, "登录成功").sendToTarget();
        }
    }

    /**
     * 注册UI控件
     */
    private void init(){
        etUserName = (EditText) findViewById(R.id.et_userName);
        etUserPassword = (EditText) findViewById(R.id.et_password);
        btnLogin = (Button) findViewById(R.id.btn_login);
        loading = new ProgressDialog(this);
        ImageView unameClear = (ImageView) findViewById(R.id.iv_unameClear);
        ImageView pwdClear = (ImageView) findViewById(R.id.iv_pwdClear);
        EditTextClearTools.addClearListener(etUserName,unameClear);
        EditTextClearTools.addClearListener(etUserPassword,pwdClear);

        //响应事件处理
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                hideLoading();
                switch (msg.what) {
                    case 0://登录成功,跳转到首页
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    case -1://登录失败
                        Toast.makeText(LoginActivity.this,msg.obj.toString(),Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        //注册事件
        btnLogin.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            startLogin();
                                        }
                                    }
        );
    }


    //登录线程处理
    private void startLogin(){
        showLoading();
        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    String acc = etUserName.getText().toString();
                    String pwd = etUserPassword.getText().toString();
                    if(acc==null||acc.length()<3){
                        mHandler.obtainMessage(-1, "请输入用户账号").sendToTarget();
                        return;
                    }
                    if(pwd==null||pwd.length()<3){
                        mHandler.obtainMessage(-1, "请输入用户密码").sendToTarget();
                        return;
                    }
                    //登录认证
                    RetObject ret = new PayService().login(acc,pwd);
                    if(ret.getSuccess()){
                        mHandler.obtainMessage(0, "登录成功").sendToTarget();
                    }else{
                        mHandler.obtainMessage(-1, ret.getMsg()).sendToTarget();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.obtainMessage(-1, "系统异常").sendToTarget();
                }
            }
        };
        t.start();
    }

    //加载中
    private void showLoading(){
        //loading.setProgressStyle(ProgressDialog.STYLE_SPINNER); //默认就是小圆圈的那种形式
        loading.setMessage("正在登录，请稍候...");
        //loading.setCancelable(true);//默认true
        loading.setCanceledOnTouchOutside(false);//默认true
        loading.show();
    }

    //加载结束
    private void hideLoading(){
        loading.hide();
    }



}
