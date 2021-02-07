/**
 * 
 */
package noo.rest.security.sessionStore;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import noo.exception.SessionTimeoutException;
import noo.json.JsonObject;
import noo.rest.security.AbstractUser;
import noo.util.S;

/**
 * @author qujianjun troopson@163.com 2021年2月7日 将session存储在redis中
 * 
 */
public class RedisStorage implements Store {

	public static final Logger log = LoggerFactory.getLogger(RedisStorage.class);

	private StringRedisTemplate redis;
	private String name;
	private String rediskey;

	private int valid_hours = 4;

	private Function<JsonObject, AbstractUser> fun;

	public RedisStorage(String name, StringRedisTemplate redis, Function<JsonObject, AbstractUser> transform) {
		if (S.isBlank(name))
			throw new NullPointerException("RedisStorage name can not be null.");
		this.name = name;
		this.redis = redis;
		this.rediskey = this.name + ".user:";
		this.fun = transform;
	}

	public void setValidHours(int hours) {
		this.valid_hours = hours;
	}

	@Override
	public AbstractUser getUserFromSession(String token, HttpServletRequest request) {
		String rediskey = this.rediskey + token;
		String user = (String) redis.opsForValue().get(rediskey);
		if (S.isBlank(user))
			throw new SessionTimeoutException();
		JsonObject j = new JsonObject(user);
		AbstractUser u = this.fun.apply(j);
		if (u == null)
			throw new SessionTimeoutException();
		redis.expire(rediskey, 4, TimeUnit.HOURS);
		return u;
	}

	@Override
	public void initUserSession(String token, AbstractUser uobj, HttpServletRequest request) {
		JsonObject js = uobj.toJsonObject();
		redis.opsForValue().set(this.rediskey + token, js.encode(), this.valid_hours, TimeUnit.HOURS);
		log.info("登录成功，用户信息：" + js.encode());
	}

	@Override
	public void updateSession(AbstractUser uobj, HttpServletRequest request) {
		JsonObject js = uobj.toJsonObject();
		redis.opsForValue().set(this.rediskey + uobj.getToken(), js.encode(), this.valid_hours, TimeUnit.HOURS);
		log.info("更新用户信息成功：" + js.encode());
	}

	
	@Override
	public void invalidSession(AbstractUser uobj, HttpServletRequest request) {
		if (uobj == null)
			return;
		String token = uobj.getToken();
		redis.delete(this.rediskey + token);

	}

}
