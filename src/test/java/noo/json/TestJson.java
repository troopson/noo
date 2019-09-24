/**
 * 
 */
package noo.json;

import java.util.Date;

import noo.util.D;
import noo.util.MD5;

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
		a.put("t2", t);
		a.put("int1", "");
		a.put("int2", "10");
		Object o = new Date();
		a.put("date", o);
		
		System.out.println(a.getString("aaa"));
		System.out.println(a.getString("aaa","def"));
		System.out.println(a.getString("bbb"));
		System.out.println(a.getInteger("int1"));
		System.out.println(a.getInteger("int2"));
		
		System.out.println(a.encode());
		System.out.println(a.encodePrettily());
		
		String s="null";
		JsonArray j =new JsonArray(s);
		System.out.println(j.getList()==null);
		System.out.println(j.isEmpty());
		j.forEachJsonObject(aa->{
			System.out.println("--------");
			System.out.println(aa==null);
			System.out.println(aa.encodePrettily());
		});

		System.out.println((int)'	');
		String st="{\"ip\":\"124.112.46.237\",\"name\":\"章如林\",\"time\":\"[25/Jun/2019:11:07:47 +0800]\",\"referer\":\"http://www.cyikao.com/zg/2019_ysjncjcx_PC/?wt.mc_id=sem-yikao-A360-pc-yis-2scj-40-hdl-yis\",\"mc_id\":\"sem-yikao-A360-pc-yis-2scj-40-hdl-yis\",\"remark\":\"准考证：	340419150S0004\",\"sid\":\"9\",\"mobile\":\"13865102415\"}";
		
		JsonObject js = new JsonObject(st);
		System.out.println(js.encodePrettily());
		
		System.out.println(MD5.encode("offcn.misoffcn.crm.hdSCHD190723701264839666260.00"));
		
	}

}
