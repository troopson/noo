/**
 * 
 */
package noo.rest.security;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;

import noo.json.JsonObject;

/**
 * @author qujianjun   troopson@163.com
 * 2018年8月23日 
 */
public abstract class User {
	
	private String token;
	
	void setToken(String s){
		this.token = s;
	}
	
	public String getToken() {
		return this.token;
	}
	
	public long getSessionTimeoutMinutes() {
		return 120L;
	}
	
	public void updateUser(StringRedisTemplate redis) {
		String ustring = this.toJsonObject().encode(); 
		String authkey =  this.getToken(); 
		redis.opsForValue().set(SecurityFilter.REDIS_KEY+":"+authkey, ustring, this.getSessionTimeoutMinutes(), TimeUnit.MINUTES);
	}
	
	//User对象发送到页面response中的结构
    public abstract JsonObject toResponseJsonObject();
	
    //User对象保存为JsonObject的结构
	public abstract JsonObject toJsonObject();
	
	
	

}
