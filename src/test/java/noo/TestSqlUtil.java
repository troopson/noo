package noo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import noo.jdbc.PageQuery;
import noo.jdbc.SqlUtil;
import noo.json.JsonArray;
import noo.json.JsonObject;
import noo.json.PageJsonArray;


public class TestSqlUtil {

	@Test
	public void test() {
		String sql="select  r.lesson_id, min(r.lesson_name) lesson_name, min(r.lesson_type) lesson_type, min(r.lesson_xl) lesson_xl, " + 
				"  min(r.kaosjd) kaosjd,  min(r.gongzlx) gongzlx,  min(r.lesson_type)  lesson_type, max(r.jiangyi_num) jiangyi_num, " + 
				"  count(if(r.is_jiangyda=1,true,null)) xxzl_num,   count(if(r.is_jiangyda=0,true,null)) jyda_num " + 
				"  from db_class.t_lesson_resource r "+
				"  where {r.lesson_name=:lesson_name} and {r.lesson_type=:lesson_type} and {r.lesson_xl=:lesson_xl} " + 
				"  group by r.lesson_id" + 
				"  order by r.create_time desc";
		Map m = new HashMap();
		String s2 = SqlUtil.processParam(sql, m);
		System.out.println(s2);
	}
	
	
	@Test
	public void testJsonArrayEncode() {
		
		JsonArray j = new JsonArray();
		
		j.add("aaa");
		j.add("bbb");
		j.add("ccc");
		
		JsonObject item = new JsonObject();
		item.put("item1", "item1value");
		item.put("item2", "item2value");
		item.put("item3", "item13value");
		j.add(item);
		
		System.out.println(j.encode());
	}
	
	@Test
	public void testPageJsonArray() {
		List<Map> lm = new ArrayList<>();
		Map m = new HashMap();
		m.put("a", 1);
		lm.add(m);
		
		PageJsonArray pja = new PageJsonArray(lm);
		System.out.println(pja.encode());
		
		
		
	}
	
	
	@Test
	public void testJsonObjectEncode() {
		JsonObject m = new JsonObject(); 
		m.put("a", 1); 
		
		 
		System.out.println(m.encode());
		
		
		
	}
	
	
}
