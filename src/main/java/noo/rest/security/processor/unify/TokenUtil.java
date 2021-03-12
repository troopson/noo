/**
 * 
 */
package noo.rest.security.processor.unify;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import net.dongliu.requests.Requests;
import noo.json.JsonArray;
import noo.json.JsonObject;
import noo.rest.security.SecueHelper;
import noo.util.ID;
import noo.util.S;

/**
 * @author qujianjun   troopson@163.com
 * 2021年3月2日 
 */
public class TokenUtil {

	public static final Logger log = LoggerFactory.getLogger(TokenUtil.class);

	private static final String UNICAS_BIGTOKEN = "cas:";
	//某个用户在24小时内的所有bigtoken值，用户可能会从多个电脑登录，这样存在多个bigToken的可能性
	private static final String UNICAS_USRE_24H_TOKENS = "cas_u24stk:";
	
	public static final String BIGTOKEN_IN_AUTHCODE_USEROBJ ="_bigtoke";
	

	public static int EXPIRED_HOURS = 18;
	
	//===========================================================  
	//以G开头，这种是bigtoken
	public static String createBigToken(String userid,String ip) {
		String uuid = ID.uuid();  
		return "G"+System.currentTimeMillis()+"-"+userid+"-"+uuid;
	}
	
	//以C开头，这种是独立的smalltoken，和bigtoken没有关系，非统一登录生成的
	public static String createSmallToken(String client,String userid) {
		String uuid = ID.uuid();
		return "C"+System.currentTimeMillis()+"-"+client+"-"+userid+"-"+uuid;
	}
	
	//以S开头，这种是统一登录产生的smalltoken，和bigtoken有关系
	public static String createSmallToken(StringRedisTemplate redis,String client,String bigToken) { 
		String uuid = ID.uuid(); 
		String smalltoken = "S"+System.currentTimeMillis()+"-"+client+"-"+uuid+"-"+bigToken;
		appendSmallTokenToBigToken(redis, smalltoken,bigToken);
		return smalltoken;
	}
	
	public static String parseUseridFromBigToken(String big_token) {
		if(big_token==null || !big_token.startsWith("G"))
			return null;
		return big_token.substring(big_token.indexOf("-")+1,big_token.lastIndexOf("-"));
		
	}
	
	public static String parseClientFromSmallToken(String small) {
		if(small==null)
			return null;
		if(small.startsWith("S") || small.startsWith("C"))
			return small.substring(small.indexOf("-")+1,small.indexOf("-", small.indexOf("-")+1));
		else
			return null;
	}
	
	public static String parseBigTokenFromSmallToken(String small) {
		if(small==null)
			return null;
		if(small.startsWith("S") || small.startsWith("C"))
			return small.substring(small.lastIndexOf("-")+1);
		else
			return null;
	}
	
	//===========================================================
	
	//保存bigToken信息到redis
	public static void storeBigTokenInfoInRedis(StringRedisTemplate redis,String big_token, JsonObject uobj) {
		//redis.opsForValue().set(UNIFY_TOKEN_REDIS + unify_token, uobj.encode(), EXPIRED_HOURS, TimeUnit.HOURS); 
		 
		final String userid = parseUseridFromBigToken(big_token);
		BigToken bk = new BigToken(uobj);   
		
		redis.opsForValue().set(UNICAS_BIGTOKEN + big_token, bk.toJson(),  EXPIRED_HOURS*60, TimeUnit.MINUTES);
		String usertokenkey = UNICAS_USRE_24H_TOKENS + userid;
		SetOperations<String, String> setops = redis.opsForSet();  
		if(redis.hasKey(usertokenkey)) { 
			setops.add(usertokenkey, big_token);
			if(redis.getExpire(usertokenkey)==-1)
				redis.expire(usertokenkey,  EXPIRED_HOURS*60, TimeUnit.MINUTES);
		}else {
			setops.add(usertokenkey, big_token);
			redis.expire(usertokenkey,  EXPIRED_HOURS*60, TimeUnit.MINUTES);
		}
	} 
	
	

	//通过bigToken获取用户信息
	public static JsonObject getUserObjByBigToken(StringRedisTemplate redis,String big_token) {
		if(big_token==null)
			return null;
		String value = redis.opsForValue().get(UNICAS_BIGTOKEN + big_token);
		if(S.isBlank(value))
			return null; 
		BigToken bk = new BigToken(value);  
		return bk.getUserObj();
	}
	
