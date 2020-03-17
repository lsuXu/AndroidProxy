package proxy.com.xianpuxu.androidproxy;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName() ;

    private Button againBtn ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        againBtn = findViewById(R.id.btn_again);
        againBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProxyServer.ACTION);
                intent.setPackage(getPackageName());
                sendBroadcast(intent);
            }
        });
        //启动核心代理服务
        startCoreService();
    }

    /**
     * 启动APP核心服务
     */
    private void startCoreService(){
        // Android 8.0使用startForegroundService在前台启动新服务
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this,ProxyServer.class));
        } else{
            startService(new Intent(this,ProxyServer.class));
        }
    }

}
