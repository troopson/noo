/**
 * 
 */
package noo.rest.security.processor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import noo.exception.AuthenticateException;
import noo.rest.security.SecueHelper;
import noo.util.Http;
import noo.util.ID;
import noo.util.S;

/**
 * @author qujianjun   troopson@163.com
 * 2018年10月16日 
 */
public class AccessCodeInterceptor extends RequestInterceptor {


	public static String ACCESSKEY="accessKey";
	public static String SECRET="accessSecret";
	public static String CODE = "code";
	
	
	public static String TOKENURL="/refresh_code";
	public static String CHECK_TOKENURL="/check_code";
	
	public static String TOKEN_PREFIX = "noo.access.code:";
	   
	private Function<String,String> secret_load; //load secret of key
	
	@Override
	public boolean process(String requrl, HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		  
		if(this.is_RefreshTokenUrl(requrl)) { 
			this.retrieveCode(req, resp);
			return true;
		}else if(this.is_CheckTokenUrl(requrl)) {
			String code = req.getParameter(CODE);
			String s = this.redis.opsForValue().get(TOKEN_PREFIX+code);
			if(S.isNotBlank(s))
				SecueHelper.writeResponse(resp, "true");
			else
				SecueHelper.writeResponse(resp, "false");
			return true;
	    }else {
			return false;
		}
	}
	
	private boolean is_RefreshTokenUrl(String requrl) {
		if(requrl.endsWith(TOKENURL)) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean is_CheckTokenUrl(String requrl) {
		if(requrl.endsWith(CHECK_TOKENURL)) {
			return true;
		}else {
			return false;
		}
	} 
	
	protected void retrieveCode(HttpServletRequest request, HttpServletResponse resp) throws IOException {
		String key = request.getParameter(ACCESSKEY);
		String secret = request.getParameter(SECRET); 
		if(secret_load!=null && S.isNotBlank(key) && S.isNotBlank(secret)){
			if(!secret.equals(this.secret_load.apply(key))) {
				throw new AuthenticateException("access key secret错误！");
			}else { 				  
				SecueHelper.writeResponse(resp, this.genCode()); 
			}
		}else {
			throw new AuthenticateException("必须有access申请信息！");  
		}  
	}
	
	 
	private String genCode() {
		String id = ID.uuid();
		this.redis.opsForValue().set(TOKEN_PREFIX+id, "0", 3600L, TimeUnit.SECONDS);
		return id;
	} 
 

	public Function<String, String> getSecret_load() {
		return secret_load;
	}

	public void setSecret_load(Function<String, String> secret_load) {
		this.secret_load = secret_load;
	}
	

	public static String getCode(String url, String accessKey, String accessSecret) {
		String result = null;
		String param  = ACCESSKEY+"="+accessKey+"&"+SECRET+"="+accessSecret;
		if(url.toLowerCase().startsWith("https://")) {
			result = Http.httpsGet(url,param);
		}else {
			result = Http.sendGet(url, param);
		}
		return result;
		 
	}
	
	public static boolean checkCode(String url, String code) {
		String param  = CODE+"="+code;
		String result = null;
		if(url.toLowerCase().startsWith("https://")) {
			result = Http.httpsGet(url,param);
		}else {
			result = Http.sendGet(url, param);
		}
		if("true".equals(result))
			return true;
		else
			return false;
	}

	
	
	 
	
}
