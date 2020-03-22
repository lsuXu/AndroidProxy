package proxy.com.xianpuxu.androidproxy.protocol;

import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import proxy.com.xianpuxu.androidproxy.HexUtils;
import proxy.com.xianpuxu.androidproxy.TestServer;

/**
 * Https处理，转发数据包前需要处理Connect请求
 */
public class HttpsImpl extends Protocal{

    public HttpsImpl(Socket remoteSocket, Socket localSocket, String data) {
        super(remoteSocket, localSocket, data);
    }

    /**
     * 获取身份验证数据
     * @return
     * @throws IOException
     */
    @Override
    byte[] getIdentityData() throws IOException {
        return new byte[]{0x05,0x01,0x00};
    }

    @Override
    byte[] getConnectData() throws IOException {
        String domain = getConnectIp();
        String port = getConnectPort();
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
        String connectTime = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        String finishTime = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        TestServer testServer = new TestServer(getConnectIp(),Integer.valueOf(getConnectPort()));
        testServer.connect(receivedData.getBytes());
        String responseData = String.format(
                " HTTP/1.1 200 Connection Established\n" +
                " FiddlerGateway: Direct\n" +
                " StartTime: %s\n" +
                " Connection: Keep-Alive\n" +
                " EndTime: %s\n" +
                " ClientToServerBytes: %s\n" +
                " ServerToClientBytes: 0",connectTime,finishTime,receivedData.length());
        localSocket.getOutputStream().write(responseData.getBytes());
        localSocket.getOutputStream().flush();
        byte[] localData = readLocalReceivedData();
        return localData;
    }

    private String getConnectIp(){
        Pattern pattern = Pattern.compile("CONNECT ([A-Za-z0-9_.]+):([0-9]*) (HTTP/1.1|HTTP/1.0)");
        Matcher matcher = pattern.matcher(receivedData);
        if(matcher.find()){
            return matcher.group(1);
        }
        return null ;
    }

    private String getConnectPort(){
        Pattern pattern = Pattern.compile("CONNECT ([A-Za-z0-9_.]+):([0-9]*) (HTTP/1.1|HTTP/1.0)");
        Matcher matcher = pattern.matcher(receivedData);
        if(matcher.find()){
            return matcher.group(2);
        }
        return null ;
    }
}
