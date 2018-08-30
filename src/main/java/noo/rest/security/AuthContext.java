/**
 * 
 */
package noo.rest.security;

/**
 * @author qujianjun   troopson@163.com
 * 2018年8月29日 
 */
public final class AuthContext {

	private static final ThreadLocal<User> local = new ThreadLocal<User>();
	
	public static void set(User u) {
		local.set(u);
	}
	
	public static User get() {
		return local.get();
	}
	
	public static void clear() {
		local.remove();
	}
	
	
}
