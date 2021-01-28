/**
 * 
 */
package noo.mq.rocket;

import java.util.ArrayList;
import java.util.List;

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
	//不需要延迟
	public static final int NO_DELAY = -1;

	private DefaultMQProducer producer;
	
	private StringRedisTemplate redis;
	 
	private IAlert alerter = null;

	public void setRedis(StringRedisTemplate redis) {
		this.redis = redis;
	}
	
	StringRedisTemplate getRedis() {
		return this.redis;
	}
	
	public void setAlert(IAlert alert) {
		this.alerter = alert;
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
		this.sendMsg(topic, tag, j,NO_DELAY);
	}
	// 同步发送消息，只要不抛异常就是成功
	public void sendMsg(String topic, String tag, JsonObject j,int delayLevel) throws Exception {

		if (S.isBlank(topic)) {
			return;
		}

		Message msg = createMessage(topic, tag, j, delayLevel);

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

	
	public void sendMsgAsync(String topic, String tag, JsonObject j) throws Exception { 
		this.sendMsgAsync(topic, tag, j, NO_DELAY, false); 
	}
	
	public void sendMsgAsync(String topic, String tag, JsonObject j, boolean retryOnfail) throws Exception { 
		this.sendMsgAsync(topic, tag, j, NO_DELAY, retryOnfail); 
	}

	//异步发送，设置延迟级别，映射conf/broker.conf 配置文件中的延迟时间,0表示不延迟
	//messageDelayLevel = 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
	public void sendMsgAsync(String topic, String tag, JsonObject j, int delayLevel, boolean retryOnfail) throws Exception {
		if (S.isBlank(topic)) {
			return;
		}

		if (log.isDebugEnabled()) 
			log.debug("Send MQ Msg: ,   topic:" + topic + " tag:" + tag + " content:" + j.encode());
		
		
		Message msg = createMessage(topic, tag, j, delayLevel); 
		this.sendMsg(msg, new RocketSendCallback(msg,this.alerter,this.redis));
	}


	public void sendMsg(Message msg, SendCallback callback) throws Exception {  
		this.producer.send(msg, callback);
	}

	

	public void sendOnewayMsg(String topic, String tag, JsonObject j) throws Exception {
		if (S.isBlank(topic)) {
			return;
		}

		if (log.isDebugEnabled()) 
			log.debug("Send MQ Msg,  topic:" + topic + " tag:" + tag + " content:" + j.encode());
		

		Message msg = createMessage(topic, tag, j,-1);
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
			Message msg = createMessage(topic, tag, c, NO_DELAY);
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
	 

	private Message createMessage(String topic, String tag, JsonObject j, int level) {
		Message msg = new Message();
		msg.setTopic(topic);
		msg.setTags(tag);
		msg.setBody(j.encode().getBytes());
		msg.setKeys(ID.uuid());
		if(level != NO_DELAY)
			msg.setDelayTimeLevel(level);
		return msg;
	}

} 

	

