package proxy.com.xianpuxu.androidproxy;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import proxy.com.xianpuxu.androidproxy.io.FinishCallback;
import proxy.com.xianpuxu.androidproxy.protocol.HttpImpl;
import proxy.com.xianpuxu.androidproxy.protocol.HttpsImpl;
import proxy.com.xianpuxu.androidproxy.protocol.Protocol;

public class SocketTask implements Runnable {

    private static final String TAG = SocketTask.class.getSimpleName();

    //本机收到的连接服务
    private Socket localSocket ;

    //连接服务端的socket服务
    private Socket remoteSocket ;


    public SocketTask(Socket socket) {
        this.localSocket = socket;
    }

    @Override
    public void run() {
        try {
            connectRemote();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectRemote() throws IOException {
        //建立和代理服务器的连接
        remoteSocket = new Socket();
        remoteSocket.connect(new InetSocketAddress("47.102.125.88",8090));

        //获取数据包
        byte[] requestData = getStreamData(localSocket);
        String requestStr = new String(requestData);

        //若连接建立，对照协议进行交互
        if(remoteSocket.isConnected()){
            //根据第一包获取协议类型
            Protocol protocol = getProtocalImpl(requestStr);
            //身份验证
            boolean checkIdentity = protocol.checkIdentity();
            if(!checkIdentity){
                release();
                return;
            }
            //建立连接
            boolean checkConnect = protocol.checkConnect();
            if(!checkConnect){
                release();
                return;
            }//转发数据
            protocol.transpondData(new FinishCallback() {
                @Override
                public void onFinish() {
                    try {
                        release();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }else{
            release();
        }
    }

    /**
     * 获取输入流的内容信息
     * @param localSocket
     * @return
     * @throws IOException
     */
    private byte[] getStreamData(Socket localSocket) throws IOException{
        InputStream inputStream = localSocket.getInputStream();
        int countLength = 0 ,length = 0;
        byte[] data = new byte[1024], resultData = new byte[0];
        if ((length = inputStream.read(data)) > 0){
            countLength = countLength + length ;
            //生成一个完整长度的流
            resultData = new byte[countLength];
            System.arraycopy(data,0,resultData,countLength - length,length);
        }
        String str = new String(resultData);
        Log.d(TAG, String.format("socket received \n\r %s", str));
        return resultData ;
    }


    private void release() throws IOException{
        Log.i(TAG,"release connect");
        if(localSocket!= null && localSocket.isConnected()){
            localSocket.close();
        }
        if(remoteSocket != null && remoteSocket.isConnected()){
            remoteSocket.close();
        }
    }

    public Protocol getProtocalImpl(String requestData){
        if(requestData.contains("CONNECT")){
            return new HttpsImpl(remoteSocket,localSocket,requestData);
        }else{
            return new HttpImpl(remoteSocket,localSocket,requestData);
        }
    }

}
