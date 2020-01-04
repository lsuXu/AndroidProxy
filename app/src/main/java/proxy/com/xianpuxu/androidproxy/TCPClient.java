package proxy.com.xianpuxu.androidproxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * 网络访问类，连接到代理服务器
 */
public class TCPClient {


    private String remoteIp ;

    private int remotePort ;

    private SocketChannel socketChannel ;

    private Selector selector ;

    public TCPClient(String remoteIp, int remotePort) {
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
        try {
            initChannel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化与代理服务端的信道通道
     * @throws IOException
     */
    private void initChannel() throws IOException {
        //打开通道选择器
        selector = Selector.open();
        //打开信道通道
        socketChannel = SocketChannel.open(new InetSocketAddress(remoteIp,remotePort));
        //设置通道非阻塞
        socketChannel.configureBlocking(false);
        //将信道通道注册到选择器
        socketChannel.register(selector, SelectionKey.OP_READ);
        new Thread(new TCPClientReader(selector)).start();

    }

    public void sendMsg(String data) throws IOException {
        //将数据转换为byteBuffer写出
        ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
        socketChannel.write(buffer);
    }

}
