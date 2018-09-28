package com.theone.pay.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.theone.pay.R;
import com.theone.pay.model.PayLog;
import com.theone.pay.model.TempObj;

import java.util.List;

/**
 * 装饰器
 */
public class PayLogAdapter extends BaseAdapter {
    private Context context;//上下文对象
    private List<PayLog> dataList;//ListView显示的数据

    public PayLogAdapter(Context context, List<PayLog> dataList){
        this.context=context;
        this.dataList=dataList;
    }
    @Override
    public int getCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        //判断是否有缓存
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_pay_log, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            //得到缓存的布局
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //设置
        PayLog log = dataList.get(position);
        viewHolder.tv_orderid.setText("序号:"+(position+1)+" 订单号:"+log.getOrderid());
        viewHolder.tv_payState.setText(log.getPayStateStr());
        if(log.getPayState()==1){
            viewHolder.tv_payState.setTextColor(Color.BLUE);
            viewHolder.tv_payImgPrice.setTextColor(Color.BLUE);
        }
        viewHolder.tv_payName.setText(log.getPayName());
        viewHolder.tv_createtime.setText(log.getCreatetimeStr());
        viewHolder.tv_prodPrice.setText("¥"+log.getProdPrice()+"");
        viewHolder.tv_prodPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);//中划线
        viewHolder.tv_payImgPrice.setText("¥"+log.getPayImgPrice()+"");
        if(log.getPayType()==2){
            viewHolder.iv_payType.setImageResource(R.mipmap.zfb_logo64);
        }else{
            viewHolder.iv_payType.setImageResource(R.mipmap.wx_logo);
        }

        return convertView;
    }

    /**
     * ViewHolder类
     */
    private final class ViewHolder {
        TextView tv_orderid;//创建时间
        TextView tv_payState;//内容
        TextView tv_payName;//内容
        TextView tv_createtime;//内容
        TextView tv_prodPrice;//内容
        TextView tv_payImgPrice;//内容
        ImageView iv_payType;
        /**
         * 构造器
         *
         * @param view 视图组件（ListView的子项视图）
         */
        ViewHolder(View view) {
            tv_orderid = (TextView) view.findViewById(R.id.tv_orderid);
            tv_payState = (TextView) view.findViewById(R.id.tv_payState);
            tv_payName = (TextView) view.findViewById(R.id.tv_payName);
            tv_createtime = (TextView) view.findViewById(R.id.tv_createtime);
            tv_prodPrice = (TextView) view.findViewById(R.id.tv_prodPrice);
            tv_payImgPrice = (TextView) view.findViewById(R.id.tv_payImgPrice);
            iv_payType =  (ImageView) view.findViewById(R.id.iv_payType);
        }
    }

}
