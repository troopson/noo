/**
 * 
 */
package noo.rest.security;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.StringRedisTemplate;

import noo.json.JsonObject;
import noo.rest.security.processor.OAuth2Interceptor;
import noo.util.Http;
import noo.util.MD5;
import noo.util.S;

/**
 * @author qujianjun   troopson@163.com
 * 2018年10月16日 
 */
public class SecueHelper {

	public static final String REDIS_KEY ="noo:session";
	
	public static final String HEADER_KEY="Authorization";
	
	
	public static void writeResponse(HttpServletResponse resp,String msg) throws IOException {
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html;charset=utf-8");  
		resp.getWriter().print(msg);
	}
	
	public static void updateUser(AbstractUser u,StringRedisTemplate redis) {
		String ustring = u.toJsonObject().encode(); 
		String authkey =  u.getToken(); 
		redis.opsForValue().set(SecueHelper.REDIS_KEY+":"+authkey, ustring, u.getSessionTimeoutMinutes(), TimeUnit.MINUTES);
	}
	
	public static AbstractUser retrieveUser(String token,SecuritySetting us,StringRedisTemplate redis) {
		if(S.isBlank(token))
			return null;
		
		String s = (String) redis.opsForValue().get(SecueHelper.REDIS_KEY + ":" + token);
		if (S.isBlank(s)) {
			return null;
		}

		AbstractUser u = us.fromJsonObject(new JsonObject(s));
		u.setToken(token);
		return u;
		
	}
	
	public static JsonObject requestOAuthKey(String url,String code, String client_id, String secret, String redirecturl) {
		String sign = MD5.encode(code+""+client_id+""+secret);
		StringBuilder param = new StringBuilder(OAuth2Interceptor.PARAM_AUTHCODE).append("=").append(code).append("&")
				      .append(OAuth2Interceptor.PARAM_CLIENTID_NAME).append("=").append(client_id).append("&")
				      .append(OAuth2Interceptor.PARAM_REDIRECT_URL).append("=").append(redirecturl).append("&")
				      .append(OAuth2Interceptor.PARAM_SERVER_SIGN).append("=").append(sign); 
		
		String result = Http.sendPost(url, param.toString());
		System.out.println(result);
		return new JsonObject(result);
	}
	
	
}
