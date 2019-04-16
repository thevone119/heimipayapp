package com.theone.pay.utils;

/***
 * 对xml,hml等文件进行数据处理
 */
public class HtmlProcess {

    //提取HTML的内容
    public static String extractHtml(String src, String startstr, String endstr)
    {
        int idx = src.indexOf(startstr);
        if (idx == -1)
        {
            return null;
        }
        String laststr = src.substring(idx + startstr.length());
        int lastidx = laststr.indexOf(endstr);

        if (lastidx == -1)
        {
            return null;
        }
        if (lastidx == 0)
        {
            return "";
        }
        return laststr.substring(0, lastidx);
    }

}
