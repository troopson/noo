/**
 * 
 */
package noo.event;
 

import noo.json.JsonObject;
import noo.util.S;
 

/**
 * @author 瞿建军       Email: troopson@163.com
 * 2016年10月10日
 */
public final class Event {
	
	//异步的事件
	public void publish(){
		this.publish(null);
	}
	public void publish(JsonObject param){
		this.params = param;
		ListenerPool.doEvent(this.getName(),  this);
	} 
	
	//同步的事件
	public Object trigger(){
		return this.trigger(null);
	}
	public Object trigger(JsonObject param){
		this.params = param;
		return ListenerPool.callEvent(this.getName(),  this);
	}
	
	//=================================================
	
	private Object target=null;
	
	private String name=null;
	
	private JsonObject params=null;
	 
	private Object result;
	
	public Event(String name, Object target){
		if(S.isBlank(name) || target==null) {
			throw new NullPointerException();
		}
		
		this.name=name;
		this.target=target; 
	}  

	Object getTarget() {
		return target;
	} 

	public String getName() {
		return name;
	}

	public JsonObject params() {
		return this.params;
	}
	
	public Object getParam(String key) {
		if(params==null || S.isBlank(key)) {
			return null;
		}
		return params.getValue(key);
	}

	public void setParams(String key, Object param) {
		if(S.isBlank(key)) {
			return;
		}
			
		if(this.params==null) {
			this.params= new JsonObject();
		}
		
		if(param==null) {
			this.params.remove(key);
		}else {
			this.params .put(key, param);
		}
	}

	public Object getResult() {
		return result;
	}

    void setResult(Object result) {
		this.result = result;
	}

}
