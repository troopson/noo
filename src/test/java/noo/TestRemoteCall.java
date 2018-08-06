package noo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import noo.json.JsonArray;
import noo.json.JsonObject;
import noo.web.NRemote;


@RunWith(SpringRunner.class)
@SpringBootTest
public class TestRemoteCall {

	@Autowired
	private NRemote ns;
	
	
	@Test
	public void testRemoteCall() {
		try {
			JsonObject param = new JsonObject();
			param.put("test", "中文是否乱码？");
		JsonArray ja = ns.getJsonArray("noo","bookService.doquery1", param);
		System.out.println(ja.encodePrettily());
		}catch(Exception e) {
			System.out.println(e.getClass());
			e.printStackTrace();
			throw e;
		}
	}

}
