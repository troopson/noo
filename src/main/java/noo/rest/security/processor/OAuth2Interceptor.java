/**
 * 
 */
package noo.rest.security.processor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;

import noo.exception.AuthenticateException;
import noo.rest.security.AbstractUser;
import noo.rest.security.SecueHelper;
import noo.util.ID;
import noo.util.MD5;
import noo.util.S;

/**
 * @author qujianjun   troopson@163.com
 * 2018年10月18日 
 */
public class OAuth2Interceptor extends RequestInterceptor {

	public static final String AUTH_CODE_PRE="noo.auth20code";
	
	private String Param_authcode ="code";	
	private String Param_clientid_Name="accesskey";	
	private String Param_username="username";
	private String Param_password="password";
	private String Param_redirect_url ="redirect_url"; 
	
	private Function<String,String> loadAccessSecret=null; 
	private String loginSubmitUrl ="/oauth2login_submit";
	private String loginPageUrl ="/oauth2login_page"; 
	private String loginPageFile ="oauth_def_loginpage.html";

	//================================================
	
	private String serverRequestUrl ="/accessToken";
	private String redirectPath="/noo/redirect";
	private String loginhtml=null; 
	
	
	//{"access_token":"409ace4f7308fbd3e236addef0e0f1dd","expires_in":3600}
	
	@Override
	public boolean process(String requrl, HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		if(this.loadAccessSecret==null)
			throw new NullPointerException("loadAccessSecret cannot be null, must set function that can load access secret with access key.");
		
		String method = req.getMethod();
		
		
		if(this.isRedirectPath(requrl)) {
			//用来进行跳转的页面
			String redirecturl = req.getParameter(this.Param_redirect_url);
			if( S.isBlank(redirecturl)) {
				SecueHelper.writeResponse(resp, new AuthenticateException("必须有client和redirecturl信息！").toString());    
			}else {
				resp.sendRedirect(redirecturl);
			}
			return true;
			
		}else if(this.isGetShowLoginPageRequest(requrl) && HttpMethod.GET.matches(method)) {
			//显示登录页面
			this.doShowLoginPage(req, resp);
			return true;
			
		}else if(this.isLoginRequest(requrl) && HttpMethod.POST.matches(method)) {
			//页面登录请求
			this.doLogin(req, resp);
			return true;
			
	    }else if(this.isServerRequest(requrl) && HttpMethod.POST.matches(method)) {
			//服务器换code的请求
			String key = this.tradeAuthenticatKey(req, resp); 
			if(S.isBlank(key)) {
				resp.setStatus(400);
			    SecueHelper.writeResponse(resp, new AuthenticateException("fake code").toString()); 
			}else {
				SecueHelper.writeResponse(resp,"{\"access_token\":\""+key+"\",\"expires_in\":3600}");
			}
			
			return true;
		}else {
			return false;
		} 
	}

	//服务器端请求，用code换Authenticatkey
	private String tradeAuthenticatKey(HttpServletRequest request, HttpServletResponse resp) throws IOException {
		
		String code = request.getParameter(this.Param_authcode);
		String client_id = request.getParameter(this.Param_clientid_Name);
		String redirecturl = request.getParameter(this.Param_redirect_url);
		
		if(S.isBlank(client_id) || S.isBlank(redirecturl)) {
			SecueHelper.writeResponse(resp, new AuthenticateException("必须有client和redirecturl信息！").toString());   
			return null;
		} 
		
		
		String mkey = this.makeKey(client_id, redirecturl, code);
		
		//只能使用一次
		String authenticationKey = this.redis.opsForValue().get(mkey);
		if(authenticationKey!=null)
			this.redis.delete(mkey);
		
		return authenticationKey;
	}
	
	//页面上用户名密码post登录　
	private void doLogin(HttpServletRequest request, HttpServletResponse resp) throws IOException {
		String u = request.getParameter(this.Param_username);
		String p = request.getParameter(this.Param_password);
		String client_id = request.getParameter(this.Param_clientid_Name);
		String redirecturl = request.getParameter(this.Param_redirect_url);
		
		if(S.isBlank(u)) {
			SecueHelper.writeResponse(resp, new AuthenticateException("必须有用户名！").toString());   
			return;
		}
		if(S.isBlank(client_id) || S.isBlank(redirecturl)) {
			SecueHelper.writeResponse(resp, new AuthenticateException("必须有client和redirecturl信息！").toString());   
			return;
		} 
		
		
		AbstractUser uobj = us.loadUserByName(u);
		if(uobj ==null) {
			SecueHelper.writeResponse(resp, new AuthenticateException("用户不存在！").toString());  
			return;
		}
		
		if(us.checkUserPassword(uobj, p, request)) {
			
			String authkey =  ID.uuid();
			uobj.setToken(authkey); 
			SecueHelper.updateUser(uobj,this.redis);
			
			String code = ID.uuid();
			
			String mkey = this.makeKey(client_id, redirecturl, code);
			
			this.redis.opsForValue().set(mkey, authkey,3600,TimeUnit.SECONDS);
			  
			us.afterLoginSuccess(uobj, request); 
			 
			Cookie c = new Cookie(SecueHelper.HEADER_KEY,authkey);
			c.setHttpOnly(true);
			c.setPath("/");
			c.setMaxAge(3600);
			resp.addCookie(c);
			
			String rurl = redirecturl+(redirecturl.indexOf("?")>0?"&":"?")+code;
			
			String path = this.redirectPath+"?"+ this.Param_redirect_url+"="+URLEncoder.encode(rurl,"UTF-8");
			resp.sendRedirect(request.getContextPath()+path);  //先跳到自己的一个path，写了cookie以后，再由那个页面负责真正的跳转
			resp.flushBuffer();
			
		}else { 
			resp.setStatus(405);
			SecueHelper.writeResponse(resp, new AuthenticateException("用户名或密码错误！").toString());  
		}
	}
	
