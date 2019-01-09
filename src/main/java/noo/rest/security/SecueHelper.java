/**
 * 
 */
package noo.rest.security;

import java.io.IOException;

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

	public static void writeResponse(HttpServletResponse resp, String msg) throws IOException {
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html;charset=utf-8");
		resp.getWriter().print(msg);
	}

	public static void updateUser(AbstractUser u, String client_type, StringRedisTemplate redis) {
		String ustring = u.toJsonObject().encode();
		String authkey = u.getToken();
		
		 
		redis.executePipelined(new RedisCallback<Object>() {

			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				setEx(connection, SecueHelper.REDIS_KEY + ":" + authkey, u.getSessionTimeoutMinutes(), ustring); 
				String client_uid_key = makeRedisClientUseridKey(client_type, u.getId());
				setEx(connection, client_uid_key, u.getSessionTimeoutMinutes(), authkey);
				return null;
			}

		});
		// redis.opsForValue().set(SecueHelper.REDIS_KEY+":"+authkey, ustring,
		// u.getSessionTimeoutMinutes(), TimeUnit.MINUTES);
		// redis.opsForValue().set(SecueHelper.REDIS_USER_KEY+":"+u.getId(),authkey,
		// u.getSessionTimeoutMinutes(), TimeUnit.MINUTES);
	}

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

		String ukey = makeRedisClientUseridKey(client_type, u.getId());

		// redis.expire(rkey, u.getSessionTimeoutMinutes(), TimeUnit.MINUTES);
		// redis.expire(ukey, u.getSessionTimeoutMinutes(), TimeUnit.MINUTES);

		redis.executePipelined(new RedisCallback<Object>() {

			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				expire(connection, rkey, u.getSessionTimeoutMinutes());
				expire(connection, ukey, u.getSessionTimeoutMinutes());
				return null;
			}

		});

		return u;

	}
	
	//获取客户端系统的类型，比如是web还是app还是其他，一种类型的系统，可能不允许重复登录
	public static String getClient_type(HttpServletRequest req) {
		String client_system = req.getParameter("client_type");
		if(S.isBlank(client_system))
			client_system = req.getHeader("client_type");
		return client_system;
	}
	
	private static String makeRedisClientUseridKey(String client_type, String userid) {
		String theclient = client_type ==null ? "web":client_type;
		return SecueHelper.REDIS_USER_KEY + ":" +theclient+":"+ userid;
	}

	/* 获取userid对应的token信息 */
	public static String getTokenByUserid(String userid, String client_type, StringRedisTemplate redis) {
		String ukey = makeRedisClientUseridKey(client_type, userid);
		return redis.opsForValue().get(ukey);
	}

	public static JsonObject requestOAuthKey(String url, String code, String client_id, String secret,
			String redirecturl) {
		String sign = MD5.encode(code + "" + client_id + "" + secret);
		StringBuilder param = new StringBuilder(OAuth2Interceptor.PARAM_AUTHCODE).append("=").append(code).append("&")
				.append(OAuth2Interceptor.PARAM_CLIENTID_NAME).append("=").append(client_id).append("&")
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
