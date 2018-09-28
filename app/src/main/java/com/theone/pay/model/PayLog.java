package com.theone.pay.model;

import com.google.gson.Gson;
import com.theone.pay.db.DBUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * 对象功能:  支付记录表Model对象
 *
 */
public class PayLog {
    protected Long  logId;//日志ID

    protected String  rid;//随机ID

    protected Long  busId;//商户ID

    protected Integer  busType;//商户类型

    protected String  busAcc;//商户账号

    protected String  uid;//商户uid

    protected String  busName;//商户姓名

    protected String  orderid;//订单ID，外部传过来的订单ID

    protected Long  prodId;//支付商品ID

    protected String  prodName;//支付商品名称

    protected Float  prodPrice;//支付商品价格

    protected Float  payImgPrice;//二维码支付价格

    protected Float  payServiceChange;//支付平台服务费，手续费,无效

    protected String  payImgContent;//支付二维码图片内容

    protected String  payDemo;//支付订单备注

    protected String  payName;//支付订单名称

    protected Integer  payType;//pay_type 1：支付宝；2：微信支付

    protected String  createtime;//创建时间

    protected String  updatetime;//更新时间，订单支付只有5分钟有效，可以更新，延长支付时间

    protected String  payTime;//pay_time

    protected Integer  payState;//支付状态 -1：未知状态，0：等待支付,未支付 1：支付成功，2：支付失败，11：账户余额不足，12：账户套餐过期，13：支付超时

    protected String  payExt2;//pay_ext2

    protected String  payExt1;//pay_ext1

    protected Integer  notifyState;//notify_state 0：未通知，1：已成功通知 2：支付通知失败

    protected Integer  notifyCount=0;//notify_count 支付通知次数

    //当前的查询条件
    private static PayLog payLog = null;

    //查看最新的日志
    private static PayLog lastPayLogInfo = null;


    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public Long getBusId() {
        return busId;
    }

    public void setBusId(Long busId) {
        this.busId = busId;
    }

    public Integer getBusType() {
        return busType;
    }

    public void setBusType(Integer busType) {
        this.busType = busType;
    }

    public String getBusAcc() {
        return busAcc;
    }

    public void setBusAcc(String busAcc) {
        this.busAcc = busAcc;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBusName() {
        return busName;
    }

    public void setBusName(String busName) {
        this.busName = busName;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public Long getProdId() {
        return prodId;
    }

    public void setProdId(Long prodId) {
        this.prodId = prodId;
    }

    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    public Float getProdPrice() {
        return prodPrice;
    }

    public void setProdPrice(Float prodPrice) {
        this.prodPrice = prodPrice;
    }

    public Float getPayImgPrice() {
        return payImgPrice;
    }

    public void setPayImgPrice(Float payImgPrice) {
        this.payImgPrice = payImgPrice;
    }

    public Float getPayServiceChange() {
        return payServiceChange;
    }

    public void setPayServiceChange(Float payServiceChange) {
        this.payServiceChange = payServiceChange;
    }

    public String getPayImgContent() {
        return payImgContent;
    }

    public void setPayImgContent(String payImgContent) {
        this.payImgContent = payImgContent;
    }

    public String getPayDemo() {
        return payDemo;
    }

    public void setPayDemo(String payDemo) {
        this.payDemo = payDemo;
    }

    public String getPayName() {
        return payName;
    }

    public void setPayName(String payName) {
        this.payName = payName;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public Integer getPayState() {
        return payState;
    }

    public void setPayState(Integer payState) {
        this.payState = payState;
    }

    public String getPayExt2() {
        return payExt2;
    }

    public void setPayExt2(String payExt2) {
        this.payExt2 = payExt2;
    }

    public String getPayExt1() {
        return payExt1;
    }

    public void setPayExt1(String payExt1) {
        this.payExt1 = payExt1;
    }

    public Integer getNotifyState() {
        return notifyState;
    }

    public void setNotifyState(Integer notifyState) {
        this.notifyState = notifyState;
    }

    public Integer getNotifyCount() {
        return notifyCount;
    }

    public void setNotifyCount(Integer notifyCount) {
        this.notifyCount = notifyCount;
    }

    public String getPayStateStr() {
        //支付状态 -1：未知状态，0：等待支付 1：支付成功，2：支付失败，11：账户余额不足，12：账户套餐过期，13：支付超时
        switch (this.payState){
            case -1:
                return "未知状态";
            case 0:
                return "未支付";
            case 1:
                return "已支付";
            case 2:
                return "支付失败";
            case 11:
                return "账户余额不足";
            case 12:
                return "账户套餐过期";
            case 13:
                return "支付超时";
        }
        return "";
    }

    public String getNotifyStateStr() {
        //0：未通知，1：已成功通知 2：支付通知失败
        switch (this.notifyState){
            case 0:
                return "未通知";
            case 1:
                return "已成功通知";
            case 2:
                return "支付通知失败";
        }
        return "";
    }
    public String getPayTypeStr() {
        //0：未知，1：支付宝支付 2：微信支付
        switch (this.payType){
            case -1:
                return "未知";
            case 0:
                return "未知";
            case 1:
                return "支付宝支付";
            case 2:
                return "微信支付";
        }
        return "";
    }

    public String getCreatetimeStr()  {
        if(this.createtime==null){
            return null;
        }
        if(this.createtime.indexOf("-")!=-1){
            return createtime;
        }

        if(this.createtime.length()==14){
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                return format2.format(format.parse(createtime));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return createtime;
    }

    /**
     * 获取当前的查询条件
     * @return
     */
    public static PayLog getCurrQueryPayLog(){
        if(payLog!=null){
            return payLog;
        }
        String key = "PAY_LOG_QUERY_PARAM";
        TempObj obj = DBUtils.getByKey(key);
        if(obj==null){
            payLog = new PayLog();
            return payLog;
        }
        Gson gson = new Gson();
        payLog = gson.fromJson(obj.getTempvalue(),PayLog.class);
        return payLog;
    }

    /**
     * 保存当前的查询条件
     * @param qlog
     */
    public static void saveCurrQueryPayLog(PayLog qlog){
        String key = "PAY_LOG_QUERY_PARAM";
        TempObj obj = new TempObj();
        obj.setTempkey(key);
        Gson gson = new Gson();
        obj.setTempvalue(gson.toJson(qlog));
        obj.setTemptype("PayLog");
        DBUtils.save(obj);
        payLog = qlog;
    }

    /**
     * 获取当前的查询条件
     * @return
     */
    public static PayLog getLastPayLogInfo(){
        if(lastPayLogInfo!=null){
            return lastPayLogInfo;
        }
        String key = "PAY_LOG_LAST_INFO";
        TempObj obj = DBUtils.getByKey(key);
        if(obj==null){
            lastPayLogInfo = new PayLog();
            return lastPayLogInfo;
        }
        Gson gson = new Gson();
        lastPayLogInfo = gson.fromJson(obj.getTempvalue(),PayLog.class);
        return lastPayLogInfo;
    }

    /**
     * 保存当前的查询条件
     * @param qlog
     */
    public static void saveLastPayLogInfo(PayLog qlog){
        String key = "PAY_LOG_LAST_INFO";
        TempObj obj = new TempObj();
        obj.setTempkey(key);
        Gson gson = new Gson();
        obj.setTempvalue(gson.toJson(qlog));
        obj.setTemptype("PayLog");
        DBUtils.save(obj);
        lastPayLogInfo = qlog;
    }


}
