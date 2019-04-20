package com.theone.pay;

import android.content.ContentValues;
import android.os.Environment;

import com.google.gson.Gson;
import com.theone.pay.model.MyNotification;
import com.theone.pay.model.WXMessage;
import com.theone.pay.utils.HtmlProcess;
import com.theone.pay.utils.MyRequests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedPayHook implements IXposedHookLoadPackage {
    //微信数据库包名称
    private static final String WECHAT_DATABASE_PACKAGE_NAME = "com.tencent.wcdb.database.SQLiteDatabase";
    //聊天精灵客户端包名称
    private static final String WECHATGENIUS_PACKAGE_NAME = "com.theone.heimipay";
    //微信主进程名
    private static final String WECHAT_PROCESS_NAME = "com.tencent.mm";


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        //XposedBridge.log("当前包名：" + lpparam.packageName + "，进程名：" + lpparam.processName);
        if (!lpparam.processName.equals(WECHAT_PROCESS_NAME)) {
            return;
        }
        XposedBridge.log("开始1" + lpparam.processName);
        //调用 hook数据库插入。
        hookDatabaseInsert(lpparam);
    }

    //hook数据库插入操作
    private void hookDatabaseInsert(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Class<?> classDb = XposedHelpers.findClassIfExists(WECHAT_DATABASE_PACKAGE_NAME, loadPackageParam.classLoader);
        if (classDb == null) {
            XposedBridge.log("hook数据库insert操作：未找到类" + WECHAT_DATABASE_PACKAGE_NAME);
            return;
        }
        XposedBridge.log("开始2" );
        XposedHelpers.findAndHookMethod(classDb,
                "insertWithOnConflict",
                String.class, String.class, ContentValues.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String tableName = (String) param.args[0];
                        ContentValues contentValues = (ContentValues) param.args[2];
                        if (tableName == null || tableName.length() == 0 || contentValues == null) {
                            return;
                        }
                        //过滤掉非聊天消息
                        if (!tableName.toLowerCase().equals("message")) {
                            return;
                        }

                        //打印出日志
//                        printInsertLog(tableName, (String) param.args[1], contentValues, (Integer) param.args[3]);

                        //提取消息内容
                        //1：表示是自己发送的消息
                        int isSend = contentValues.getAsInteger("isSend");
                        if(isSend==1){
                            return;
                        }
                        //消息内容
                        String strContent = contentValues.getAsString("content");
                        //说话人ID
                        String strTalker = contentValues.getAsString("talker");
                        //数据不对，直接返回
                        if(strTalker==null|| strContent==null){
                            return;
                        }
                        //抽取部分即可
                        strContent = HtmlProcess.extractHtml(strContent,"<title>","</des>");
                        if(strContent==null){
                            return;
                        }
                        XposedBridge.log("开始3" );
                        //contentValues.keySet();
                        //消息ID,避免重复，其实这里应该不存在重复的说法
                        String msgSvrId = contentValues.getAsString("msgSvrId");
                        String createTime = contentValues.getAsString("createTime");
                        if(strTalker=="notifymessage"|| strContent.indexOf("收款")!=-1){
                            WXMessage msg = new WXMessage();
                            msg.content = strContent;
                            msg.talker=strTalker;
                            msg.msgid=msgSvrId;
                            msg.createtime = parseLong(createTime);
                            postMsg(new MyNotification(msg));
                            //发送并保存
                            //new PayService().saveAndSendNotify(new MyNotification(msg));
                        }
                    }
                });
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

    private static void postMsg(MyNotification msg){
        XposedBridge.log("发送数据" );

        String path = Environment.getExternalStoragePublicDirectory("") + "/theonepay/";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        try{
            String uuid = null;
            String url = null;
            if(uuid==null){
                //读取UUID
                InputStream instream = new FileInputStream(path + "uuid");
                if(instream!=null){
                    InputStreamReader inputreader
                            = new InputStreamReader(instream, "UTF-8");
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line = "";
                    //分行读取
                    if ((line = buffreader.readLine()) != null) {
                        uuid = line;
                    }
                    instream.close();//关闭输入流
                    buffreader.close();
                }
            }
            if(url==null){
                //读取url
                InputStream instream = new FileInputStream(path + "url");
                if(instream!=null){
                    InputStreamReader inputreader
                            = new InputStreamReader(instream, "UTF-8");
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line = "";
                    //分行读取
                    if ((line = buffreader.readLine()) != null) {
                        url = line;
                    }
                    instream.close();//关闭输入流
                    buffreader.close();;
                }
            }
            XposedBridge.log("发送数据,uuid:" +uuid+",url:"+url);
            msg.setUid(uuid);

            //第三个参数：真，后续内容被追加到文件末尾处，反之则替换掉文件全部内容
            FileWriter fw = new FileWriter(path + msg.getNkey(), false);
            BufferedWriter bw = new BufferedWriter(fw);
            Gson gson = new Gson();
            bw.write(gson.toJson(msg));//
            bw.close();
            fw.close();
            if(url!=null && uuid!=null){
                sendMsg(msg,url);
            }
        }catch (Exception e){
            XposedBridge.log("创建数据失败" );
        }


    }

    private static void sendMsg(MyNotification notify,String baseurl){
        //baseurl ="http://192.168.1.100:8090/";
        String url = baseurl+"/api/pay/payappnotification/saveAppNotification?t="+System.currentTimeMillis();
        //XposedBridge.log("发送数据,,url:"+url);
        MyRequests req =new MyRequests();
        Map<String,String> postData =new HashMap<String,String>();
        req.setConnectTimeout(3000);
        req.setReadTimeout(5000);
        postData.put("nkey",notify.getNkey()+"");
        postData.put("id",notify.getId()+"");
        postData.put("packageName",notify.getPackageName()+"");
        postData.put("postTime",notify.getPostTime()+"");

        postData.put("title",notify.getTitle()+"");
        postData.put("text",notify.getText()+"");
        postData.put("subText",notify.getSubText()+"");

        postData.put("uid",notify.getUid()+"");
        postData.put("postTimeService",notify.getPostTimeService()+"");

        postData.put("sign",notify.MarkSign());
        //发送服务器请求
        String retstr = req.post(url,postData);
        XposedBridge.log("发送数据返回:" +retstr);
    }


}
