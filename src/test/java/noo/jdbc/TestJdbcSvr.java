package noo.jdbc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import noo.App;
import noo.json.JsonObject;

/**
 * @author qujianjun   troopson@163.com
 * 2018年8月15日 
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes=App.class)
public class TestJdbcSvr {
 
	@Autowired
	private JdbcSvr s;
	 
	
	@Test
	public void testInsertKeyHolder() {
	 
		JsonObject j = new JsonObject();
		j.put("name", "test");
		j.put("price", 200);
		j.put("publish_day", "2018-02-12");
		j.put("author", "1122");
		
		InsertKeyHolder kh = s.insertRowWithGenkey("book",j);
		
		System.out.println("======================="+kh.getKey());
		
	}
	
	 
	 
}