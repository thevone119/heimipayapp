package com.theone.pay;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.theone.pay.adapter.MovieListAdapter;
import com.theone.pay.adapter.PayLogAdapter;
import com.theone.pay.db.DBUtils;
import com.theone.pay.httpservice.PayService;
import com.theone.pay.model.PayLog;
import com.theone.pay.model.RetObject;
import com.theone.pay.model.TempObj;
import com.theone.pay.view.PayLogInfoPopupWindow;
import com.theone.pay.view.PayLogQueryPopupWindow;
import com.theone.pay.view.TopBar01;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PayLogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PayLogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PayLogFragment extends Fragment {
    //单列
    private static PayLogFragment fragment;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mParam1;

    private OnFragmentInteractionListener mListener;

    private ListView listView;//ListView组件
    List<PayLog> listData = new ArrayList<PayLog>();
    private Handler mHandler;//用handler进行处理

    private PayLogQueryPopupWindow popupQuery;

    //查询条件
    private int pageNo = 1;
    private int pageSize = 10;

    private ProgressDialog loading;

    public PayLogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment PayLogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PayLogFragment newInstance(String param1) {
        if(fragment!=null){
            return fragment;
        }
        fragment = new PayLogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
        loading = new ProgressDialog(this.getActivity());
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_pay_log, container, false);
        //顶部导航
        TopBar01 topBar = (TopBar01) view.findViewById(R.id.topbar);
        topBar.setLeftButtonVisibility(false);
        topBar.setRightButtonVisibility(true);
        //事件监听
        topBar.setOnLeftAndRightClickListener(new TopBar01.OnLeftAndRightClickListener() {
                                                  @Override
                                                  public void OnLeftButtonClick(View v) {

                                                  }

                                                  @Override
                                                  public void OnRightButtonClick(View v) {
                                                      //弹出选择菜单
                                                      initQueryPage(inflater,v);
                                                  }
                                              });

        //初始化listView
        listView = (ListView) view.findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //点击
                PayLog tobj = listData.get(position);
                View popView = inflater.inflate(R.layout.popu_pay_log_info, null);
                PayLogInfoPopupWindow popinfo = new PayLogInfoPopupWindow(PayLogFragment.this.getView(),view,popView,mHandler,tobj);
                popinfo.show();
                //Intent intent = new Intent(PayLogFragment.this.getActivity(), PayLogInfoActivity.class);
                //startActivity(intent);
            }
        });
        //txt_content.setText("hello "+mParam1);
        //下拉刷新组件
        RefreshLayout refreshLayout = (RefreshLayout)view.findViewById(R.id.refreshLayout);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(final RefreshLayout refreshLayout1) {
                //线程刷新
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        pageNo=1;
                        listData.clear();
                        RetObject retobj = new PayService().queryPayLog(pageNo,pageSize);
                        if(retobj.getSuccess() && retobj.getData()!=null){
                            listData.addAll((List<PayLog>)retobj.getData());
                            mHandler.obtainMessage(0, "ok").sendToTarget();
                        }else{
                            mHandler.obtainMessage(-1, retobj.getMsg()).sendToTarget();
                        }
                        refreshLayout1.finishRefresh();
                    }
                }, 0);
                //refreshlayout.finishRefresh(2000/*,false*/);//传入 false 表示刷新失败
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(final RefreshLayout refreshLayout1) {
                //线程刷新
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        RetObject retobj = new PayService().queryPayLog(++pageNo,pageSize);
                        if(retobj.getSuccess() && retobj.getData()!=null){
                            List<PayLog> list = (List<PayLog>)retobj.getData();
                            if(list!=null && list.size()>=pageSize){
                                refreshLayout1.finishLoadMore(2000,true,true);
                            }else{
                                refreshLayout1.finishLoadMoreWithNoMoreData();
                            }
                            listData.addAll(list);
                            mHandler.obtainMessage(0, "ok").sendToTarget();
                        }else{
                            refreshLayout1.finishLoadMore(false);
                            mHandler.obtainMessage(-1, retobj.getMsg()).sendToTarget();
                        }
                    }
                }, 0);
                //refreshlayout.finishLoadMore(2000/*,false*/);//传入 false 表示加载失败
            }
        });

        //
        //响应事件处理
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                hideLoading();
                switch (msg.what) {
                    case 0://查询成功
                        listView.setAdapter(new PayLogAdapter(view.getContext(), listData));
                        break;
                    case 11://重置查询条件，重新查询
                        showLoading();
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                pageNo=1;
                                listData.clear();
                                RetObject retobj = new PayService().queryPayLog(pageNo,pageSize);
                                if(retobj.getSuccess() && retobj.getData()!=null){
                                    listData.addAll((List<PayLog>)retobj.getData());
                                    mHandler.obtainMessage(0, "ok").sendToTarget();
                                }else{
                                    mHandler.obtainMessage(-1, retobj.getMsg()).sendToTarget();
                                }
                            }
                        }, 0);
                        break;
                    case -1://失败
                        Toast.makeText(PayLogFragment.this.getActivity(),msg.obj.toString(),Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        return view;
    }

    /**
     * 视图创建完成执行的方法
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        //先进行一次查询
        mHandler.obtainMessage(11, "ok").sendToTarget();
    }

    /**
     * 初始化查询页面
     * @param inflater
     */
    private void initQueryPage(LayoutInflater inflater,View rightButton){
        View popView = inflater.inflate(R.layout.popu_pay_log, null);
        popupQuery = new PayLogQueryPopupWindow(this.getView(),rightButton,popView,mHandler);
        popupQuery.initQueryValue();
        popupQuery.show();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            //throw new RuntimeException(context.toString()+ " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    //加载中
    private void showLoading(){
        //loading.setProgressStyle(ProgressDialog.STYLE_SPINNER); //默认就是小圆圈的那种形式
        loading.setMessage("正在查询数据，请稍候...");
        //loading.setCancelable(true);//默认true
        loading.setCanceledOnTouchOutside(false);//默认true
        loading.show();
    }

    //加载结束
    private void hideLoading(){
        loading.hide();
    }
}
