/**
 * 
 */
package noo.rest.security.processor.unify;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import noo.util.Req;
import noo.util.S;

/**
 * 
 * 登录获取一个authcode，并且返回对应系统的redirectUrl
 * 
 * 一、正常的流程是：
 * 1. 用户在cas系统登录页面输入用户名密码，前端发送到cas系统的登录地址，cas后端校验成功后，获得一个用户对象，用户对象保存到redis中，并返回一个authcode，以及目标系统的跳转地址tourl
 * 2. 前端收到返回的authcode和tourl以后，发送到登录地址，并带上authcode和tourl参数，服务器端将authcode作为参数拼接到tourl地址后，进行转发，转发的同时种下一个大Token cookie
 * 3. tourl对应的第三方服务器通过调用cas系统的接口，交换authcode，cas服务器将用户信息返回给第三方，返回的时候，已经生成了Authorization 小Token
 * 
 * 二、 其他系统，进入cas的登录界面时，如果带有大Token cookie，加载/cas地址时，会当做一个脚本加载，在脚本中返回authcode，这样第三方系统可以直接使用authcode交换 小Token
 * 
 * @author qujianjun troopson@163.com Jul 28, 2020
 */
public class UniCasInterceptor extends RequestInterceptor implements InfInvalidUser{

	public static final Logger log = LoggerFactory.getLogger(AuthCodeLoginInterceptor.class);

	public static final String BIG_TOKEN_COOKIE_NAME = "uni_identify";
	
	public static final String REDIRECT_URL ="tourl";


	@Autowired
	private ApiRateLimitPool arlp;

	@Autowired(required = false)
	private UniCasDefinition definition;

	
	//-------------------------------------------------------------------
	
	private boolean isCasUrl(String path) {
		return path.equals(this.definition.casUrl());
	}
	
	
	@Override
	public void doInvalidUser(StringRedisTemplate redis, String userid) {
		UniCasTokenUtil.doInvalidUser(redis, userid); 
	}
	
	//-------------------------------------------------------------------
	
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
		 * 2. 带authcode和redirect_url的跳转请求，利用该请求生成并设置bigtoken，由于跨域，必须利用转发来种cookie
		 * 3. 首次加载页面时候引入cas JavaScript的请求，利用该请求，判断是否有bigToken，如果有，返回authocode直接登录
		 * 4. 以上都不是，不做任何操作
		 */
		// 如果是POST请求，认为是使用 用户名密码登录 的请求
		
