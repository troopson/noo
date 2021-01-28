/**
 * 
 */
package noo.mq.rocket;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import noo.json.JsonObject;
import noo.util.S;

/**
 * @author qujianjun   troopson@163.com
 * 2021年1月28日 
 * 
 * 异步发送失败的消息，会暂时保存到redis中，
 * 通过这个类，对保存在redis中的失败消息，重新进行发送
 * 
 */

public class RocketSendCompensation {
	 
	private RocketProducer producer;
	
	public RocketSendCompensation(RocketProducer producer) {
		this.producer = producer;
	}
	/**
	 * 每5分钟检查一下，对发送失败的进行补偿，重新进行发送
	 */
	@Scheduled(fixedDelay = 1000*60*5 )
	private void start() {
		StringRedisTemplate redis = this.producer.getRedis();
		if(redis==null)
			return;
		String s = redis.opsForList().rightPop(RocketProducer.EXPCEPTION_REDIS_KEY);
		while(S.isNotBlank(s)) { 
			JsonObject j = new JsonObject(s);
			String topic = j.getString("topic");
			String tag = j.getString("tag");
			String content = j.getString("v");
			JsonObject msg = new JsonObject(content);
			try {
				this.producer.sendMsgAsync(topic, tag, msg);
				RocketProducer.log.info("Compensation send MQ message OK, topic:" + topic + " tag:" + tag + " content:" + content);
			}catch(Exception e) {
				RocketProducer.log.error("Compensation send MQ message failed, topic:" + topic + " tag:" + tag + " content:" + content);
			}
			s = redis.opsForList().rightPop(RocketProducer.EXPCEPTION_REDIS_KEY);
		}
		
	} 
	
	
}
