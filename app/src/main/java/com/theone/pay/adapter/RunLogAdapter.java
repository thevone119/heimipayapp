package com.theone.pay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.theone.pay.R;
import com.theone.pay.model.PayBusChange;
import com.theone.pay.model.RunLog;

import java.util.List;

/**
 * 装饰器
 */
public class RunLogAdapter extends BaseAdapter {
    private Context context;//上下文对象
    private List<RunLog> dataList;//ListView显示的数据

    public RunLogAdapter(Context context, List<RunLog> dataList){
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_run_log, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            //得到缓存的布局
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //设置
        RunLog log = dataList.get(position);
        viewHolder.tv_logText.setText(log.logText);
        viewHolder.tv_logType.setText(log.logType);
        viewHolder.tv_runTime.setText(log.runTime);


        return convertView;
    }

    /**
     * ViewHolder类
     */
    private final class ViewHolder {
        TextView tv_runTime;//内容
        TextView tv_logType;//内容
        TextView tv_logText;//内容
        /**
         * 构造器
         *
         * @param view 视图组件（ListView的子项视图）
         */
        ViewHolder(View view) {
            tv_runTime = (TextView) view.findViewById(R.id.tv_runTime);
            tv_logType = (TextView) view.findViewById(R.id.tv_logType);
            tv_logText = (TextView) view.findViewById(R.id.tv_logText);
        }
    }

}
