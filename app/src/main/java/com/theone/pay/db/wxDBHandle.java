package com.theone.pay.db;

import android.content.Context;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;              //注意导入的包
import net.sqlcipher.database.SQLiteDatabaseHook;

import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.theone.pay.MyApplication;
import com.theone.pay.httpservice.PayService;
import com.theone.pay.model.MyNotification;
import com.theone.pay.model.PayLog;
import com.theone.pay.model.RetObject;
import com.theone.pay.model.WXMessage;
import com.theone.pay.utils.HtmlProcess;
import com.theone.pay.utils.SDCardHelper;

import org.w3c.dom.Document;

import java.io.OutputStream;
import java.io.*;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * 微信的数据库读取，处理
 */
public class wxDBHandle {
    private static  final  String TAG = "wxDBHandle";
    //微信数据的根目录
    public static final String WX_ROOT_PATH = "/data/data/com.tencent.mm/";
    public static final String WX_SP_UIN_PATH = WX_ROOT_PATH + "shared_prefs/auth_info_key_prefs.xml";

    public static final String CP_WX_SP_UIN_PATH =  "auth_info_key_prefs.xml";

    public static final String DB_NAME =  "EnMicroMsg.db";

    //微信UID
    private static String wxuid = null;
    private static String wxDataPath = null;
    private static boolean mHaveRoot = false;

    private static long lastTime;//最后运行时间

    private static long lastIdx = 0;

    //存在判断
    private static Map<String,String> hasMap = new HashMap<String,String>();

    private static String wxpwd=null;

