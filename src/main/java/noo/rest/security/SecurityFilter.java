/**
 * 
 */
package noo.rest.security;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsProcessor;
import org.springframework.web.cors.DefaultCorsProcessor;

import noo.exception.BusinessException;
import noo.exception.SessionTimeoutException;
import noo.json.JsonObject;
import noo.util.ID;
import noo.util.S;

/**
 * @author qujianjun   troopson@163.com
 * 2018年8月23日 
 */


public class SecurityFilter implements Filter {
  
	
	@Autowired
	private SecuritySetting us;
	
	@Autowired
	private RedisTemplate<String, Object> redis;
	
	public static final String REDIS_KEY ="noo:session";
	
	public static final String HEADER_KEY="Authorization";
	 
	public String username="username";
	public String password="password";
	
	private CorsProcessor processor = new DefaultCorsProcessor();
	
	private CorsConfiguration corsConfiguration = null;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		if(this.corsConfiguration==null) { 
		    this.corsConfiguration = new CorsConfiguration();
		    this.corsConfiguration.addAllowedOrigin("*"); // 1允许任何域名使用
		    this.corsConfiguration.addAllowedHeader("*"); // 2允许任何头
		    this.corsConfiguration.addAllowedMethod("*"); // 3允许任何方法（post、get等）
		    this.corsConfiguration.setAllowCredentials(true);
		}
	}
 

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response; 
		
		this.processor.processRequest(corsConfiguration, req, resp);
		
		String method = req.getMethod();
		
		if(method.equals(HttpMethod.OPTIONS.name())) {
			resp.getWriter().print(0);
			return;
		}
		
		String requrl = req.getRequestURI();
		
		if(us.isIgnore(requrl)) {
			chain.doFilter(request, response);
			return;
	    }
		
		try {	
			if(us.isLoginUrl(requrl) && method.equalsIgnoreCase("POST")) {
			
				this.doLogin(request, req, resp);
				return;
			}else if(us.isLogoutUrl(requrl)) {
				this.doLogout(req, resp);
				return;
			}else {
			
				User u = this.retrieveUser(req, resp);
				if(u==null) {
					resp.setStatus(401);
					resp.getWriter().print(new SessionTimeoutException().toString());
					return;
				}
				
				if(us.canAccess(u, requrl)) {
					AuthContext.set(u); 
					chain.doFilter(request, response);
				}else { 
					resp.setStatus(403);
					this.writeResponse(resp, new BusinessException("403","access denied.").toString());   
				}
				
			}
		}finally {
			AuthContext.clear();
		}
		
	}

	private User retrieveUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String token = req.getHeader(HEADER_KEY);
		if(S.isBlank(token)) { 
			return null;
		}
		
		String s = (String)this.redis.opsForValue().get(REDIS_KEY+":"+token);
		if(S.isBlank(s)) { 
			return null;
		}
		
		User u = us.fromJsonObject(new JsonObject(s));
		return u;
	}

	private void doLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String authkey = req.getHeader(HEADER_KEY);
		if(S.isNotBlank(authkey)) {
			this.redis.opsForValue().set(authkey, null); 
		}
		resp.addHeader(HEADER_KEY, "");
		this.writeResponse(resp, "0");  
	}

	private void doLogin(ServletRequest request, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String u = request.getParameter(this.username);
		String p = request.getParameter(this.password);
		if(S.isBlank(u)) {
			this.writeResponse(resp, new BusinessException("400","必须有用户名！").toString());   
			return;
		}
		
		User uobj = us.loadUserByName(u);
		if(uobj ==null) {
			this.writeResponse(resp, new BusinessException("400","用户不存在！").toString());  
			return;
		}
		
		if(us.checkUserPassword(uobj, p, req)) {
			
			Object ustring = us.toJsonObject(uobj).encode();
			String authkey =  ID.uuid(); 
			this.redis.opsForValue().set(REDIS_KEY+":"+authkey, ustring, 120L, TimeUnit.MINUTES);
			 
			resp.setCharacterEncoding("UTF-8");
			resp.setContentType("text/html;charset=utf-8");  
			resp.addHeader(HEADER_KEY, authkey); 
			
			us.afterLoginSuccess(uobj, req); 
			JsonObject respJson = us.toResponseJsonObject(uobj);
			respJson.put(HEADER_KEY, authkey); 
			resp.getWriter().print(respJson.encode());
			
		}else { 
			this.writeResponse(resp, "-1");  
		}
	}
	
	private void writeResponse(HttpServletResponse resp,String msg) throws IOException {
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html;charset=utf-8");  
		resp.getWriter().print(msg);
	}

	public void setSecuritySetting(SecuritySetting us) {
		this.us = us;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setRedis(RedisTemplate r) {
		this.redis = r;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public CorsConfiguration getCorsConfiguration() {
		return corsConfiguration;
	}

	public void setCorsConfiguration(CorsConfiguration corsConfiguration) {
		this.corsConfiguration = corsConfiguration;
	}

	@Override
	public void destroy() {
		
		
	}
	
 

	
}
