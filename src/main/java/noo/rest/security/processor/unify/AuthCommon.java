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
public class AuthCommon {

	public static final Logger log = LoggerFactory.getLogger(AuthCommon.class);

	public static final String AUTHCODE = "authcode";

	/**
	 * 校验手机号、密码是否正确 本方法只是校验用户名和密码，生成用户对象，不生成token
	 * 
	 * @param rawrequest
	 * @param resp
	 * @throws IOException
	 */
	public static AbstractUser verifyNamePasswordToGetUserObj(HttpServletRequest rawrequest, HttpServletResponse resp,
			SecuritySetting us, StringRedisTemplate redis) throws IOException {

		HttpServletRequest request = new DelegateHttpServletRequest(rawrequest);
		String u = request.getParameter(LoginInterceptor.USERNAME);
		String p = request.getParameter(LoginInterceptor.PASSWORD);

		return checkUsernamePasswordForUserObj(request,resp,us,u,p);
	}

	// 通过用户名密码登录
	public static AbstractUser checkUsernamePasswordForUserObj(HttpServletRequest request, HttpServletResponse resp, SecuritySetting us, String username,
			String password) throws IOException {
		
		if (S.isBlank(username)) {
			SecueHelper.writeResponse(resp, new AuthenticateException("必须有用户名！").toString());
			return null;
		}
		
		AbstractUser uobj = us.loadUserByName(username);
		if (uobj == null) {
			SecueHelper.writeResponse(resp, new AuthenticateException("用户不存在！").toString());
			return null;
		}

		String client_type = SecueHelper.getClient(request);

		if (us.checkUserPassword(uobj, password, request) && us.checkClient(uobj, client_type)) { 
			uobj.setClient(client_type);
			return uobj;

		} else {
			resp.setStatus(405);
			SecueHelper.writeResponse(resp, new AuthenticateException("用户名或密码错误！").toString());
			return null;
		}
	}

}
