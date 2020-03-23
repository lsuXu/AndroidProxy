package proxy.com.xianpuxu.androidproxy.tools;

/**
 * Created by 12852 on 2020/2/13.
 */
public class HexUtils {

    private static int toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    /**
     *  10进制数转为16进制数
     */
    public static String intToHex(int n) {
        StringBuffer s = new StringBuffer();
        String a;
        char []b = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        while(n != 0){
            s = s.append(b[n%16]);
            n = n/16;
        }
        a = s.reverse().toString();
        return a;
    }

    /**
     *  将输入的int 转换为16进制字符串，不足补0
     * @param n 待转换的数字
     * @param count 设置转换后的字符串位数，不足补0
     * @return  16进制字符串
     */
    public static String intToHex(int n ,int count){
        StringBuilder stringBuilder = new StringBuilder(intToHex(n));
        //不足补0
        while(stringBuilder.length() < count){
            stringBuilder.insert(0,"0");
        }
        return stringBuilder.toString() ;
    }

    /**
     * 获取char字符对应的ASSIC码值
     * @param value char
     * @return  ASSIC 码值
     */
    private static int charToInt(char value){
        switch(value){
            case '1':
                return 1 ;
            case '2':
                return 2 ;
            case '3':
                return 3 ;
            case '4':
                return 4 ;
            case '5':
                return 5 ;
            case '6':
                return 6 ;
            case '7':
                return 7 ;
            case '8':
                return 8 ;
            case '9':
                return 9 ;
            case 'A':
                return 10 ;
            case 'B':
                return 11 ;
            case 'C' :
                return 12 ;
            case 'D':
                return 13 ;
            case 'E':
                return 14 ;
            case 'F':
                return 15 ;
            default:return 0 ;
        }
    }

    /**
     * 将String类型的ip字符串转化为对应的byte数组
     * @param ip    ip地址，需要遵循规范，例如192.168.4.1
     * @return
     */
    public static byte[] ipToByte(String ip){
        if(ip == null){
            return new byte[]{0x00,0x00,0x00,0x00};
        }
        String[] ipItems = ip.split("\\.");
        byte [] result = new byte[4];
        for(int i = 0 ; i < result.length;i++){
            result[i] = (byte) Integer.parseInt(ipItems[i]);
        }
        return result ;
    }

    /**
     * 将String类型的port字符串转化为对应的byte数组
     * @param port    端口，需要遵循规范，范围0~65535
     * @return
     */
    public static byte[] portToByte(String port){
        if(port == null){
            return new byte[]{0x00,0x00};
        }
        byte [] result = new byte[2];
        int portValue = Integer.valueOf(port);
        result[0] = (byte) (portValue/256);
        result[1] = (byte) (portValue%256);
        return result ;
    }

}
