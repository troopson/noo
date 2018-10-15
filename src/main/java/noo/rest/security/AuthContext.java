/**
 * 
 */
package noo.rest.security;

/**
 * @author qujianjun   troopson@163.com
 * 2018年8月29日 
 */
public final class AuthContext {

	private static final ThreadLocal<AbstractUser> LOCAL = new ThreadLocal<AbstractUser>();
	
	public static void set(AbstractUser u) {
		LOCAL.set(u);
	}
	
	public static AbstractUser get() {
		return LOCAL.get();
	}
	
	public static void clear() {
		LOCAL.remove();
	}
	
	
}
