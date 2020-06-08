/**
 * 
 */
package noo.mq.rocket;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author qujianjun   troopson@163.com
 * Mar 18, 2020 
 * 
 * 消费去重的校验
 * 
 */

public class ConsumDupCheck {

	public static final String CONSUM_DUP_REDIS_PREFIX="csnmd:";
	
	public static long EXPIRED_SECOND =300;
	
	private static final Logger logger=LoggerFactory.getLogger(ConsumDupCheck.class);
	
	
	private static StringRedisTemplate redis;
	
	
	@Autowired
	public void setRedis(StringRedisTemplate redis){
		ConsumDupCheck.redis = redis;
	}
	
	 
	public static boolean is_consumed(String topic, String tag, String uniquId) {
		String c = CONSUM_DUP_REDIS_PREFIX+topic+"_"+tag+"_"+uniquId; 
		boolean consumed = redis.hasKey(c);
		if(consumed) {
			logger.info("message is consumed, topic:"+topic+"  tag:"+tag+"  content:"+uniquId);
			return true;
		}else {
			redis.opsForValue().set(c, "0", EXPIRED_SECOND, TimeUnit.SECONDS);
			return false;
		}
	} 
	
	
}
