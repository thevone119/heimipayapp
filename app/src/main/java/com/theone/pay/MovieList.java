package com.theone.pay;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.theone.pay.db.DBUtils;
import com.theone.pay.adapter.MovieListAdapter;
import com.theone.pay.model.TempObj;

import java.io.File;
import java.util.List;

/**
 * 作废
 */
public class MovieList extends AppCompatActivity {
    private static final String TAG="MovieList";
    List<TempObj> listData;
    private ListView movie_list;//ListView组件



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.but_nav);
        //navigation.getChildAt(1).setSelected(true);


        //navigation.setSelectedItemId(R.id.nav_list);

        navigation.getMenu().getItem(1).setChecked(true);
        //Log.i(TAG,navigation.getMenu().getItem(2).getItemId()+"");
        initView();
        initData();

    }


    /**
     * 初始化组件
     */
    private void initView() {
        movie_list = (ListView) findViewById(R.id.movie_list);
        movie_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //点击则进行播放
                TempObj tobj = listData.get(position);
                File file = new File(tobj.getTempvalue());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("oneshot", 0);
                intent.putExtra("configchange", 0);
                Uri uri = getUri(intent, file);
                intent.setDataAndType(getUri(intent,file), "video/*");
                startActivity(intent);
            }
        });



    }


    /**
     * 获取对应文件的Uri
     * @param intent 相应的Intent
     * @param file 文件对象
     * @return
     */
    private  Uri getUri(Intent intent, File file) {
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //判断版本是否在7.0以上
            uri =
                    FileProvider.getUriForFile(this,
                            this.getPackageName() + ".fileprovider",
                            file);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }


    /**
     * 初始化数据
     */
    private void initData() {
        //查询所有已经解码的资源，如果资源已经不存在，则删除记录
        listData = DBUtils.listObj(TempObj.TYPE_MOVIE_UN_ZIP);
        for(int i=0;i<listData.size();i++){
            TempObj tobj = listData.get(i);
            String path = tobj.getTempvalue();
            if(!new File(path).exists()){
                DBUtils.delete(tobj.getTempkey());
                listData.remove(i);
                i--;
            }
        }
        movie_list.setAdapter(new MovieListAdapter(this, listData));
    }



}
