/**
 * 
 */
package noo.rest.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.StringRedisTemplate;

import noo.exception.BusinessException;
import noo.exception.ExpCode;
import noo.exception.SessionTimeoutException;
import noo.rest.security.processor.unify.TokenUtil;
import noo.util.S;

/**
 * @author qujianjun troopson@163.com 2018年10月16日
 */

public class UsualHandler  {
 
	private SecuritySetting us;
	 
	private StringRedisTemplate redis;

	public UsualHandler(SecuritySetting us,StringRedisTemplate redis) {
		this.us = us;
		this.redis = redis;
	} 
	
	 
	public void process(String requrl, HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws Exception {
		AbstractUser u = this.retrieveUser(req, resp);
		if (u == null) {
			resp.setStatus(401);
			resp.getWriter().print(new SessionTimeoutException().toString());
			return;
		}
		
		try {	
			if (us.canAccess(u, requrl)) {
				AuthContext.set(u);
				AuthContext.setReq(req);
				chain.doFilter(req, resp);
			} else {
				resp.setStatus(403);
				SecueHelper.writeResponse(resp, new BusinessException(ExpCode.AUTHORIZE, "没有权限访问！").toString());
			} 
		}finally {		
			AuthContext.clear();
		}

	}

	// 从request的header中，读取Authorization信息，然后从redis中读取user信息，生成user对象
	private AbstractUser retrieveUser(HttpServletRequest req,HttpServletResponse resp) throws IOException {
		String token = req.getHeader(SecueHelper.HEADER_KEY);
		if (S.isBlank(token) && SecueHelper.isWebSocket(req)) { 
			token = req.getHeader("Sec-WebSocket-Protocol");  
			resp.setHeader("Sec-WebSocket-Protocol", token);
		}
		if (S.isBlank(token)) { 
			return null;
		} 
		
		String client_type = TokenUtil.parseClientFromSmallToken(token);
		
		//String client_type = SecueHelper.getClient(req);
		AbstractUser ab =  SecueHelper.retrieveUser(token, us, client_type,redis);  
		return ab;
	}
	
	
	

}
