package com.theone.pay.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.ColorRes;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.theone.pay.LoginActivity;
import com.theone.pay.MainActivity;
import com.theone.pay.R;
import com.theone.pay.httpservice.PayService;
import com.theone.pay.model.PayLog;
import com.theone.pay.model.RetObject;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 支付日志明细窗口
 * 查询条件弹出窗口
 */
public class PayLogInfoPopupWindow {
    private View rootView;//根视图
    private View clickView;//点击按钮的视图
    private View popView;//弹出窗口的视图
    private Handler pHandler;


    //弹出窗口
    PopupWindow window;
    private PayLog log;

    private ProgressDialog loading;

    //视图对象
    Button but_confirm_pay ;
    Button but_notify ;
    TextView tv_notifyCount;
    TextView tv_notifyState;

    TextView tv_payState;
    TextView tv_payImgPrice;
    TextView tv_prodPrice;
    TextView tv_payExt2;
    TextView tv_payExt1;
    TextView tv_createtime;
    TextView tv_payType;
    TextView tv_payName;
    TextView tv_orderid;



    public PayLogInfoPopupWindow(View rootView, View clickView, View popView, Handler pHandler,PayLog log){
        this.rootView = rootView;
        this.clickView = clickView;
        this.popView = popView;
        this.pHandler= pHandler;
        this.log=log;
        initView();
        initViewValue();
    }

