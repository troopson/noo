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

public class TestPostgres {
	
	private static HikariDataSource ds;
	
	private static StringRedisTemplate redis; 

	private static JdbcSvr svr;
	
	 
	
	@BeforeClass
	public static void setUp() {
		ds = new HikariDataSource();
		ds.setJdbcUrl("jdbc:postgresql://192.168.1.251:5432/offcndatax");
		ds.setDriverClassName("org.postgresql.Driver");
		ds.setUsername("data");
		ds.setPassword("12345678"); 
		
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
 

	



}
