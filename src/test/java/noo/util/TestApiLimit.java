package noo.util;

import org.junit.Test;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import noo.rest.security.ApiRateLimitPool;
import redis.clients.jedis.JedisPoolConfig;

public class TestApiLimit {

	@Test
	public void test() throws InterruptedException {
		JedisPoolConfig jc = new JedisPoolConfig();
		
		JedisConnectionFactory jf = new JedisConnectionFactory(jc);
		jf.setHostName("192.168.64.251");  
		StringRedisTemplate s = new StringRedisTemplate(jf); 
		
		s.opsForValue().set("aa","sss");
		System.out.println(s.opsForValue().get("aa"));
		
		ApiRateLimit ar = new ApiRateLimit(s,"login",2L);
		System.out.println("login 192.168.1.1->"+ar.checkLimit("192.168.1.1"));
		System.out.println("login 192.168.1.1->"+ar.checkLimit("192.168.1.1"));
		System.out.println("login 192.168.1.2->"+ar.checkLimit("192.168.1.2"));
		System.out.println("login 192.168.1.3->"+ar.checkLimit("192.168.1.3"));
		System.out.println("login2 192.168.1.1->"+ar.checkLimit("192.168.1.1"));
		try {
			System.out.println("login 192.168.1.1->"+ar.checkLimit("192.168.1.1"));
		}catch(Exception e) {
			System.out.println("here---------> exception---");
		}
		Thread.sleep(60000);
		System.out.println("login 192.168.1.1->"+ar.checkLimit("192.168.1.1"));
		System.out.println("login 192.168.1.1->"+ar.checkLimit("192.168.1.1"));
		System.out.println("login 192.168.1.2->"+ar.checkLimit("192.168.1.2"));
		System.out.println("login 192.168.1.2->"+ar.checkLimit("192.168.1.2"));
		System.out.println("login 192.168.1.3->"+ar.checkLimit("192.168.1.3"));
		System.out.println("login 192.168.1.3->"+ar.checkLimit("192.168.1.3"));
		
		
		
	}
	
	

}
