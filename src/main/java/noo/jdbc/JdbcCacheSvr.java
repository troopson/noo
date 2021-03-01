/**
 * 
 */
package noo.jdbc;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
	
	public static final String CACHE_GROUP_PRE="cache_query_group:";
	
	public static int cache_group_expire_hours = 12; //查询分组的过期时间 12小时
	public static int cache_time_threshold = 1000;  //查询时间的阈值，1秒
	public static int cache_rec_num_threshold = 500;  //查询记录数的阈值
	
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
	 

	public JsonArray qry(String group_name,String sql, JsonObject param, int second) {
		if(S.isBlank(group_name))
			group_name="null_group";
		String key = "jdbc.result."+MD5.encode(sql+" param:"+(param==null?"null":param.encode()) );
		
		if(redis.hasKey(key)) {
			log.info("find in cache, return result from cache key "+key+" +for sql:"+sql);
			String value = redis.opsForValue().get(key);
			return new JsonArray(value);
		}else {
			long start = System.currentTimeMillis();
			JsonArray ja = svr.qry(sql, param);
			long duration = System.currentTimeMillis() - start; 
			//如果查询时间超过阈值，并且查询结果集小于阈值，就缓存起来
			//时间短不缓存
			if(ja!=null && duration>cache_time_threshold && ja.size()<cache_rec_num_threshold) {
				redis.opsForValue().set(key, ja.encode(), second, TimeUnit.SECONDS);
				//将key缓存到group_name中，后面可以按照group_name找到所有缓存的sql键
				String groupkey = CACHE_GROUP_PRE+group_name;
				redis.opsForHash().put(groupkey, key, ""+System.currentTimeMillis());
				long expire = redis.getExpire(groupkey);
				if(expire==-1) {
					redis.expire(groupkey, cache_group_expire_hours, TimeUnit.HOURS);
				}
			}
			return ja;
		}
	}
	
	/**
	 * 将某个组中缓存的
	 * @param group_name
	 */
	public void evictGroup(String group_name) {
		if(S.isBlank(group_name))
			group_name="null_group";
		String groupkey = CACHE_GROUP_PRE+group_name;
		Map<Object,Object> m =redis.opsForHash().entries(groupkey);
		if(m==null || m.isEmpty())
			return;
		List<String> keys = m.keySet().stream().map(k->k.toString()).collect(Collectors.toList());
		keys.add(groupkey);
		redis.delete(keys);  
	}

	public JsonArray qry(String sql, JsonObject param, int second) {	
		String key = "jdbc.result."+MD5.encode(sql+" param:"+param.encode());
		
		if(redis.hasKey(key)) {
			log.info("find in cache, return result from cache key "+key+" +for sql:"+sql);
			String value = redis.opsForValue().get(key);
			return new JsonArray(value);
		}else {
			long start = System.currentTimeMillis();
			JsonArray ja = svr.qry(sql, param);
			long duration = System.currentTimeMillis() - start; 
			//如果查询时间超过阈值，并且查询结果集小于阈值，就缓存起来
			//时间短不缓存
			if(ja!=null && duration>cache_time_threshold && ja.size()<cache_rec_num_threshold) {
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
