/**
 * 
 */
package noo.rest.security.sessionStore;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import noo.exception.SessionTimeoutException;
import noo.rest.security.AbstractUser;

/**
 * @author qujianjun   troopson@163.com
 * 2021年2月7日 
 * 将session存储在web服务器的session中
 * 
 */
public class WebSessionStorage implements Store { 
	
	public static final Logger log = LoggerFactory.getLogger(WebSessionStorage.class); 
 
	private String name; 
	 
	public WebSessionStorage(String name) {
		this.name = name;
	}

	@Override
	public AbstractUser getUserFromSession(String token, HttpServletRequest request) {
		AbstractUser u = (AbstractUser) request.getSession().getAttribute(this.name); 
		if (u == null)
			throw new SessionTimeoutException(); 
		return u;
	}

	@Override
	public void initUserSession(String token, AbstractUser uobj, HttpServletRequest request) {
		request.getSession().setAttribute(this.name, uobj); 
		log.info("登录成功，用户信息："+uobj.toJsonObject());
	}
	
	@Override	
	public void updateSession(AbstractUser uobj, HttpServletRequest request) {
		request.getSession().setAttribute(this.name, uobj); 
		log.info("更新用户信息成功："+uobj.toJsonObject());
	} 

	@Override
	public void invalidSession(AbstractUser uobj, HttpServletRequest request) { 
		request.getSession().invalidate(); 
		log.info("注销用户信息成功："+uobj.toJsonObject());
	}
	
}
