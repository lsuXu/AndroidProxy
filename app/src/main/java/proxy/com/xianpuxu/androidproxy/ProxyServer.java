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
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 代理核心服务，运行在后台，监听端口
 */
public class ProxyServer extends Service {

    private static final String TAG = ProxyServer.class.getSimpleName() ;

    //监听端口
    private static final int LISTENER_PORT = 9089 ;

    private ServerSocket serverSocket ;

    private ExecutorService executors = Executors.newFixedThreadPool(15);

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
        initServerSocket();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterBroadcast();
        closeServerSocket();
    }

    /**
     * 初始化socketServer，开启监听
     * @throws IOException
     */
    private void initServerSocket() {
        //接收一个请求
        acceptOne();
    }

    private void closeServerSocket(){
        try {
            if(serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void acceptOne(){
        executors.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (serverSocket == null || serverSocket.isClosed()) {
                        serverSocket = new ServerSocket(LISTENER_PORT);
                    }
                    while(true) {
                        //该方法会阻塞IO线层，直到收到请求后，才会获取到连接
                        executors.execute(new SocketTask(serverSocket.accept()));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
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
//                acceptOne();
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
