/**
 * 
 */
package noo.rest.security;

import javax.servlet.http.HttpServletRequest;

/**
 * @author qujianjun   troopson@163.com
 * 2018年8月29日 
 */
public final class AuthContext {

	private static final ThreadLocal<AbstractUser> LOCAL = new ThreadLocal<AbstractUser>();
	private static final ThreadLocal<HttpServletRequest> REQUEST = new ThreadLocal<HttpServletRequest>();
	
	public static void set(AbstractUser u) {
		LOCAL.set(u);
	}
	
	public static AbstractUser get() {
		return LOCAL.get();
	}
	
	public static void setReq(HttpServletRequest req) {
		REQUEST.set(req);
	}
	
	public static HttpServletRequest getReq() {
		return REQUEST.get();
	}
	
	public static void clear() {
		LOCAL.remove();
		REQUEST.remove();
	}
	
	
}
