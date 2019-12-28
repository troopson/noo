/**
 * 
 */
package noo.mq.rocket;

import org.apache.rocketmq.client.log.ClientLogger;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;


/**
 * @author qujianjun troopson@163.com 2018年8月31日
 */
/*
@Configuration
@ConditionalOnClass(org.apache.rocketmq.client.producer.DefaultMQProducer.class)
@ConditionalOnProperty("rocketmq.address")
*/
public class RocketMQConfig {

	  
	@Value("${rocketmq.address}")
	private String onsaddr;
	
	@Value("${rocketmq.producerid}")
	private String producerid;
	
	@Value("${rocketmq.consumerid}")
	private String consumerid;
 
	
	@Value("${rocketmq.send_timeout:10000}")
	private String send_timeout;
	
	@Value("${rocketmq.consumer_thread:3}")
	private String consumer_thread;
	  
	
	public RocketMQConfig() {
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
	
	
	@Bean
	@Lazy
	public RocketProducer createProducer() {
		
		RocketProducer rmp = new RocketProducer();
		rmp.start(this.producerid, this.onsaddr, Integer.parseInt(this.send_timeout));
		return rmp;
		
	}
	
	@Bean
	public RocketConsumerHolder createConsumer() {
		 
	    RocketConsumerHolder ch = new RocketConsumerHolder(this.consumerid, this.onsaddr, Integer.parseInt(this.consumer_thread)); 
	    return ch;
		
	}
	
	 
	
	

}
