/**
 * 
 */
package noo.rest.security.delegate;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;

import noo.exception.AuthenticateException;
import noo.exception.BaseException;
import noo.exception.BusinessException;
import noo.exception.ExpCode;
import noo.exception.SessionTimeoutException;
import noo.json.JsonObject;
import noo.rest.security.AbstractUser;
import noo.rest.security.AuthContext;
import noo.rest.security.SecueHelper;
import noo.rest.security.SecurityFilter;
import noo.util.S;

/**
 * @author qujianjun   troopson@163.com
 * 2018年8月23日 
 */


public class DelegateSecurityFilter extends SecurityFilter {
  
	 
	private DelegateSecuritySetting delegateUs;  
	 
  
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response;  
		
		if (!this.isPassCors(req, resp)) 
			return;  
		
		String method = req.getMethod();
		
		if(HttpMethod.OPTIONS.matches(method)) {
			resp.getWriter().print(0);
			return;
		}
		
		String requrl = req.getRequestURI();
		
		if(delegateUs.isIgnore(requrl)) {
			chain.doFilter(request, response);
			return;
	    }
		
		try {	
			
			if(delegateUs.isLoginUrl(requrl)){
				AbstractUser u = delegateUs.loginByDelegate(req, resp);
				if(u==null) {
					throw new AuthenticateException("用户不存在！");
				}
				this.setupContextOnCheckSuccess(req, resp, u);
				
			}else if(delegateUs.isLogoutUrl(requrl)){	
				AbstractUser u = this.retrieveUser(req, resp); 
				delegateUs.doLogout(req,u); 
			}else { 
			    this.process(requrl, req, resp, chain); 
			}
		}catch(Throwable e){
			e.printStackTrace();
			if(e instanceof SessionTimeoutException) { 
				resp.setStatus(401);
				SecueHelper.writeResponse(resp, e.toString());
			}else if(e instanceof BaseException) {
				resp.setStatus(400);
				SecueHelper.writeResponse(resp, e.toString());  
			}else {
				resp.setStatus(403);
				SecueHelper.writeResponse(resp, new BusinessException(ExpCode.AUTHORIZE,"没有权限访问！").toString());  
			}
		} 
		
	}
	
	
	public void process(String requrl, HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws Exception {
		AbstractUser u = this.retrieveUser(req, resp);
		if (u == null) {
			resp.setStatus(401);
			resp.getWriter().print(new SessionTimeoutException().toString());
			return;
		}
		
		try {	
			if (delegateUs.canAccess(u, requrl)) {
				AuthContext.set(u);
				chain.doFilter(req, resp);
			} else {
				resp.setStatus(403);
				SecueHelper.writeResponse(resp, new BusinessException(ExpCode.AUTHORIZE, "没有权限访问！").toString());
			} 
		}finally {		
			AuthContext.clear();
		}

	}

	// 从request的header中，读取Authorization信息，然后从redis中读取user信息，生成user对象
	private AbstractUser retrieveUser(HttpServletRequest req,HttpServletResponse resp) throws IOException {
		String token = req.getHeader(SecueHelper.HEADER_KEY);
		if (S.isBlank(token)) {
			return null;
		}
		 
		AbstractUser ab =  delegateUs.getUserFromSession(token,req); 
		return ab;
	}
	
	
	protected void setupContextOnCheckSuccess(HttpServletRequest request, HttpServletResponse resp, AbstractUser uobj)
			throws IOException {
		String authkey =  uobj.getToken();
		 
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html;charset=utf-8");  
		resp.addHeader(SecueHelper.HEADER_KEY, authkey); 
		
		delegateUs.initUserSession(authkey, uobj, request); 
		JsonObject respJson = uobj.toResponseJsonObject();
		respJson.put(SecueHelper.HEADER_KEY, authkey); 
		resp.getWriter().print(respJson.encode());
	}


	public DelegateSecuritySetting getDelegateSecuritySetting() {
		return delegateUs;
	}


	public void setDelegateSecuritySetting(DelegateSecuritySetting us) {
		this.delegateUs = us;
	}
	
	
	
}
