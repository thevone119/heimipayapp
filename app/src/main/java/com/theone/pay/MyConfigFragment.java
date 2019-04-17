package com.theone.pay;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.theone.pay.adapter.PayLogAdapter;
import com.theone.pay.db.DBUtils;
import com.theone.pay.db.wxDBHandle;
import com.theone.pay.httpservice.PayService;
import com.theone.pay.model.PayBus;
import com.theone.pay.model.PayLog;
import com.theone.pay.model.RetObject;
import com.theone.pay.model.SysConfig;
import com.theone.pay.model.TempObj;
import com.theone.pay.view.PayLogQueryPopupWindow;
import com.theone.pay.view.SelectRunTypePopupWindow;
import com.theone.pay.view.TopBar01;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * 我的配置
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyConfigFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyConfigFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyConfigFragment extends Fragment {
    private static final String TAG="MyConfigFragment";

    public static long lastPostTime=0;//最后接收通知的时间

    private static boolean mHaveRoot = false;

    private String apkRoot;
    //单列
    private static MyConfigFragment fragment;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mParam1;

    private OnFragmentInteractionListener mListener;

    private Handler mHandler;//用handler进行处理

    private ProgressDialog loading;

    private SelectRunTypePopupWindow popupQuery;

    //各种控件
    private Button but_exitsys;
    private Button but_test;

    private Button but_set1;
    private Button but_set2;
    private Button but_set3;
    private Button but_set4;


    //private TextView tv_gobackUrl;
    //private TextView tv_notifyUrl;
    private Switch switch_nofity;
    private Button but_run_tye;
    private TextView tv_busValidity;
    private TextView tv_busType;
    private TextView tv_eMoney;
    private TextView tv_uuid;
    private TextView tv_busAcc;


    public MyConfigFragment() {
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
    public static MyConfigFragment newInstance(String param1) {
        if(fragment!=null){
            return fragment;
        }
        fragment = new MyConfigFragment();
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
        final View view = inflater.inflate(R.layout.fragment_my, container, false);
        //顶部导航
        TopBar01 topBar = (TopBar01) view.findViewById(R.id.topbar);
        topBar.setLeftButtonVisibility(false);
        topBar.setRightButtonVisibility(false);

        apkRoot="chmod -R 777 "+ this.getActivity().getPackageCodePath();

        //初始化控件
        but_exitsys= (Button) view.findViewById(R.id.but_exitsys);
        but_test= (Button) view.findViewById(R.id.but_test);
        but_set1= (Button) view.findViewById(R.id.but_set1);
        but_set2= (Button) view.findViewById(R.id.but_set2);
        but_set3= (Button) view.findViewById(R.id.but_set3);
        but_set4= (Button) view.findViewById(R.id.but_set4);


        //tv_gobackUrl= (TextView) view.findViewById(R.id.tv_gobackUrl);
        //tv_notifyUrl= (TextView) view.findViewById(R.id.tv_notifyUrl);
        switch_nofity =  (Switch)view.findViewById(R.id.switch_nofity);
        but_run_tye =  (Button)view.findViewById(R.id.but_run_tye);

        //switch_nofity.setTextOff("关");
        //switch_nofity.setTextOn("开");
        tv_busValidity= (TextView) view.findViewById(R.id.tv_busValidity);
        tv_busType= (TextView) view.findViewById(R.id.tv_busType);
        tv_eMoney= (TextView) view.findViewById(R.id.tv_eMoney);
        tv_uuid= (TextView) view.findViewById(R.id.tv_uuid);
        tv_busAcc= (TextView) view.findViewById(R.id.tv_busAcc);
        initData();
        //注册事件
        switch_nofity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SysConfig config= SysConfig.getCurrSysConfig();
                if (isChecked){
                    mHandler.obtainMessage(-1, "开启激活通知，APP将定时自动激活").sendToTarget();
                    config.setKeepNotifyTime(1);
                }else {
                    mHandler.obtainMessage(-1, "关闭激活通知，APP可能会被系统清理，请设置APP内存锁定，避免被清理").sendToTarget();
                    config.setKeepNotifyTime(0);
                }
                SysConfig.saveCurrSysConfig(config);
            }
        });
        //注册事件
        but_run_tye.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               //弹出选择菜单
                                               initQueryPage(inflater,v);
                                           }
                                       }
        );

        //注册事件
        but_exitsys.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               //清空当前商户
                                               PayBus.clearCurrPayBus();
                                               //退出系统
                                               System.exit(0);
                                           }
                                       }
        );
        //注册事件
        but_test.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               //1.发送通知
                                               sendTestNotify();
                                               //2.判断通知是否存在数据库
                                               new Timer().schedule(new TimerTask() {
                                                   @Override
                                                   public void run() {
                                                       //5秒内是否发送成功
                                                       for(int i=0;i<5;i++){
                                                           try {
                                                               Thread.sleep(1000);
                                                               //直接用静态变量判断判断
                                                               if(new Date().getTime()-lastPostTime<1000*5){
                                                                   mHandler.obtainMessage(-1, "测试成功，已成功监听并发送通知，请保持当前APP处于活动状态").sendToTarget();
                                                                   return;
                                                               }
                                                               String nkey="notifiy_"+"com.theone.pay"+"_1";
                                                               SimpleDateFormat shortDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                                               TempObj tobj = DBUtils.getByKey(nkey);
                                                               if(tobj!=null){
                                                                   if((new Date().getTime()-shortDateFormat.parse(tobj.getCreatetime()).getTime())<1000*5){
                                                                       mHandler.obtainMessage(-1, "测试成功，已成功监听并发送通知，请保持当前APP处于活动状态").sendToTarget();
                                                                       return;
                                                                   }
                                                               }
                                                           } catch (Exception e) {
                                                               e.printStackTrace();
                                                           }
                                                       }
                                                       mHandler.obtainMessage(-1, "测试失败，请检查当前手机是否开启<通知读取>的权限,确认权限没问题则重启手机后再试").sendToTarget();
                                                       return;
                                                   }
                                               }, 0);
                                           }
                                       }
        );

        //注册事件
        but_set1.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            openNotificationListenSettings();
                                        }
                                    }
        );
        //注册事件
        but_set2.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            setTaskConfig();
                                        }
                                    }
        );
        //注册事件
        but_set3.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            jumpStartInterface();
                                        }
                                    }
        );

        //注册事件
        but_set4.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            if (!mHaveRoot) {
                                                int ret = wxDBHandle.execRootCmdSilent("echo test"); // 通过执行测试命令来检测
                                                if (ret != -1) {
                                                    wxDBHandle.execRootCmd(apkRoot);
                                                    mHandler.obtainMessage(-1, "申请成功，当前手机已经具有ROOT权限!").sendToTarget();
                                                    mHaveRoot = true;
                                                } else {
                                                    mHandler.obtainMessage(-1, "请对手机进行ROOT，并给与相应的ROOT权限!").sendToTarget();
                                                }
                                            } else {
                                                mHandler.obtainMessage(-1, "申请成功，当前手机已经具有ROOT权限!").sendToTarget();
                                            }
                                        }
                                    }
        );

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
                        RetObject ret= new PayService().RefreshPayBus();
                        if(ret.getSuccess()){
                            mHandler.obtainMessage(0, "ok").sendToTarget();
                        }else{
                            mHandler.obtainMessage(-1, ret.getMsg()).sendToTarget();
                        }
                        refreshLayout1.finishRefresh();
                    }
                }, 0);
                //refreshlayout.finishRefresh(2000/*,false*/);//传入 false 表示刷新失败
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
                        initData();
                        break;
                    case 11://重置查询条件，重新查询
                        showLoading();
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {

                            }
                        }, 0);
                        break;
                    case 12://选择运行方式
                        initData();
                        break;
                    case -1://失败
                        Toast.makeText(MyConfigFragment.this.getActivity(),msg.obj.toString(),Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        return view;
    }

    /**
     * 初始化数据到视图
     */
    private void initData(){
        PayBus bus = PayBus.getCurrPayBus();
        if(bus==null){
            return;
        }
        //tv_gobackUrl.setText(bus.getGobackUrl());
        //tv_notifyUrl.setText(bus.getNotifyUrl());
        SysConfig config= SysConfig.getCurrSysConfig();
        if(config!=null){
            if(config.getKeepNotifyTime()==1){
                switch_nofity.setChecked(true);
            }else{
                switch_nofity.setChecked(false);
            }
            switch (config.listenerPay){
                case 0:
                    but_run_tye.setText("手工收款（不监控）");
                    break;
                case 1:
                    but_run_tye.setText("通知栏监控");
                    break;
                case 2:
                    but_run_tye.setText("ROOT数据监控");
                    break;
                case 3:
                    but_run_tye.setText("X框架监控");
                    break;
            }
        }

        if(bus.getBusValidity()!=null&&bus.getBusValidity()>0){
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat format2 = new SimpleDateFormat("yyyy年MM月dd日");
            try {
                tv_busValidity.setText(format2.format(format.parse(bus.getBusValidity()+"")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        tv_busType.setText(bus.getBusTypeStr());
        tv_eMoney.setText("¥"+bus.geteMoney()+"");
        tv_uuid.setText(bus.getUuid());
        if(bus.getBusName()!=null&&bus.getBusName().length()>0){
            tv_busAcc.setText(bus.getBusAcc()+"("+bus.getBusName()+")");
        }else{
            tv_busAcc.setText(bus.getBusAcc());
        }
    }


    /**
     * 初始化查询页面
     * @param inflater
     */
    private void initQueryPage(LayoutInflater inflater,View rightButton){
        View popView = inflater.inflate(R.layout.popu_pay_run_type, null);
        popupQuery = new SelectRunTypePopupWindow(this.getView(),rightButton,popView,mHandler);
        popupQuery.initQueryValue();
        popupQuery.show();
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
        //mHandler.obtainMessage(11, "ok").sendToTarget();
    }

    /**
     * 发送测试通知
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void sendTestNotify(){
        //1.获取系统的通知服务：
        NotificationManager myManager = (NotificationManager) fragment.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        //3.定义一个PendingIntent，点击Notification后启动一个Activity
        PendingIntent pi = PendingIntent.getActivity(
                fragment.getContext(),
                100,
                new Intent(fragment.getContext(), WelcomeActivity.class),
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        //2.通过Notification.Builder来创建通知
        Notification.Builder myBuilder = new Notification.Builder(fragment.getContext());
        myBuilder.setContentTitle("黑米支付测试通知")
                .setContentText("黑米支付测试通知内容,黑米支付APP自动监听此内容")
                //.setSubText("黑米支付测试补充小行内容")
                .setTicker("黑米支付测试通知")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setAutoCancel(true)//点击后取消
                .setWhen(System.currentTimeMillis())//设置通知时间
                .setPriority(Notification.PRIORITY_HIGH)//高优先级
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(pi);  //3.关联PendingIntent
        Notification  myNotification = myBuilder.build();
        //4.通过通知管理器来发起通知，ID区分通知
        myManager.notify(1, myNotification);
        Log.i(TAG,"send_Notification");
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
        loading.setMessage("正在刷新数据，请稍候...");
        //loading.setCancelable(true);//默认true
        loading.setCanceledOnTouchOutside(false);//默认true
        loading.show();
    }

    //加载结束
    private void hideLoading(){
        loading.hide();
    }

    /**
     * 打开监听设置页面
     */
    public void openNotificationListenSettings() {
        try {
            Intent intent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            } else {
                intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            }
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开通知栏权限设置页面
     */
    public void setTaskConfig() {
        try {
            Intent localIntent = new Intent();
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= 9) {
                localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                localIntent.setData(Uri.fromParts("package", this.getActivity().getPackageName(), null));
            } else if (Build.VERSION.SDK_INT <= 8) {
                localIntent.setAction(Intent.ACTION_VIEW);
                localIntent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails");
                localIntent.putExtra("com.android.settings.ApplicationPkgName", this.getActivity().getPackageName());
            }
            startActivity(localIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取手机类型
    private  String getMobileType() {
        return Build.MANUFACTURER;
    }

    /**
     * 设置开启启动
     */
    public  void jumpStartInterface() {
        Intent intent = new Intent();
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Log.e("HLQ_Struggle", "******************当前手机型号为：" + getMobileType());
            ComponentName componentName = null;
            if (getMobileType().equals("Xiaomi")) { // 红米Note4测试通过
                componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
            } else if (getMobileType().equals("Letv")) { // 乐视2测试通过
                intent.setAction("com.letv.android.permissionautoboot");
            } else if (getMobileType().equals("samsung")) { // 三星Note5测试通过
                //componentName = new ComponentName("com.samsung.android.sm_cn", "com.samsung.android.sm.ui.ram.AutoRunActivity");
                //componentName = ComponentName.unflattenFromString("com.samsung.android.sm/.ui.ram.RamActivity");// Permission Denial not exported from uid 1000，不允许被其他程序调用
                componentName = ComponentName.unflattenFromString("com.samsung.android.sm/.app.dashboard.SmartManagerDashBoardActivity");
            } else if (getMobileType().equals("HUAWEI")) { // 华为测试通过
                //componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity");//锁屏清理
                componentName = ComponentName.unflattenFromString("com.huawei.systemmanager/.startupmgr.ui.StartupNormalAppListActivity");//跳自启动管理
                //SettingOverlayView.show(context);
            } else if (getMobileType().equals("vivo")) { // VIVO测试通过
                componentName = ComponentName.unflattenFromString("com.iqoo.secure/.safeguard.PurviewTabActivity");
            } else if (getMobileType().equals("Meizu")) { //万恶的魅族
                //componentName = ComponentName.unflattenFromString("com.meizu.safe/.permission.PermissionMainActivity");//跳转到手机管家
                componentName = ComponentName.unflattenFromString("com.meizu.safe/.permission.SmartBGActivity");//跳转到后台管理页面
            } else if (getMobileType().equals("OPPO")) { // OPPO R8205测试通过
                componentName = ComponentName.unflattenFromString("com.oppo.safe/.permission.startup.StartupAppListActivity");
            } else if (getMobileType().equals("ulong")) { // 360手机 未测试
                componentName = new ComponentName("com.yulong.android.coolsafe", ".ui.activity.autorun.AutoRunListActivity");
            } else {
                // 将用户引导到系统设置页面
                if (Build.VERSION.SDK_INT >= 9) {
                    Log.e("HLQ_Struggle", "APPLICATION_DETAILS_SETTINGS");
                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.setData(Uri.fromParts("package", this.getActivity().getPackageName(), null));
                } else if (Build.VERSION.SDK_INT <= 8) {
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                    intent.putExtra("com.android.settings.ApplicationPkgName", this.getActivity().getPackageName());
                }
            }
            intent.setComponent(componentName);
            startActivity(intent);
        } catch (Exception e) {//抛出异常就直接打开设置页面
            Log.e("HLQ_Struggle", e.getLocalizedMessage());
            intent = new Intent(Settings.ACTION_SETTINGS);
            startActivity(intent);
        }
    }


}
