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
import noo.util.S;

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


	public JsonArray qry(String sql, JsonObject param, int second) {	
		String key = "jdbc.result."+MD5.encode(sql+" param:"+param.encode());
		
		if(redis.hasKey(key)) {
			log.info("find in cache, return result from cache for sql:"+sql);
			String value = redis.opsForValue().get(key);
			return new JsonArray(value);
		}else {
			JsonArray ja = svr.qry(sql, param);
			if(ja!=null) {
				redis.opsForValue().set(key, ja.encode(), second, TimeUnit.SECONDS);
			}
			return ja;
		}
		 
	}
	
	
	public JsonObject get(String table, String pkfield, Object uuid,int second) {
		String uid = uuid.toString();
		String key = "jdbc.result."+MD5.encode(table+"."+pkfield+"."+uid);
		if(redis.hasKey(key)) {
			log.info("find in cache, return result from cache for table uuid:"+table+"."+uid);
			String value = redis.opsForValue().get(key);
			return new JsonObject(value);
		}else {
			JsonObject jo = svr.get(table, pkfield, uuid);
			if(jo!=null) {
				redis.opsForValue().set(key, jo.encode(), second, TimeUnit.SECONDS);
			}
			return jo;
		}
		
	} 
	
	public String qryString(String sql, JsonObject param,int second) {
		String key = "jdbc.result."+MD5.encode(sql+" param:"+param.encode());
		
		if(redis.hasKey(key)) {
			log.info("find in cache, return result from cache for sql:"+sql);
			String value = redis.opsForValue().get(key);
			return value;
		}else {
			String s = svr.qryString(sql, param);
			if(S.isNotBlank(s)) {
				redis.opsForValue().set(key, s, second, TimeUnit.SECONDS);
			}
			return s;
		}
	}
	
}