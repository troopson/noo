/**
 * 
 */
package noo.rest.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsProcessor;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.DefaultCorsProcessor;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import noo.exception.BaseException;
import noo.exception.BusinessException;
import noo.exception.ExpCode;
import noo.exception.SessionTimeoutException;
import noo.rest.security.processor.RequestInterceptor;
import noo.util.SpringContext;

/**
 * @author qujianjun   troopson@163.com
 * 2018年8月23日 
 */


public class SecurityFilter implements Filter {
  
	//public static final Logger log = LoggerFactory.getLogger(SecurityFilter.class);
	 
	private final UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
	
	protected final CorsProcessor corsProcessor = new DefaultCorsProcessor();
	
	protected SecuritySetting us; 

	private StringRedisTemplate redis;  
	 
	private UsualHandler usualprocess;
	 
	private List<RequestInterceptor> requestHandler; 

	public CorsConfiguration buildDefaultCorsConfig() {
		CorsConfiguration ccf = new CorsConfiguration();
		ccf.addAllowedOrigin("*"); // 1允许任何域名使用
		ccf.addAllowedHeader("*"); // 2允许任何头
		ccf.addAllowedMethod("*"); // 3允许任何方法（post、get等）
		ccf.setAllowCredentials(true); 
		return ccf;
	}

	public void registCorsConfiguration(String path, CorsConfiguration corsConfiguration) {
		this.configSource.registerCorsConfiguration(path, corsConfiguration);
	} 
	
	public void setCorsConfiguration(CorsConfiguration corsConfiguration) {
		this.registCorsConfiguration("/**", corsConfiguration);  
	}
	
  
	protected boolean isPassCors(HttpServletRequest req,HttpServletResponse resp) throws IOException {
		if (CorsUtils.isCorsRequest(req)) {
			CorsConfiguration corsConfiguration = this.configSource.getCorsConfiguration(req);
			if (corsConfiguration != null) {
				boolean isValid = this.corsProcessor.processRequest(corsConfiguration, req, resp);
				if (!isValid || CorsUtils.isPreFlightRequest(req)) {
					return false;
				}
			}
		} 
		return true;
	}



	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response;   
		
		if (!this.isPassCors(req, resp)) 
			return; 
		
		String requrl = req.getRequestURI(); 
		
		if(us.isIgnore(requrl)) {
			chain.doFilter(request, response);
			return;
	    }
		 
		
		try {	
			
			boolean handled = false;
			List<RequestInterceptor> rh = this.getInterceptors();
			for(RequestInterceptor h : rh) {
				handled = h.process(requrl, req, resp);
				if(handled)
					break;
			}
			
			if(!handled) { 
			    this.doUsualHandler(requrl, req, resp, chain); 
			}
		}catch(Throwable e){
			e.printStackTrace();
			if(e instanceof SessionTimeoutException) { 
				resp.setStatus(401);
				SecueHelper.writeResponse(resp, e.toString());
			}else if(e instanceof BaseException) {
				resp.setStatus(400);
				SecueHelper.writeResponse(resp, e.toString());  
			}else {
				resp.setStatus(403);
				SecueHelper.writeResponse(resp, new BusinessException(ExpCode.AUTHORIZE,"没有权限访问！").toString());  
			}
		} 
		
	}
	
	
	private List<RequestInterceptor> getInterceptors(){
		if(this.requestHandler==null) {
			this.requestHandler = new ArrayList<>();
			Map<String,RequestInterceptor> rh = SpringContext.getBeansOfType(RequestInterceptor.class);
			if(rh !=null) { 
				for(RequestInterceptor i : rh.values()) {
					i.setSecuritySetting(this.us);
					i.setRedis(this.redis);
					this.requestHandler.add(i);
				}
			}
		}
		return this.requestHandler;
	}
	
	private void doUsualHandler(String requrl, HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws Exception {
		if(this.usualprocess == null)
			this.usualprocess = new UsualHandler(this.us,this.redis);
		
		this.usualprocess.process(requrl, req, resp, chain);

	}
 


	public void setSecuritySetting(SecuritySetting us) {
		this.us = us;
	}


	public void setRedis(StringRedisTemplate redis) {
		this.redis = redis;
	}

	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	} 

	@Override
	public void destroy() {
		
		
	}
	
 

	
}
