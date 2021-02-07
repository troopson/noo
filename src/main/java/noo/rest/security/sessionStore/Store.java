/**
 * 
 */
package noo.rest.security.sessionStore;

import javax.servlet.http.HttpServletRequest;

import noo.rest.security.AbstractUser;

/**
 * @author qujianjun   troopson@163.com
 * 2021年2月7日 
 */
public interface Store {
	
	public void initUserSession(String token, AbstractUser uobj, HttpServletRequest request);
	
	public AbstractUser getUserFromSession(String token, HttpServletRequest request);
	
	public void updateSession(AbstractUser uobj, HttpServletRequest request);
	
	public void invalidSession(AbstractUser uobj, HttpServletRequest request);
	

}
