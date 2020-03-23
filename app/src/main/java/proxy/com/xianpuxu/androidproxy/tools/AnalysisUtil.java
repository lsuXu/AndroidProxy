package proxy.com.xianpuxu.androidproxy.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析首包工具类
 * Created by 12852 on 2020/3/23.
 */

public class AnalysisUtil {

    /**
     * 解析域名，用于http请求
     * @param requestStr
     * @return
     */
    public static String getHttpDomain(String requestStr){
        Pattern pattern = Pattern.compile("(GET |POST )((http|https)://([A-Za-z0-9_.]+))/");
        Matcher matcher = pattern.matcher(requestStr);
        if(matcher.find()){
            //调用group方法之前，必须先调用find方法，否则会报错
            return matcher.group(4);
        }else{
            return null ;
        }
    }

    /**
     * 解析默认端口,用于Http请求
     * @param requestStr
     * @return
     */
    public static String getHttpPort(String requestStr){
        Pattern pattern = Pattern.compile("(GET |POST )((http|https)://([A-Za-z0-9_.]+))/");
        Matcher matcher = pattern.matcher(requestStr);
        if(matcher.find()){
            //调用group方法之前，必须先调用find方法，否则会报错
            return matcher.group(3).equals("http")?"80":"443";
        }else{
            return null ;
        }
    }

    /**
     * 获取连接请求的Ip ,用于Https
     * @param requestStr
     * @return
     */
    public static String getConnectIp(String requestStr){
        Pattern pattern = Pattern.compile("CONNECT ([A-Za-z0-9_.]+):([0-9]*) (HTTP/1.1|HTTP/1.0)");
        Matcher matcher = pattern.matcher(requestStr);
        if(matcher.find()){
            return matcher.group(1);
        }
        return null ;
    }

    /**
     * 获取连接请求的端口，用于Https
     * @param requestStr
     * @return
     */
    public static String getConnectPort(String requestStr){
        Pattern pattern = Pattern.compile("CONNECT ([A-Za-z0-9_.]+):([0-9]*) (HTTP/1.1|HTTP/1.0)");
        Matcher matcher = pattern.matcher(requestStr);
        if(matcher.find()){
            return matcher.group(2);
        }
        return null ;
    }

    /**
     * 获取目标主机地址，当主机地址为IP地址时使用
     * @param requestStr   遵循http的请求数据
     * @return
     */
    private String getHost(String requestStr){
        Pattern pattern = Pattern.compile("Host: ((((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)):(6553[0-5]|655[0-2]\\d|65[0-4]\\d{2}|6[0-4]\\d{3}|[1-5]\\d{4}|[1-9](\\d{0,3})?|0]))");
        Matcher matcher = pattern.matcher(requestStr);
        if(matcher.find()){
            //调用group方法之前，必须先调用find方法，否则会报错
            return matcher.group(2);
        }else{
            return null ;
        }
    }


    /**
     * 获取目标主机端口，当主机地址为IP地址时使用
     * @param requestStr   遵循http的请求数据
     * @return
     */
    private String getPort(String requestStr){
        Pattern pattern = Pattern.compile("Host: ((((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)):(6553[0-5]|655[0-2]\\d|65[0-4]\\d{2}|6[0-4]\\d{3}|[1-5]\\d{4}|[1-9](\\d{0,3})?|0]))");
        Matcher matcher = pattern.matcher(requestStr);
        if(matcher.find()){
            //调用group方法之前，必须先调用find方法，否则会报错
            return matcher.group(6);
        }else{
            return null ;
        }
    }
}
