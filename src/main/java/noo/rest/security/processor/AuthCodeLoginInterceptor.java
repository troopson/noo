/**
 * 
 */
package noo.rest.security.processor;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import noo.exception.AuthenticateException;
import noo.json.JsonObject;
import noo.rest.security.AbstractUser;
import noo.rest.security.AuthcodeService;
import noo.rest.security.SecueHelper;
import noo.rest.security.delegate.DelegateHttpServletRequest;
import noo.util.ID;
import noo.util.S;

/**
 * 完成的两个功能
 * 1. 登录验证后，得到授权码， 
 * 换取授权码的操作，由业务系统自行实现
 *   AuthcodeService.exchangeCode(redis, authcode); 
 * @author qujianjun   troopson@163.com
 * 2018年10月16日 
 */
public class AuthCodeLoginInterceptor extends RequestInterceptor {
 
	public static final Logger log = LoggerFactory.getLogger(AuthCodeLoginInterceptor.class);
	    
	
	public static final String AUTHCODE ="authcode"; 
	 
	
	@Override
	public boolean process(String requrl, HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		 
		
		String method = req.getMethod();
		
		if(AuthcodeService.is_AuthcodeUrl(requrl) && HttpMethod.POST.matches(method)) {
			//用户名密码登录，得到authcode
			this.checkAndGenAuthcode(req, resp);
			return true; 
			
		}else {
			return false;
		}
	}
	
	
	protected void checkAndGenAuthcode(HttpServletRequest rawrequest, HttpServletResponse resp) throws IOException {
		HttpServletRequest request = new DelegateHttpServletRequest(rawrequest);
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
		
		log.info("generate auth code "+code+ "for user:"+uobj.toJsonObject());
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html;charset=utf-8");  
		resp.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		 
		JsonObject respJson = new JsonObject();
		respJson.put(AUTHCODE, code); 
		resp.getWriter().print(respJson.encode());
	}
	
 
	
	
	 
	
}