    private void initView(){
        loading = new ProgressDialog(popView.getContext());
        //顶部导航
        TopBar01 topBar = (TopBar01) popView.findViewById(R.id.topbar);
        topBar.setLeftButtonVisibility(true);
        topBar.setRightButtonVisibility(false);
        //事件监听
        topBar.setOnLeftAndRightClickListener(new TopBar01.OnLeftAndRightClickListener() {
            @Override
            public void OnLeftButtonClick(View v) {
                window.dismiss();
            }
            @Override
            public void OnRightButtonClick(View v) {
            }
        });

        //刷新按钮
        //下拉刷新组件
        RefreshLayout refreshLayout = (RefreshLayout)popView.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(final RefreshLayout refreshLayout1) {
                //线程刷新
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {

                        RetObject retobj = new PayService().queryByRid(log.getRid());
                        if(retobj.getSuccess() && retobj.getData()!=null){
                            log = (PayLog)retobj.getData();
                            tHandler.obtainMessage(0, "ok").sendToTarget();
                        }else{
                            tHandler.obtainMessage(-1, retobj.getMsg()).sendToTarget();
                        }
                        refreshLayout1.finishRefresh();
                    }
                }, 0);
                //refreshlayout.finishRefresh(2000/*,false*/);//传入 false 表示刷新失败
            }
        });

        //界面元素初始化
        but_confirm_pay = (Button) popView.findViewById(R.id.but_confirm_pay);
        but_notify = (Button) popView.findViewById(R.id.but_notify);

        tv_notifyCount = (TextView) popView.findViewById(R.id.tv_notifyCount);
        tv_notifyState = (TextView) popView.findViewById(R.id.tv_notifyState);
        tv_payState = (TextView) popView.findViewById(R.id.tv_payState);
        tv_payImgPrice = (TextView) popView.findViewById(R.id.tv_payImgPrice);
        tv_prodPrice = (TextView) popView.findViewById(R.id.tv_prodPrice);
        tv_payExt2 = (TextView) popView.findViewById(R.id.tv_payExt2);
        tv_payExt1= (TextView) popView.findViewById(R.id.tv_payExt1);
        tv_createtime = (TextView) popView.findViewById(R.id.tv_createtime);
        tv_payType = (TextView) popView.findViewById(R.id.tv_payType);
        tv_payName = (TextView) popView.findViewById(R.id.tv_payName);
        tv_orderid = (TextView) popView.findViewById(R.id.tv_orderid);

        //按钮事件
        //注册事件
        but_confirm_pay.setOnClickListener(new View.OnClickListener() {
                                                         @Override
                                                         public void onClick(View v) {
                                                             AlertDialog.Builder builder = new AlertDialog.Builder(popView.getContext());
                                                             builder.setTitle("是否确认收款");
                                                             builder.setIcon(android.R.drawable.stat_notify_error);
                                                             builder.setMessage("确认收款操作将把订单设置为已支付状态，并且不可回退，确认需要进行此操作？");
                                                             builder.setCancelable(true);
                                                             builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                                 @Override
                                                                 public void onClick(DialogInterface dialog, int which) {
                                                                     showLoading("正在进行手工确认收款处理，请稍候...");
                                                                     but_confirm_pay.setEnabled(false);
                                                                     //线程处理
                                                                     new Timer().schedule(new TimerTask() {
                                                                         @Override
                                                                         public void run() {
                                                                             RetObject retobj = new PayService().payCheck(log.getLogId(),log.getOrderid(),log.getRid());
                                                                             if(retobj.getSuccess()){
                                                                                 //成功后，刷新订单
                                                                                 RetObject retobj2 = new PayService().queryByRid(log.getRid());
                                                                                 tHandler.obtainMessage(100, "已成功确认收款").sendToTarget();
                                                                                 if(retobj2.getSuccess() && retobj2.getData()!=null){
                                                                                     log = (PayLog)retobj2.getData();
                                                                                 }
                                                                             }else{
                                                                                 tHandler.obtainMessage(-1, retobj.getMsg()).sendToTarget();
                                                                             }
                                                                         }
                                                                     }, 0);
                                                                 }
                                                             });
                                                             builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                                 @Override
                                                                 public void onClick(DialogInterface dialog, int which) {
                                                                 }
                                                             });
                                                             builder.create().show();
                                                         }
                                                     }
        );
        but_notify.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View v) {
                                                   but_notify.setEnabled(false);
                                                   showLoading("正在进行发起通知回调，请稍候...");
                                                   //线程处理
                                                   new Timer().schedule(new TimerTask() {
                                                       @Override
                                                       public void run() {
                                                           RetObject retobj = new PayService().payNotify(log.getRid());
                                                           //无论成功失败，都刷新订单
                                                           RetObject retobj2 = new PayService().queryByRid(log.getRid());
                                                           if(retobj2.getSuccess() && retobj2.getData()!=null){
                                                               log = (PayLog)retobj2.getData();
                                                           }
                                                           if(retobj.getSuccess()){
                                                               tHandler.obtainMessage(100, "已发起回调通知").sendToTarget();
                                                           }else{
                                                               tHandler.obtainMessage(-1, retobj.getMsg()).sendToTarget();
                                                           }
                                                       }
                                                   }, 0);
                                               }
                                           }
        );
    }

    /**
     * 初始化界面的值
     */
    private void initViewValue(){
        this.tv_createtime.setText(log.getCreatetimeStr());
        if(log.getNotifyCount()!=null){
            tv_notifyCount.setText(log.getNotifyCount()+"");
        }
        tv_notifyState.setText(log.getNotifyStateStr());
        tv_payState.setText(log.getPayStateStr());

        tv_orderid.setText(log.getOrderid());
        if(log.getPayState()==1){
            tv_payState.setTextColor(Color.BLUE);
            tv_payImgPrice.setTextColor(Color.BLUE);
        }
        tv_payName.setText(log.getPayName());
        tv_prodPrice.setText("¥"+log.getProdPrice()+"");
        tv_prodPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);//中划线
        tv_payImgPrice.setText("¥"+log.getPayImgPrice()+"");
        tv_payType.setText(log.getPayTypeStr());
        if(log.getPayExt1()!=null){
            tv_payExt1.setText(log.getPayExt1());
        }
        tv_payExt2.setText(log.getPayExt2());

        //如果已经支付，不允许确认支付
        if(log.getPayState()==1){
            but_confirm_pay.setEnabled(false);
            but_notify.setEnabled(true);
        }else{
            but_confirm_pay.setEnabled(true);
            but_notify.setEnabled(false);
        }
        //如果未支付，不允许点击发起回调

    }

    //响应事件处理
    private Handler tHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            hideLoading();
            initViewValue();
            switch (msg.what) {
                case 0://刷新成功
                    break;
                case -1://登录失败
                    Toast.makeText(popView.getContext(),msg.obj.toString(),Toast.LENGTH_LONG).show();
                    break;
                case 100://只是提醒
                    Toast.makeText(popView.getContext(),msg.obj.toString(),Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };




    public void show(){
        //位置计算定位
        int width = rootView.getWidth();
        int height = rootView.getHeight();
        window=new PopupWindow(popView,  width,  height, true);
        // 设置PopupWindow的背景
        //window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // 设置PopupWindow是否能响应外部点击事件
        window.setOutsideTouchable(true);
        // 设置PopupWindow是否能响应点击事件
        window.setTouchable(true);
        // 显示PopupWindow，其中：
        // 第一个参数是PopupWindow的锚点，第二和第三个参数分别是PopupWindow相对锚点的x、y偏移
        window.showAtLocation(rootView, Gravity.NO_GRAVITY,0,0);
    }

    //加载中
    private void showLoading(String msg){
        //loading.setProgressStyle(ProgressDialog.STYLE_SPINNER); //默认就是小圆圈的那种形式
        loading.setMessage(msg);
        //loading.setCancelable(true);//默认true
        loading.setCanceledOnTouchOutside(false);//默认true
        loading.show();
    }

    //加载结束
    private void hideLoading(){
        loading.hide();
    }


}
