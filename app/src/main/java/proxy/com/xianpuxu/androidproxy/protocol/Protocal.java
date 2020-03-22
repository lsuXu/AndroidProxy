package proxy.com.xianpuxu.androidproxy.protocol;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Socekt5协议步骤
 */
public abstract class Protocal {

    private static final String TAG = Protocal.class.getSimpleName();

    final Socket remoteSocket,localSocket;

    final String receivedData ;

    public Protocal(Socket remoteSocket, Socket localSocket,String data) {
        this.remoteSocket = remoteSocket;
        this.localSocket = localSocket;
        this.receivedData = data ;
    }

    /**
     * 获取身份验证数据
     * @return
     * @throws IOException
     */
    abstract byte[] getIdentityData() throws IOException;

    /**
     * 获取建立连接的数据
     * @return
     * @throws IOException
     */
    abstract byte[] getConnectData() throws IOException;

    /**
     * 获取需要转发的数据
     * @return
     * @throws IOException
     */
    abstract byte[] getTranspondData() throws IOException ;

    /**
     * 第一步：身份验证
     * @return
     * @throws IOException
     */
    public boolean checkIdentity() throws IOException {
        byte[] data = getIdentityData();
        //打印
        printData(data,true);
        //发送身份验证数据
        remoteSocket.getOutputStream().write(data);
        remoteSocket.getOutputStream().flush();
        //获取身份验证的结果数据
        byte[] checkData = getRemoteReceivedData();
        //打印
        printData(checkData,false);
        //返回验证结果
        return checkData != null && checkData.length>1 && checkData[1] != 0xFF ;
    }

    /**
     * 第二步：建立连接
     * @return
     * @throws IOException
     */
    public boolean checkConnect() throws IOException{
        byte[] data = getConnectData();
        //打印
        printData(data,true);
        //发送建立连接数据
        remoteSocket.getOutputStream().write(data);
        remoteSocket.getOutputStream().flush();
        //获取建立连接的结果数据
        byte[] checkData = getRemoteReceivedData();
        //打印
        printData(checkData,false);
        return checkData != null && checkData.length>1 && checkData[1] == 0x00 ;
    }

    /**
     * 第三步，交换数据
     * @throws IOException
     */
    public void transpondData() throws IOException{
        byte[] data = getTranspondData();
        if(data != null) {
            //打印
            printData(data, true);
            //获取需要转发的数据
            remoteSocket.getOutputStream().write(data);
            remoteSocket.getOutputStream().flush();
            //接收转发处理后的数据
            byte[] responseData = getRemoteReceivedData();
            //打印
            printData(responseData, false);
            if (responseData != null) {
                //转发数据
                localSocket.getOutputStream().write(responseData);
            }
        }
    }


    /**
     * 获取接收到的来自代理服务器的数据
     * @return
     * @throws IOException
     */
    byte[] getRemoteReceivedData() throws IOException{
        byte[] data = new byte[10240];
        int length = 0 ;
        InputStream inputStream = remoteSocket.getInputStream();
        if((length = inputStream.read(data))> 0){
            Log.i(TAG,String.format("remote received length = %s",length));
            byte[] result = new byte[length];
            System.arraycopy(data,0,result,0,length);
            return result ;
        }else{
            return new byte[0] ;
        }
    }

    /**
     * 获取接收到的来自代理服务器的数据
     * @return
     * @throws IOException
     */
    byte[] readLocalReceivedData() throws IOException{
        byte[] data = new byte[10240];
        int length;
        InputStream inputStream = localSocket.getInputStream();
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
