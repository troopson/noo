/**
 * 
 */
package noo.rest.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import noo.json.JsonObject;
import noo.rest.security.processor.unify.TokenUtil;
import noo.util.S;
import noo.util.SpringContext;

/**
 * @author qujianjun troopson@163.com 2018年10月16日
 */
public class SecueHelper {
	
	public static final Logger log = LoggerFactory.getLogger(SecueHelper.class);

	public static final String REDIS_KEY = "tk";

	public static final String REDIS_USER_KEY = "tk:uid";

	public static final String HEADER_KEY = "Authorization";
	
	//Upgrade: websocket
	public static final String WEBSOCKET_KEY = "Upgrade";

	//前端传递过来的，表明是什么端的变量名称
	public static final String CLIENT = "request-client";
	//默认的client值，如果前端没有传递这个值，就是默认值
	public static final String DEFAULT_CLIENT = "none";
	
	
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
	
	public static void writeJsonResponse(HttpServletResponse resp, String msg) throws IOException {
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json;charset=utf-8");
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
		
		String client = TokenUtil.parseClientFromSmallToken(auth);
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
			log.info("token key "+rkey +" is not found in redis");
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
	
	//将某个用户id的登录session设置为过期，可以按照client类型设置
	public static void invalidUser(StringRedisTemplate redis,String userid,String[] client_types) {
		if(redis==null || S.isBlank(userid))
			return;
		List<String> to_delete = new ArrayList<>();
		if(client_types==null)
			client_types = new String[] {DEFAULT_CLIENT}; 
		for(String one : client_types) {
			String userid_key = makeRedisClientUseridKey(one, userid);
			String token = redis.opsForValue().get(userid_key);
			if(S.isNotBlank(token)) {
				String rkey = SecueHelper.REDIS_KEY + ":" + token;
				to_delete.add(userid_key);
				to_delete.add(rkey);
			}
		}
		redis.delete(to_delete); 
		
		Map<String,InfInvalidUser> rh = SpringContext.getBeansOfType(InfInvalidUser.class);
		if(rh !=null) { 
			for(InfInvalidUser i : rh.values()) {
				i.doInvalidUser(redis, userid);
			}
		}
		
	}
	
	
	//获取客户端系统的类型，比如是web还是app还是其他，一种类型的系统，可能不允许重复登录
	public static String getClient(HttpServletRequest req) {
		String client_system = req.getParameter(CLIENT);
		if(S.isBlank(client_system))
			client_system = req.getHeader(CLIENT);
		if(S.isBlank(client_system))
			return DEFAULT_CLIENT;
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
