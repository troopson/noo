/**
 * 
 */
package noo;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import noo.json.JsonArray;
import noo.json.JsonObject;
import noo.json.PageJsonArray;

/**
 * @author qujianjun   troopson@163.com
 * 2018年5月28日 
 */
@Service
public class BookService {

	@Autowired
	private BookDao book;
	
	@HystrixCommand(fallbackMethod="doqueryHystrix")
	public JsonArray doquery(Map<String,String> j) {
		//System.out.println(j.encodePrettily());
		System.out.println(j);
	    return book.findAll();
		
	}
	
	public JsonArray doqueryHystrix(Map<String,String> j) {
		return null;
	}
	
	
	public JsonObject getOne(Map<String,String> j) {
		//System.out.println(j.encodePrettily());
		return this.getBook("1");
		
	}
	
	public JsonArray getOneByPage(Map<String,String> m) {
		//System.out.println(j.encodePrettily());
		PageJsonArray j =  book.findByPage(" bm =? ",new Object[] {1} , 1, 20);
		System.out.println(j.encode());
		return j;
		
	}
	
	public  JsonObject getBook(String id) {
		return book.getById(id);
	}
	
	public  int createBook(JsonObject b) {
		return book.insertRow(b);
	}
	
}
