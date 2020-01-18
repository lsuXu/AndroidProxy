package proxy.com.xianpuxu.androidproxy.local;

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

import proxy.com.xianpuxu.androidproxy.Protocol;
import proxy.com.xianpuxu.androidproxy.ProtocolImpl;
import proxy.com.xianpuxu.androidproxy.remote.TCPClient;

public class ProxyServer extends Service {

    private static final String TAG = ProxyServer.class.getSimpleName() ;

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
        initRemoteClient();
        initLocalClient();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeServerSocket();
    }

    /**
     * 初始化本地socketServer，开启监听
     * @throws IOException
     */
    private void initLocalClient() {

        executors.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //创建调度器
                    Selector selector = Selector.open();
                    //创建新的服务器连接通道
                    serverSocketChannel = ServerSocketChannel.open();
                    //待监听的本地地址以及端口，此处只监听本地端口
                    SocketAddress socketAddress = new InetSocketAddress(8090);
                    //绑定监听的地址
                    serverSocketChannel.socket().bind(socketAddress);
                    //设置为非阻塞模式
                    serverSocketChannel.configureBlocking(false);
                    //将通道注册到调度器
                    if (!serverSocketChannel.isRegistered()) {
                        //向调度器注册，该通道关注的是新的连接请求
                        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                    }
                    while(true) {
                        if(selector.select(3000) == 0){
                            continue;
                        }

                        Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
                        while (keyIter.hasNext()){

                            SelectionKey key = keyIter.next();
                            Log.i(TAG,"接收到新请求，准备处理 + key =" + key);

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
    }

    private void initRemoteClient(){
        //初始化与服务端的代理服务器连接
        if(client == null){
            executors.execute(new Runnable() {
                @Override
                public void run() {
                    client = new TCPClient("47.102.125.88",7001,executors);
                }
            });
        }
    }

    private void closeServerSocket(){
        try {
            if(serverSocketChannel != null) {
                serverSocketChannel.close();
            }
            if(client != null){
                client.release();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
