/**
 * 
 */
package noo.rest.security;

import noo.json.JsonObject;

/**
 * @author qujianjun   troopson@163.com
 * 2018年8月23日 
 */
public abstract class AbstractUser {
	
	private transient String client; 
	
	private transient String token;
	
	private String id;
	
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return this.id;
	}
	
	public void setToken(String s){
		this.token = s;
	}
	
	public String getToken() {
		return this.token;
	} 
	
	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	} 

	public long getSessionTimeoutMinutes(String client) {
		return 120L;
	}
	
	//User对象发送到页面response中的结构
    public abstract JsonObject toResponseJsonObject();
	
    //User对象保存为JsonObject的结构
	public JsonObject toJsonObject() {
		return this.toResponseJsonObject(); 
	}
	
	
	

}