    /**
     * 每分钟调用一次此方法
     *
     */
    public static void loadWXData(){
        if(!getRootAhth()){
            return;
        }
        lastIdx++;
        //线程刷新
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                long _lastIdx = lastIdx;
                for(int i=0;i<15;i++){
                    try{
                        //如果已经大于55秒了，直接退出了
                        if(_lastIdx!=lastIdx){
                            return;
                        }
                        loadWXDataThread();
                        Thread.sleep(1000*10);
                    }catch (Exception e){

                    }
                }
            }
        }, 0);
    }

    /**
     * 每分钟执行一次，加载微信的数据
     * 这里搞成10秒执行一次吧
     */
    private static void loadWXDataThread(){
        //小于5秒，直接返回
        if(System.currentTimeMillis()-lastTime<5000){
            return;
        }
        lastTime = System.currentTimeMillis();
        //1.判断是否有ROOT权限，如果没有权限，直接返回
        if(!getRootAhth()){
            Log.i(TAG,"没有ROOT权限，无法读取数据");
            return;
        }
        //2.获取微信uid
        if(getCurrWxUin()==null){
            Log.i(TAG,"微信UID获取失败");
            return;
        }
        //3.复制微信数据
        cpWXData();
        if(!new File(wxDataPath).exists()){
            Log.i(TAG,"微信数据文件不存在");
            return;
        }
        readWxDb(new File(wxDataPath));

    }

    /**
     * 读取微信数据
     * @param dbFile
     */
    public static void readWxDb(File dbFile) {
        Context context = MyApplication.getApplication();

        SQLiteDatabase.loadLibs(context);
        SQLiteDatabaseHook hook = new SQLiteDatabaseHook() {
            public void preKey(SQLiteDatabase database) {
            }

            public void postKey(SQLiteDatabase database) {
                database.rawExecSQL("PRAGMA cipher_migrate;"); //兼容2.0的数据库
            }
        };

        try {
            //打开数据库连接
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbFile, getWxPwd(), null, hook);
            //1天内的所有收款通知
            Calendar now =Calendar.getInstance();
            now.add(Calendar.DAY_OF_YEAR,-1);
            long qt = now.getTime().getTime();
            Log.i(TAG,"redtime:"+qt);
            Cursor c1 = db.rawQuery("select * from message where createtime > "+qt+"  ", null);
            while (c1.moveToNext()) {
                try{
                    String msgSvrId = c1.getString(c1.getColumnIndex("msgSvrId"));
                    String createTime = c1.getString(c1.getColumnIndex("createTime"));
                    String talker = c1.getString(c1.getColumnIndex("talker"));
                    String content = c1.getString(c1.getColumnIndex("content"));
                    String msgSeq = c1.getString(c1.getColumnIndex("msgSeq"));
                    content = HtmlProcess.extractHtml(content,"<title>","</des>");

                    if(talker=="notifymessage"|| content.indexOf("收款")!=-1){
                        WXMessage msg = new WXMessage();
                        msg.content = content;
                        msg.talker=talker;
                        msg.msgid=msgSvrId;
                        msg.createtime = parseLong(createTime);
                        msg.msgseq = parseInt(msgSeq);
                        if(hasMap==null){
                            hasMap =  new HashMap<String,String>();
                        }
                        if(!hasMap.containsKey(msg.msgid)){
                            hasMap.put(msg.msgid,"1");
                            //发送并保存
                            new PayService().saveAndSendNotify(new MyNotification(msg));
                        }
                    }
                }catch (Exception e){

                }
            }
            c1.close();
            db.close();
        } catch (Exception e) {
            //LogUtil.log("读取数据库信息失败" + e.toString());
            e.printStackTrace();
        }
    }

    private static  int parseInt(String c){
        if(c==null||c==""){
            return 0;
        }
        try{
            return Integer.parseInt(c);
        }catch (Exception e){
        }
        return 0;
    }

    private static  long parseLong(String c){
        if(c==null||c==""){
            return 0;
        }
        try{
            return Long.parseLong(c);
        }catch (Exception e){
        }
        return 0;
    }


    /**
     * 执行linux指令
     * 获取ROOT权限
     * @param paramString
     */
    public static void execRootCmd(String paramString) {
        try {
            Process localProcess = Runtime.getRuntime().exec("su");
            Object  localObject = localProcess.getOutputStream();
            DataOutputStream localDataOutputStream = new DataOutputStream((OutputStream) localObject);
            String str = String.valueOf(paramString);
            localObject = str + "\n";
            localDataOutputStream.writeBytes((String) localObject);
            localDataOutputStream.flush();
            localDataOutputStream.writeBytes("exit\n");
            localDataOutputStream.flush();
            localProcess.waitFor();
            localObject = localProcess.exitValue();
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }



    /***
     * 复制微信数据
     * @return
     */
    public static void cpWXData(){
        try {
            String sdprivatePath   = SDCardHelper.getSDCardPrivateCacheDir(MyApplication.getApplication());
            String wxdp = md5("mm"+getCurrWxUin());

            String wxdp2 = WX_ROOT_PATH+"MicroMsg/"+wxdp+"/"+DB_NAME;
            wxDataPath = sdprivatePath+"/"+DB_NAME;
            String str = String.valueOf("cp "+wxdp2+" "+ wxDataPath);
            execRootCmd2(str);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    /**
     * 获取微信数据库的密码
     * @return
     */
    public static String getWxPwd(){
        if(wxpwd==null){
            String md5 = md5(MyApplication.getApplication().getImei() + getCurrWxUin());
            wxpwd = md5.substring(0, 7).toLowerCase();
        }
       //Log.i(TAG,"密码"+wxpwd);
        return wxpwd;
    }

    /**
     * 获取微信的UID,缓存处理
     * @return
     */
    public static String getCurrWxUin() {
        if(wxuid!=null){
            return wxuid;
        }
        try {
            String sdprivatePath   = SDCardHelper.getSDCardPrivateCacheDir(MyApplication.getApplication());

            //execRootCmd2("chmod -R 777 "+ sdprivatePath);
            //execRootCmd2("chmod -R 777 "+ WX_ROOT_PATH);
            String cp_path1 = sdprivatePath+"/"+CP_WX_SP_UIN_PATH;
            String str = String.valueOf("cp "+WX_SP_UIN_PATH+" "+ cp_path1);
            //Log.i(TAG,"文件路径"+cp_path1);

            execRootCmd2(str);
            File file = new File(cp_path1);
            if(!file.exists()){
                Log.i(TAG,"获取微信UID失败，文件不存在");
                return null;
            }
            String _wxuid = getCurrWxUin(cp_path1);
            if(_wxuid!=null){
                wxuid=_wxuid;
            }
            return wxuid;
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,"获取微信uid失败，请检查auth_info_key_prefs文件权限");
        }
        return null;
    }


    /**
     * 获取微信的uid
     * 微信的uid存储在SharedPreferences里面
     * 存储位置\data\data\com.tencent.mm\shared_prefs\auth_info_key_prefs.xml
     */
    private static String getCurrWxUin(String filePath) {
        String mCurrWxUin = null;
        File file = new File(filePath);
        try {
            FileInputStream in = new FileInputStream(file);
            InputStreamReader inputReader = new InputStreamReader(in,"utf-8");
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            String Result="";
            while((line = bufReader.readLine()) != null){
                if(line.indexOf("_auth_uin")!=-1){
                    String uid = HtmlProcess.extractHtml(line,"value=\"","\"");
                    return uid;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,"获取微信uid失败，请检查auth_info_key_prefs文件权限");
        }
        return null;
    }





    // 执行命令并且输出结果
    public static String execRootCmd2(String cmd) {
        String result = "";
        DataOutputStream dos = null;
        DataInputStream dis = null;

        try {
            Process p = Runtime.getRuntime().exec("su");// 经过Root处理的android系统即有su命令
            dos = new DataOutputStream(p.getOutputStream());
            dis = new DataInputStream(p.getInputStream());

            //Log.i(TAG, cmd);
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            String line = null;
            while ((line = dis.readLine()) != null) {
                Log.d("result", line);
                result += line;
            }
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 判断是否具有ROOT权限
     * @return
     */
    public static synchronized boolean getRootAhth()
    {
        Process process = null;
        DataOutputStream os = null;
        try
        {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            int exitValue = process.waitFor();
            if (exitValue == 0)
            {
                return true;
            } else
            {
                return false;
            }
        } catch (Exception e)
        {
            Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: "
                    + e.getMessage());
            return false;
        } finally
        {
            try
            {
                if (os != null)
                {
                    os.close();
                }
                process.destroy();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }



    // 执行命令但不关注结果输出
    public static int execRootCmdSilent(String cmd) {
            int result = -1;
            DataOutputStream dos = null;

            try {
                Process p = Runtime.getRuntime().exec("su");
                dos = new DataOutputStream(p.getOutputStream());

                //Log.i(TAG, cmd);
                dos.writeBytes(cmd + "\n");
                dos.flush();
                dos.writeBytes("exit\n");
                dos.flush();
                p.waitFor();
                result = p.exitValue();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (dos != null) {
                    try {
                        dos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return result;
    }

    /**
     * 根据imei和uin生成的md5码，获取数据库的密码（去前七位的小写字母）
     *
     * @param imei
     * @param uin
     * @return
     */
    public static String initDbPassword(String imei, String uin) {
        if (TextUtils.isEmpty(imei) || TextUtils.isEmpty(uin)) {
            Log.i(TAG,"初始化数据库密码失败：imei或uid为空");
            return null;
        }
        String md5 = md5(imei + uin);
        String password = md5.substring(0, 7).toLowerCase();
        //Log.i(TAG,"初始化数据库密码"+password);
        return password;
    }

    /**
     * md5加密
     *
     * @param content
     * @return
     */
    public static String md5(String content) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(content.getBytes("UTF-8"));
            byte[] encryption = md5.digest();//加密
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < encryption.length; i++) {
                if (Integer.toHexString(0xff & encryption[i]).length() == 1) {
                    sb.append("0").append(Integer.toHexString(0xff & encryption[i]));
                } else {
                    sb.append(Integer.toHexString(0xff & encryption[i]));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