	//为实现oss，显示登录页面时判断是否直接跳转
	private void doShowLoginPage(HttpServletRequest request, HttpServletResponse resp) throws IOException {
		
		String client_id = request.getParameter(this.Param_clientid_Name);
		String redirecturl = request.getParameter(this.Param_redirect_url); 
		 
		if(S.isBlank(client_id) || S.isBlank(redirecturl)) {
			SecueHelper.writeResponse(resp, new AuthenticateException("必须有client和redirecturl信息！").toString());   
			return;
		} 
		
		Cookie[] cookies = request.getCookies();
		String authkey = null;
		for(Cookie c : cookies) {
			if(SecueHelper.HEADER_KEY.equals(c.getName())){
				authkey = c.getValue();
				break;
			}
		}
		
		AbstractUser user = SecueHelper.retrieveUser(authkey, us, redis);  
		 
		if(user!=null) {
		
			String code = ID.uuid();
			
			String mkey = this.makeKey(client_id, redirecturl, code);
			
			this.redis.opsForValue().set(mkey, authkey,3600,TimeUnit.SECONDS);  
			
			//跳转
			resp.sendRedirect(redirecturl+(redirecturl.indexOf("?")>0?"&":"?")+code); 
			resp.flushBuffer();
			return;
		} else {  

			SecueHelper.writeResponse(resp, this.getLoginHtml(client_id, redirecturl));
		} 
		
	}
	
	
	private boolean isServerRequest(String path) {
		if(path.equals(this.serverRequestUrl)) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isLoginRequest(String path) {
		if(path.equals(this.loginSubmitUrl)) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isGetShowLoginPageRequest(String path) {
		if(path.equals(this.loginPageUrl)) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isRedirectPath(String path) {
		if(path.endsWith(this.redirectPath)) {
			return true;
		}else {
			return false;
		}
	}
	
	
	private String getLoginHtml(String clientid, String redirect_url) {
		try {
			if(this.loginhtml==null) {
				//{{submiturl}}  {{client_id}}  {{redirect_url}}  {{client_id_name}}   {{redirect_url_name}}
				InputStream is = this.getClass().getResourceAsStream("/"+loginPageFile);
				this.loginhtml = S.readAndCloseInputStream(is, "UTF-8"); 
			}
			return this.loginhtml.replace("{{submiturl}}", this.loginSubmitUrl)
			.replace("{{client_id}}", clientid)
			.replace("{{redirect_url}}", redirect_url)
			.replace("{{client_id_name}}", this.Param_clientid_Name)
			.replace("{{redirect_url_name}}", this.Param_redirect_url);
			
		} catch (IOException e) { 
			e.printStackTrace();
			return "error in load login page!";
		}
		
	}
	
	
	
	
	private String makeKey(String client_id, String redirecturl,String code) {
		return  AUTH_CODE_PRE+":"+MD5.encode(client_id+"-"+redirecturl+"-"+code+"-"+this.loadAccessSecret.apply(client_id));
	}
	
	
	//================================================
	 

	public void setParam_clientid_Name(String param_clientid_Name) {
		Param_clientid_Name = param_clientid_Name;
	}

	public void setParam_username(String param_username) {
		Param_username = param_username;
	}

	public void setParam_password(String param_password) {
		Param_password = param_password;
	}

	public void setParam_redirect_url(String param_redirect_url) {
		Param_redirect_url = param_redirect_url;
	}

	public void setLoadAccessSecret(Function<String, String> loadAccessSecret) {
		this.loadAccessSecret = loadAccessSecret;
	}

	public void setLoginSubmitUrl(String loginSubmitUrl) {
		this.loginSubmitUrl = loginSubmitUrl;
	}

	public void setLoginPageUrl(String loginPageUrl) {
		this.loginPageUrl = loginPageUrl;
	}

	public void setLoginPageFile(String loginPageFile) {
		this.loginPageFile = loginPageFile;
	}
 
	
	

}
