/**
 * 
 */
package noo.mq.rocket;

import org.apache.rocketmq.client.log.ClientLogger;
import org.apache.rocketmq.remoting.common.RemotingHelper;


/**
 * @author qujianjun troopson@163.com 2018年8月31日
 * 可以在Configuration中定义producer和consumer bean
 */
/*
@Configuration
@ConditionalOnClass(org.apache.rocketmq.client.producer.DefaultMQProducer.class)
@ConditionalOnProperty("rocketmq.address")
*/
public class RocketMQConfig {
 
	private String onsaddr;
	    
	
	public RocketMQConfig(String onsaddr) {
		this.onsaddr = onsaddr;
		System.setProperty(ClientLogger.CLIENT_LOG_LEVEL, "WARN");
		System.setProperty(RemotingHelper.ROCKETMQ_REMOTING, "WARN");
	}
	
	/*
	 rocketmq: 
	     accesskey: DEMO_AK
	     secretkey: DEMO_SK
	     producerid: DEMO_PID
	     consumerid: DEMO_CID
	     send_timeout: 3000
	     onsaddr: http://onsaddr-internal.aliyun.com:8080/rocketmq/nsaddr4client-internal   
	 */
	

	public RocketProducer createProducer(String producerid,Integer send_timeout) {
		
		if(send_timeout==null || send_timeout<=0)
			send_timeout=10000;
		RocketProducer rmp = new RocketProducer();
		rmp.start(producerid, this.onsaddr, send_timeout);
		return rmp;
		
	}
	

	public RocketConsumerHolder createConsumer(String consumerid,Integer consumer_thread_num) {
		 
		if(consumer_thread_num==null || consumer_thread_num<=0)
			consumer_thread_num = 3;
	    RocketConsumerHolder ch = new RocketConsumerHolder(consumerid, this.onsaddr, consumer_thread_num); 
	    return ch;
		
	}
	
	 
	
	

}
