package com.theone.pay.httpservice;

import android.util.Log;

import com.google.gson.Gson;
import com.theone.pay.db.DBUtils;
import com.theone.pay.db.DatabaseHelper;
import com.theone.pay.model.MyNotification;
import com.theone.pay.model.PayBus;
import com.theone.pay.model.PayBusChange;
import com.theone.pay.model.PayLog;
import com.theone.pay.model.RetObject;
import com.theone.pay.model.RunLog;
import com.theone.pay.model.SysConfig;
import com.theone.pay.model.TempObj;
import com.theone.pay.utils.BaseUrlTools;
import com.theone.pay.utils.MyRequests;
import com.theone.pay.utils.SecurityClass;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 各种支付接口的实现,http接口实现
 *
 */
public class PayService {
    private static final String TAG="PayService";


    //判断是否是已存在的，如果是已存在的，则不进行处理
    private static Map<String,String> hasMap = new HashMap<String,String>();

    //客户端监听通知的包名
    private static List<String> listPackageName =new ArrayList<>();

    //app通知的text监听过滤
    private static List<String> listTextFilter =new ArrayList<>();

    /**
     * 登录接口
     * @return
     */
    public RetObject login(String acc, String pwd){
        RetObject ret = new RetObject();
        String url = BaseUrlTools.getBaseUrl()+"/api/pay/paybus/loginAndQuery";
        MyRequests req =new MyRequests();
        Map<String,String> postData =new HashMap<String,String>();
        postData.put("acc",acc);
        postData.put("pwd",pwd);
        Log.w(TAG, "url:" + url);
        String retstr = req.post(url,postData);
        if(retstr==null){
            ret.setSuccess(false);
            ret.setCode(-1);
            ret.setMsg("系统繁忙，请稍候再试");
            return ret;
        }else{
            ret = new RetObject(retstr);
        }
        RunLog.putLog("系统登录","登录APP");
        //登录成功，保存当前商户信息
        if(ret.getSuccess()){
            try{
                JSONObject jsobj = new JSONObject(retstr);
                Gson gson = new Gson();
                PayBus p = gson.fromJson(jsobj.getJSONObject("data").toString(),PayBus.class);
                //未开通商户套餐
                if(p.getBusType()==null||p.getBusType()==0){
                    ret.setSuccess(false);
                    ret.setCode(-1);
                    ret.setMsg("对不起，您还未开通套餐，请先在网站开通套餐");
                    return ret;
                }
                PayBus.saveCurrPayBus(p);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return ret;
    }


    /**
     * 刷新当前商户接口
     * @return
     */
    public RetObject RefreshPayBus(){
        RetObject ret = new RetObject();
        PayBus paybus = PayBus.getCurrPayBus();
        if(paybus==null){
            ret.setSuccess(false);
            ret.setCode(-2);
            ret.setMsg("当前商户数据无效，请重新登录");
            return ret;
        }

        String url = BaseUrlTools.getBaseUrl()+"/api/pay/paybus/queryByUid";
        MyRequests req =new MyRequests();
        Map<String,String> postData =new HashMap<String,String>();
        postData.put("uid",paybus.getUuid());
        String retstr = req.post(url,postData);
        if(retstr==null){
            ret.setSuccess(false);
            ret.setCode(-1);
            ret.setMsg("系统繁忙，请稍候再试");
            return ret;
        }else{
            ret = new RetObject(retstr);
        }
        //查询，保存/刷新当前商户信息
        if(ret.getSuccess()){
            try{
                JSONObject jsobj = new JSONObject(retstr);
                PayBus.saveCurrPayBus(jsobj.getJSONObject("data").toString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * 查询支付日志，分页查询(一次最多允许查询100条)
     * @return
     */
    public RetObject queryPayLog(int pageNo,int pageSize){
        RetObject ret = new RetObject();
        PayBus paybus = PayBus.getCurrPayBus();
        if(paybus==null){
            ret.setSuccess(false);
            ret.setCode(-2);
            ret.setMsg("当前商户数据无效，请重新登录");
            return ret;
        }
        RunLog.putLog("订单查询","订单查询");
        String url = BaseUrlTools.getBaseUrl()+"/api/pay/paylog/findPage";
        Log.i("PAY","url:"+url);
        MyRequests req =new MyRequests();
        Map<String,String> postData =new HashMap<String,String>();
        postData.put("uid",paybus.getUuid());
        postData.put("busId",paybus.getBusId()+"");
        postData.put("pageNo",pageNo+"");
        postData.put("pageSize",pageSize+"");
        postData.put("totalCount","10000");
        //查询条件
        PayLog log = PayLog.getCurrQueryPayLog();
        if(log.getPayType()!=null && log.getPayType()>0){
            postData.put("payType",log.getPayType()+"");
        }
        if(log.getPayState()!=null && log.getPayState()>=0){
            postData.put("payState",log.getPayState()+"");
        }
        if(log.getOrderid()!=null&& log.getOrderid().length()>0){
            postData.put("orderid",log.getOrderid());
        }
        if(log.getCreatetime()!=null ){
            if(log.getCreatetime().length()>2){
                postData.put("createtime",log.getCreatetime());
            }
            SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
            Calendar calendar=Calendar.getInstance();
            calendar.add(Calendar.MONTH,-1);
            if(log.getCreatetime().equals("1")){
                postData.put("createtime",format.format(new Date()));
            }
            if(log.getCreatetime().equals("2")){
                postData.put("createtime",format.format(calendar.getTime()));
            }
        }


        String retstr = req.post(url,postData);
        if(retstr==null){
            ret.setSuccess(false);
            ret.setCode(-1);
            ret.setMsg("系统繁忙，请稍候再试");
            return ret;
        }else{
            ret = new RetObject(retstr);
        }

        //查询成功，把list转换,放入data
        if(ret.getSuccess() && ret.getTotal()>0){
            try{
                List<PayLog> list =new ArrayList<PayLog>();
                JSONObject jsobj = new JSONObject(retstr);
                JSONArray ja = jsobj.getJSONArray("data");
                for(int i=0;i<ja.length();i++){
                    Gson gson = new Gson();
                    PayLog l = gson.fromJson(ja.get(i).toString(),PayLog.class);
                    list.add(l);
                }
                ret.setData(list);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * 查询支付日志，单条查询
     * @return
     */
    public RetObject queryByRid(String rid){
        RetObject ret = new RetObject();
        String url = BaseUrlTools.getBaseUrl()+"/api/pay/paylog/queryByRid";
        MyRequests req =new MyRequests();
        Map<String,String> postData =new HashMap<String,String>();
        postData.put("rid",rid);
        String retstr = req.post(url,postData);
        if(retstr==null){
            ret.setSuccess(false);
            ret.setCode(-1);
            ret.setMsg("系统繁忙，请稍候再试");
            return ret;
        }else{
            ret = new RetObject(retstr);
        }

        //查询成功，把list转换,放入data
        if(ret.getSuccess()){
            try{
                Gson gson = new Gson();
                JSONObject jsobj = new JSONObject(retstr);
                PayLog l = gson.fromJson( jsobj.getJSONObject("data").toString(),PayLog.class);
                ret.setData(l);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * 查询账单
     * @param pageNo
     * @param pageSize
     * @return
     */
    public RetObject queryPayBusChange(int pageNo,int pageSize){
        RetObject ret = new RetObject();
        PayBus paybus = PayBus.getCurrPayBus();
        if(paybus==null){
            ret.setSuccess(false);
            ret.setCode(-2);
            ret.setMsg("当前商户数据无效，请重新登录");
            return ret;
        }
        RunLog.putLog("账单查询","账单查询");
        String url = BaseUrlTools.getBaseUrl()+"/api/pay/paybuschange/findPage";
        MyRequests req =new MyRequests();
        Map<String,String> postData =new HashMap<String,String>();
        postData.put("uid",paybus.getUuid());
        postData.put("busId",paybus.getBusId()+"");
        postData.put("pageNo",pageNo+"");
        postData.put("pageSize",pageSize+"");
        postData.put("totalCount","10000");
        //查询条件
        PayBusChange q = PayBusChange.getCurrQueryPayBusChange();
        if(q.getCtype()!=null){
            postData.put("ctype",q.getCtype()+"");
        }
        if(q.getCreatetime()!=null ){
            if(q.getCreatetime().length()>2){
                postData.put("createtime",q.getCreatetime());
            }
            SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
            Calendar calendar=Calendar.getInstance();
            calendar.add(Calendar.MONTH,-1);
            if(q.getCreatetime().equals("1")){
                postData.put("createtime",format.format(new Date()));
            }
            if(q.getCreatetime().equals("2")){
                postData.put("createtime",format.format(calendar.getTime()));
            }
        }

        String retstr = req.post(url,postData);
        if(retstr==null){
            ret.setSuccess(false);
            ret.setCode(-1);
            ret.setMsg("系统繁忙，请稍候再试");
            return ret;
        }else{
            ret = new RetObject(retstr);
        }

        //查询成功，把list转换,放入data
        if(ret.getSuccess() && ret.getTotal()>0){
            try{
                List<PayBusChange> list =new ArrayList<PayBusChange>();
                JSONObject jsobj = new JSONObject(retstr);
                JSONArray ja = jsobj.getJSONArray("data");
                for(int i=0;i<ja.length();i++){
                    Gson gson = new Gson();
                    PayBusChange l = gson.fromJson(ja.get(i).toString(),PayBusChange.class);
                    list.add(l);
                }
                ret.setData(list);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * 手工确认收款接口
     * @return
     */
    public RetObject payCheck(Long logId,String orderid,String rid){
        RetObject ret = new RetObject();
        ret.setSuccess(false);
        PayBus paybus = PayBus.getCurrPayBus();
        if(paybus==null){
            ret.setSuccess(false);
            ret.setCode(-2);
            ret.setMsg("当前商户数据无效，请重新登录");
            return ret;
        }
        StringBuffer stringA =new  StringBuffer();
        String nonce_str = System.currentTimeMillis()+"";
        stringA.append("logId="+logId+"&orderid="+orderid+"&rid="+rid+"&uid="+paybus.getUuid()+"&busId="+paybus.getBusId()+"&nonce_str="+nonce_str);
        String _sign =  SecurityClass.encryptMD5(stringA.toString()).toUpperCase();
        String url = BaseUrlTools.getBaseUrl()+"/api/pay/paylog/app_pay_check";
        MyRequests req =new MyRequests();
        Map<String,String> postData =new HashMap<String,String>();
        postData.put("uid",paybus.getUuid());
        postData.put("sign",_sign);
        postData.put("logId",logId+"");
        postData.put("orderid",orderid);
        postData.put("rid",rid);
        postData.put("nonce_str",nonce_str);
        String retstr = req.post(url,postData);
        if(retstr==null){
            ret.setSuccess(false);
            ret.setCode(-1);
            ret.setMsg("系统繁忙，请稍候再试");
            return ret;
        }else{
            ret = new RetObject(retstr);
        }

        return ret;
    }

    /**
     * 发起通知
     * @return
     */
    public RetObject payNotify(String rid){
        RetObject ret = new RetObject();
        ret.setSuccess(false);
        PayBus paybus = PayBus.getCurrPayBus();
        if(paybus==null){
            ret.setSuccess(false);
            ret.setCode(-2);
            ret.setMsg("当前商户数据无效，请重新登录");
            return ret;
        }
        String nonce_str = System.currentTimeMillis()+"";

        String url = BaseUrlTools.getBaseUrl()+"/api/pay/paylog/payNotify";
        MyRequests req =new MyRequests();
        Map<String,String> postData =new HashMap<String,String>();
        postData.put("uid",paybus.getUuid());
        postData.put("rid",rid);
        postData.put("nonce_str",nonce_str);
        String retstr = req.post(url,postData);
        if(retstr==null){
            ret.setSuccess(false);
            ret.setCode(-1);
            ret.setMsg("系统繁忙，请稍候再试");
            return ret;
        }else{
            ret = new RetObject(retstr);
        }

        return ret;
    }

    /**
     * 获取当前服务器时间
     * @return
     */
    public long getServiceTime(){
        String url = BaseUrlTools.getBaseUrl()+"/comm/sys_time?t="+System.currentTimeMillis();
        MyRequests req =new MyRequests();
        Map<String,String> postData =new HashMap<String,String>();
        req.setConnectTimeout(3000);
        req.setReadTimeout(3000);
        String retstr = req.get(url);
        if(retstr==null){
            return 0;
        }
        try{
            if(retstr.indexOf("code")!=-1){
                JSONObject jsobj = new JSONObject(retstr);
                return jsobj.getLong("data");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 根据客户端时间，计算服务器的时间
     * @param clienTime
     * @return
     */
    public long getServiceTime(long clienTime){
        long cstime = getServiceTime();//当前的服务器时间
        long cctime = new Date().getTime();//当前的客户端时间
        if(cstime==0){
            return 0;
        }
        //服务器时间=客户端时间+当前服务时间与当前客户端时间的差
        return clienTime+(cstime-cctime);
    }


    /**
     * 保存，并且发送服务器请求
     * @param notify
     * @return
     */
    public RetObject saveAndSendNotify(MyNotification notify){
        RetObject ret = new RetObject();
        ret.setSuccess(false);
        if(SysConfig.getCurrSysConfig().getListenerPay()==0){
            //不监控
            return ret;
        }
        if(hasMap==null){
            hasMap = new HashMap<String,String>();
        }
        //Log.i(TAG,"hasMap size:"+hasMap.size());
        //已处理过的不处理
        if(hasMap.containsKey(notify.getNkey())){
            ret.setCode(-2);
            ret.setMsg("当前通知已提交");
            return ret;
        }

        //判断包名是否有效，只要匹配一个即匹配
        boolean isenable = false;
        List<String> listpname=this.queryPackageName();
        for(String pn:listpname){
            if(notify.getPackageName().indexOf(pn)!=-1){
                isenable=true;
                break;
            }
        }
        if(!isenable){
            ret.setSuccess(false);
            ret.setCode(-2);
            //Log.i(TAG,"当前包名无效:"+notify.getPackageName());
            ret.setMsg("当前包名无效");
            return ret;
        }
        //判断text是否有效,必须多个都匹配，有一个不匹配，则不匹配
        listpname=this.queryTextFilter();
        for(String pn:listpname){
            if(notify.getText()==null || notify.getText().indexOf(pn)==-1){
                isenable=false;
                break;
            }
        }
        if(!isenable){
            ret.setSuccess(false);
            ret.setCode(-2);
            Log.i(TAG,"通知内容无效:"+notify.getText());
            ret.setMsg("通知内容无效");
            return ret;
        }
        RunLog.putLog(notify.toRunLog());
        Log.i(TAG,"通过过滤的通知:"+notify.toString());
        Gson gson = new Gson();
        TempObj obj = DBUtils.getByKey(notify.getNkey());
        if(obj!=null){
            MyNotification tn = gson.fromJson(obj.getTempvalue(),MyNotification.class);
            if(tn.getState()!=0){
                ret.setCode(-2);
                ret.setMsg("当前通知已提交");
                hasMap.put(notify.getNkey(),"");
                return ret;
            }
        }else{
            obj = new TempObj();
        }
        //2.发送服务器请求
        try{
            RetObject ret2 = sendNotify(notify);
            Log.i(TAG,"发送数据返回:"+ret2.getSuccess());
            if(ret2.getSuccess()){
                notify.setState(1);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //3.保存到数库
        obj.setTempkey(notify.getNkey());
        obj.setTempvalue(gson.toJson(notify));
        //失败
        if(notify.getState()==0){
            obj.setTemptype("MyNotification_0");
        }else{//成功
            obj.setTemptype("MyNotification_1");
            ret.setSuccess(true);
            hasMap.put(notify.getNkey(),"");
            Log.i(TAG,"put:"+notify.getNkey());
        }
        DBUtils.save(obj);
        return ret;
    }

    /**
     * 发送所有的通知到服务器
     * 每分钟执行一次？
     */
    public void sendAllNotify(){
        List<TempObj> listo = DBUtils.listObj("MyNotification_0");
        if(listo==null||listo.size()<=0){
            Log.i(TAG,"sendAllNotify size:"+0);
            return ;
        }else{
            Log.i(TAG,"sendAllNotify size:"+listo.size());
        }
        Gson gson = new Gson();
        for(TempObj o : listo){
            MyNotification tn = gson.fromJson(o.getTempvalue(),MyNotification.class);
            if(tn.getState()!=0){
                continue;
            }
            saveAndSendNotify(tn);
        }
    }


    /**
     * 发送APP通知到服务器
     * 单个发送
     * @return
     */
    private RetObject sendNotify(MyNotification notify){
        RetObject ret = new RetObject();
        ret.setSuccess(false);
        PayBus paybus = PayBus.getCurrPayBus();
        if(paybus==null){
            ret.setSuccess(false);
            ret.setCode(-2);
            ret.setMsg("当前商户数据无效，请重新登录");
            return ret;
        }

        if(notify.getPostTimeService()==null||notify.getPostTimeService()<=0){
            long postTime = getServiceTime(notify.getPostTime());
            if(postTime<=0){
                ret.setSuccess(false);
                ret.setCode(-2);
                ret.setMsg("无法获取服务器时间，稍候再试");
                return ret;
            }
            notify.setPostTimeService(postTime);
        }
        if(notify.getUid()==null){
            notify.setUid(paybus.getUuid());
        }

        String url = BaseUrlTools.getBaseUrl()+"/api/pay/payappnotification/saveAppNotification?t="+System.currentTimeMillis();
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
        if(retstr==null){
            ret.setCode(-1);
            ret.setMsg("请求服务器失败");
            return ret;
        }
        ret = new RetObject(retstr);
        return ret;
    }

    /**
     * 获取监听的包名
     * @return
     */
    public List<String> queryPackageName(){
        if(listPackageName==null){
            listPackageName = new ArrayList<>();
        }
        if(listPackageName.size()<=0){
            String url = BaseUrlTools.getBaseUrl()+"/api/pay/payappnotification/queryPackageName?t="+System.currentTimeMillis();
            MyRequests req =new MyRequests();
            req.setConnectTimeout(3000);
            req.setReadTimeout(5000);
            String retstr = req.get(url);
            try{
                RetObject ret = new RetObject(retstr);
                if(ret.getSuccess()){
                    JSONObject jsobj = new JSONObject(retstr);
                    JSONArray ja = jsobj.getJSONArray("data");
                    for(int i=0;i<ja.length();i++){
                        listPackageName.add(ja.get(i).toString());
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(listPackageName.size()<=0){
            listPackageName.add("com.tencent.mm");
            listPackageName.add("com.eg.android.AlipayGphone");
        }
        return listPackageName;
    }

    public List<String> queryTextFilter(){
        if(listTextFilter==null){
            listTextFilter = new ArrayList<>();
        }
        if(listTextFilter.size()<=0){
            String url = BaseUrlTools.getBaseUrl()+"/api/pay/payappnotification/queryTextFilter?t="+System.currentTimeMillis();
            MyRequests req =new MyRequests();
            req.setConnectTimeout(3000);
            req.setReadTimeout(5000);
            String retstr = req.get(url);
            try{
                RetObject ret = new RetObject(retstr);
                if(ret.getSuccess()){
                    JSONObject jsobj = new JSONObject(retstr);
                    JSONArray ja = jsobj.getJSONArray("data");
                    for(int i=0;i<ja.length();i++){
                        listTextFilter.add(ja.get(i).toString());
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(listTextFilter.size()<=0){
            listTextFilter.add("款");
            listTextFilter.add("元");
        }
        return listTextFilter;
    }

    /**
     * 请求百度链接，报错每分钟请求一次，避免长期不请求网络，网络中断
     *
     */
    public void getbaidu(){
        MyRequests req =new MyRequests();
        req.setConnectTimeout(3000);
        req.setReadTimeout(5000);
        req.get("https://www.baidu.com");


    }



    }
