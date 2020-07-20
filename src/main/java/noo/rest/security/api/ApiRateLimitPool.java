/**
 * 
 */
package noo.rest.security.api;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import noo.util.Req;
import noo.util.S;
import noo.util.SpringContext;

/**
 * @author qujianjun   troopson@163.com
 * 
 * Jul 20, 2020 
 */
public class ApiRateLimitPool {
 
	private static final Map<String, ApiRateLimit> pools = new ConcurrentHashMap<>();
	private static final Set<String> noLimit = new HashSet<>();
	
	private static final String PROPERTY_PREX="limit.api.";
	
	@Autowired
	private StringRedisTemplate redis;
	
	public void setRedis(StringRedisTemplate redis) {
		this.redis = redis;
	}


	private ApiRateLimit getApiRateLimit(String apiName) {  
		if(noLimit.contains(apiName))
			return null;
		if(pools.containsKey(apiName)) {
			return pools.get(apiName); 
		}else { 
			String apiLimit = SpringContext.getProperty(PROPERTY_PREX+apiName);
			if(S.isBlank(apiLimit)) { 
				noLimit.add(apiName);
				return null;  
			}else {
				ApiRateLimit ar = new ApiRateLimit(this.redis,apiName,Long.parseLong(apiLimit));
				pools.put(apiName, ar);
				return ar;
			}
		} 
	}
	
	
	public void checkLimit(String apiName,HttpServletRequest rawrequest) {
		ApiRateLimit ar = this.getApiRateLimit(apiName);
		if(ar==null)
			return;
		String ip = Req.getClientIP(rawrequest);
		ar.checkLimit(ip);
	}
	
	public void checkLimit(String apiName,String ip) {
		ApiRateLimit ar = this.getApiRateLimit(apiName);
		if(ar==null)
			return; 
		ar.checkLimit(ip);
	}
	

}
