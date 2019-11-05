package noo.jdbc;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.zaxxer.hikari.HikariDataSource;

import noo.json.JsonArray;
import noo.json.JsonObject;
import noo.json.PageJsonArray;

public class TestJdbcSvr {
	
	private static HikariDataSource ds;
	
	@BeforeClass
	public static void setUp() {
		ds = new HikariDataSource();
		ds.setJdbcUrl("jdbc:mysql://192.168.64.251:3306/crmdev?useUnicode=true&characterEncoding=utf-8&autoReconnect=true&serverTimezone=Asia/Shanghai");
		ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
		ds.setUsername("root");
		ds.setPassword("0123456789"); 
	}
	
	@AfterClass
	public static void teardown() {
		if(ds!=null)
			ds.close();
	}

	@Test
	public void testQueryPageById() { 
		 
		JdbcSvr svr = new JdbcSvr(ds);
		JsonObject jso = svr.qryMoreRowStartFrom("select uuid, mobile from xs_xs where uuid > ? order by uuid limit 3", new Object[] {132}, 3, "uuid");
		System.out.println(jso.encodePrettily());
		
	}
 
	
	@Test
	public void testQueryPage2() { 
		 
		JsonObject param = new JsonObject();
		param.put("maxid", 132);
		param.put("pageSize", 3);
		JdbcSvr svr = new JdbcSvr(ds);
		JsonObject jso = svr.qryMoreRowStartFrom("select uuid, mobile from xs_xs where {uuid>:maxid} order by uuid limit {pageSize}", param,"uuid");
		System.out.println(jso.encodePrettily());
		
	}
	
	@Test
	public void testQueryPage3() { 
		 
		JsonObject param = new JsonObject();
		param.put("maxid", 132);
		param.put("pageSize", 3);
		JdbcSvr svr = new JdbcSvr(ds);
		JsonArray jso = svr.qry("select uuid, mobile from xs_xs where {uuid=:maxid} order by uuid limit {pageSize}", param);
		System.out.println(jso.encodePrettily());
		
	}
	
	@Test
	public void testQueryPage4() { 
		 
		JsonObject param = new JsonObject();
		param.put("maxid", 132);
		param.put("pageSize", 3);
		JdbcSvr svr = new JdbcSvr(ds);
		JsonArray jso = svr.qry("select uuid, mobile from xs_xs where 1=2 and {uuid=:maxid} order by uuid limit {pageSize}", param);
		System.out.println(jso.encodePrettily());
		
	}
	
	@Test
	public void testQueryByPage_NoCount() {  
		 
		JdbcSvr svr = new JdbcSvr(ds);
		JsonObject param = new JsonObject();
		PageJsonArray jso = svr.qryByPage("select uuid, mobile from xs_xs where mobile is not null", param, false);
		System.out.println(jso.encodePrettily());
		
	}
	
	@Test
	public void testQueryByPage_HasCount() {  
		 
		JdbcSvr svr = new JdbcSvr(ds);
		JsonObject param = new JsonObject();
		PageJsonArray jso = svr.qryByPage("select uuid, mobile from xs_xs where mobile is not null", param);
		System.out.println(jso.encodePrettily());
		
	}
	

}
