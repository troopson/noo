package noo.data;

import org.junit.Test;

import noo.json.JsonArray;
import noo.json.JsonObject;
import parsii.tokenizer.ParseException;

public class TestSumcount {

	@Test
	public void test() {
		JsonArray jary = new JsonArray();
		
		JsonObject d1 = new JsonObject();
		d1.put("aa", "aa");
		d1.put("bb", 12);
		d1.put("cc", 4.5);
		d1.put("dd", 3);
		d1.put("ee", "1232");
		jary.add(d1);
		
		JsonObject d2 = new JsonObject();
		d2.put("aa", "aa");
		d2.put("bb", 1);
		d2.put("cc", 3.5224);
		d2.put("dd", -1);
		d2.put("ee", "3");
		jary.add(d2);
		
		JsonObject d3 = new JsonObject();
		d3.put("aa", "aa");
		d3.put("bb", 2);
		d3.put("cc", 5.53);
		d3.put("dd", 23);
		d3.put("ee", "12");
		jary.add(d3);
		
		DataSet ds  = new DataSet();
		
		SumCountUnit sc = new SumCountUnit("bb,cc,dd", "ee");
		sc.setScale(3); 
		JsonObject h = new JsonObject();
		h.put("aa", "合计");
		sc.setSumRow(h);
		
		ds.addUnit(sc);
		ds.transform(jary);
		
		System.out.println( jary.encodePrettily()); 
		 
	}
	
	
	@Test
	public void testExprUnit() throws ParseException {
		JsonArray jary = new JsonArray();
		
		JsonObject d1 = new JsonObject();
		d1.put("aa", "aa");
		d1.put("bb", 12);
		d1.put("cc", 4.5);
		d1.put("dd", 3);
		d1.put("ee", "1232");
		jary.add(d1);
		
		JsonObject d2 = new JsonObject();
		d2.put("aa", "aa");
		d2.put("bb", 1);
		d2.put("cc", 3.5224);
		d2.put("dd", -1);
		d2.put("ee", "3");
		jary.add(d2);
		
		JsonObject d3 = new JsonObject();
		d3.put("aa", "aa");
		d3.put("bb", 2);
		d3.put("cc", 5.53);
		d3.put("dd", 23);
		d3.put("ee", "12");
		jary.add(d3);
		
		DataSet ds  = new DataSet();
		
		ExprUnit sc = new ExprUnit();
		sc.addExpr("test", "bb+cc*dd-ee",0  );  
		ds.addUnit(sc);
		ds.transform(jary);
		
		System.out.println( jary.encodePrettily()); 
		 
	}

}
