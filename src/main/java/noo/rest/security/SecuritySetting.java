/**
 * 
 */
package noo.rest.security;

import javax.servlet.http.HttpServletRequest;

import noo.json.JsonObject;

/**
 * @author qujianjun   troopson@163.com
 * 2018年8月23日 
 */
public interface SecuritySetting {

	
	public AbstractUser loadUserByName(String username);
	
	public AbstractUser loadUserByAuthCode(String authcode);
	
	public boolean checkUserPassword(AbstractUser u, String requestPassword, HttpServletRequest req);	
	
	default public boolean checkClient(AbstractUser u, String client_type) {
		return true;
	}
	
	default public boolean is_AuthcodeUrl(String url) {
		return false;
	}
	
	public boolean isIgnore(String url);
	
	public boolean isLoginUrl(String url);
	
	public boolean isLogoutUrl(String url);
	
	public boolean canAccess(AbstractUser uobj, String path);
	
	public AbstractUser fromJsonObject(JsonObject j);
	
	default public void afterLoginSuccess(AbstractUser uo, HttpServletRequest req) {
		
	}
	
	
	

	
}
