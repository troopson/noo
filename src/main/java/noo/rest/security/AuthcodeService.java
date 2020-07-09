/**
 * 
 */
package noo.rest.security;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;

import noo.exception.BusinessException;
import noo.json.JsonObject;
import noo.util.MD5;
import noo.util.S;

/**
 * 每次登录成功后，得到一个授权码，而不是直接返回用户信息，授权码有效期5分钟，
 * 通过授权码，可以换取到用户信息，这个换取的过程，由应用实现。
 * 
 * @author qujianjun   troopson@163.com
 * Dec 19, 2019 
 */

public class AuthcodeService { 

	public static int EXPIRED_MINUTES = 3;
	
	private static final String REDIS_AUTHCODE_PREFIX="noo:authcode:";  

	public static final int AUTHCODE_EXPIRED = 40011;
	
 
	
	//给某个用户产生一个授权码，3分钟内有效
	public static String genAuthcode(StringRedisTemplate redis,AbstractUser ui) { 
		if(ui==null)
			return null;
		String userinfo = ui.toJsonObject().encode();
		String authcode = MD5.encode(UUID.randomUUID().toString());
		redis.opsForValue().set(REDIS_AUTHCODE_PREFIX+authcode, userinfo, EXPIRED_MINUTES, TimeUnit.MINUTES);
		return authcode; 
		
	}
	
	//用授权码换取用户信息，只能换取一次
	public static JsonObject exchangeCode(StringRedisTemplate redis,String authcode) {
		if(S.isBlank(authcode))
			throw new BusinessException(AUTHCODE_EXPIRED,"无效的授权码!");
		
		String userinfo = redis.opsForValue().get(REDIS_AUTHCODE_PREFIX+authcode);
		if(S.isBlank(userinfo))
			throw new BusinessException(AUTHCODE_EXPIRED,"无效的授权码!");
		
		redis.delete(REDIS_AUTHCODE_PREFIX+authcode);
		JsonObject j = new JsonObject(userinfo); 
		return j;
		
	}
	
	
	public static final String AUTHCODELOGIN_URL="/acode_login";    
	
	public static boolean is_AuthcodeUrl(String requrl) {
		if(requrl.matches(AUTHCODELOGIN_URL)) {
			return true;
		}else {
			return false;
		}
	}
	
}
