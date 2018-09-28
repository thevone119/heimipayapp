package com.theone.pay.utils;

import com.theone.pay.db.DBUtils;
import com.theone.pay.model.TempObj;

import org.json.JSONObject;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取系统的跟路径
 *
 */
public class BaseUrlTools {

    //BASE_URL格式http://www.xxxx.com
    public static String BASE_URL = null;

    //所有可能的地址
    private static final String[] urls=new String[]{
            "http://www.heimipay.com",
            "http://www.heimipaya.com",
            "https://www.heimipay.com",
            "https://www.heimipaya.com",
            "http://www.heimipaypp.com",
            "http://www.heimipaydd.com",
            "http://www.heimipayun.com",
            "http://www.heimipayix.com",
            "http://www.heimipayjk.com",
            "http://www.heimipayww.com"
    };

    /**
     * 初始化基础路径,多线程的方式获取真实的路径
     */
    public static void init(){
        //1.通过数据库获取路径
        List<String> listurl = new ArrayList<String>();
        String hasurl = null;
        try{
            TempObj tobj = DBUtils.getByKey("BASE_URL");
            if(tobj!=null && tobj.getTempvalue()!=null && tobj.getTempvalue().toLowerCase().indexOf("http")!=-1){
                hasurl = tobj.getTempvalue();
            }
        }catch (Exception e){
        }
        if(hasurl!=null){
            listurl.add(hasurl);
        }
        //所有可能的url进行循环
        for(String turl:urls){
            if(hasurl!=null &&hasurl.equals(turl)){
                continue;
            }
            if(turl==null||turl.length()<5){
                continue;
            }
            listurl.add(turl);
        }

        //对每个url进行相关的测试
        testUrlThread(listurl);
    }

    /**
     * 线程调用
     * @return
     */
    private static void testUrlThread(final List<String> listurl){
            Thread t = new Thread(){
                @Override
                public void run() {
                    try {
                        for(String url:listurl){
                            if(testUrl(url)){
                                return;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
    }

    /**
     * 测试某个URL是否可用
     * 测试url格式http://www.xxxx.com/test_service?p=xxxx
     * 返回内容必须包含这个xxxx,则认为是成功
     * @return
     */
    private static boolean testUrl(String url){
        String p = System.currentTimeMillis()+"";
        String turl = url+"/comm/test_service";
        //Jsoup.connect(url).get().charset(Charset.forName("utf-8"));
        try{
            MyRequests req = new MyRequests();
            req.setConnectTimeout(3);
            Map<String,String> postData =new HashMap<String,String>();
            postData.put("p",p);
            postData.put("app","heimipay");
            String body = req.post(turl,postData);
            if(body!=null&& body.indexOf(p)!=-1){
                JSONObject json = new JSONObject(body);
                BASE_URL = json.getString("url");
                TempObj tobj = DBUtils.getByKey("BASE_URL");
                if(tobj==null){
                    tobj = new TempObj();
                    tobj.setTempkey("BASE_URL");
                    tobj.setTemptype("url");
                    tobj.setTempvalue(BASE_URL);
                }
                tobj.setTempvalue(BASE_URL);
                DBUtils.save(tobj);
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取跟路径
     * 1.直接获取静态变量
     * 2.如果静态变量为空则读取数据库
     * 3.如果数据库是空，则使用默认的第一个URL
     * @return
     */
    public static String getBaseUrl(){
        if(BASE_URL==null){
            TempObj tobj = DBUtils.getByKey("BASE_URL");
            if(tobj!=null){
                BASE_URL = tobj.getTempvalue();
                return BASE_URL;
            }
            return urls[0];
        }
        return BASE_URL;
    }





}
