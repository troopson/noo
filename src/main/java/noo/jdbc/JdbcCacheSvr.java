/**
 * 
 */
package noo.jdbc;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import noo.json.JsonArray;
import noo.json.JsonObject;
import noo.util.MD5;

/**
 * @author qujianjun   troopson@163.com
 * Sep 30, 2020 
 */
public class JdbcCacheSvr {

	
	public static final Logger log = LoggerFactory.getLogger(JdbcCacheSvr.class);
	
	@Autowired
	private JdbcSvr svr;
	
	@Autowired
	private StringRedisTemplate redis; 
	

	public void setSvr(JdbcSvr svr) {
		this.svr = svr;
	}


	public void setRedis(StringRedisTemplate redis) {
		this.redis = redis;
	}


	public JsonArray qry(String sql, JsonObject param, int cache_second) {	
		String key = "jdbc.result."+MD5.encode(sql+" param:"+param.encode());
		
		if(redis.hasKey(key)) {
			log.info("find in cache, return result from cache for sql:"+sql);
			String value = redis.opsForValue().get(key);
			return new JsonArray(value);
		}else {
			JsonArray ja = svr.qry(sql, param);
			if(ja!=null) {
				redis.opsForValue().set(key, ja.encode(), cache_second, TimeUnit.SECONDS);
			}
			return ja;
		}
		 
	}
	
}
