/**
 * 
 */
package noo.security;

/**
 * @author qujianjun   troopson@163.com
 * 2018年8月23日 
 */
public interface SecuritySettingService {

	
	public User loadUserByName(String username);
	
	public boolean checkUserPassword(User u, String request);	
	
	public boolean isIgnore(String url);
	
	public String getLoginUrl();
	
	public String getLogoutUrl();
	
	public String getSuccessUrl();
	
	public boolean checkPath(User uobj, String path);
	
}
