/**
 * 
 */
package noo.rest.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import noo.json.JsonObject;
import noo.rest.security.processor.OAuth2Interceptor;
import noo.util.Http;
import noo.util.MD5;
import noo.util.S;

/**
 * @author qujianjun troopson@163.com 2018年10月16日
 */
public class SecueHelper {

	public static final String REDIS_KEY = "noo:session";

	public static final String REDIS_USER_KEY = "noo:user:session";

	public static final String HEADER_KEY = "Authorization";
	
	//Upgrade: websocket
	public static final String WEBSOCKET_KEY = "Upgrade";

	//前端传递过来的，表明是什么端的变量名称
	public static final String CLIENT = "request-client";
	//默认的client值，如果前端没有传递这个值，就是默认值
	public static final String DEFAULT_CLIENT = "web";
	
	
	public static boolean isWebSocket(HttpServletRequest req) {
		String ws = req.getHeader(WEBSOCKET_KEY);
		if("websocket".equalsIgnoreCase(ws))
			return true;
		return false;
	}

	public static void writeResponse(HttpServletResponse resp, String msg) throws IOException {
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html;charset=utf-8");
		resp.getWriter().print(msg);
	}

	public static void updateUser(AbstractUser u,  StringRedisTemplate redis) {
		String ustring = u.toJsonObject().encode();
		String authkey = u.getToken();
		
		 
		redis.executePipelined(new RedisCallback<Object>() {

			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				String client_type= u.getClient();
				String client_uid_key = makeRedisClientUseridKey(u.getClient(), u.getId());
				setEx(connection, SecueHelper.REDIS_KEY + ":" + authkey, u.getSessionTimeoutMinutes(client_type), ustring); 
				setEx(connection, client_uid_key, u.getSessionTimeoutMinutes(client_type), authkey);
				return null;
			}

		});
		// redis.opsForValue().set(SecueHelper.REDIS_KEY+":"+authkey, ustring,
		// u.getSessionTimeoutMinutes(), TimeUnit.MINUTES);
		// redis.opsForValue().set(SecueHelper.REDIS_USER_KEY+":"+u.getId(),authkey,
		// u.getSessionTimeoutMinutes(), TimeUnit.MINUTES);
	}
	
	public static void deleteUserLoginInfo(HttpServletRequest req, StringRedisTemplate redis,SecuritySetting us, String auth) {
		if(S.isBlank(auth))
			return;
		
		String client = getClient(req);
		String sessionkey = SecueHelper.REDIS_KEY + ":" + auth;
		String sessionval = redis.opsForValue().get(sessionkey);
		//System.out.println(sessionval);
		List<String> ls = new ArrayList<>();
		ls.add(sessionkey);
		if(S.isNotBlank(sessionval)) {
			AbstractUser u = us.fromJsonObject(new JsonObject(sessionval));
			String userid = u.getId(); 
			String client_uid_key = makeRedisClientUseridKey(client, userid);  
			ls.add(client_uid_key);
		}
		//System.out.println(ls);
		redis.delete(ls);
		 
	}

	/**
	 * 依据token获取用户对象
	 * @param token
	 * @param us
	 * @param client_type
	 * @param redis
	 * @return
	 */
	public static AbstractUser retrieveUser(String token, SecuritySetting us, String client_type, StringRedisTemplate redis) {
		if (S.isBlank(token))
			return null;

		String rkey = SecueHelper.REDIS_KEY + ":" + token;
		String s = (String) redis.opsForValue().get(rkey);
		if (S.isBlank(s)) {
			return null;
		}

		AbstractUser u = us.fromJsonObject(new JsonObject(s));
		u.setToken(token);
		// 更新redis中的缓存时间  

		// redis.expire(rkey, u.getSessionTimeoutMinutes(), TimeUnit.MINUTES);
		// redis.expire(ukey, u.getSessionTimeoutMinutes(), TimeUnit.MINUTES);

		redis.executePipelined(new RedisCallback<Object>() {

			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				String ukey = makeRedisClientUseridKey(client_type, u.getId());
				expire(connection, rkey, u.getSessionTimeoutMinutes(client_type));
				expire(connection, ukey, u.getSessionTimeoutMinutes(client_type));
				return null;
			}

		});
		
		u.setClient(client_type);

		return u;

	}
	
	
	//获取客户端系统的类型，比如是web还是app还是其他，一种类型的系统，可能不允许重复登录
	public static String getClient(HttpServletRequest req) {
		String client_system = req.getParameter(CLIENT);
		if(S.isBlank(client_system))
			client_system = req.getHeader(CLIENT);
		return client_system;
	}
	
	private static String makeRedisClientUseridKey(String client_type, String userid) {
		String theclient = client_type ==null ? DEFAULT_CLIENT :client_type;
		return SecueHelper.REDIS_USER_KEY + ":" +theclient+":"+ userid;
	}

	/* 获取userid最后一次登录的token信息 */
	public static String getLastLoginTokenOfUser(AbstractUser uobj, StringRedisTemplate redis) {
		String userid = uobj.getId();
		String client_type = uobj.getClient();
		return getLastLoginTokenOfUser(userid, client_type, redis);
	}
	
	
	/* 获取userid最后一次登录的token信息 */
	public static String getLastLoginTokenOfUser(String userid,String client_type, StringRedisTemplate redis) { 
		String ukey = makeRedisClientUseridKey(client_type, userid);
		return redis.opsForValue().get(ukey);
	}
	
	

	public static JsonObject requestOAuthKey(String url, String code, String client_id, String secret,
			String redirecturl) {
		String sign = MD5.encode(code + "" + client_id + "" + secret);
		StringBuilder param = new StringBuilder(OAuth2Interceptor.PARAM_AUTHCODE).append("=").append(code).append("&")
				.append(SecueHelper.CLIENT).append("=").append(client_id).append("&")
				.append(OAuth2Interceptor.PARAM_REDIRECT_URL).append("=").append(redirecturl).append("&")
				.append(OAuth2Interceptor.PARAM_SERVER_SIGN).append("=").append(sign);

		String result = null;
		if (url.toLowerCase().startsWith("https://")) {
			result = Http.httpsPost(url, param.toString());
		} else {
			result = Http.sendPost(url, param.toString());
		}

		if (S.isBlank(result))
			return null;
		else
			return new JsonObject(result);
	}

	public static void setEx(RedisConnection connection, String key, long minute, String val) {
		byte[] keybyte = key.getBytes();
		byte[] value = val.getBytes();
		long second = minute * 60;
		connection.setEx(keybyte, second, value);
	}

	public static void expire(RedisConnection connection, String key, long minute) {
		connection.expire(key.getBytes(), minute * 60);
	}

}
