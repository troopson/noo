/**
 * 
 */
package noo.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;


 

/**

 * MD5加密解密工具类<br>

 * 

 * 

 * 关于MD5的算法请参考RFC1321规范<br>

 * RFC1321给出了Test suite用来检验你的实现是否正确:<br>

 * MD5 ("") =d41d8cd98f00b204e9800998ecf8427e <br>

 * MD5 ("a") = 0cc175b9c0f1b6a831c399e269772661<br>

 * MD5 ("abc") = 900150983cd24fb0d6963f7d28e17f72<br>

 * MD5 ("message digest") =f96b697d7cb7938d525a2f31aaf161d0<br>

 * MD5 ("abcdefghijklmnopqrstuvwxyz") =c3fcd3d76192e4007dfb496cca67e13b

 * 

 * @author wangxiaoxue

 * 

 * 传入参数：一个字节数组 传出参数：字节数组的 MD5 结果字符串

 */

public class MD5 {

 

    /**

     * 对二进制数组进行MD5算法加密,并将加密结果按照建行的协议算法进行转换

     * @param source

     * @return

     */

    public static byte[] encode(byte[] source) {

       byte[] result = new byte[0];

 

       try {

           MessageDigest md = MessageDigest.getInstance("MD5");

           md.update(source);

 

           // MD5 的计算结果是一个 128 位的长整数，用字节表示就是 16 个字节

           result = md.digest();

 

       } catch (Exception e) {

           e.printStackTrace();

       }

       return result;

    }

    public static String encode(String source){
	  try {
			return byteToChar(MD5.encode(source.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			return byteToChar(MD5.encode(source.getBytes()));
		}
		
    }

    public static void main(String xu[]) { // 计算 "a" 的 MD5

       // 代码，应该为：0cc175b9c0f1b6a831c399e269772661

       System.out.println(byteToChar(MD5.encode("a".getBytes())));

    }
    
    //====================================================================================//
   
    public static boolean isMatch(String s,String encoded){
    	if(S.isBlank(s) && S.isBlank(encoded)) {
			return true;
		}
    	if(s==null) {
			return false;
		}
    	String ecodeS=MD5.encode(s);
    	return ecodeS.equals(encoded);    	
    }
    
    
    private static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    
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
    
	
 

}