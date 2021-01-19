/**
 * 
 */
package noo.rest.security.api;
 
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import noo.util.Req;
import noo.util.S;
import noo.util.SpringContext;

/**
 * @author qujianjun   troopson@163.com
 * Jul 20, 2020 
 读取spring配置文件中的配置项，实现对接口访问次数的控制
 
 使用方式
 
 在需要控制的类中，配置属性
	 
	private ApiRateLimitPool arlp; 
 
	
	public JsonObject refreshAccessToken(JsonObject params,HttpServletRequest request) { 
		arlp.checkLimit("refreshToken", request);
		
 配置方式： 
 一分钟5次调用
 limit.api.refreshToken: 5
 
 30分钟内5次调用
 limit.api.refreshToken: 30,5
  
 * 
 */
public class ApiRateLimitPool {
 
	private static final Map<String, ApiRateLimit> pools = new ConcurrentHashMap<>(); 
	
	private static final String PROPERTY_PREX="limit.api.";
	
	@Autowired
	private StringRedisTemplate redis;
	
	public void setRedis(StringRedisTemplate redis) {
		this.redis = redis;
	}


	private ApiRateLimit getApiRateLimitFromPool(String apiName,Integer def_minutes, Long def_limit) {  
		//if(noLimit.contains(apiName))
		//	return null;
		if(pools.containsKey(apiName)) {
			return pools.get(apiName); 
		}else { 
			
			ApiRateLimit ar = this.buildApiRateLimit(apiName, def_minutes, def_limit);
			if(ar==null)
				return null;
			pools.put(apiName, ar);
			return ar; 
		} 
	}
	
	private ApiRateLimit buildApiRateLimit(String apiName,Integer def_minutes, Long def_limit) {
		if(def_minutes!=null && def_limit!=null && def_limit>0 && def_minutes>0) {
			return new ApiRateLimit(this.redis,apiName,def_minutes,def_limit);
		}else {
			String apiLimit = SpringContext.getProperty(PROPERTY_PREX+apiName);
			if(S.isBlank(apiLimit)) {  
				return null;  
			}else {
				ApiRateLimit ar;
				if(apiLimit.indexOf(",")!=-1) {
					String[] minute_rate = apiLimit.split(",");
					int minute = Integer.parseInt(minute_rate[0]);
					long rate = Long.parseLong(minute_rate[1]);
					ar = new ApiRateLimit(this.redis,apiName,minute,rate);
				}else {
					ar = new ApiRateLimit(this.redis,apiName,Long.parseLong(apiLimit));
				}
				pools.put(apiName, ar);
				return ar;
			}
		}
	}
	
	public void checkLimit(String apiName,String ip,Integer def_minutes, Long def_limit) {
		ApiRateLimit ar = this.getApiRateLimitFromPool(apiName,def_minutes,def_limit);
		if(ar==null)
			return; 
		ar.checkLimit(ip);
	}
	
	public void checkLimit(String apiName,HttpServletRequest rawrequest,Integer def_minutes, Long def_limit) {
		this.checkLimit(apiName, Req.getClientIP(rawrequest), def_minutes, def_limit);
	}
	
	public void checkLimit(String apiName,HttpServletRequest rawrequest) {
		this.checkLimit(apiName, Req.getClientIP(rawrequest), null, null);
	}
	
	public void checkLimit(String apiName,String ip) {
		this.checkLimit(apiName, ip, null, null);
	}
	

}
