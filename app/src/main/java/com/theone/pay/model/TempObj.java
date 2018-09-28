package com.theone.pay.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TempObj {
    //文件解压类型
    public static final String TYPE_MOVIE_UN_ZIP = "TYPE_MOVIE_UN_ZIP";

    //手机的当前ID(存储一个随机的ID进去),这个只存一个哦。
    public static final String TYPE_PHONE_PID = "TYPE_PHONE_PID";

    private String tempkey;
    private String temptype= TYPE_MOVIE_UN_ZIP;
    private String tempvalue;
    private String createtime;

    public TempObj(){
        SimpleDateFormat shortDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        createtime = shortDateFormat.format(new Date());
        //默认使用用随机UUID做主机
        tempkey = java.util.UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }

    public String getTemptype() {
        return temptype;
    }

    public void setTemptype(String temptype) {
        this.temptype = temptype;
    }

    public String getTempkey() {
        return tempkey;
    }

    public void setTempkey(String tempkey) {
        this.tempkey = tempkey;
    }

    public String getTempvalue() {
        return tempvalue;
    }

    public void setTempvalue(String tempvalue) {
        this.tempvalue = tempvalue;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }
}
