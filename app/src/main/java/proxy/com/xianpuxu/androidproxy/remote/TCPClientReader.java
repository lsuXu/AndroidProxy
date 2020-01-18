package proxy.com.xianpuxu.androidproxy.remote;

import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class TCPClientReader implements Runnable{

    private static final String TAG = TCPClient.class.getSimpleName();

    private Selector selector ;

    public TCPClientReader(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            while (selector.select() > 0){
                Set<SelectionKey> selectionKeys =  selector.selectedKeys() ;
                for(SelectionKey selectionKey : selectionKeys){

                    if(selectionKey.isReadable()){//可读
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        buffer.flip();// 调用此方法为一系列通道写入或相对获取 操作做好准备
                        int size = socketChannel.read(buffer);
                        String receiverData = new String(buffer.array());
//                        Log.d(TAG,String.format("收到了来自代理服务器%s的响应数据,大小%s：%s", socketChannel.socket().getRemoteSocketAddress(),size,receiverData));
                        //为下一次读取数据做准备
//                            selectionKey.interestOps(SelectionKey.OP_READ);
                        /*if(size <= 0){
                            socketChannel.close();
                        }*/
                    }
                    //删除正在处理的SelectionKey
                    selectionKeys.remove(selectionKey);
                }

            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
