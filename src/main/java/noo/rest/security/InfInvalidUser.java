/**
 * 
 */
package noo.rest.security;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author qujianjun   troopson@163.com
 * Oct 9, 2020 
 */
public interface InfInvalidUser {

	public void doInvalidUser(StringRedisTemplate redis, String userid);
	
}
