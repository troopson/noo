/**
 * 
 */
package noo.mq.rocket;

import java.util.HashSet;
import java.util.Set;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.common.message.MessageExt;

import noo.json.JsonObject;
import noo.util.C;

/**
 * @author qujianjun   troopson@163.com
 * 2018年9月4日 
 */
public interface RocketConsumer {
	
	public static Set<String> asSet(String...topics){
		Set<String> s = new HashSet<>();
		for(String a: topics) {
			s.add(a);
		}
		return s;
	}
	
	default public String getConsumerid() { 
		return C.uid();
	}
	
	default public String getTag() { 
		return "*";
	}
	
	default public String getConsumerGroupID() {
		return this.getClass().getName().replace(".", "_");
	}
	
	public Set<String> onTopics(); 
	
	public void consumer(JsonObject msg,String tag, MessageExt msgraw, ConsumeConcurrentlyContext context);
	
}
