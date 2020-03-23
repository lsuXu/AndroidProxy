package proxy.com.xianpuxu.androidproxy.io;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by 12852 on 2020/3/23.
 */

public class ReadRunnable extends Thread{

    private static final String TAG = ReadRunnable.class.getSimpleName();

    private final InputStream inputStream ;

    private final OutputStream outputStream ;

    private final FinishCallback finishCallback ;

    public ReadRunnable(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.finishCallback = null;
    }

    public ReadRunnable(InputStream inputStream, OutputStream outputStream, FinishCallback finishCallback) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.finishCallback = finishCallback;
    }

    @Override
    public void run() {
        super.run();
        int length = 0;
        byte[] data = new byte[1024] ;
        //读取交换的数据
        try {
            while ((length = inputStream.read(data)) > 0) {
                outputStream.write(data, 0, length);
                outputStream.flush();
            }
            if(finishCallback != null){
                finishCallback.onFinish();
            }
            Log.i(TAG,String.format("read finish "));
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
