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
import noo.rest.security.AuthcodeService;
import noo.rest.security.SecueHelper;
import noo.util.ID;
import noo.util.S;

/**
 * @author qujianjun   troopson@163.com
 * 2018年10月16日 
 */
public class AuthCodeLoginInterceptor extends RequestInterceptor {
 
	  
	public static final String AUTHCODELOGIN_URL="/Aclogin";  
	public static final String EXCHANGE_AUTHCODE_URL="/exchangeAc";  
	
	public static final String AUTHCODE ="authcode";
	
	private boolean is_AuthcodeUrl(String requrl) {
		if(requrl.endsWith(AUTHCODELOGIN_URL)) {
			return true;
		}else {
			return false;
		}
	}
	 

	private boolean is_Exchange_AuthcodeUrl(String requrl) {
		if(requrl.endsWith(EXCHANGE_AUTHCODE_URL)) {
			return true;
		}else {
			return false;
		}
	}
	
	@Override
	public boolean process(String requrl, HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		
		String method = req.getMethod();
		
		if(this.is_AuthcodeUrl(requrl) && HttpMethod.POST.matches(method)) {
			//用户名密码登录，得到authcode
			this.checkAndGenAuthcode(req, resp);
			return true;
			
		}else if(this.is_Exchange_AuthcodeUrl(requrl) && HttpMethod.GET.matches(method)){
			//使用authcode换取用户信息
			String authcode = req.getParameter(AUTHCODE);
			this.exchangeAuthCode(resp,authcode);
			return true;
			
		}else {
			return false;
		}
	}
	
	
	protected void checkAndGenAuthcode(HttpServletRequest request, HttpServletResponse resp) throws IOException {
		String u = request.getParameter(LoginInterceptor.USERNAME);
		String p = request.getParameter(LoginInterceptor.PASSWORD); 
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
			genAndReturnAuthcodeOnSuccess(request, resp, uobj); 
			
		}else { 
			checkFailed(resp);  
		}
	}


	protected void checkFailed(HttpServletResponse resp) throws IOException {
		resp.setStatus(405);
		SecueHelper.writeResponse(resp, new AuthenticateException("用户名或密码错误！").toString());
	}


	protected void genAndReturnAuthcodeOnSuccess(HttpServletRequest request, HttpServletResponse resp, AbstractUser uobj)
			throws IOException {
		String authkey =  ID.uuid();
		uobj.setToken(authkey);  
		
		String code = AuthcodeService.genAuthcode(redis, uobj);
		 
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html;charset=utf-8");  
		 
		JsonObject respJson = new JsonObject();
		respJson.put(AUTHCODE, code); 
		resp.getWriter().print(respJson.encode());
	}
	
	
	private void exchangeAuthCode(HttpServletResponse resp, String authcode) throws IOException {
		JsonObject respJson = AuthcodeService.exchangeCode(redis, authcode); 
		resp.getWriter().print(respJson.encode());
	}
	
	
	 
	
}
