/**
 * 
 */
package noo.util;

/**
 * @author qujianjun   troopson@163.com
 * 2021年3月2日 
 */
public class TestStringParse {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String big = "G1221213123-123-daddfiesdhfyeesese";
		System.out.println("userid:"+big.substring(big.indexOf("-")+1,big.lastIndexOf("-")));
		
		String small = "S9655123812-crm-345-1231231284826234";
		System.out.println("client:"+small.substring(small.indexOf("-")+1,small.indexOf("-", small.indexOf("-")+1)));
		
		String small2 = "S9655123812-345-1231231284826234";
		System.out.println("client:"+small2.substring(small2.indexOf("-")+1,small2.indexOf("-", small2.indexOf("-")+1)));
		
		String big_token = ID.uuid();
		System.out.println(big_token);
		System.out.println(MD5.encode("132.12.12.11"));
		
		
		String url ="crmbackend.offcn.com:6443";
		System.out.println(url.substring(0,url.indexOf(":")));

	}

}
