package com.theone.pay.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 运行日志
 * 最多保留5条运行日志
 */
public class RunLog {
    //运行时间
    public String runTime;
    //日志类型
    public String logType;
    //日志内容
    public String logText;

    public static List<RunLog> listlog = new ArrayList<RunLog>();
    private static Lock lock = new ReentrantLock();

    public RunLog(String logType,String logText){
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        runTime = format2.format(new Date());
        this.logType = logType;
        this.logText = logText;
    }

    private static void init(){
        if(lock==null){
            lock = new ReentrantLock();
        }
        if(listlog==null){
            listlog = new ArrayList<RunLog>();
        }
    }

    public static void putLog(RunLog log){
        init();
        lock.lock();
        if(listlog.size()>=5){
            listlog.remove(0);
        }
        listlog.add(log);
        lock.unlock();
    }
    public static void putLog(String logType,String logText){
        init();
        lock.lock();
        if(listlog.size()>=5){
            listlog.remove(0);
        }
        listlog.add(new RunLog(logType,logText));
        lock.unlock();
    }

    public static List<RunLog> getLogs(){
        init();
        lock.lock();
        List<RunLog> retlist = new ArrayList<RunLog>();
        retlist.addAll(listlog);
        lock.unlock();
        return retlist;
    }
}
