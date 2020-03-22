package proxy.com.xianpuxu.androidproxy.protocol;

import java.io.IOException;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import proxy.com.xianpuxu.androidproxy.HexUtils;

public class HttpImpl extends Protocal{

    public HttpImpl(Socket remoteSocket, Socket localSocket, String data) {
        super(remoteSocket, localSocket, data);
    }

    @Override
    byte[] getIdentityData() throws IOException {
        return new byte[]{0x05,0x01,0x00};
    }

    @Override
    byte[] getConnectData() throws IOException {
        String domain = getDomain(receivedData);
        String port = getDefaultPort(receivedData);
        if(domain != null && port != null) {
            //目标IP字节流
            byte[] domainBytes = domain.getBytes();
            //目标端口字节流
            byte[] portBytes = HexUtils.portToByte(port);
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
            return connectInstruct ;
        }else{
            return new byte[0];
        }
    }

    @Override
    byte[] getTranspondData() throws IOException {
        return receivedData.getBytes();
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
}
