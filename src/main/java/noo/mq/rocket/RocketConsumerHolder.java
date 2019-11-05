package noo.mq.rocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.springframework.beans.factory.annotation.Autowired;

import noo.util.C;

/**
 * @author qujianjun troopson@163.com 2018年9月4日
 */


public class RocketConsumerHolder{

	public static final Log log = LogFactory.getLog(RocketConsumerHolder.class);

	private List<DefaultMQPushConsumer> beans = new ArrayList<>();

	private String consumerid;
	private String address;
	private int thread = 3; 

	public RocketConsumerHolder(String consumerid, String address, int thread) {
		this.consumerid = consumerid;
		this.address = address;
		this.thread = thread; 
	}

	

	
	 

	@PreDestroy
	public void destroy() {
		if (beans == null) {
			return;
		}

		try {
			for (DefaultMQPushConsumer c : beans) {
				c.shutdown();
			}
		}catch(Exception e) {
			
		}
	} 
	 

	@Autowired(required=false)
	private List<RocketConsumer> types;
	
	@PostConstruct
	public void startConsumers() throws MQClientException {
		if (types == null || types.isEmpty()) {
			return;
		}
		
		System.out.println("==========here=============");

		try {
			for (RocketConsumer r : types) {
				String cid = r.getConsumerid();
				if(cid==null) {
					cid = consumerid;
				}
				DefaultMQPushConsumer cb = this.buildConsumer(cid, address, thread, r);
				cb.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
				this.addConsumer(cb);
			}
		} catch (MQClientException e) {
			e.printStackTrace();
			throw new RuntimeException("RocketMQ consumer start failed.", e);
		}
		
		log.info("RocketMQ ConsumerHold starting......");
		if (beans == null) {
			return;
		}

		for (DefaultMQPushConsumer c : beans) {
			c.start();
		}

		log.info("RocketMQ ConsumerHold start OK.");

	}
	
	
	public DefaultMQPushConsumer buildConsumer(String consumerid, String address, int thread, RocketConsumer rl)
			throws MQClientException {
		RocketMessageAdapter ra = new RocketMessageAdapter(rl);
		DefaultMQPushConsumer cb = new DefaultMQPushConsumer(consumerid);
		cb.setNamesrvAddr(address);
		cb.setConsumeThreadMin(thread);
		cb.setConsumeTimeout(5L);
		cb.setConsumerGroup(rl.getConsumerGroupID()); 
		cb.setInstanceName(C.uid());
		
		Set<String> topic = rl.onTopics();
		if (topic == null || topic.isEmpty()) {
			throw new NullPointerException("must set topic of RocketMessageListener.");
		}

		for (String t : topic) {
			cb.subscribe(t, rl.getTag());
		}
		cb.registerMessageListener(ra);

		return cb;

	}

	public void addConsumer(DefaultMQPushConsumer c) {
		this.beans.add(c);
	}

}
