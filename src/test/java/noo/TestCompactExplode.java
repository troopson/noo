package noo;

import org.junit.Test;

import noo.json.JsonObject;

public class TestCompactExplode {

	@Test
	public void test() {
		
		JsonObject j = new JsonObject();
		j.put("aa", 23);
		j.put("bb", "ccc");
		j.put("cc", true);
		j.put("dd", "fff");
		j.put("ee", "{\"x\":12,\"y\":\"sssss\"}");
		j.compact(new String[] {"aa","bb","cc"}, "compact");
		System.out.println(j.getString("compact"));
		System.out.println(j.encodePrettily());
		
		j.explode("ee");
		System.out.println(j.getString("x"));
		System.out.println(j.encodePrettily());
		

	}

}
