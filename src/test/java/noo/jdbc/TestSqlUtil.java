package noo.jdbc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import noo.json.JsonArray;
import noo.json.JsonObject;
import noo.json.PageJsonArray;


public class TestSqlUtil {
	
	
	@Test
	public void testParseEvent() { 
		String sql ="select a from t where {c in #cdd} and {f=:fdd}"; 
		 
		 
		String s2 = SqlUtil.parseEvent(sql, p->{
			System.out.println(p[0]+" "+p[1]);
			return  " '1' ";
		});
		
		System.out.println(s2);
				
	}
	
	
	@Test
	public void testParseIn() { 
		String sql ="select a from t where {c in #c} and {f=:f}";
		Map<String,Object> m = new HashMap<>();
		 
		List<String> v = new ArrayList<>();
		//List<String> v = Arrays.asList("incc","indd","inee");
		m.put("c", v);
		String s2 = SqlUtil.processParam(sql, m);
		System.out.println(s2);
	}
	

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
	public void testReplaceAnd() {
		String sql="select as and and 2=1 from aaa r where {r.l=:l} and  {r.f=:f} and {r.t=#t} AND {r.x=:x} " ;
		Map m = new HashMap();
		String s2 = SqlUtil.processParam(sql, m);
		System.out.println(s2);
	}
	
	
	@Test
	public void testCleanParam() {
		String sql="select as and and 2=1 from aaa r where {r.l=:l} and  {r.f=:f} and {r.t=#t} AND {r.x=:x} " ;
	    String s2 = SqlUtil.replaceParam(sql,"1=2");
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
	

	
	@Test
	public void testDate() {
		
		long l = -28800000;
		
		
		
		LocalDate ld = LocalDate.ofEpochDay(l);
		
		String s = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(ld);
		
		System.out.println(s);
		
	}
	
	
}
