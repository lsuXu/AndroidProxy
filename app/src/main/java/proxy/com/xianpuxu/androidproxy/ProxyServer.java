package proxy.com.xianpuxu.androidproxy;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProxyServer extends Service {

    private static final String TAG = ProxyServer.class.getSimpleName() ;

    private Thread serverThread ;

    private ServerSocketChannel serverSocketChannel ;

    private TCPClient client ;

    private ExecutorService executors = Executors.newFixedThreadPool(5);

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initServerSocket();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeServerSocket();
    }

    /**
     * 初始化socketServer，开启监听
     * @throws IOException
     */
    private void initServerSocket() {

        serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //创建调度器
                    Selector selector = Selector.open();
                    //创建新的服务器连接通道
                    serverSocketChannel = ServerSocketChannel.open();
                    //待监听的地址
                    SocketAddress socketAddress = new InetSocketAddress(8090);
                    //绑定监听的地址
                    serverSocketChannel.socket().bind(socketAddress);

                    //设置为非阻塞模式
                    serverSocketChannel.configureBlocking(false);
                    if (!serverSocketChannel.isRegistered()) {
                        //向调度器注册，该通道关注的是新的连接请求
                        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                    }
                    while(true) {
                        if(selector.select(3000) == 0){
                            continue;
                        }
                        //初始化与服务端的代理服务器连接
                        if(client == null){
                            client = new TCPClient("47.102.125.88",7071);
                        }

                        Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
                        while (keyIter.hasNext()){

                            Log.i(TAG,"接收到新请求，准备处理");
                            SelectionKey key = keyIter.next();

                            //生成缓冲区为1024byte的
                            Protocol protocol = new ProtocolImpl(1024,client);

                            if (key.isAcceptable()) {
                                // 有客户端连接请求时
                                Log.i(TAG,"有客户端连接请求");
                                protocol.handleAccept(key);
                            }
                            if (key.isReadable()) {// 判断是否有数据发送过来
                                // 从客户端读取数据
                                Log.i(TAG,"客户端可读取数据");
                                protocol.handleRead(key);
                            }
                            if (key.isValid() && key.isWritable()) {// 判断是否有效及可以发送给客户端
                                // 客户端可写时
                                Log.i(TAG,"客户端可写入数据");
                                protocol.handleWrite(key);
                            }
                            // 移除处理过的键
                            keyIter.remove();
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
        serverThread.start();
    }

    private void closeServerSocket(){
        try {
            serverSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
