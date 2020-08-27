/**
 * 
 */
package noo.mq.rocket;

import java.util.concurrent.TimeUnit;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.data.redis.core.StringRedisTemplate;

import noo.json.JsonObject;
import noo.util.S;

/**
 * @author qujianjun troopson@163.com May 5, 2020
 * 异步发送的时候，的回掉对象
 * 
 */
public class RocketSendCallback implements SendCallback { 
	
	public final Message msg;
	public final IAlert alerter;
	public final StringRedisTemplate redis;

	public RocketSendCallback(Message msg , IAlert isAlert, StringRedisTemplate redis) {
		this.msg = msg;
		this.alerter = isAlert;
		this.redis = redis;
	}

	@Override
	public void onSuccess(SendResult sendResult) {
		if (RocketProducer.log.isDebugEnabled()) 
			RocketProducer.log.debug("Send MQ Msg OK:" + sendResult.getMsgId() + ",  topic:" + msg.getTopic() + " tag:" + msg.getTags());
		
	}

	@Override
	public void onException(Throwable e) {
		String topic = msg.getTopic();
		String tags = msg.getTags();
		String content = new String(msg.getBody());
		String msgid = msg.getKeys();
		if (this.redis != null) {
			// 如果发送失败了，放到redis中保存24小时，可以通过定时任务补发 
			JsonObject vs = new JsonObject();
			vs.put("topic", topic);
			if (S.isNotBlank(tags))
				vs.put("tag", tags);
			vs.put("v", content);
			vs.put("msgid", msgid); 

			this.redis.opsForList().leftPush(RocketProducer.EXPCEPTION_REDIS_KEY, vs.encode());
			this.redis.expire(RocketProducer.EXPCEPTION_REDIS_KEY, 24, TimeUnit.HOURS);
			RocketProducer.log.info("Send MQ Msg failed, push MQ msg into redis. topic:" + topic + " tag:" + tags
					+ " content:" + content);
			if(this.alerter!=null)
				this.alerter.alert("Send MQ Msg failed, push MQ msg into redis. topic:" + topic + " tag:" + tags + " content:"
					+ content);
		} else {
			RocketProducer.log.error("Send MQ Msg failed, topic:" + topic + " tag:" + tags + " content:" + content);
			if(this.alerter!=null)
				this.alerter.alert("Send MQ Msg failed, topic:" + topic + " tag:" + tags + " content:" + content);
		}
		e.printStackTrace();
	}
}
