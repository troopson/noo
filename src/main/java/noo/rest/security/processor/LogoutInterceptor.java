/**
 * 
 */
package noo.rest.security.processor;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import noo.rest.security.SecueHelper;
import noo.util.S;

/**
 * @author qujianjun   troopson@163.com
 * 2018年10月16日 
 */
public class LogoutInterceptor extends RequestInterceptor {
 
 
	
	@Override
	public boolean process(String requrl, HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		if(us.isLogoutUrl(requrl)) {
			this.doLogout(req, resp);
			return true;
		}else {
			return false;
		}
	}
	
	private void doLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String authkey = req.getHeader(SecueHelper.HEADER_KEY); 
		if(S.isNotBlank(authkey)) {
			SecueHelper.deleteUserLoginInfo(req, redis, us, authkey);
			//this.redis.delete(authkey); 
		}
		resp.addHeader(SecueHelper.HEADER_KEY, "");
		
		Cookie c = new Cookie(SecueHelper.HEADER_KEY,"");
		c.setHttpOnly(true);
		c.setPath("/");
		c.setMaxAge(0);
		resp.addCookie(c);
		
		SecueHelper.writeResponse(resp, "0");  
	}
 
}
