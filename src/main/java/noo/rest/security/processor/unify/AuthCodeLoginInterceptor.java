/**
 * 
 */
package noo.rest.security.processor.unify;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsUtils;

import noo.json.JsonObject;
import noo.rest.security.AbstractUser;
import noo.rest.security.AuthcodeService;
import noo.rest.security.api.ApiRateLimitPool;
import noo.rest.security.processor.RequestInterceptor;

/**
 * 给APP端提供的登录接口，通过用户名密码，验证成功后，得到授权码，
 * 
 * 换取授权码的操作，由业务系统自行实现
 * 
 * 后面需要统一到web端的接口，目前暂时保留
 *   AuthcodeService.exchangeCode(redis, authcode); 
 * @author qujianjun   troopson@163.com
 * 2018年10月16日 
 */
public class AuthCodeLoginInterceptor extends RequestInterceptor {
 
	public static final Logger log = LoggerFactory.getLogger(AuthCodeLoginInterceptor.class);
	    
	 
	@Autowired
	private ApiRateLimitPool arlp;

	
	@Override
	public boolean process(String requrl, HttpServletRequest req, HttpServletResponse resp)
			throws Exception { 
		
		String method = req.getMethod();
		
		if(!CorsUtils.isCorsRequest(req) || !HttpMethod.POST.matches(method)  || !this.us.is_AuthcodeUrl(requrl) )
			return false; 
		 
		//校验一下访问频次
		this.arlp.checkLimit("authcode",req);
		
		AbstractUser uobj = AuthCommon.verifyNamePasswordToGetUserObj(req, resp, this.us,this.redis);
		if(uobj!=null) {
			this.genAndReturnAuthcodeOnSuccess(req, resp, uobj); 
		}  
		return true;
		 
	}
	
	
	protected void genAndReturnAuthcodeOnSuccess(HttpServletRequest request, HttpServletResponse resp, AbstractUser uobj)
			throws IOException {
		
		String code = AuthcodeService.genAuthcode(redis,uobj.getClient(),uobj);
		
		log.info("generate auth code "+code+ "for user:"+uobj.toJsonObject());
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html;charset=utf-8");  
		resp.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		 
		JsonObject respJson = new JsonObject();
		respJson.put(AuthCommon.AUTHCODE, code); 
		resp.getWriter().print(respJson.encode());
	}
	
	
	

 
 
	
	
	 
	
}
