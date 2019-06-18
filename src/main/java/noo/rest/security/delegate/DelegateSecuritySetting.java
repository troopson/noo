package noo.rest.security.delegate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import noo.rest.security.AbstractUser;

/**
 * @author qujianjun   troopson@163.com
 * 2018年8月23日 
 */

public interface DelegateSecuritySetting {

	boolean isIgnore(String requrl);

	boolean isLoginUrl(String requrl);

	boolean isLogoutUrl(String requrl);
	
	AbstractUser loginByDelegate(HttpServletRequest req,HttpServletResponse resp);
	
	void doLogout(HttpServletRequest req,AbstractUser u);

	void initUserSession(String token, AbstractUser uobj, HttpServletRequest request);

	AbstractUser getUserFromSession(String token, HttpServletRequest request);

	boolean canAccess(AbstractUser u, String requrl);

}
