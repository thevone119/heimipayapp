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
import com.theone.pay.model.WXMessage;
import com.theone.pay.utils.HtmlProcess;
import com.theone.pay.utils.SDCardHelper;

import org.w3c.dom.Document;

import java.io.OutputStream;
import java.io.*;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

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
    //存在判断
    private static Map<String,String> hasMap = new HashMap<String,String>();

    /**
     * 每分钟执行一次，加载微信的数据
     * 这里搞成10秒执行一次吧
     */
    public static void loadWXData(){
        //小于5秒，直接返回
        if(System.currentTimeMillis()-lastTime<5000){
            return;
        }
        lastTime= System.currentTimeMillis();
        //1.判断是否有ROOT权限，如果没有权限，直接返回
        if(!mHaveRoot){
            int ret = execRootCmdSilent("echo test"); // 通过执行测试命令来检测
            if (ret != -1) {
                mHaveRoot = true;
            }
        }
        if(!mHaveRoot){
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
            Log.i(TAG,"time:"+qt);
            Cursor c1 = db.rawQuery("select * from message where createtime > "+qt+" and talker='notifymessage' ", null);
            while (c1.moveToNext()) {

                String msgSvrId = c1.getString(c1.getColumnIndex("msgSvrId"));
                String createTime = c1.getString(c1.getColumnIndex("createTime"));
                String talker = c1.getString(c1.getColumnIndex("talker"));
                String content = c1.getString(c1.getColumnIndex("content"));
                String msgSeq = c1.getString(c1.getColumnIndex("msgSeq"));
                if(content==null||content.indexOf("[收款到账通知]")==-1){
                    continue;
                }
                content = HtmlProcess.extractHtml(content,"<title>","</title>");
                WXMessage msg = new WXMessage();
                msg.content = content;
                msg.talker=talker;
                msg.msgid=parseLong(msgSvrId);
                msg.createtime = parseLong(createTime);
                msg.msgseq = parseInt(msgSeq);

                Log.i(TAG,msg.toString());
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
        String md5 = md5(MyApplication.getApplication().getImei() + getCurrWxUin());
        String password = md5.substring(0, 7).toLowerCase();
        return password;
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
            String cp_path1 = sdprivatePath+"/"+CP_WX_SP_UIN_PATH;
            String str = String.valueOf("cp "+WX_SP_UIN_PATH+" "+ cp_path1);
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

            Log.i(TAG, cmd);
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

    // 执行命令但不关注结果输出
    public static int execRootCmdSilent(String cmd) {
            int result = -1;
            DataOutputStream dos = null;

            try {
                Process p = Runtime.getRuntime().exec("su");
                dos = new DataOutputStream(p.getOutputStream());

                Log.i(TAG, cmd);
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
