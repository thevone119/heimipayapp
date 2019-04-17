package com.theone.pay.view;

import android.os.Handler;
import android.support.annotation.IdRes;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RadioGroup;

import com.theone.pay.R;
import com.theone.pay.model.PayLog;
import com.theone.pay.model.SysConfig;

/**
 * 选择运行方式
 *
 */
public class SelectRunTypePopupWindow {
    private View rootView;//根视图
    private View clickView;//点击按钮的视图
    private View popView;//弹出窗口的视图
    private Handler mHandler;
    //弹出窗口
    PopupWindow window;



    RadioGroup rg_selectType;


    public SelectRunTypePopupWindow(View rootView, View clickView, View popView, Handler mHandler){
        this.rootView = rootView;
        this.clickView = clickView;
        this.popView = popView;
        this.mHandler= mHandler;
        initView();
    }

    private void initView(){
        rg_selectType = popView.findViewById(R.id.rg_selectType);
        initQueryValue();
        rg_selectType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                SysConfig config = SysConfig.getCurrSysConfig();
                if (checkedId == R.id.rb_ctype_0 && config.listenerPay!=0) //rb3设定为正确答案
                {
                    config.listenerPay=0;
                    SysConfig.saveCurrSysConfig(config);
                    mHandler.obtainMessage(12, "重设运行模式").sendToTarget();
                    //window.dismiss();
                }
                if (checkedId == R.id.rb_ctype_1 && config.listenerPay!=1) //rb3设定为正确答案
                {
                    config.listenerPay=1;
                    SysConfig.saveCurrSysConfig(config);
                    mHandler.obtainMessage(12, "重设运行模式").sendToTarget();
                    //window.dismiss();
                }
                if (checkedId == R.id.rb_ctype_2 && config.listenerPay!=2 ) //rb3设定为正确答案
                {
                    config.listenerPay=2;
                    SysConfig.saveCurrSysConfig(config);
                    mHandler.obtainMessage(12, "重设运行模式").sendToTarget();
                    //window.dismiss();
                }
                if (checkedId == R.id.rb_ctype_3 && config.listenerPay!=3 ) //rb3设定为正确答案
                {
                    config.listenerPay=3;
                    SysConfig.saveCurrSysConfig(config);
                    mHandler.obtainMessage(12, "重设运行模式").sendToTarget();
                    //window.dismiss();
                }
            }
        });


    }


    //初始化参数
    public void initQueryValue(){
        SysConfig config = SysConfig.getCurrSysConfig();
        switch (config.listenerPay){
            case 0:
                rg_selectType.check(R.id.rb_ctype_0);
                break;
            case 1:
                rg_selectType.check(R.id.rb_ctype_1);
                break;
            case 2:
                rg_selectType.check(R.id.rb_ctype_2);
                break;
            case 3:
                rg_selectType.check(R.id.rb_ctype_3);
                break;
        }
    }



    public void show(){
        //位置计算定位
        int[] location = new int[2];
        clickView.getLocationOnScreen(location);
        int width = rootView.getWidth()*2/3;
        int height = rootView.getHeight()*2/3;
        window=new PopupWindow(popView,  width,  height, true);

        // 设置PopupWindow的背景
        //window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // 设置PopupWindow是否能响应外部点击事件
        window.setOutsideTouchable(true);
        // 设置PopupWindow是否能响应点击事件
        window.setTouchable(true);
        // 显示PopupWindow，其中：
        // 第一个参数是PopupWindow的锚点，第二和第三个参数分别是PopupWindow相对锚点的x、y偏移
        //window.showAtLocation(rootView, Gravity.NO_GRAVITY,location[0]-width-10,location[1]);
        window.showAtLocation(rootView, Gravity.CENTER,0,0);

        //window.showAsDropDown(rootView);

    }


}
