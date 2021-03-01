package noo.json;

import org.junit.Test;

public class JsonArrayTest {

	@Test
	public void test() {
		JsonArray j = new JsonArray();
		j.add("adfsdf");
		j.add("adfsdf");
		j.add("adfsdf2");
		j.add("adfsdf3");
		j.add("adfsdf4");
		j.add("adfsdf2");
		j.add("adfsdf3");
		j.add("adfsdf4");
		j.add("ooooo");
		j.add(new String("ooooo"));
		
		System.out.println(j.encode());
		j.distinct();
		System.out.println(j.encode());
		
	}
	
	
	@Test
	public void testJsonObject() {
		JsonObject j = new JsonObject();
		j.put("abc", 123);
		
		System.out.println(j.getInteger("Abc"));
		
		
	}

}
