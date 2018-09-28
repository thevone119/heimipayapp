package com.theone.pay.view;

import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioGroup;

import com.theone.pay.R;
import com.theone.pay.model.PayBusChange;
import com.theone.pay.model.PayLog;

/**
 * 账单查询的
 * 查询条件弹出窗口
 */
public class PayChangeQueryPopupWindow {
    private View rootView;//根视图
    private View clickView;//点击按钮的视图
    private View popView;//弹出窗口的视图
    private Handler mHandler;
    //弹出窗口
    PopupWindow window;



    Button but_sure ;
    Button but_reset ;
    RadioGroup rg_ctype;
    RadioGroup rg_createtime ;

    public PayChangeQueryPopupWindow(View rootView, View clickView, View popView, Handler mHandler){
        this.rootView = rootView;
        this.clickView = clickView;
        this.popView = popView;
        this.mHandler= mHandler;
        initView();
    }

    private void initView(){
        but_sure = popView.findViewById(R.id.but_sure);
        but_reset = popView.findViewById(R.id.but_reset);
        rg_ctype = popView.findViewById(R.id.rg_ctype);
        rg_createtime = popView.findViewById(R.id.rg_createtime);

        //注册事件
        but_reset.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            //
                                            PayBusChange.saveCurrQueryPayBusChange(new PayBusChange());
                                            initQueryValue();
                                        }
                                    }
        );
        but_sure.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            PayBusChange log = new PayBusChange();
                                            if(rg_createtime.getCheckedRadioButtonId()==R.id.rb_createtime_1){
                                                log.setCreatetime("1");
                                            }
                                            if(rg_createtime.getCheckedRadioButtonId()==R.id.rb_createtime_2){
                                                log.setCreatetime("2");
                                            }
                                            if(rg_ctype.getCheckedRadioButtonId()==R.id.rb_ctype_1){
                                                log.setCtype(1);
                                            }
                                            if(rg_ctype.getCheckedRadioButtonId()==R.id.rb_ctype_2){
                                                log.setCtype(2);
                                            }

                                            PayBusChange.saveCurrQueryPayBusChange(log);
                                            mHandler.obtainMessage(11, "重新查询").sendToTarget();
                                            window.dismiss();
                                        }
                                    }
        );
    }


    //初始化参数
    public void initQueryValue(){
        PayBusChange ql = PayBusChange.getCurrQueryPayBusChange();
        if(ql!=null){
            if(ql.getCreatetime()==null){
                rg_createtime.clearCheck();
            }else{
                if(ql.getCreatetime().equals("1")){
                    rg_createtime.check(R.id.rb_createtime_1);
                }
                if(ql.getCreatetime().equals("2")){
                    rg_createtime.check(R.id.rb_createtime_2);
                }
            }
            if(ql.getCtype()==null){
                rg_ctype.clearCheck();
            }else{
                if(ql.getCtype()==1){
                    rg_ctype.check(R.id.rb_ctype_1);
                }
                if(ql.getCtype()==2){
                    rg_ctype.check(R.id.rb_ctype_2);
                }
            }

        }
    }



    public void show(){
        //位置计算定位
        int[] location = new int[2];
        clickView.getLocationOnScreen(location);
        int width = rootView.getWidth()*2/3;
        int height = rootView.getHeight()*1/2;
        window=new PopupWindow(popView,  width,  height, true);
        // 设置PopupWindow的背景
        //window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // 设置PopupWindow是否能响应外部点击事件
        window.setOutsideTouchable(true);
        // 设置PopupWindow是否能响应点击事件
        window.setTouchable(true);
        // 显示PopupWindow，其中：
        // 第一个参数是PopupWindow的锚点，第二和第三个参数分别是PopupWindow相对锚点的x、y偏移
        window.showAtLocation(rootView, Gravity.NO_GRAVITY,location[0]-width-10,location[1]);
    }


}
