/**
 * 
 */
package noo.util;

/**
 * @author qujianjun   troopson@163.com
 * 2018年8月15日 
 */
public class ID {

	public static String uuid() {
		return java.util.UUID.randomUUID().toString().replace("-", "").toLowerCase();
	}
	
	
	public static void main(String[] args) {
		
		for(int i=0;i<1000;i++) {
			System.out.println(ID.uuid()); 
		}
		System.out.println(ID.uuid().length());
	}
	
}
