package proxy.com.xianpuxu.androidproxy;

import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;

/**
 * 处理内部请求
 */
public class ProtocolImpl implements Protocol {

    private static final String TAG = Protocol.class.getSimpleName();

    private final int capacity ;

    public ProtocolImpl(int capacity) {
        this.capacity = capacity;
    }

    /**
     * 接收已连接的请求，开始监听可读状态
     * @param key
     * @throws IOException
     */
    @Override
    public void handleAccept(SelectionKey key) throws IOException {
        //获取客户端建立连接的请求，返回 SocketChannel 对象
        SocketChannel channel = ((ServerSocketChannel)key.channel()).accept();
        //设置为非阻塞状态
        channel.configureBlocking(false);
        //将SocketChannel注册到通道，可读时获取到的是SocketChannel对象
        channel.register(key.selector(),SelectionKey.OP_READ,ByteBuffer.allocate(capacity));
    }

    /**
     * 执行此方法时，连接已经可读，从该处读出数据
     * @param key
     * @throws IOException
     */
    @Override
    public void handleRead(SelectionKey key) throws IOException {
        //到这步，说明socket已经可读，再次进行读取数据
        SocketChannel socketChannel = (SocketChannel) key.channel();
        //得到并清空缓存区
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        buffer.clear();
        int size = socketChannel.read(buffer);
        if(size >  0){
            // 将缓冲区准备为数据传出状态
            buffer.flip();
            // 将字节转化为为UTF-16的字符串
//            String receivedString = Charset.forName("UTF-16").newDecoder().decode(buffer).toString();
            String receivedString = new String(buffer.array());

            // 控制台打印出来
            Log.d(TAG,String.format("接收到来自%s的信息：%s",socketChannel.socket().getRemoteSocketAddress(),receivedString));
            // 准备发送的文本
            String sendString = "你好,客户端. @" + new Date().toString()
                    + "，已经收到你的信息" + receivedString;
            buffer = ByteBuffer.wrap(sendString.getBytes("UTF-16"));
            socketChannel.write(buffer);
            //设置为下一次读/写作准备
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }else{//未读到数据，则关闭连接
            Log.d(TAG,"无数据可读，关闭连接");
            socketChannel.close();
        }
    }

    @Override
    public void handleWrite(SelectionKey key) throws IOException {

    }
}
