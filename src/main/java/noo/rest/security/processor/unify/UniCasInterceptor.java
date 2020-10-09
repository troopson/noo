/**
 * 
 */
package noo.rest.security.processor.unify;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsUtils;

import noo.json.JsonObject;
import noo.rest.security.AbstractUser;
import noo.rest.security.AuthcodeService;
import noo.rest.security.InfInvalidUser;
import noo.rest.security.SecueHelper;
import noo.rest.security.api.ApiRateLimitPool;
import noo.rest.security.processor.RequestInterceptor;
import noo.util.ID;
import noo.util.S;

/**
 * 
 * 登录获取一个authcode，并且返回对应系统的redirectUrl
 * 
 * @author qujianjun troopson@163.com Jul 28, 2020
 */
public class UniCasInterceptor extends RequestInterceptor implements InfInvalidUser{

	public static final Logger log = LoggerFactory.getLogger(AuthCodeLoginInterceptor.class);

	public static final String UNIFY_TOKEN_REDIS = "unify_token:";
	public static final String UNIFY_TOKEN_USREID_REDIS = "unify_token_userid:";

	public static final String COOKIENAME = "uni_identify";
	
	public static final String REDIRECT_URL ="tourl";

	public static final int EXPIRED_HOURS = 24;

	@Autowired
	private ApiRateLimitPool arlp;

	@Autowired(required = false)
	private UniCasDefinition definition;

	@Override
	public boolean process(String requrl, HttpServletRequest req, HttpServletResponse resp) throws Exception {

		if (definition == null)
			throw new NullPointerException("Can't find UniCasDefinition Object.");
		
		if(!this.isCasUrl(requrl))
			return false;
		
		String method = req.getMethod();  

		//如果登录的链接是一个GET请求，认为是种cookie的请求
		/*
		 * 依据参数，区分为如下几种：
		 * 1. 登出的jsonp操作
		 * 2. 带authcode和redirect_url的跳转请求，利用该请求种一个cookie
		 * 3. 首次加载页面时候引入cas JavaScript的请求，利用该请求，判断是否有cookie，如果有，返回authocode直接登录
		 * 4. 以上都不是，不做任何操作
		 */
		if (HttpMethod.GET.matches(method)) {
			String cookie_identify = this.findCookie(req, resp); 
			
			
			//1. 如果是注销的，执行注销流程
			String type = req.getParameter("type");
			if("logout".equals(type)) {
				this.doLogout(req, resp, cookie_identify);
				return true;
			}  

			//2. 如果是一个跳转的请求, 将cookie对应的用户信息保存下来，下次有对应的cookie，就可以直接跳转了
			String authcode = req.getParameter(AuthcodeCommon.AUTHCODE);
			String redirectUrl = req.getParameter(REDIRECT_URL);
			if(S.isNotBlank(authcode) && S.isNotBlank(redirectUrl)) { 
				//如果已经有cookie了，这里会更新这个cookie的值
				this.doRedirect(req, resp, cookie_identify, authcode, redirectUrl);
				return true;
				
			} 
			//3. 以上参数都没有，当作页面初始化脚本的请求，如果有cookie，直接生成一个authcode返回，这样前端不需要登录
			if(S.isNotBlank(cookie_identify)) {
				 
				JsonObject u = this.loadUserObjByCookie(cookie_identify);
				if (u!= null) {  
					String client = SecueHelper.getClient(req);
					this.genAndReturnAuthcodeOnSuccess(req, resp, client, u, true);
					log.info("find cookie, auto login ");
				}else{
					this.removeCookie(resp);
				}
			}
			return true;
			
		}else if (CorsUtils.isCorsRequest(req) && HttpMethod.POST.matches(method)) {
			// 登录相关 
			// 校验一下访问频次
			this.arlp.checkLimit("unify_login", req);
			
			AbstractUser uobj = AuthcodeCommon.checkAndGetUserObj(req, resp, this.us, this.redis);
			if (uobj != null) {
				JsonObject u = uobj.toJsonObject();
				//this.persistCookieUserObj(cookie_identify, u);  // 保存到redis中，过期时间比较长，
				this.genAndReturnAuthcodeOnSuccess(req, resp, uobj.getClient(), u, false); // 生成一个authcode
				log.info("login success, return auth code ok.");
			}
			return true;
		}else {
			return false;
		} 

	}

	private void doLogout(HttpServletRequest req, HttpServletResponse resp, String cookie_identify) throws IOException {
		// 如果是注销，需要第三方系统从前端发起一个jsonp的logout的请求 
		if (S.isNotBlank(cookie_identify)) {
			this.removeCookie(resp);
			this.removeCookieUserObj(cookie_identify);
		}
		String loginpage = req.getParameter("loginpage");
	
		if(S.isNotBlank(loginpage)) {
			loginpage = this.addRequestParams(req, loginpage);
			SecueHelper.writeResponse(resp,"<!DOCTYPE html><html><body onload=\"window.location.href='"+loginpage+"'\">跳转登录页中...</body></html>"); 
		}else
			SecueHelper.writeResponse(resp,"<!DOCTYPE html><html><body>登出成功!</body></html>");
	}
	
