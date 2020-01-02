package proxy.com.xianpuxu.androidproxy;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Selector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProxyServer extends Service {

    private static final String TAG = ProxyServer.class.getSimpleName() ;

    ServerSocket serverSocket ;

    private Thread serverThread ;

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
                    serverSocket = new ServerSocket(55555);
                    do{
                        //该方法会阻塞IO线层，直到收到请求后，才会获取到连接
                        executors.execute(new SocketTask(serverSocket.accept()));
                    }while (true);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        serverThread.start();
    }

    private void closeServerSocket(){
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
