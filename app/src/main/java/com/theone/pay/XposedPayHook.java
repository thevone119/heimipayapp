package com.theone.pay;

import android.content.ContentValues;

import com.theone.pay.httpservice.PayService;
import com.theone.pay.model.MyNotification;
import com.theone.pay.model.SysConfig;
import com.theone.pay.model.WXMessage;
import com.theone.pay.utils.HtmlProcess;

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
                        if(SysConfig.getCurrSysConfig().getListenerPay()!=3){
                            XposedBridge.log("非X框架运行，APP不做处理" );
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
                            //发送并保存
                            new PayService().saveAndSendNotify(new MyNotification(msg));
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


    //输出插入操作日志
    private void printInsertLog(String tableName, String nullColumnHack, ContentValues contentValues, int conflictValue) {
        String[] arrayConflicValues =
                {"", " OR ROLLBACK ", " OR ABORT ", " OR FAIL ", " OR IGNORE ", " OR REPLACE "};
        if (conflictValue < 0 || conflictValue > 5) {
            return;
        }
        XposedBridge.log("Hook数据库insert。table：" + tableName
                + "；nullColumnHack：" + nullColumnHack
                + "；CONFLICT_VALUES：" + arrayConflicValues[conflictValue]
                + "；contentValues:" + contentValues);
    }

}
