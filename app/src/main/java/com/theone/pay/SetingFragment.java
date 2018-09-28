package com.theone.pay;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.theone.pay.db.DBUtils;
import com.theone.pay.httpservice.PayService;
import com.theone.pay.model.PayBus;
import com.theone.pay.model.RetObject;
import com.theone.pay.model.TempObj;
import com.theone.pay.view.TopBar01;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


/**
 * 各种权限设置
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SetingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SetingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetingFragment extends Fragment {
    private static final String TAG="SetingFragment";



    //单列
    private static SetingFragment fragment;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mParam1;

    private OnFragmentInteractionListener mListener;



    //各种控件
    private Button but_set1;
    private Button but_set2;
    private Button but_set3;
    private Button but_set4;



    public SetingFragment() {
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
    public static SetingFragment newInstance(String param1) {
        if(fragment!=null){
            return fragment;
        }
        fragment = new SetingFragment();
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
        final View view = inflater.inflate(R.layout.fragment_set, container, false);
        //顶部导航
        TopBar01 topBar = (TopBar01) view.findViewById(R.id.topbar);
        topBar.setLeftButtonVisibility(false);
        topBar.setRightButtonVisibility(false);

        //初始化控件
        but_set1= (Button) view.findViewById(R.id.but_set1);
        but_set2= (Button) view.findViewById(R.id.but_set2);
        but_set3= (Button) view.findViewById(R.id.but_set3);
        but_set4= (Button) view.findViewById(R.id.but_set4);
        but_set4.setVisibility(View.GONE);


        initData();
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

                                        }
                                    }
        );






        return view;
    }

    /**
     * 初始化数据到视图
     */
    private void initData(){

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
