package proxy.com.xianpuxu.androidproxy;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import proxy.com.xianpuxu.androidproxy.io.ConnectThread;

/**
 * 代理核心服务，运行在后台，监听端口
 */
public class ProxyServer extends Service {

    private static final String TAG = ProxyServer.class.getSimpleName() ;

    //默认监听端口
    private static final int LISTENER_PORT = 9089 ;

    //本地监听地址
    private InetSocketAddress localAddress ;

    private Selector selector ;

    private ServerSocketChannel serverSocketChannel ;

    private ConnectThread connectThread ;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //如果API在26以上即版本为 O 则调用startForeground()方法启动服务
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setForegroundService();
        }

        registerBroadcast();
        try {
            initServerSocket();

            connectThread = new ConnectThread(selector);
            connectThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterBroadcast();
        try {
            closeServerSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化socketServer，开启监听
     * @throws IOException
     */
    private void initServerSocket() throws IOException{
        if(selector == null){
            selector = Selector.open();
        }
        if(localAddress == null){
            localAddress = new InetSocketAddress(LISTENER_PORT);
        }
        if(serverSocketChannel == null){
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(localAddress);
            serverSocketChannel.configureBlocking(false);
            if(!serverSocketChannel.isRegistered()) {
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            }
        }
    }


    /**
     * 关闭监听器
     */
    private void closeServerSocket() throws IOException{
        if(serverSocketChannel != null) {
            serverSocketChannel.close();
            serverSocketChannel = null ;
        }
        if(selector != null){
            selector.close();
            selector = null ;
        }
    }


    /**
     *通过通知启动服务
     */
    @TargetApi(Build.VERSION_CODES.O)
    public void  setForegroundService() {
        //设定的通知渠道名称
        String channelName = "代理服务";
        String channelId = "APP_SYSTEM_SERVICE";
        //设置通知的重要程度
        int importance = NotificationManager.IMPORTANCE_LOW;
        //构建通知渠道
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setDescription("代理核心服务，保证代理正常工作");
        //在创建的通知渠道上发送通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setSmallIcon(R.mipmap.ic_launcher) //设置通知图标
                .setContentTitle("代理服务")//设置通知标题
                .setContentText("代理核心服务")//设置通知内容
                .setAutoCancel(false) //用户触摸时，自动关闭
                .setOngoing(true);//设置处于运行状态
        //向系统注册通知渠道，注册后不能改变重要性以及其他通知行为
        NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        //将服务置于启动状态 NOTIFICATION_ID指的是创建的通知的ID
        startForeground(0x99,builder.build());
    }

    /**
     * 注册广播
     */
    public void registerBroadcast(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        sendMsgReceive = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG,"ready to accept one");
            }
        };
        this.registerReceiver(sendMsgReceive,intentFilter);
    }

    /**
     * 取消注册广播
     */
    public void unRegisterBroadcast(){
        if(sendMsgReceive != null)
            this.unregisterReceiver(sendMsgReceive);
    }

    private BroadcastReceiver sendMsgReceive ;

    public static final String ACTION = "proxy.com.xianpuxu.androidproxy.msg.action",KEY = "msgKey";

}
