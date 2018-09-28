package com.theone.pay.model;

import com.google.gson.Gson;
import com.theone.pay.db.DBUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * 消费记录
 */
public class PayBusChange {
    protected String  cid;//主键ID

    protected Long  busId;//商户ID

    protected String  bizId;//关联的业务单号

    protected Integer  ctype;//充值消费类型,1:充值，2：消费

    protected Integer  state;//状态0：无效 1：有效

    protected float  emoney;//金额,充值，则+,消费则-

    protected String  createtime;//createtime

    protected String  demo;//备注

    private static PayBusChange payBusChange;


    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public Long getBusId() {
        return busId;
    }

    public void setBusId(Long busId) {
        this.busId = busId;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public Integer getCtype() {
        return ctype;
    }

    public void setCtype(Integer ctype) {
        this.ctype = ctype;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public float getEmoney() {
        return emoney;
    }

    public void setEmoney(float emoney) {
        this.emoney = emoney;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getDemo() {
        return demo;
    }

    public void setDemo(String demo) {
        this.demo = demo;
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
    public static PayBusChange getCurrQueryPayBusChange(){
        if(payBusChange!=null){
            return payBusChange;
        }
        String key = "PAY_BUS_CHANGE_QUERY_PARAM";
        TempObj obj = DBUtils.getByKey(key);
        if(obj==null){
            payBusChange = new PayBusChange();
            return payBusChange;
        }
        Gson gson = new Gson();
        payBusChange = gson.fromJson(obj.getTempvalue(),PayBusChange.class);
        return payBusChange;
    }

    /**
     * 保存当前的查询条件
     * @param obj
     */
    public static void saveCurrQueryPayBusChange(PayBusChange obj){
        String key = "PAY_BUS_CHANGE_QUERY_PARAM";
        TempObj tobj = new TempObj();
        tobj.setTempkey(key);
        Gson gson = new Gson();
        tobj.setTempvalue(gson.toJson(obj));
        tobj.setTemptype("PayLog");
        DBUtils.save(tobj);
        payBusChange = obj;
    }


}
