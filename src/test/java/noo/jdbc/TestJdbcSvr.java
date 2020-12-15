package noo.jdbc;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.zaxxer.hikari.HikariDataSource;

import noo.json.JsonArray;
import noo.json.JsonObject;
import noo.json.PageJsonArray;

public class TestJdbcSvr {
	
	private static HikariDataSource ds;
	
	private static StringRedisTemplate redis; 

	private static JdbcSvr svr;
	
	@BeforeClass
	public static void setUp() {
		ds = new HikariDataSource();
		ds.setJdbcUrl("jdbc:mysql://192.168.1.251:3306/crmdev?useUnicode=true&characterEncoding=utf-8&autoReconnect=true&serverTimezone=Asia/Shanghai");
		ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
		ds.setUsername("crm");
		ds.setPassword("0123456789"); 
		
		RedisStandaloneConfiguration jcc = new RedisStandaloneConfiguration();
		jcc.setHostName("192.168.1.251");  
		RedisConnectionFactory rcf = new JedisConnectionFactory(jcc);
		redis = new StringRedisTemplate(rcf);  
		
		svr = new JdbcSvr(ds);
	}
	
	@AfterClass
	public static void teardown() {
		if(ds!=null)
			ds.close();  
	}
	
	@Test
	public void testIniType() { 
		  
		
		
	}
 
	

	@Test
	public void testQueryPageById() { 
		  
		JsonObject jso = svr.qryMoreRowStartFrom("select uuid, mobile from xs_xs where uuid > ? order by uuid limit 3", new Object[] {132}, 3, "uuid");
		System.out.println(jso.encodePrettily());
		
	}
 
	
	@Test
	public void testQueryPage2() { 
		 
		JsonObject param = new JsonObject();
		param.put("maxid", 132);
		param.put("pageSize", 3); 
		JsonObject jso = svr.qryMoreRowStartFrom("select uuid, mobile from xs_xs where {uuid>:maxid} order by uuid limit {pageSize}", param,"uuid");
		System.out.println(jso.encodePrettily());
		
	}
	
	@Test
	public void testQueryPage3() { 
		 
		JsonObject param = new JsonObject();
		param.put("maxid", 132);
		param.put("pageSize", 3); 
		JsonArray jso = svr.qry("select uuid, mobile from xs_xs where {uuid=:maxid} order by uuid limit {pageSize}", param);
		System.out.println(jso.encodePrettily());
		
	}
	
	@Test
	public void testQueryPage4() { 
		 
		JsonObject param = new JsonObject();
		param.put("maxid", 132);
		param.put("pageSize", 3); 
		JsonArray jso = svr.qry("select uuid, mobile from xs_xs where 1=2 and {uuid=:maxid} order by uuid limit {pageSize}", param);
		System.out.println(jso.encodePrettily());
		
	}
	
	@Test
	public void testQueryByPage_NoCount() {  
		  
		JsonObject param = new JsonObject();
		PageJsonArray jso = svr.qryByPage("select uuid, mobile from xs_xs where mobile is not null", param, false);
		System.out.println(jso.encodePrettily());
		
	}
	
	@Test
	public void testQueryByPage_HasCount() {  
		  
		JsonObject param = new JsonObject();
		PageJsonArray jso = svr.qryByPage("select uuid, mobile from xs_xs where mobile is not null", param);
		System.out.println(jso.encodePrettily());
		
	}
	
	@Test
	public void testGetField() { 
		String s = svr.allField("xs_xs");
		System.out.println(s);
	}
	
	@Test
	public void testGetSQLField() { 
		svr.getSQLMeta("select * from xs_xs where {name=:name} limit 0", c->System.out.println(c)); 
	}
	
	@Test
	public void testCachQuery() {
		 
		JdbcCacheSvr cq = new JdbcCacheSvr();
		cq.setSvr(svr);
		cq.setRedis(redis);
		JsonObject param = new JsonObject();
		JsonArray ja = cq.qry("select uuid, mobile from xs_xs where mobile is not null order by uuid limit  10", param, 25);
		System.out.println(ja.encode());
	}
	



}
