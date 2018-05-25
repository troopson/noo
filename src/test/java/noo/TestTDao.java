package noo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import noo.json.JsonObject;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestTDao {

	@Autowired
	private BookDao book;
	
	
	@Test
	public void test() {
		
		try {
		JsonObject books = book.getByField("name", "三国演义");
		
		//System.out.println("=====>"+environment.getProperty("spring.datasource.url"));
		System.out.println(books);
		
		
		JsonObject book1 = book.getById("4");
		System.out.println(book1);
		
		int bm = new Double(Math.random()*100).intValue();
		book1.put("bm", bm);
		book1.put("name", "book test");
		book.insertRow(book1);
		
		book.updateRow("name","bm", new Object[] {"book test modified",bm});
		System.out.println("update ok "+bm);
	    book1 = book.getById(bm+"");
	    System.out.println(book1.encodePrettily());
	    
	    JsonObject j = new JsonObject("{\"name\":\"book test modifyed by id\"}");
	    book.updateRowById(bm, j);
	    book1 = book.getById(bm+"");
	    System.out.println(book1.encodePrettily());
	    
	    book.deleteRow("bm", new Object[] {bm});
	    
	    book1.put("bm", 20);
		book1.put("name", "book test to delete");
		book.insertRow(book1);
	    int i = book.deleteById(20);
	    System.out.println("delete row "+i);
		
	    System.out.println(book.queryByPage("select * from book",null,0,10));
	    
		}catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		
	}

}
