/**
 * 
 */
package noo.util;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import noo.exception.BusinessException;

/**
 * @author qujianjun   troopson@163.com
 * 限制来源IP地址调用某个接口的频率
 * 默认是按照1分钟来限制
 * Jul 16, 2020 
 */
public class ApiRateLimit {

	public static final Logger log = LoggerFactory.getLogger(ApiRateLimit.class);
	public static final String API_CALL_REDIS_KEY = "limit_api_rate:";
	  
	
	private final StringRedisTemplate redis;  
	private final Long limit;
	private final String apiname;
	private final int period;
	
	
	public ApiRateLimit(StringRedisTemplate redis,String apiname,  long limit) {
		this(redis,apiname,1,limit);
	}
	
	public ApiRateLimit(StringRedisTemplate redis,String apiname, int minutes, long limit) {
		if(S.isBlank(apiname))
			throw new IllegalArgumentException("api name can't be null.");
		this.redis = redis;
		this.period = minutes;
		this.limit = limit;
		this.apiname = apiname;
		log.info("ApiRateLimit: limit api "+apiname+" can be called "+this.limit +" times in "+this.period+" minutes");
	}
	
	public long checkLimit(String ip) {
		if(this.redis==null || this.limit<=0)
			return 0;
		if(S.isBlank(ip) )
			throw new NullPointerException("No IP address found!");  
		
		String key = API_CALL_REDIS_KEY+apiname+":"+ip;
		boolean setted = redis.opsForValue().setIfAbsent(key, "1"); 
		if(setted) {
			redis.expire(key, this.period, TimeUnit.MINUTES); 
			return 1;
		}else {
			long value = redis.opsForValue().increment(key, 1);
			if(value>this.limit)
				throw new BusinessException(508,"访问频率超过了系统限制！");
			return value;
		}
		
	}
	
	
}


