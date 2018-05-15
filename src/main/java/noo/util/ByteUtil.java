package noo.util;

import java.io.UnsupportedEncodingException;




/**

 * 字节运算工具,其作用和背景请见建行接口文档的"附录1：MAC算法说明"

 * 

 * @author wangxiaoxue

 * 

 */

public class ByteUtil {

 

    // 用来将字节转换成 16 进制表示的字符

    private static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7',

           '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

 

    /**

     * 找到字符在数组中的位置

     * 

     * @param c

     * @return

     */

    private static int getIndex(char c) {

       int p = -1;

       for (int i = 0; i < hexDigits.length; i++) {

           if (hexDigits[i] == c) {

              p = i;

              break;

           }

       }

       return p;

    }

 

    /**

     * 将字节转化成字符串，转换算法如下:<br>

     * 1:每个字节长度为8位，分割为两个4位，高四位和低四位<br>

     * 2:将每个四位换算成16进制，并且对应ascii码，如0x01对应1,0x0d对应d,具体对应关系请见数组hexDigits[]<br>

     * 3:将得到的字符拼成字符串

     * 

     * @param bytes

     * @return

     */

    public static String byteToChar(byte[] bytes) {

       // 每个字节用 16 进制表示的话，使用两个字符，所以字符数组长度是字节数字长度的2倍

       char str[] = new char[bytes.length * 2];

 

       // 表示转换结果中对应的字符位置

       int k = 0;

 

       // 每一个字节转换成 16 进制字符

       for (int i = 0; i < bytes.length; i++) {

           byte byte0 = bytes[i]; // 取第 i 个字节

 

           // 取字节中高 4 位(左边四位)的数字转换,>>>为逻辑右移，右移后，高四位变成低四位，需要对低四位之外的值进行消零运算

           str[k++] = hexDigits[byte0 >>> 4 & 0xf];

 

           // 取字节中低 4 位(右边四位)的数字转换，并且和0xf进行"逻辑与"运算，以消除高位的值，得到纯净的低四位值

           str[k++] = hexDigits[byte0 & 0xf];

       }

       return new String(str);

    }

 

    /**

     * 将字节转换成二进制数组，是byteToChar方法的逆运算，转换算法如下:<br>

     * 1:将字符按顺序每两个分为一组，分别找出每个字符在映射表hexDigits[]中的索引值，请见getIndex(char c)方法<br>

     * 2:每两个字符一组进行运算,将第一个字符的索引值逻辑左移四位，并和"0xf"进行"逻辑或"运算，目的是将低四位都设置为1，因为逻辑左移后，低四位都变成0了<br>

     * 3:将第二个字符的索引值和

"0xf0"进行"逻辑或"运算，目的的是将高位设置为1<br>

     * 4:将两个运算完的索引值进行"逻辑与"运算，得到了两个字符所代表的一个字节值<br>

     * 5:依次运算,最后得到字节数组,返回

     * 

     * @param str

     * @return

     */

    public static byte[] charToByte(String str) {

       char[] chars = str.toCharArray();

       byte[] bytes = new byte[chars.length / 2];

       int k = 0;

       for (int i = 0; i < chars.length; i = i + 2) {

           // 得到索引值

           byte high = (byte) getIndex(chars[i]);

           byte low = (byte) getIndex(chars[i + 1]);

 

           // 第一个字符索引逻辑左移四位,并进行或运算,将低四位设置为1

           high = (byte) ((high << 4) | 0xf);

 

           // 第二个字符索引进行或运算,将高四位设置为1

           low = (byte) (low | 0xf0);

 

           // 两个字节进行与运算

           bytes[k++] = (byte) (high & low);

       }

       return bytes;

    }
    
    
    public static boolean equals(byte[] a, byte[] b){
    	if(a==b) return true;
    	if((a==null && b!=null) || (a!=null && b==null)) return false;
    	if(a.length!=b.length) return false;
    	for(int i=0;i<a.length;i++){
    		if(a[i]!=b[i]) 
    			return false;
    	}
    	return true;
    }
    
    
    public static byte[] join(byte[] a,byte[] b){
    	if(a==null) return b;
    	if(b==null) return a;
    	int al=a.length;
    	int bl=b.length;
    	byte[] c=new byte[al+bl];
    	for(int i=0; i<al;i++)
    		c[i]=a[i];
    	for(int i=0;i<bl;i++)
    		c[al+i]=b[i];
    	return c;    	
    }
 

    public static void main(String[] args) throws UnsupportedEncodingException {

 

       String str = "abgcd1234";

       System.out.println("原始字符串:" + str);

       String result = ByteUtil.byteToChar(str.getBytes());

       System.out.println("运算结果:" + result);

       byte[] resultbytes = ByteUtil.charToByte(result);

       System.out.println("逆运算结果:" + new String(resultbytes));
       
       
       byte[] a="中国".getBytes();
       byte[] b="中国".getBytes();
       System.out.println(equals(a, b));
       System.out.println(equals("f".getBytes("GBK"), "f".getBytes("UTF-8")));
 
       System.out.println(new String(join(a,b)));
    }

}
