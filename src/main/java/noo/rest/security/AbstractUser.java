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
	
	private String token;
	
	void setToken(String s){
		this.token = s;
	}
	
	public String getToken() {
		return this.token;
	}
	
	public long getSessionTimeoutMinutes() {
		return 120L;
	}
	
	//User对象发送到页面response中的结构
    public abstract JsonObject toResponseJsonObject();
	
    //User对象保存为JsonObject的结构
	public abstract JsonObject toJsonObject();
	
	
	

}
