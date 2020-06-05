/**
 * 
 */
package noo;

/**
 * @author qujianjun   troopson@163.com
 * Apr 29, 2020 
 */
public class Encode {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("0-9:"('0')+"  "+encode_mapping('9'));
		System.out.println("a-z:"+encode_mapping('a')+"  "+encode_mapping('z'));
		System.out.println("A-Z:"+encode_mapping('A')+"  "+encode_mapping('Z')); 
		
		System.out.println("0 decode: "+decode_mapping((byte)0));
		
		byte[] s = encode("paro8wzow0se12");
		System.out.println(new String(s));
		String ss = decode(s);
		System.out.println(ss);
		System.out.println("paro8wzow0se12".getBytes().length+"     "+s.length);
		
		  
		
	}
	 
	
	public static String decode(byte[] b) {
		if(b==null || b.length==0)
			return null;
		String s = "";
		for(byte i : b) {
			char c = decode_mapping(i);
			s = s+c;
		}
		return s;
			
	}
	
	public static byte[] encode(String s) {
		if(s==null || s.length()==0)
			return null;
		byte[] res = new byte[s.length()];
		for(int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			res[i] = encode_mapping(c);
		}
		return res;
	}
 
	
	public static byte encode_mapping(char c) {
		int i = (int)c;
		if(i>=48 && i<=57) // 0-9
			return (byte)(i-48);
		if(i>=97 && i<=122) //a-z
			return (byte) (i-97+10);
		if(i>=65 && i<=90) //A-Z
			return (byte)(i-65+36);
		else
			throw new IllegalArgumentException("char ["+c+"] is not in char list."); 
	}
	
	public static char decode_mapping(byte b) {
		int i = (int) b;
		if(i>=0 && i<=9)
			return (char)(i+48);
		else if(i>=10 && i<=35)
			return (char)(i-10+97);
		else if(i>=36 && i<=61)
			return (char)(i-36+65);
		else
			throw new IllegalArgumentException("byte ["+i+"] is not in char list."); 
		
	}
	
	 
}
