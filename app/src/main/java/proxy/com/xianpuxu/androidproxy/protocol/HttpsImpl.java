package proxy.com.xianpuxu.androidproxy.protocol;

import java.io.IOException;
import java.net.Socket;

import proxy.com.xianpuxu.androidproxy.io.FinishCallback;
import proxy.com.xianpuxu.androidproxy.io.ReadRunnable;
import proxy.com.xianpuxu.androidproxy.tools.AnalysisUtil;
import proxy.com.xianpuxu.androidproxy.tools.HexUtils;

/**
 * Https处理，转发数据包前需要处理Connect请求
 */
public class HttpsImpl extends Protocol {

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
        String domain = AnalysisUtil.getConnectIp(receivedData);
        String port = AnalysisUtil.getConnectPort(receivedData);
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

    /**
     * 预先处理Https的Connect请求
     * @throws IOException
     */
    private void connectResponse() throws IOException {
        String responseData = String.format(
                "HTTP/1.1 200 Connection Established\r\n\r\n");
        localSocket.getOutputStream().write(responseData.getBytes());
        localSocket.getOutputStream().flush();
    }

    @Override
    public void transpondData(FinishCallback finishCallback) throws IOException {
        //处理https的connect请求
        connectResponse();
        //交换本地数据
        new ReadRunnable(localSocket.getInputStream(),remoteSocket.getOutputStream(),finishCallback).start();
        //接收代理服务器返回的数据
        new ReadRunnable(remoteSocket.getInputStream(),localSocket.getOutputStream(),finishCallback).start();
    }

}
