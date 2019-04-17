package com.theone.pay.model;

/**
 * 微信的消息对象
 */
public class WXMessage {
    public String msgid;
    public long createtime;
    public int msgseq;
    public String talker;
    public String content;

    public WXMessage(){

    }

    @Override
    public String toString() {
        return "WXMessage{" +
                "msgid=" + msgid +
                ", createtime=" + createtime +
                ", msgseq=" + msgseq +
                ", talker='" + talker + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
