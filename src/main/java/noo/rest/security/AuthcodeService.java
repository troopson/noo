/**
 * 
 */
package noo.rest.security;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;

import noo.exception.BusinessException;
import noo.json.JsonObject;
import noo.rest.security.processor.unify.UniCasTokenUtil;
import noo.util.MD5;
import noo.util.S;

/**
 * 每次登录成功后，得到一个授权码，而不是直接返回用户信息，授权码有效期5分钟， 通过授权码，可以换取到用户信息，这个换取的过程，由应用实现。
 * 
 * @author qujianjun troopson@163.com Dec 19, 2019
 */

public class AuthcodeService {

	public static int EXPIRED_MINUTES = 3;

	private static final String REDIS_AUTHCODE_PREFIX = "noo:authcode:";

	public static final int AUTHCODE_EXPIRED = 40011;

	// 给某个用户产生一个授权码，3分钟内有效
	public static String genAuthcode(StringRedisTemplate redis, AbstractUser ui) {
		return genAuthcode(redis, ui.toJsonObject()); 
	}

	// 给某个用户产生一个授权码，3分钟内有效
	public static String genAuthcode(StringRedisTemplate redis, JsonObject ui) {
		if (ui == null)
			return null;
		String userinfo = ui.encode();
		String authcode = MD5.encode(UUID.randomUUID().toString());
		redis.opsForValue().set(REDIS_AUTHCODE_PREFIX + authcode, userinfo, EXPIRED_MINUTES, TimeUnit.MINUTES);
		return authcode;
	}
	
	//读取授权码对应的数据，不删除
	public static JsonObject readCode(StringRedisTemplate redis, String authcode) {
		if (S.isBlank(authcode))
			return null;
		
		String userinfo = redis.opsForValue().get(REDIS_AUTHCODE_PREFIX + authcode);
		if (S.isBlank(userinfo))
			return null;
 
		JsonObject j = new JsonObject(userinfo);
		return j;
	}
	
	/*
	 * 更新authcode中保存的用户对象，给用户对象加上一个属性
	 */
	public static void addAttrToAuthCodeUserObj(StringRedisTemplate redis, String authcode, String key, String value) {
		if (S.isBlank(authcode))
			return;
		
		String userinfo = redis.opsForValue().get(REDIS_AUTHCODE_PREFIX + authcode);
		if (S.isBlank(userinfo))
			return;
 
		JsonObject j = new JsonObject(userinfo);
		j.put(key, value);
		redis.opsForValue().set(REDIS_AUTHCODE_PREFIX + authcode, j.encode(), EXPIRED_MINUTES, TimeUnit.MINUTES);
		
	}

	// 用授权码换取用户信息，只能换取一次
	public static JsonObject exchangeCode(StringRedisTemplate redis, String authcode, String client) {
		if (S.isBlank(authcode))
			throw new BusinessException(AUTHCODE_EXPIRED, "无效的授权码!");

		String userinfo = redis.opsForValue().get(REDIS_AUTHCODE_PREFIX + authcode);
		if (S.isBlank(userinfo))
			throw new BusinessException(AUTHCODE_EXPIRED, "无效的授权码!");

		redis.delete(REDIS_AUTHCODE_PREFIX + authcode);
		JsonObject j = new JsonObject(userinfo);
		
		String bigToken =(String) j.remove(UniCasTokenUtil.BIGTOKEN_IN_AUTHCODE_USEROBJ);
		//如果用户存在bigToken，给bigToken关联上对应的smallToken
		if(S.isNotBlank(bigToken)) {
			String small_token = UniCasTokenUtil.createSmallToken(redis,client,bigToken);
			j.put(SecueHelper.HEADER_KEY, small_token);  
			return j;
		}else {
			String userid = j.getString("userid");
			String small_token = UniCasTokenUtil.createSmallToken(userid);
			j.put(SecueHelper.HEADER_KEY, small_token);  
			return j;
		} 

	}

}
