package proxy.com.xianpuxu.androidproxy;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketTask implements Runnable {

    private static final String TAG = SocketTask.class.getSimpleName();

    private Socket socket ;

    public SocketTask(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            byte[] data = new byte[1024];
            while (inputStream.read(data) > 0){
                String str = new String(data);
                Log.d(TAG , String.format("socket received %s", str));
            }

            outputStream.write("finish".getBytes());
            outputStream.flush();//刷新
            outputStream.close();//关闭流

            inputStream.close();
            outputStream.close();
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