		if (HttpMethod.GET.matches(method)) {
			String bigToken_cookie = this.findBigTokenInCookie(req, resp); 
			
			
			//1. 如果是注销请求，执行注销流程
			String type = req.getParameter("type");
			if("logout".equals(type)) {
				this.doLogout(req, resp, bigToken_cookie);
				return true;
			}  

			//2. 如果是一个跳转的请求, 将cookie对应的用户信息保存下来，下次有对应的cookie，就可以直接跳转了
			String authcode = req.getParameter(AuthCommon.AUTHCODE);
			String redirectUrl = req.getParameter(REDIRECT_URL);
			if(S.isNotBlank(authcode) && S.isNotBlank(redirectUrl)) { 
				//authcode存在，并且指定了转发地址，转发到第三方系统的tourl主界面
				this.createBigTokenAndRedirect(req, resp, authcode, redirectUrl);
				return true;
				
			} 
			//3. 如果是一个页面初始化的脚本请求，有bigToken直接生成一个authcode返回，这样前端进入登录页面以后，不需要登录，直接跳转到目标tourl页面
			if(S.isNotBlank(bigToken_cookie)) {
				 
				JsonObject u = UniCasTokenUtil.getUserObjByBigToken(redis,bigToken_cookie);
				if (u!= null) {  
					//该cookie存在，直接返回一个authcode给前端，前端拿到后直接走后面的处理
					String client = SecueHelper.getClient(req);
					this.genAndReturnAuthcodeOnSuccess(req, resp, client, u, true);
					log.info("find cookie, auto login ");
				}else{
					this.removeBigTokenCookie(resp);
				}
				return true;
			}
			//4. 以上都不是，直接返回
			return true;
		
	    //5. 如果是用户名 密码登录的请求，校验成功后，生成一个authcode，并且查一下client对应的设置，看看有没有tourl，然后将authcode和tourl返回给前端
		}else if (CorsUtils.isCorsRequest(req) && HttpMethod.POST.matches(method)) { 
			// 校验一下访问频次
			this.arlp.checkLimit("unify_login", req);
			
			AbstractUser uobj = AuthCommon.verifyNamePasswordToGetUserObj(req, resp, this.us, this.redis);
			if (uobj != null) {
				JsonObject u = uobj.toJsonObject();
				if(!u.containsKey("userid"))  //生成authcode前，放置一个userid属性，后面方便获取
					u.put("userid", uobj.getId());
				this.genAndReturnAuthcodeOnSuccess(req, resp, uobj.getClient(), u, false); // 生成一个authcode
				log.info("login success, return auth code ok.");
			}
			return true;
		}else {
			return false;
		}

	}

	private void doLogout(HttpServletRequest req, HttpServletResponse resp, String bigToken_cookie) throws IOException {
		// 如果是注销，需要第三方系统从前端发起一个jsonp的logout的请求 
		if (S.isNotBlank(bigToken_cookie)) {
			log.info("logout bigToken:"+bigToken_cookie);
			this.removeBigTokenCookie(resp);
			UniCasTokenUtil.removeStoredBigTokenUserObj(redis,this.definition,bigToken_cookie);
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
	
	
	
	private void createBigTokenAndRedirect(HttpServletRequest req, HttpServletResponse resp, String authcode, String redirectUrl) throws IOException {
		JsonObject uobj = AuthcodeService.readCode(redis, authcode);
		String userid = uobj.getString("userid");
		String ip = Req.getClientIP(req);
		String bigToken = UniCasTokenUtil.createBigToken(userid,ip);
		this.plantBigTokenCookie(resp, bigToken);
		UniCasTokenUtil.storeBigTokenInfoInRedis(redis,bigToken, uobj);  
		//给authcode中保存的用户对象，加上bigToken值
		AuthcodeService.addAttrToAuthCodeUserObj(redis, authcode, UniCasTokenUtil.BIGTOKEN_IN_AUTHCODE_USEROBJ, bigToken);
		
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
	protected String findBigTokenInCookie(HttpServletRequest request, HttpServletResponse resp) throws IOException {
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		for (Cookie c : cookies) {
			if (BIG_TOKEN_COOKIE_NAME.equals(c.getName())) {
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

		

		JsonObject respJson = new JsonObject();
		respJson.put(AuthCommon.AUTHCODE, code);
		if (S.isNotBlank(url))
			respJson.put(REDIRECT_URL, url); 
		 
		if(isOnload) {
			resp.getWriter().print("var auth="+respJson.encode()+"");
		}else { 
			resp.getWriter().print(respJson.encode());
		}
	}

	

	//-------------------------------------------------------------------

	private void plantBigTokenCookie(HttpServletResponse resp, String bigToken) {
		// 设置一个cookie，生成大Token 
		Cookie cookie = new Cookie(BIG_TOKEN_COOKIE_NAME, bigToken);
		cookie.setHttpOnly(true);
		cookie.setPath(this.definition.casUrl());
		cookie.setVersion(1);  
		resp.addCookie(cookie); 
	}

	private void removeBigTokenCookie(HttpServletResponse resp) {
		Cookie c = new Cookie(BIG_TOKEN_COOKIE_NAME, null);
		c.setPath("/");
		c.setMaxAge(0);
		resp.addCookie(c);
	}
 

}
