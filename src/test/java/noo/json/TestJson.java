/**
 * 
 */
package noo.json;

/**
 * @author qujianjun   troopson@163.com
 * 2018年8月15日 
 */
public class TestJson {

	/**
	 * @param args
	 */
	public static void main(String[] args) { 
		
		JsonObject a = new JsonObject();
		String v = null;
		a.put("aaa", v);
		a.put("bbb", "ff");
		
		System.out.println(a.getString("aaa"));
		System.out.println(a.getString("aaa","def"));
		System.out.println(a.getString("bbb"));
		
	}

}
