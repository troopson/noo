/**
 * 
 */
package noo.rest.security.processor;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import noo.exception.AuthenticateException;
import noo.json.JsonObject;
import noo.rest.security.AbstractUser;
import noo.rest.security.SecueHelper;
import noo.rest.security.api.ApiRateLimitPool;
import noo.rest.security.delegate.DelegateHttpServletRequest;
import noo.rest.security.processor.unify.AuthcodeCommon;
import noo.util.ID;
import noo.util.S;

/**
 * @author qujianjun   troopson@163.com
 * 2018年10月16日 
 */
public class LoginInterceptor extends RequestInterceptor {


	public static final String USERNAME="username";
	public static final String PASSWORD="password";
	
	@Autowired
	private ApiRateLimitPool arlp;
	
	
	
	
	@Override
	public boolean process(String requrl, HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		
		String method = req.getMethod();
		
		if(us.isLoginUrl(requrl) && HttpMethod.POST.matches(method)) {
			
			this.doLogin(req, resp);
			return true;
		}else {
			return false;
		}
	}
	
//	public void rateLimit() {
//		RateLimiter rl = new RateLimiter();
//	}
	
	protected void doLogin(HttpServletRequest rawrequest, HttpServletResponse resp) throws IOException {
		
		this.arlp.checkLimit("login",rawrequest);
		
		HttpServletRequest request = new DelegateHttpServletRequest(rawrequest);
		String u = request.getParameter(USERNAME);
		String p = request.getParameter(PASSWORD);  
		String ac = request.getParameter(AuthcodeCommon.AUTHCODE);  
		 
		
		if(S.isNotBlank(u)) {
			this.loginByUsernamePassword(request, resp, u, p);
		}else if(S.isNotBlank(ac)) {
			this.loginByAuthCode(request, resp, ac);
		}else {
			SecueHelper.writeResponse(resp, new AuthenticateException("必须有用户名！").toString()); 
		} 
		
	}
	
	//通过用户名密码登录
	private void loginByUsernamePassword(HttpServletRequest request, HttpServletResponse resp, String username, String password) throws IOException {
		AbstractUser uobj = us.loadUserByName(username);
		if(uobj ==null) {
			SecueHelper.writeResponse(resp, new AuthenticateException("用户不存在！").toString());  
			return;
		}
		
		
		String client_type = SecueHelper.getClient(request); 
		
		if(us.checkUserPassword(uobj, password, request) && us.checkClient(uobj,client_type)) {
			
			uobj.setClient(client_type);
			setupContextOnCheckSuccess(request, resp, uobj); 
			
		}else { 
			resp.setStatus(405);
			SecueHelper.writeResponse(resp, new AuthenticateException("用户名或密码错误！").toString());
		}
	}
	
	//通过授权码登录
	private void loginByAuthCode(HttpServletRequest request, HttpServletResponse resp, String authcode) throws IOException {
		AbstractUser uobj = us.loadUserByAuthCode(authcode);
		if(uobj ==null) {
			SecueHelper.writeResponse(resp, new AuthenticateException("无效的授权码！").toString());  
			return;
		}
		
		String client_type = SecueHelper.getClient(request); 
		
		if(us.checkClient(uobj,client_type)) { 
			
			uobj.setClient(client_type);
			setupContextOnCheckSuccess(request, resp, uobj); 
			
		}else { 
			resp.setStatus(405);
			SecueHelper.writeResponse(resp, new AuthenticateException("授权码错误！").toString());
		}
	}

 

	//登录成功后的处理
	protected void setupContextOnCheckSuccess(HttpServletRequest request, HttpServletResponse resp, AbstractUser uobj)
			throws IOException {
		String authkey =  ID.uuid();
		uobj.setToken(authkey);  
		
		SecueHelper.updateUser(uobj,this.redis);
		 
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html;charset=utf-8");  
		resp.addHeader(SecueHelper.HEADER_KEY, authkey); 
		
		us.afterLoginSuccess(uobj, request); 
		JsonObject respJson = uobj.toResponseJsonObject();
		respJson.put(SecueHelper.HEADER_KEY, authkey); 
		resp.getWriter().print(respJson.encode());
	}
	 
	 
	
}
