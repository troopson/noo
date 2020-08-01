/**
 * 
 */
package noo.rest.security.processor.unify;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import noo.exception.AuthenticateException;
import noo.rest.security.AbstractUser;
import noo.rest.security.SecueHelper;
import noo.rest.security.SecuritySetting;
import noo.rest.security.delegate.DelegateHttpServletRequest;
import noo.rest.security.processor.LoginInterceptor;
import noo.util.ID;
import noo.util.S;

/**
 * @author qujianjun troopson@163.com Jul 28, 2020
 */
public class AuthcodeCommon {
	
	public static final Logger log = LoggerFactory.getLogger(AuthcodeCommon.class);

	public static final String AUTHCODE = "authcode";

	/**
	 * 校验手机号、密码是否正确
	 * @param rawrequest
	 * @param resp
	 * @throws IOException
	 */
	public static AbstractUser checkAndGetUserObj(HttpServletRequest rawrequest, HttpServletResponse resp, SecuritySetting us, StringRedisTemplate redis) throws IOException {

		HttpServletRequest request = new DelegateHttpServletRequest(rawrequest);
		String u = request.getParameter(LoginInterceptor.USERNAME);
		String p = request.getParameter(LoginInterceptor.PASSWORD);

		if (S.isBlank(u)) {
			SecueHelper.writeResponse(resp, new AuthenticateException("必须有用户名！").toString());
			return null;
		}

		AbstractUser uobj = us.loadUserByName(u);
		if (uobj == null) {
			SecueHelper.writeResponse(resp, new AuthenticateException("用户不存在！").toString());
			return null;
		}

		String client_type = SecueHelper.getClient(request);

		if (us.checkUserPassword(uobj, p, request) && us.checkClient(uobj, client_type)) {

			uobj.setClient(client_type);
			String authkey =  ID.uuid();
			uobj.setToken(authkey);  
			//genAndReturnAuthcodeOnSuccess(request, resp, uobj,redis);
			return uobj;

		} else {
			resp.setStatus(405);
			SecueHelper.writeResponse(resp, new AuthenticateException("用户名或密码错误！").toString());
			return null;
		}
	}
	
	
 
	
	
	
	


}
