package com.theone.pay.model;

import android.app.Notification;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;

import com.theone.pay.httpservice.PayService;
import com.theone.pay.utils.EmojiFilter;
import com.theone.pay.utils.SecurityClass;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 接收系统消息，存储在这里
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MyNotification {
    private String nkey;//通知的唯一主键
    private String id;
    private long postTime;
    private Long postTimeService;//app通知的时间，计算得到的服务器时间
    private String packageName;
    private String title;
    private String text;
    private String subText;


    private String uid;//商户ID
    private Integer state=0;//状态，0：未提交，1：已提交服务器 2：已关联相关定的订单

    private String logid;//对应的订单ID

    private String sign;//对应的签名数据





    public MyNotification(){

    }

    public RunLog toRunLog(){
        String logType = "";
        String logText = "";
        if(nkey.startsWith("n_")){
            logType = "任务栏通知数据";
            logText = title+"\n"+text;
        }
        if(nkey.startsWith("wxm_")){
            logType = "微信消息数据";
            logText = title+"\n"+text;
        }


        RunLog log = new RunLog(logType,logText);

        return log;
    }

    public MyNotification(WXMessage msg){
        this.id=msg.msgid;
        this.postTime = msg.createtime;
        this.postTimeService = msg.createtime;
        this.packageName = "com.tencent.mm";
        this.title = msg.talker;
        this.text = msg.content;
        this.subText = msg.msgseq+"";
        this.nkey = "wxm_"+msg.msgid;
    }

    public MyNotification(StatusBarNotification sbn){
        this.id =  sbn.getId()+"";
        this.postTime=sbn.getPostTime();
        this.packageName= sbn.getPackageName();
        Notification notification = sbn.getNotification();
        try{
            if (Build.VERSION.SDK_INT >Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if(notification!=null){
                    Bundle extras = notification.extras;

                    String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
                    //int notificationIcon = extras.getInt(Notification.EXTRA_SMALL_ICON);
                    //Bitmap notificationLargeIcon = ((Bitmap)extras.getParcelable(Notification.EXTRA_LARGE_ICON));
                    CharSequence notificationText = extras.getCharSequence(Notification.EXTRA_TEXT);
                    CharSequence notificationSubText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
                    this.title=notificationTitle;
                    if(notificationText!=null){
                        this.text=notificationText.toString();
                        if(EmojiFilter.containsEmoji(this.text)){
                            this.text = EmojiFilter.filterEmoji(this.text);
                        }
                    }
                    if(notificationSubText!=null){
                        this.subText = notificationSubText.toString();
                        if(EmojiFilter.containsEmoji(this.subText)){
                            this.subText = EmojiFilter.filterEmoji(this.subText);
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        this.nkey = "n_"+packageName+"_"+id+"_"+postTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getPostTime() {
        return postTime;
    }

    public void setPostTime(long postTime) {
        this.postTime = postTime;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSubText() {
        return subText;
    }

    public void setSubText(String subText) {
        this.subText = subText;
    }

    public String getNkey() {
        return nkey;
    }

    public void setNkey(String nkey) {
        this.nkey = nkey;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getLogid() {
        return logid;
    }

    public void setLogid(String logid) {
        this.logid = logid;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public Long getPostTimeService() {
        return postTimeService;
    }

    public void setPostTimeService(Long postTimeService) {
        this.postTimeService = postTimeService;
    }

    /**
     * 对对象进行签名,随便签一下就行了,自己用的
     * @return
     */
    public String MarkSign() {
        //组成相关的
        StringBuffer stringA =new  StringBuffer();
        stringA.append("sign_key=heimipay.com");
        stringA.append("&"+nkey);
        stringA.append("&"+id);
        stringA.append("&"+postTime);
        stringA.append("&"+packageName);
        stringA.append("&"+title);
        stringA.append("&"+text);
        stringA.append("&"+subText);
        stringA.append("&"+uid);
        //签名
        String _sign =  SecurityClass.encryptMD5(stringA.toString()).toUpperCase();
        return _sign;
    }

    @Override
    public String toString() {
        return "MyNotification{" +
                "id=" + id +
                ", postTime=" + postTime +
                ", packageName='" + packageName + '\'' +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", subText='" + subText + '\'' +
                '}';
    }


}
