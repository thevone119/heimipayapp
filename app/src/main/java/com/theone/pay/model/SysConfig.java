package com.theone.pay.model;

import com.google.gson.Gson;
import com.theone.pay.db.DBUtils;

/**
 * 系统配置参数
 *
 */
public class SysConfig {
    private Integer keepNotifyTime = 1;//是否保持通知 0：否 1:是

    public Integer listenerPay = 1;//监控类型，0：不监控，手工提交，1：通知监控，2：ROOT监控，3：X框架模式


    private Integer pagesize = 10;//分页显示的每页显示大小

    private static SysConfig config;

    public Integer getKeepNotifyTime() {
        return keepNotifyTime;
    }

    public void setKeepNotifyTime(Integer keepNotifyTime) {
        this.keepNotifyTime = keepNotifyTime;
    }

    public Integer getPagesize() {
        return pagesize;
    }

    public void setPagesize(Integer pagesize) {
        this.pagesize = pagesize;
    }

    public Integer getListenerPay() {
        return listenerPay;
    }

    public void setListenerPay(Integer listenerPay) {
        this.listenerPay = listenerPay;
    }


    /**
     * 保存当前配置
     * @return
     */
    public static void saveCurrSysConfig(SysConfig p){
        TempObj obj = new TempObj();
        obj.setTempkey("CURR_SYS_CONFIG");
        Gson gson = new Gson();
        obj.setTempvalue(gson.toJson(p));
        obj.setTemptype("SysConfig");
        DBUtils.save(obj);
        config = p;
    }

    /**
     * 获取当前配置
     * @return
     */
    public static SysConfig getCurrSysConfig(){
        if(config==null){
            TempObj obj = DBUtils.getByKey("CURR_SYS_CONFIG");
            if(obj==null){
                return new SysConfig();
            }
            Gson gson = new Gson();
            config = gson.fromJson(obj.getTempvalue(),SysConfig.class);
        }
        return config;
    }
}
