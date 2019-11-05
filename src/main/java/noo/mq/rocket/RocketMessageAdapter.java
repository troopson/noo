/**
 * 
 */
package noo.mq.rocket;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import noo.json.JsonObject;

/**
 * @author qujianjun   troopson@163.com
 * 2018年9月4日 
 */
public class RocketMessageAdapter implements MessageListenerConcurrently {


	public static final Log log = LogFactory.getLog(RocketProducer.class);

	private RocketConsumer rl;
	 
	
	public RocketMessageAdapter(RocketConsumer l) {
		this.rl =l; 
	}



	@Override
	public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {


	   try {
		   for(MessageExt m: msgs) {
			   String s = new String(m.getBody() ,"UTF-8");
			   JsonObject mbody = new JsonObject(s);
 
			   if(log.isDebugEnabled()) {
				log.debug("Consumer MQ Msg, topic:"+m.getTopic()+"  tag:"+m.getTags()+"  content:"+s);
			}
			   
			   rl.consumer(mbody, m.getTags(), m,  context);
		   }
		} catch (Exception e) {
			e.printStackTrace();
		    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
	   }
		return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;


//		//针对MQ不消费问题的修改
//		for (MessageExt m : msgs) {
//			try {
//				String s = new String(m.getBody(), "UTF-8");
//				JsonObject mbody = new JsonObject(s);
//				rl.consumer(mbody, m.getTags(), m, context);
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//		        if (msgs.get(0).getReconsumeTimes() == 3) {
//					log.info("-------消费超时------");
//			    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;// 成功
//		   } else {
//			   return ConsumeConcurrentlyStatus.RECONSUME_LATER;// 重试
//		       }
//			}
//		}
//		return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;




	}

}


