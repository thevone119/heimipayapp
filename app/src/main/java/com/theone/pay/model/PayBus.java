package com.theone.pay.model;

import com.google.gson.Gson;
import com.theone.pay.db.DBUtils;

import org.json.JSONObject;

/**
 * 商户表Model对象
 */
public class PayBus {
    protected Long  busId;//商户ID,直接使用用户表的ID
    protected String  uuid;//用户的uuid
    protected String  busAcc;//商户账号
    protected String  busName;//bus_name
    protected Integer  busType;//商户类型,0：默认，无套餐  1：商户基础版套餐，2：商户高级版套餐，3：商户专业版套餐
    protected Long  busValidity;//套餐有效期
    protected float  eMoney=0.0f;//商户金额，余额
    protected String  createtime;//createtime
    protected Integer  payTimeOut=5;//支付超时时间。分钟，默认5分钟，可选5分钟，10分钟，15分钟
    protected String  gobackUrl;//goback_url

    protected String  notifyUrl;//notify_url



    private static PayBus paybus = null;


    public PayBus(){

    }


    public PayBus(String jsonstr){
        try{
            JSONObject jsobj = new JSONObject(jsonstr);
            busId = jsobj.getLong("busId");
            busValidity = jsobj.getLong("busValidity");

            uuid = jsobj.getString("uuid");
            busAcc = jsobj.getString("busAcc");
            busName = jsobj.getString("busName");
            createtime = jsobj.getString("createtime");

            busType = jsobj.getInt("busType");
            payTimeOut = jsobj.getInt("payTimeOut");

            eMoney = new Float(jsobj.getDouble("eMoney"));

        }catch (Exception e){
            e.printStackTrace();
        }
    }



    public Long getBusId() {
        return busId;
    }

    public void setBusId(Long busId) {
        this.busId = busId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getBusAcc() {
        return busAcc;
    }

    public void setBusAcc(String busAcc) {
        this.busAcc = busAcc;
    }

    public String getBusName() {
        return busName;
    }

    public void setBusName(String busName) {
        this.busName = busName;
    }

    public Integer getBusType() {
        return busType;
    }

    public void setBusType(Integer busType) {
        this.busType = busType;
    }

    public Long getBusValidity() {
        return busValidity;
    }

    public void setBusValidity(Long busValidity) {
        this.busValidity = busValidity;
    }

    public float geteMoney() {
        return eMoney;
    }

    public void seteMoney(float eMoney) {
        this.eMoney = eMoney;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public Integer getPayTimeOut() {
        return payTimeOut;
    }

    public void setPayTimeOut(Integer payTimeOut) {
        this.payTimeOut = payTimeOut;
    }

    public String getGobackUrl() {
        return gobackUrl;
    }

    public void setGobackUrl(String gobackUrl) {
        this.gobackUrl = gobackUrl;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getBusTypeStr() {
        switch (this.busType){
            case 0:
                return "无";
            case 1:
                return "基础版";
            case 2:
                return "高级版";
            case 3:
                return "专业版";
        }
        return "无";
        //return busTypeStr;
    }

    /**
     * 保存当前商户
     * @param jsonobj
     */
    public static void saveCurrPayBus(String jsonobj){
        Gson gson = new Gson();
        PayBus p = gson.fromJson(jsonobj,PayBus.class);
        saveCurrPayBus(p);
    }
    /**
     * 保存当前商户
     * @return
     */
    public static void saveCurrPayBus(PayBus p){
        TempObj obj = new TempObj();
        obj.setTempkey("CURR_PAY_BUS");
        Gson gson = new Gson();
        obj.setTempvalue(gson.toJson(p));
        obj.setTemptype("PayBus");
        DBUtils.save(obj);
        paybus = p;
    }

    /**
     * 获取当前的商户
     * @return
     */
    public static PayBus getCurrPayBus(){
        if(paybus==null){
            TempObj obj = DBUtils.getByKey("CURR_PAY_BUS");
            if(obj==null){
                return null;
            }
            Gson gson = new Gson();
            paybus = gson.fromJson(obj.getTempvalue(),PayBus.class);
        }
        return paybus;
    }

    /**
     * 清除当前用户，退出系统，切换用户
     */
    public static void clearCurrPayBus(){
        DBUtils.delete("CURR_PAY_BUS");
    }


}
