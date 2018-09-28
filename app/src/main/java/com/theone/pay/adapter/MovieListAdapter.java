package com.theone.pay.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.theone.pay.R;
import com.theone.pay.model.TempObj;

import java.util.List;

/**
 * 装饰器，作废
 */
public class MovieListAdapter  extends BaseAdapter {
    private Context context;//上下文对象
    private List<TempObj> dataList;//ListView显示的数据

    public MovieListAdapter(Context context,List<TempObj> dataList){
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            //得到缓存的布局
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //设置图片
        viewHolder.txt_content.setText("文件路径："+dataList.get(position).getTempvalue());
        //设置内容
        viewHolder.txt_createtime.setText("解码时间："+dataList.get(position).getCreatetime());

        return convertView;
    }

    /**
     * ViewHolder类
     */
    private final class ViewHolder {
        TextView txt_createtime;//创建时间
        TextView txt_content;//内容
        /**
         * 构造器
         *
         * @param view 视图组件（ListView的子项视图）
         */
        ViewHolder(View view) {
            txt_createtime = (TextView) view.findViewById(R.id.txt_createtime);
            txt_content = (TextView) view.findViewById(R.id.txt_content);
        }
    }

}
