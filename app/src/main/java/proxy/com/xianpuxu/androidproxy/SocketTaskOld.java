package proxy.com.xianpuxu.androidproxy;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SocketTaskOld implements Runnable {

    private static final String TAG = SocketTaskOld.class.getSimpleName();

    //本机收到的连接服务
    private Socket localSocket ;

    //连接服务端的socket服务
    private Socket remoteSocket ;


    public SocketTaskOld(Socket socket) {
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

        byte[] data = new byte[300];
        int length ;

        //若连接建立，对照协议进行交互
        if(remoteSocket.isConnected()){
            InputStream inputStream = remoteSocket.getInputStream();
            OutputStream outputStream = remoteSocket.getOutputStream();

            printData(new byte[]{0x05,0x01,0x00},new byte[]{0x05,0x01,0x00}.length,true);

            //1.身份验证,告诉服务端：客户端只支持无验证需求
            //数据组成：协议版本，支持的验证数量，验证方式
            outputStream.write(new byte[]{0x05,0x01,0x00});
            outputStream.flush();

            length = inputStream.read(data);
            printData(data,length,false);

            if(data[1] == 0xFF){
                //无可验证方式
                release();
                return;
            }


            String targetHost = getHost(requestStr);
            String targetPort = getPort(requestStr);

            //ip + port方式
            if(targetHost != null) {

                //2.建立代理连接
                byte[] host = HexUtils.ipToByte(targetHost);
                byte[] port = HexUtils.portToByte(targetPort);
                //数据说明：协议版本，请求的类型，使用Connect，保留字段，地址类型，地址数据，地址端口
                byte[] connectInstruct = new byte[]{0x05, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                //设置目标ip
                System.arraycopy(host, 0, connectInstruct, 4, 4);
                //设置目标端口
                System.arraycopy(port, 0, connectInstruct, 8, 2);

                printData(connectInstruct, connectInstruct.length,true);

                outputStream.write(connectInstruct);
                outputStream.flush();

                //接收建立代理连接返回值
                length = inputStream.read(data);
                printData(data, length,false);
                //0x00标识建立成功
                if (data[1] != 0x00) {
                    //建立连接失败
                    release();
                    return;
                }
                printData(requestData, requestData.length,true);

                //3.数据包转发
                outputStream.write(requestData);
                outputStream.flush();

                length = inputStream.read(data);
                printData(data, length,false);

                if(length > 0) {
                    printData(data, length,false);
                    byte[] result = new byte[length];
                    System.arraycopy(data, 0, result, 0, length);

                    //写入数据返回
                    writeResultData(result);

                }

                release();
            }else{//域名 + 端口方式
                //解析目标域名
                String domain = getDomain(requestStr);
                //解析目标端口
                String port = getDefaultPort(requestStr);

                Log.i(TAG,String.format("target domain = %s , port = %s",domain,port));

                if(domain == null || port == null){
                    release();
                    return;
                }
                //目标IP字节流
                byte[] domainBytes =domain.getBytes();

                //目标端口字节流
                byte[] portBytes =HexUtils.portToByte(port);

                //数据说明：协议版本，请求的类型，保留字段，地址类型，地址数据，地址端口
                byte[] prefix = new byte[]{0x05, 0x01, 0x00, 0x03, (byte) domainBytes.length};

                //连接指令
                byte[] connectInstruct = new byte[domainBytes.length + prefix.length + 2];

                //设置请求头部协议信息
                System.arraycopy(prefix, 0, connectInstruct, 0, prefix.length);
                //设置目标域名
                System.arraycopy(domainBytes, 0, connectInstruct, prefix.length, domainBytes.length);
                //设置域名端口
                System.arraycopy(portBytes, 0, connectInstruct, domainBytes.length + prefix.length, portBytes.length);

                printData(connectInstruct, connectInstruct.length,true);

                outputStream.write(connectInstruct);
                outputStream.flush();

                //接收建立代理连接返回值
                length = inputStream.read(data);
                printData(data, length,false);
                //0x00标识建立成功
                if (data[1] != 0x00) {
                    //建立连接失败
                    release();
                    return;
                }
                printData(requestData, requestData.length,true);

                //数据包转发
                outputStream.write(requestData);
                outputStream.flush();

                //接收结果数据
                length = inputStream.read(data);
                if(length > 0) {
                    printData(data, length,false);
                    byte[] result = new byte[length];
                    System.arraycopy(data, 0, result, 0, length);

                    //写入数据返回
                    writeResultData(result);

                }

                release();

            }
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
     * byte数据打印方法
     * @param data
     * @param length
     */
    private void printData(byte[] data ,int length , boolean isSend){
        StringBuilder sb = new StringBuilder();
        for(int index = 0 ; index < length ;index ++){
            sb.append(data[index]).append(" ");
        }
        Log.i(TAG,String.format("%s %s",isSend?"send":"receive",sb.toString()));
        Log.i(TAG,String.format("%s %s",isSend?"send":"receive",new String(data)));
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

}
