package proxy.com.xianpuxu.androidproxy;

import java.nio.channels.SocketChannel;
import java.util.Hashtable;
import java.util.Map;

public class Mapping {

    public static final Map<String ,SocketChannel> localChannelMap = new Hashtable<>();

    public static final Map<String,SocketChannel> remoteChannelMap = new Hashtable<>();


}
