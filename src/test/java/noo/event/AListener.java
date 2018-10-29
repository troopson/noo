/**
 * 
 */
package noo.event;

import org.springframework.stereotype.Component;

import noo.json.JsonObject;

/**
 * @author qujianjun   troopson@163.com
 * 2018年7月31日 
 */
@Component
public class AListener implements Listener {

	@Override
	public String[] on() {
		return new String[] {"just.test"};
	}

	@Override
	public void invoke(Event e) {
		JsonObject p = e.params();
		System.out.println(p==null? "params is null": p.encode());
		System.out.println(e.getName()+"  "+e.getTarget().getClass().getName());
		e.setResult("event called ok!");
	}

	 

}
