/**
 * 
 */
package noo.json;

import java.util.Date;

import noo.util.D;

/**
 * @author qujianjun   troopson@163.com
 * 2018年8月15日 
 */
public class TestJson {

	/**
	 * @param args
	 */
	public static void main(String[] args) { 
		
		Date d = D.toDate("1969-12-30");
		long l = d.getTime();
		System.out.println(l);
		
		java.sql.Date d2 = java.sql.Date.valueOf("1969-12-30");
		
		java.sql.Timestamp t = new java.sql.Timestamp(l);
		
		JsonObject a = new JsonObject();
		String v = null;
		a.put("aaa", v);
		a.put("bbb", "ff");
		a.put("dd", d);
		a.put("dd2", d2);
		a.put("t2", t);
		
		System.out.println(a.getString("aaa"));
		System.out.println(a.getString("aaa","def"));
		System.out.println(a.getString("bbb"));
		
		System.out.println(a.encode());
		System.out.println(a.encodePrettily());
		
	}

}
