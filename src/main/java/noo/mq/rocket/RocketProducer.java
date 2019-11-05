/**
 * 
 */
package noo.mq.rocket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.data.redis.core.StringRedisTemplate;

import noo.json.JsonArray;
import noo.json.JsonObject;
import noo.util.C;
import noo.util.ID;
import noo.util.S;

/**
 * @author qujianjun troopson@163.com 2018年8月31日
 */
public class RocketProducer {

	public static final Log log = LogFactory.getLog(RocketProducer.class);
	
	public static final String EXPCEPTION_REDIS_KEY="mq_producer_failed_key";

	private DefaultMQProducer producer;
	
	private StringRedisTemplate redis;
	

	

	public void setRedis(StringRedisTemplate redis) {
		this.redis = redis;
	}

	public void start(String producerid, String address, int timeout) {

		try {
			this.producer = new DefaultMQProducer(producerid);
			this.producer.setNamesrvAddr(address);
			this.producer.setInstanceName(C.uid());
			this.producer.setSendMsgTimeout(timeout);
			this.producer.setVipChannelEnabled(false);
			this.producer.start();

			log.info("RocketMQ Producer started OK !  ID:" + producerid + "    address:" + address + "   timeout:"
					+ timeout);
		} catch (MQClientException e) {
			e.printStackTrace();
			throw new RuntimeException("RocketMQ Producer start failed", e);
		}

	}

	// 同步发送消息，只要不抛异常就是成功
	public void sendMsg(String topic, String tag, JsonObject j) throws Exception {

		if (S.isBlank(topic)) {
			return;
		}

		Message msg = createMessage(topic, tag, j);

		try {
			SendResult sendResult = this.producer.send(msg);
			if (sendResult != null) {
				if (log.isDebugEnabled()) {
					log.debug("Send MQ Msg:" + sendResult.getMsgId() + ",  topic:" + topic + " tag:" + tag + " content:"
							+ j.encode());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new java.lang.IllegalStateException(
					"RocketMq Send mq message failed. Topic is:" + msg.getTopic() + " content:" + j.encode(), e);
		}

	}

	public void sendMsgAsync(String topic, String tag, JsonObject j, boolean retryOnfail) throws Exception {

		this.sendMsg(topic, tag, j, new SendCallback() {
			@Override
			public void onSuccess(SendResult sendResult) {
				if (log.isDebugEnabled()) {
					log.debug("Send MQ Msg:" + sendResult.getMsgId() + ",  topic:" + topic + " tag:" + tag + " content:"
							+ j.encode());
				}
			}

			@Override
			public void onException(Throwable e) {
				if(RocketProducer.this.redis!=null) {
					//如果发送失败了，放到redis中保存24小时，可以通过定时任务补发
					JsonObject vs = new JsonObject();
					vs.put("topic", topic);
					if(S.isNotBlank(tag))
						vs.put("tag", tag);
					vs.put("v", j);
					
					RocketProducer.this.redis.opsForList().leftPush(EXPCEPTION_REDIS_KEY, vs.encode());
					RocketProducer.this.redis.expire(EXPCEPTION_REDIS_KEY, 24, TimeUnit.HOURS);
					log.info("Send MQ Msg failed, push MQ msg into redis. topic:"+ topic + " tag:" + tag + " content:" + j.encode());
				}else
					log.error("Send MQ Msg failed, topic:" + topic + " tag:" + tag + " content:" + j.encode());
				
				e.printStackTrace();
			}
		});
	}
	
	public void sendMsgAsync(String topic, String tag, JsonObject j) throws Exception {

		this.sendMsgAsync(topic, tag, j, false);
	}

	public void sendMsg(String topic, String tag, JsonObject j, SendCallback callback) throws Exception {
		if (S.isBlank(topic)) {
			return;
		}

		if (log.isDebugEnabled()) {
			log.debug("Send MQ Msg: ,   topic:" + topic + " tag:" + tag + " content:" + j.encode());
		}

		Message msg = createMessage(topic, tag, j);
		this.producer.send(msg, callback);
	}

	public void sendOnewayMsg(String topic, String tag, JsonObject j) throws Exception {
		if (S.isBlank(topic)) {
			return;
		}

		if (log.isDebugEnabled()) {
			log.debug("Send MQ Msg,  topic:" + topic + " tag:" + tag + " content:" + j.encode());
		}

		Message msg = createMessage(topic, tag, j);
		this.producer.sendOneway(msg);
	}

	public void sendBatchMsgAsync(String topic, String tag, JsonArray j) throws Exception {
		if (S.isBlank(topic)) {
			return;
		}

		if (log.isDebugEnabled()) {
			log.debug("Send MQ Msg: ,   topic:" + topic + " tag:" + tag + " content:" + j.encode());
		}

		List<Message> lmsg = new ArrayList<Message>();
		j.forEachJsonObject(c -> {
			Message msg = createMessage(topic, tag, c);
			lmsg.add(msg);
		});

		ListSplitter splitter = new ListSplitter(lmsg);
		while (splitter.hasNext()) {
			try {

				List<Message> listItem = splitter.next();
				SendResult sendResult = producer.send(listItem);

				if (sendResult != null) {
					if (log.isDebugEnabled()) {
						log.debug("Send MQ Msg:" + sendResult.getMsgId() + ",  topic:" + topic + " tag:" + tag
								+ " content:" + j.encode());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new java.lang.IllegalStateException(
						"RocketMq Send mq message failed. Topic is:" + topic + " content:" + j.encode(), e);
			}
		}

	}

	private Message createMessage(String topic, String tag, JsonObject j) {
		Message msg = new Message();
		msg.setTopic(topic);
		msg.setTags(tag);
		msg.setBody(j.encode().getBytes());
		msg.setKeys(ID.uuid());
		return msg;
	}

}
