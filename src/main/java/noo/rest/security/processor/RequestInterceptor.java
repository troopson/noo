/**
 * 
 */
package noo.rest.security.processor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.StringRedisTemplate;

import noo.rest.security.SecuritySetting;

/**
 * @author qujianjun   troopson@163.com
 * 2018年10月16日 
 */
public abstract class RequestInterceptor { 

	protected SecuritySetting us;
	 
	protected StringRedisTemplate redis; 
	 
	public void setSecuritySetting(SecuritySetting us) {
		this.us = us; 
	} 
	
	public void setRedis(StringRedisTemplate redis) {
		this.redis = redis; 
	}
	
	public abstract boolean process(String requrl, HttpServletRequest req ,HttpServletResponse resp) throws Exception;
	
}
