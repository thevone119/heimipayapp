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
import com.theone.pay.model.PayBusChange;
import com.theone.pay.model.PayLog;

import java.util.List;

/**
 * 装饰器
 */
public class PayChangeAdapter extends BaseAdapter {
    private Context context;//上下文对象
    private List<PayBusChange> dataList;//ListView显示的数据

    public PayChangeAdapter(Context context, List<PayBusChange> dataList){
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_pay_change, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            //得到缓存的布局
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //设置
        PayBusChange log = dataList.get(position);

        viewHolder.tv_createtime.setText(log.getCreatetimeStr());
        if(log.getEmoney()>0){
            viewHolder.tv_emoney.setText("+"+log.getEmoney()+"");
        }else{
            viewHolder.tv_emoney.setText(""+log.getEmoney()+"");
        }
        viewHolder.tv_demo.setText(""+log.getDemo()+"");
        if(log.getCtype()==1){
            viewHolder.iv_ctype.setImageResource(R.mipmap.chong);
        }else{
            viewHolder.iv_ctype.setImageResource(R.mipmap.xiao);
        }

        return convertView;
    }

    /**
     * ViewHolder类
     */
    private final class ViewHolder {
        TextView tv_createtime;//内容
        TextView tv_emoney;//内容
        TextView tv_demo;//内容
        ImageView iv_ctype;
        /**
         * 构造器
         *
         * @param view 视图组件（ListView的子项视图）
         */
        ViewHolder(View view) {
            tv_demo = (TextView) view.findViewById(R.id.tv_demo);
            tv_emoney = (TextView) view.findViewById(R.id.tv_emoney);
            tv_createtime = (TextView) view.findViewById(R.id.tv_createtime);
            iv_ctype =  (ImageView) view.findViewById(R.id.iv_ctype);
        }
    }

}
