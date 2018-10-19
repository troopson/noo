/**
 * 
 */
package noo.event.security;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import noo.json.JsonObject;
import noo.rest.security.AbstractUser;
import noo.rest.security.SecuritySetting;  

/**
 * @author qujianjun   troopson@163.com
 * 2018年8月21日 
 */

@Service
public class UserService implements SecuritySetting {

	public static final Log log = LogFactory.getLog(UserService.class);
	 
 
	public User getUser(int userid) {
		User u = new User();
		u.setCode("admin");
		u.setName("admin");
		u.setToken("1");
		u.setUserid(userid);
		return u;		
	}
	 

	@Override 
	public AbstractUser loadUserByName(String username) {
		
		return this.getUser(0);
		
	} 
 

	@Override
	public boolean checkUserPassword(AbstractUser u, String requestPassword, HttpServletRequest req) {
		if("admin".equals(requestPassword))
			return true;
		return false;

	}

	@Override
	public boolean isIgnore(String url) {
		return false;
	}

	@Override
	public boolean isLoginUrl(String url) {
		if(url.contains("/login"))
			return true;
		return false;
	}

	@Override
	public boolean isLogoutUrl(String url) {
		if(url.contains("/logout"))
			return true;
		return false;
	}

	@Override
	public boolean canAccess(AbstractUser uobj, String path) { 
		return true;
	}


	@Override
	public AbstractUser fromJsonObject(JsonObject j) {
		return j.mapTo(User.class); 
	}

	 
}
