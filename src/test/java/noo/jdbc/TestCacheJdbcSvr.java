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

public class TestCacheJdbcSvr {
	
	private static HikariDataSource ds;
	
	private static StringRedisTemplate redis; 

	private static JdbcSvr svr;
	
	private static JdbcCacheSvr csvr;
	
	@BeforeClass
	public static void setUp() {
		ds = new HikariDataSource();
		ds.setJdbcUrl("jdbc:mysql://localhost:3306/dev?ssl=false&useUnicode=true&characterEncoding=utf-8&autoReconnect=true&serverTimezone=Asia/Shanghai");
		ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
		ds.setUsername("root");
		ds.setPassword("12345678"); 
		
		RedisStandaloneConfiguration jcc = new RedisStandaloneConfiguration();
		jcc.setHostName("localhost");  
		RedisConnectionFactory rcf = new JedisConnectionFactory(jcc);
		redis = new StringRedisTemplate(rcf);  
		
		svr = new JdbcSvr(ds);
		csvr = new JdbcCacheSvr();
		csvr.setSvr(svr);
		csvr.setRedis(redis);
	}
	
	@AfterClass
	public static void teardown() {
		if(ds!=null)
			ds.close();  
	}
	
	@Test
	public void testCachSvr() { 
		JsonArray jary = csvr.qry("grouptest", "select * from book", null, 300);
		System.out.println(jary); 
	}
	
	
 
	

	@Test
	public void testEvictGtroup() { 
		  
		csvr.evictGroup("grouptest");
		
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
