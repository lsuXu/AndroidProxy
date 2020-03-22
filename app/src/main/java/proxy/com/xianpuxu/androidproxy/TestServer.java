package proxy.com.xianpuxu.androidproxy;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class TestServer {

    private static final String TAG = TestServer.class.getSimpleName();

    Socket socket ;
    final String domain ;
    final int port ;

    public TestServer(String domain, int port) {
        this.domain = domain;
        this.port = port;
    }

    public void connect(byte[] data) throws IOException {
        socket = new Socket(domain, port);
        if(socket.isConnected()){
            socket.getOutputStream().write(data);
            socket.getOutputStream().flush();
            byte[] da = readLocalReceivedData();
            printData(da,false);
        }
    }

    /**
     * 获取接收到的来自代理服务器的数据
     * @return
     * @throws IOException
     */
    byte[] readLocalReceivedData() throws IOException{
        byte[] data = new byte[1024];
        int length;
        InputStream inputStream = socket.getInputStream();
        if((length = inputStream.read(data))> 0){
            Log.i(TAG,String.format("local received length = %s",length));
            byte[] result = new byte[length];
            System.arraycopy(data,0,result,0,length);
            return result ;
        }else{
            return new byte[0] ;
        }
    }


    /**
     * byte数据打印方法
     * @param data
     */
    private void printData(byte[] data, boolean isSend){
        if(data != null) {
            StringBuilder sb = new StringBuilder();
            for (int index = 0; index < data.length; index++) {
                sb.append(data[index]).append(" ");
            }
            Log.i(TAG, String.format("%s %s", isSend ? "send" : "receive", sb.toString()));
            Log.i(TAG, String.format("%s %s", isSend ? "send" : "receive", new String(data)));
        }else{
            Log.i(TAG,String.format("%s %s", isSend ? "send" : "receive", "null"));
        }
    }
}
