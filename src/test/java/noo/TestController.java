/**
 * 
 */
package noo;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver;

import noo.json.JsonObject;

/**
 * @author 瞿建军 troopson@163.com 2018年5月27日
 */  
@RestController
public class TestController {

	@PostMapping("test/body")
	public Object getResult(JsonObject test) {
	
		System.out.println(test.encode());
		return test.encode();

	}
	
	
	@GetMapping("test/body2")
	public Object getResult2(JsonObject test) {
	
		System.out.println(test.encode());
		return test.encode();

	}
	
	@GetMapping("test/body3")
	public Object getResult3(@RequestParam Map<String,Object> test) { 
		System.out.println(test.toString());
		return test.toString();

	}


}
