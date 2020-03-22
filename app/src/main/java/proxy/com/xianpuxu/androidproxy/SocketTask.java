package proxy.com.xianpuxu.androidproxy;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import proxy.com.xianpuxu.androidproxy.protocol.HttpImpl;
import proxy.com.xianpuxu.androidproxy.protocol.HttpsImpl;
import proxy.com.xianpuxu.androidproxy.protocol.Protocal;

public class SocketTask implements Runnable {

    private static final String TAG = SocketTask.class.getSimpleName();

    //本机收到的连接服务
    private Socket localSocket ;

    //连接服务端的socket服务
    private Socket remoteSocket ;


    public SocketTask(Socket socket) {
        this.localSocket = socket;
    }

    @Override
    public void run() {
        try {
            connectRemote();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectRemote() throws IOException {
        //建立和代理服务器的连接
        remoteSocket = new Socket();
        remoteSocket.connect(new InetSocketAddress("47.102.125.88",8090));

        //获取数据包
        byte[] requestData = getStreamData(localSocket);
        String requestStr = new String(requestData);

        //若连接建立，对照协议进行交互
        if(remoteSocket.isConnected()){
            //根据第一包获取协议类型
            Protocal protocal = getProtocalImpl(requestStr);
            //身份验证
            boolean checkIdentity = protocal.checkIdentity();
            if(!checkIdentity){
                release();
                return;
            }
            //建立连接
            boolean checkConnect = protocal.checkConnect();
            if(!checkConnect){
                release();
                return;
            }//转发数据
            protocal.transpondData();
            release();
        }else{
            release();
        }
    }

    private byte[] packageData(byte[] ip ,byte[] port ,byte[] data){
        byte[] packageData = new byte[ip.length + port.length + data.length + 4] ;
        packageData[0] = 0x00 ;
        packageData[1] = 0x00 ;
        packageData[2] = 0x01 ;
        packageData[3] = 0x01 ;
        System.arraycopy(ip,0,packageData,4,ip.length);
        System.arraycopy(port,0,packageData,ip.length + 4,port.length);
        System.arraycopy(data,0,packageData,ip.length + port.length + 4,data.length);
        return packageData;
    }


    /**
     * 获取输入流的内容信息
     * @param localSocket
     * @return
     * @throws IOException
     */
    private byte[] getStreamData(Socket localSocket) throws IOException{
        InputStream inputStream = localSocket.getInputStream();
        int countLength = 0 ,length = 0;
        byte[] data = new byte[1024], resultData = null;
        if ((length = inputStream.read(data)) > 0){
            countLength = countLength + length ;
            //生成一个完整长度的流
            resultData = new byte[countLength];
            System.arraycopy(data,0,resultData,countLength - length,length);
        }
        if(resultData != null) {
            String str = new String(resultData);
            Log.d(TAG, String.format("socket received \n\r %s", str));
        }else{
            Log.d(TAG, String.format("socket received nothing"));
        }
        return resultData ;
    }

    /**
     * 写入数据返回
     * @param data
     * @throws IOException
     */
    private void writeResultData(byte[] data) throws IOException {
        if(localSocket != null && localSocket.isConnected()){
            OutputStream outputStream = localSocket.getOutputStream();
            outputStream.write(data);
            outputStream.flush();
        }else{
            Log.e(TAG,"local socket has already closed");
        }
    }

    /**
     * 获取目标主机地址
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
     * 获取目标主机端口
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

    /**
     * 解析域名
     * @param requestStr
     * @return
     */
    private String getDomain(String requestStr){
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
     * 解析默认端口
     * @param requestStr
     * @return
     */
    private String getDefaultPort(String requestStr){
        Pattern pattern = Pattern.compile("(GET |POST )((http|https)://([A-Za-z0-9_.]+))/");
        Matcher matcher = pattern.matcher(requestStr);
        if(matcher.find()){
            //调用group方法之前，必须先调用find方法，否则会报错
            return matcher.group(3).equals("http")?"80":"443";
        }else{
            return null ;
        }
    }

    private void release() throws IOException{
        Log.i(TAG,"release connect");
        if(localSocket!= null && localSocket.isConnected()){
            localSocket.close();
        }
        if(remoteSocket != null && remoteSocket.isConnected()){
            remoteSocket.close();
        }
    }

    public Protocal getProtocalImpl(String requestData){
        if(requestData.contains("CONNECT")){
            return new HttpsImpl(remoteSocket,localSocket,requestData);
        }else{
            return new HttpImpl(remoteSocket,localSocket,requestData);
        }
    }

}
