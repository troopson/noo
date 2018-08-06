package noo.event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import noo.json.JsonObject;
import noo.web.NRemote;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=noo.App.class)
public class TestCallEvent {
 
	
	@Test
	public void test() {
		
		JsonObject param = new JsonObject();
		param.put("aaa", "this is a param");
		param.put("bbb", 5);
		
		Event e = new Event("just.test",this);
		Object o = e.trigger(param);
		System.out.println(o);
	 
		
	}
	
	@Autowired
	private NRemote r;
	
	@Test
	public void testRemotePub() {
		JsonObject param = new JsonObject();
		param.put("aaa", "this is a param");
		param.put("bbb", 5);
		
		r.publishRemoteEvent("localhost:8000", "just.test", param);
		
	}

}
