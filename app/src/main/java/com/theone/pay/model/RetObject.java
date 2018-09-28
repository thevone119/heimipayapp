package com.theone.pay.model;

import org.json.JSONObject;

/**
 * 针对http请求返回的json的封装
 */
public class RetObject {
    private Boolean success = false;
    private Integer code = 200;
    private String msg = "ok";
    private Integer total = 0;
    private Object data;

    public RetObject(){

    }

    public RetObject(String jsonStr){
        try{
            JSONObject jsobj = new JSONObject(jsonStr);
            if(jsobj.has("success")){
                success = jsobj.getBoolean("success");
            }
            code = jsobj.getInt("code");
            msg = jsobj.getString("msg");
            if(jsobj.has("total")){
                total = jsobj.getInt("total");
            }
            if(jsobj.has("data")){
                //data = jsobj.getJSONObject("data");
            }
        }catch (Exception e){
            e.printStackTrace();
            success=false;
            code=-1;
            msg="系统数据错误，请稍候再试";
        }
    }



    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
