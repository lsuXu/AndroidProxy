package proxy.com.xianpuxu.androidproxy;

/**
 * Created by 12852 on 2020/2/13.
 */
public class HexUtils {

    /**
     * 字符串转换成十六进制字符串
     * @param  str 待转换的ASCII字符串
     * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
     */
    public static String str2HexStr(String str)
    {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++)
        {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
        }
        return sb.toString().trim();
    }

    /**
     * 十六进制转换字符串
     * @param hexStr Byte字符串(Byte之间无分隔符 如:[616C6B])
     * @return String 对应的字符串
     */
    public static String hexStr2Str(String hexStr)
    {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++)
        {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    /**
     * bytes转换成十六进制字符串
     * @param b byte数组
     * @return String 每个Byte值之间空格分隔
     */
    public static String byte2HexStr(byte[] b)
    {
        String stmp="";
        StringBuilder sb = new StringBuilder("");
        for (int n=0;n<b.length;n++)
        {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length()==1)? "0"+stmp : stmp);
            sb.append("");
        }
        return sb.toString().toUpperCase().trim();
    }


    /**
     * 把16进制字符串转换成字节数组
     * @param hex
     * @return byte[]
     */
    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

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
     * 将16进制的字符串按字节读取转换为char
     * @param hexString 16进制字符串
     * @return  charArray
     */
    public static char[] hexStringToCharArray(String hexString ){
        hexString = hexString.toUpperCase();
        int resultSize = hexString.length()/2 ;
        char[] result = new char[resultSize];
        for(int i = 0 ,j = 0; i < resultSize ; i ++,j = j+2){
            int left = charToInt(hexString.charAt(j));
            int right = charToInt(hexString.charAt(j+1));
            char v = (char) ((left << 4) + right);
            result[i] = v ;
        }
        return result ;
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
     * 计算16进制字符串数据对应的int值
     * @param hexString
     * @return
     */
    public static int hexStringToInt(String hexString){
        hexString = hexString.toUpperCase();
        int result = 0 ;
        for(int i = 0;i < hexString.length() ; i++){
            char c = hexString.charAt(i);
            result = result * 16 + charToInt(c);
        }
        return result ;
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
