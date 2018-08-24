/**
 * 
 */
package noo.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import noo.exception.AuthenticateException;
import noo.exception.SessionTimeoutException;
import noo.exception.UnAuthrizedException;

/**
 * @author qujianjun   troopson@163.com
 * 2018年8月23日 
 */


public class SecurityFilter implements Filter {
 
	@Autowired
	private SecuritySettingService us;
	
	public static final String SESSION_KEY ="noo.session.userobj";
	
	public String username="username";
	public String password="password";
	

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		 
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest)request;
		String requrl = req.getRequestURI();
		if(us.isIgnore(requrl)) {
			this.doFilter(request, response, chain);
			return;
	    }
		
		if(requrl.startsWith(us.getLoginUrl())) {
		
			String u = request.getParameter(this.username);
			String p = request.getParameter(this.password);
			User uobj = us.loadUserByName(u);
			if(us.checkUserPassword(uobj, p)) {
				req.getSession(true).setAttribute(SESSION_KEY, uobj);
				((HttpServletResponse)response).sendRedirect(us.getSuccessUrl());
				//request.getRequestDispatcher(us.getSuccessUrl()).
			}else {
				HttpSession s = req.getSession();
				if(s!=null)
				   s.invalidate();
				throw new AuthenticateException("username, password is wrong.");
			}
			return;
		}
		
		if(requrl.startsWith(us.getLogoutUrl())) {
			HttpSession s = req.getSession();
			if(s!=null)
				s.invalidate();
			return;
		}
		
		HttpSession s = req.getSession();
		if(s == null )
			throw new SessionTimeoutException();
		
		User uobj = (User)s.getAttribute(SESSION_KEY);
		if(uobj == null )
			throw new SessionTimeoutException();
		
		if(us.checkPath(uobj, requrl)) {
			this.doFilter(request, response, chain);
		}else {
			throw new UnAuthrizedException("username, password is wrong.");
		}
		
	}

	@Override
	public void destroy() {
		
		
	}
	
 

	
}