	//将logout请求中携带的参数，都添加到loginpage的url后面
	private String addRequestParams(HttpServletRequest req,String loginpage) throws UnsupportedEncodingException {
		Enumeration<String> e = req.getParameterNames();
		StringBuilder sb = null;
		while(e.hasMoreElements()) {
			String pn=e.nextElement();
			if(!pn.equals("type") && !pn.equals("loginpage")) {
				String value = req.getParameter(pn);
				if(S.isBlank(value))
					continue;
				value = URLEncoder.encode(value, "UTF-8");
				if(sb==null)
				    sb=new StringBuilder(pn).append("=").append(value);
				else 
					sb.append("&").append(pn).append("=").append(value);
			}	
		}
		if(sb!=null)
			loginpage = loginpage+ (loginpage.indexOf("?")==-1?"?":"&") + sb.toString();
		return loginpage;
	}
	
	private void doRedirect(HttpServletRequest req, HttpServletResponse resp, String cookie_identify, String authcode, String redirectUrl) throws IOException {
		String uni_cookie = this.plantCookie(resp);
		JsonObject uobj = AuthcodeService.readCode(redis, authcode);
		this.persistCookieUserObj(uni_cookie, uobj);  
		
		String destUrl = URLDecoder.decode(redirectUrl, "utf-8");
		destUrl =destUrl + (destUrl.indexOf("?")==-1?"?":"&") +"authcode="+authcode;
		resp.sendRedirect(destUrl); 
		
	}

	/**
	 * 通过cookie，获取用户信息，如果有，直接使用该用户信息去登录
	 * 
	 * @param request
	 * @param resp
	 * @return
	 * @throws IOException
	 */
	protected String findCookie(HttpServletRequest request, HttpServletResponse resp) throws IOException {
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		for (Cookie c : cookies) {
			if (COOKIENAME.equals(c.getName())) {
				String cookie_token = c.getValue();
				if (S.isNotBlank(cookie_token))
					return cookie_token;
				else
					return null;
			}
		}
		return null;
	}

	protected void genAndReturnAuthcodeOnSuccess(HttpServletRequest request, HttpServletResponse resp, String client, JsonObject uobj, boolean isOnload) throws IOException {

		String url = client == null ? null : this.definition.getSystemRedirectUrl(client);

		String code = AuthcodeService.genAuthcode(redis, uobj);

		log.info("generate auth code " + code + "for user:" + uobj);
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html;charset=utf-8");

		// String authkey = ID.uuid();
		// uobj.put(SecueHelper.HEADER_KEY, authkey);

		JsonObject respJson = new JsonObject();
		respJson.put(AuthcodeCommon.AUTHCODE, code);
		if (S.isNotBlank(url))
			respJson.put(REDIRECT_URL, url); 
		 
		if(isOnload) {
			resp.getWriter().print("var auth="+respJson.encode()+"");
		}else { 
			resp.getWriter().print(respJson.encode());
		}
	}
	
	//-------------------------------------------------------------------
	
	private boolean isCasUrl(String path) {
		return path.equals(this.definition.casUrl());
	}
	

	//-------------------------------------------------------------------

	private String plantCookie(HttpServletResponse resp) {
		// 设置一个cookie
		String unify_token = ID.uuid();
		Cookie cookie = new Cookie(COOKIENAME, unify_token);
		cookie.setHttpOnly(true);
		cookie.setPath(this.definition.casUrl());
		cookie.setVersion(1);  
		resp.addCookie(cookie);
		return unify_token;
	}

	private void removeCookie(HttpServletResponse resp) {
		Cookie c = new Cookie(COOKIENAME, null);
		c.setPath("/");
		c.setMaxAge(0);
		resp.addCookie(c);
	}
	
	//==============================================
	
	private void persistCookieUserObj(String unify_token, JsonObject uobj) {
		//redis.opsForValue().set(UNIFY_TOKEN_REDIS + unify_token, uobj.encode(), EXPIRED_HOURS, TimeUnit.HOURS);
		
		String id = uobj.getString("id");
		if(S.isBlank(id))
			id = uobj.getString("userid");
		final String userid = id;
		redis.executePipelined(new RedisCallback<Object>() { 
			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				SecueHelper.setEx(connection,UNIFY_TOKEN_REDIS + unify_token, EXPIRED_HOURS*60, uobj.encode());
				SecueHelper.setEx(connection,UNIFY_TOKEN_USREID_REDIS + userid, EXPIRED_HOURS*60, unify_token); 
				return null;
			}

		});
		
	}

	private JsonObject loadUserObjByCookie(String unify_token) {
		if(unify_token==null)
			return null;
		String value = redis.opsForValue().get(UNIFY_TOKEN_REDIS + unify_token);
		if(S.isBlank(value))
			return null;
		return new JsonObject(value);
	}

	private void removeCookieUserObj(String unify_token) {
		redis.delete(UNIFY_TOKEN_REDIS +unify_token);
	}
	
	 

	public void doInvalidUser(StringRedisTemplate redis, String userid) {
		String unify_token = redis.opsForValue().get(UNIFY_TOKEN_USREID_REDIS + userid);
		if(S.isNotBlank(unify_token)) {
			List<String> a =new ArrayList<>();
			a.add(UNIFY_TOKEN_USREID_REDIS + userid);
			a.add(UNIFY_TOKEN_REDIS + unify_token);
			redis.delete(a);
		}
	}
	

}
