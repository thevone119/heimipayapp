package com.theone.pay.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.theone.pay.R;

/**
 * 描述：自定义topbar的实现
 * 自定义顶部标题栏
 * <p>
 * Created by
 */
public class TopBar01 extends RelativeLayout {
    private Button leftButton, rightButton;
    private TextView titleTextView;
    private OnLeftAndRightClickListener listener;//监听点击事件

    //设置监听器
    public void setOnLeftAndRightClickListener(OnLeftAndRightClickListener listener) {
        this.listener = listener;
    }

    //按钮点击接口
    public interface OnLeftAndRightClickListener {
        void OnLeftButtonClick(View leftButton);
        void OnRightButtonClick(View rightButton);
    }

    //设置左边按钮的可见性
    public void setLeftButtonVisibility(boolean flag){
        if (flag)
            leftButton.setVisibility(View.VISIBLE);
        else
            leftButton.setVisibility(View.GONE);
    }

    //设置右边按钮的可见性
    public void setRightButtonVisibility(boolean flag){
        if (flag)
            rightButton.setVisibility(View.VISIBLE);
        else
            rightButton.setVisibility(View.GONE);
    }
    public TopBar01(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_topbar_01, this);
        leftButton = (Button) findViewById(R.id.leftButton);
        rightButton = (Button) findViewById(R.id.rightButton);
        titleTextView = (TextView) findViewById(R.id.titleText);
        leftButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.OnLeftButtonClick(leftButton);//点击回调
                }
            }
        });
        rightButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.OnRightButtonClick(rightButton);//点击回调
                }
            }
        });



        //获得自定义属性并赋值
        TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.TopBar);
        int leftBtnBackground = typeArray.getResourceId(R.styleable.TopBar_leftBackground, 0);
        int rightBtnBackground = typeArray.getResourceId(R.styleable.TopBar_rightBackground, 0);
        String titleText = typeArray.getString(R.styleable.TopBar_titleText);
        float titleTextSize = typeArray.getDimension(R.styleable.TopBar_titleTextSize, 0);
        int titleTextColor = typeArray.getColor(R.styleable.TopBar_titleTextColor, 0x38ad5a);
        //释放资源
        typeArray.recycle();
        leftButton.setBackgroundResource(leftBtnBackground);
        rightButton.setBackgroundResource(rightBtnBackground);
        titleTextView.setText(titleText);
        titleTextView.setTextSize(titleTextSize);
        titleTextView.setTextColor(titleTextColor);
    }

}