	//给bigToken追加一个smallToken
	public static void appendSmallTokenToBigToken(StringRedisTemplate redis,String small_token,String bigToken) { 
		String value = redis.opsForValue().get(UNICAS_BIGTOKEN + bigToken);
		if(S.isBlank(value))
			return; 
		BigToken bk = new BigToken(value);  
		bk.addSmallToken(small_token);
		redis.opsForValue().set(UNICAS_BIGTOKEN + bigToken, bk.toJson(), EXPIRED_HOURS*60, TimeUnit.MINUTES); 
	}
	 

	//移除bigToken信息,并且放回对应smalltokens
	public static void removeStoredBigTokenUserObj(StringRedisTemplate redis,UniCasDefinition ucdf, String big_token) {  
		log.info("--------remove bigToken from redis:"+big_token);
		String userid = parseUseridFromBigToken(big_token);   
		
		String value = redis.opsForValue().get(UNICAS_BIGTOKEN + big_token);
		JsonArray smalltokens = null;
		if(S.isNotBlank(value)) {
			BigToken bk = new BigToken(value);  
			smalltokens = bk.getSmallTokens();
		} 
		 
		redis.delete(UNICAS_BIGTOKEN +big_token);
		//从用户24小时内BigToken中去除该Token
		redis.opsForSet().remove(UNICAS_USRE_24H_TOKENS + userid, big_token);
		if(redis.opsForSet().size(UNICAS_USRE_24H_TOKENS + userid)==0)
			redis.delete(UNICAS_USRE_24H_TOKENS + userid);
		
		if(smalltokens!=null) {
			log.info("--------remove smallToken from redis: "+smalltokens.encode());
			logoutSmallTokens(ucdf, smalltokens);
		}
		
	}

	//将某个用户的所有token全部清除，针对用户信息修改等场景，有可能需要清除用户的登录状态，强制重新登录
	public static void doInvalidUser(StringRedisTemplate redis, String userid) {
		Set<String> bigtokens = redis.opsForSet().members(UNICAS_USRE_24H_TOKENS + userid);
		if(bigtokens==null || bigtokens.isEmpty())
			return;
		bigtokens.add(UNICAS_USRE_24H_TOKENS + userid);
		redis.delete(bigtokens); 
	}
	
	
	public static void logoutSmallTokens(UniCasDefinition ucdf, JsonArray smalltokens) {
		if(smalltokens==null || smalltokens.isEmpty())
			return;
		for(Object small: smalltokens) {
			String stoken = (String)small;
			String client = parseClientFromSmallToken(stoken);
			
			String logout_url = ucdf.getSystemLogoutUrl(client);
			if(S.isNotBlank(logout_url)) {
				//如果logout的url以=结尾，直接将stoken拼接到后面，发送get请求
				if(logout_url.endsWith("=")) {
					logout_url = logout_url+stoken;
					Requests.get(logout_url).send();
					log.info("logout smalltoken: "+stoken);
				}else { 
					//token同时放在header和params中 
					Map<String,String> header = new HashMap<>();
					header.put(SecueHelper.HEADER_KEY, stoken);
					Requests.get(logout_url).headers(header).params(header).send();
					log.info("logout "+logout_url+" smalltoken: "+stoken);
				}
			}
		}
		
	}
	 
	
	
}

class BigToken{
	JsonObject userobj;
	JsonArray smallTokens; 

	public BigToken(JsonObject j) {
		this.userobj = j;
	}
	
	public BigToken(String s) {
		JsonObject j = new JsonObject(s);
		if(j.containsKey("u"))
			this.userobj = j.getJsonObject("u");
		if(j.containsKey("t"))
			this.smallTokens = j.getJsonArray("t");
	}
	

	public String toJson() {
		JsonObject j = new JsonObject();
		if(this.userobj!=null)
			j.put("u", this.userobj);
		if(this.smallTokens!=null)
			j.put("t", this.smallTokens);
		return j.encode();
	}
	
	public void addSmallToken(String stoken) {
		if(this.smallTokens==null)
			this.smallTokens = new JsonArray();
		this.smallTokens.add(stoken);
	}
	
	public JsonObject getUserObj() {
		return this.userobj;
	}
	
	public JsonArray getSmallTokens() {
		return this.smallTokens;
	}
	
	
	
}
