/**
 * 
 */
package noo.mq.rocket;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import noo.util.MD5;

/**
 * @author qujianjun   troopson@163.com
 * Mar 18, 2020 
 * 
 * 消费去重的校验
 * 
 */
@Component
public class DupCheck {

	public static final String CONSUM_DUP_REDIS_PREFIX="csnmd:";
	
	public static long EXPIRED_SECOND = 30;
	
	private static final Logger logger=LoggerFactory.getLogger(DupCheck.class);
	
	
	private static StringRedisTemplate redis;
	
	
	@Autowired
	public void setRedis(StringRedisTemplate redis){
		DupCheck.redis = redis;
	}
	
	 
	public static boolean is_consumed(String topic, String content) {
		String c = CONSUM_DUP_REDIS_PREFIX+topic+"_"+MD5.encode(content); 
		boolean not_consumed = redis.opsForValue().setIfAbsent(c, "0");
		//boolean consumed = redis.hasKey(c);
		if(not_consumed) {
			redis.expire(c, EXPIRED_SECOND, TimeUnit.SECONDS);
			return false;
		}else { 
			logger.info("message is consumed, topic:"+topic+"  content:"+content);
			return true;
		}
	} 
	
	
}
