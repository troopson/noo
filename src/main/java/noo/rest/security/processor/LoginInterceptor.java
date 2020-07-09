/**
 * 
 */
package noo.rest.security.processor;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;

import noo.exception.AuthenticateException;
import noo.json.JsonObject;
import noo.rest.security.AbstractUser;
import noo.rest.security.SecueHelper;
import noo.rest.security.delegate.DelegateHttpServletRequest;
import noo.util.ID;
import noo.util.S;

/**
 * @author qujianjun   troopson@163.com
 * 2018年10月16日 
 */
public class LoginInterceptor extends RequestInterceptor {


	public static final String USERNAME="username";
	public static final String PASSWORD="password";
	 
	
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
	
	
	protected void doLogin(HttpServletRequest rawrequest, HttpServletResponse resp) throws IOException {
		HttpServletRequest request = new DelegateHttpServletRequest(rawrequest);
		String u = request.getParameter(USERNAME);
		String p = request.getParameter(PASSWORD);  
		
		if(S.isBlank(u)) {
			SecueHelper.writeResponse(resp, new AuthenticateException("必须有用户名！").toString());   
			return;
		}
		
		AbstractUser uobj = us.loadUserByName(u);
		if(uobj ==null) {
			SecueHelper.writeResponse(resp, new AuthenticateException("用户不存在！").toString());  
			return;
		}
		
		
		String client_type = SecueHelper.getClient(request); 
		
		if(us.checkUserPassword(uobj, p, request) && us.checkClient(u,p,client_type)) {
			
			uobj.setClient(client_type);
			setupContextOnCheckSuccess(request, resp, uobj); 
			
		}else { 
			checkFailed(resp);  
		}
	}


	protected void checkFailed(HttpServletResponse resp) throws IOException {
		resp.setStatus(405);
		SecueHelper.writeResponse(resp, new AuthenticateException("用户名或密码错误！").toString());
	}


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
