package com.theone.pay;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.theone.pay.adapter.PayChangeAdapter;
import com.theone.pay.adapter.RunLogAdapter;
import com.theone.pay.httpservice.PayService;
import com.theone.pay.model.PayBusChange;
import com.theone.pay.model.PayLog;
import com.theone.pay.model.RetObject;
import com.theone.pay.model.RunLog;
import com.theone.pay.view.PayChangeQueryPopupWindow;
import com.theone.pay.view.TopBar01;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RunLogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RunLogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RunLogFragment extends Fragment {
    //单列
    private static RunLogFragment fragment;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mParam1;

    private OnFragmentInteractionListener mListener;

    private ListView listView;//ListView组件
    List<RunLog> listData = new ArrayList<RunLog>();




    private   Handler mHandler;



    public RunLogFragment() {
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
    public static RunLogFragment newInstance(String param1) {
        if(fragment!=null){
            return fragment;
        }
        fragment = new RunLogFragment();
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

    }





    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_run_log, container, false);
        //顶部导航
        TopBar01 topBar = (TopBar01) view.findViewById(R.id.topbar);
        topBar.setLeftButtonVisibility(false);
        topBar.setRightButtonVisibility(false);


        //初始化listView
        listView = (ListView) view.findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //点击
                //PayLog tobj = listData.get(position);
            }
        });

        //下拉刷新组件
        RefreshLayout refreshLayout = (RefreshLayout)view.findViewById(R.id.refreshLayout);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(final RefreshLayout refreshLayout1) {
                //线程刷新
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mHandler.obtainMessage(0, "ok").sendToTarget();
                        refreshLayout1.finishRefresh();
                    }
                }, 200);
                //refreshlayout.finishRefresh(2000/*,false*/);//传入 false 表示刷新失败
            }
        });

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0://刷新界面
                        listData = RunLog.getLogs();
                        Collections.reverse(listData);
                        listView.setAdapter(new RunLogAdapter(view.getContext(), listData));
                        break;
                    case -1://失败
                        Toast.makeText(RunLogFragment.this.getActivity(),msg.obj.toString(),Toast.LENGTH_LONG).show();
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
        mHandler.obtainMessage(0, "ok").sendToTarget();
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




}
