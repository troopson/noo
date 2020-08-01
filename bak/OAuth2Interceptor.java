/**
 * 
 */
package noo.rest.security.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;

import noo.exception.AuthenticateException;
import noo.exception.BusinessException;
import noo.json.JsonObject;
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
	
	public static String PARAM_AUTHCODE ="code";	  //redirect的时候，返回的授权码名称
	//public static String PARAM_CLIENTID_NAME="_client";	//client_id参数名称
	public static String PARAM_USERNAME="username";  
	public static String PARAM_PASSWORD="password";
	public static String PARAM_REDIRECT_URL ="redirect_url"; 
	public static String PARAM_SERVER_SIGN="sign";  //服务器端请求时的签名变量的名称
	
	public static String ACCESS_TOKEN ="access_token";
	
	//===========可配置属性================== 
	
	
	private Function<String,String> loadAccessSecret=null; 
	private String loginPageFile ="oauth_def_loginpage.html";  //　登录页面的html文件

	//================================================

	private String loginSubmitUrl ="/oauthlogin_submit";  //登录提交的请求链接
	private String loginPageUrl ="/oauthlogin";      //登录页面的显示链接
	private String serverRequestUrl ="/accessToken";       //服务器请求authentic的链接 
	private String checkAccessTokenUrl = "/checkAccessToken"; //校验某个AccessToken是否真实
	private String redirectPath="/noo/redirect";           //认证服务器做转发用的专门链接
	
	
	private String loginhtml=null;   //缓存登录页面的内容
	
	private OAuth2ProcInf procInf =null;
	
	
	//{"access_token":"409ace4f7308fbd3e236addef0e0f1dd","expires_in":3600}
	
	@Override
	public boolean process(String requrl, HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		if(this.loadAccessSecret==null)
			throw new NullPointerException("loadAccessSecret cannot be null, must set function that can load access secret with access key.");
		
		String method = req.getMethod();
		
		
		if(this.isGetShowLoginPageRequest(requrl) && HttpMethod.GET.matches(method)) {
			//显示登录页面
			this.doShowLoginPage(req, resp);
			return true;
			
		}else if(this.isLoginRequest(requrl) && HttpMethod.POST.matches(method)) {
			//页面登录请求
			this.doLogin(req, resp);
			return true;
			
	    }else if(this.isRedirectPath(requrl)) {
			//用来进行跳转的页面
			String redirecturl = req.getParameter(PARAM_REDIRECT_URL); 
			String client_id = req.getParameter(SecueHelper.CLIENT);
			String code = req.getParameter(PARAM_AUTHCODE); 
           
			if( S.isBlank(redirecturl) || S.isBlank(client_id) || S.isBlank(code)) {
				SecueHelper.writeResponse(resp, new AuthenticateException("必须有client/redirecturl/code信息！").toString());    
			}else {
				
				 String client_url = makeRealRedirectUrl(redirecturl,code,client_id); 
					//跳转
				 resp.sendRedirect(client_url);  
			}
			return true;
			
		}else if(this.isServerRequest(requrl) && HttpMethod.POST.matches(method)) {
			//服务器换code的请求
	    	JsonObject key = this.tradeAuthenticatKey(req, resp); 
			SecueHelper.writeResponse(resp,key.encode()); 
			return true;
		}else if(this.isCheckAccessToken(requrl)) {
			//校验某个AccessToken是否真实
			String at = req.getParameter(ACCESS_TOKEN);
			String client_type = SecueHelper.getClient(req);
			AbstractUser u = SecueHelper.retrieveUser(at, us, client_type, redis);
			if(u==null) {
				SecueHelper.writeResponse(resp, "false");
			}else {
				SecueHelper.writeResponse(resp, "true");
			}
			return true;
		}else {
			return false;
		} 
	}
	
	

	//服务器端请求，用code换Authenticatkey
	private JsonObject tradeAuthenticatKey(HttpServletRequest request, HttpServletResponse resp) throws IOException {
		
		String code = request.getParameter(PARAM_AUTHCODE);
		String client_id = request.getParameter(SecueHelper.CLIENT);
		String redirecturl = request.getParameter(PARAM_REDIRECT_URL);
		String sign = request.getParameter(PARAM_SERVER_SIGN); 
		
		if(S.isBlank(client_id) || S.isBlank(redirecturl) || S.isBlank(sign)) {
			throw new AuthenticateException("必须有client和redirecturl信息！");
		} 
		
		String secret = this.getClientIdSecret(client_id); 
			
		String tocheck = MD5.encode(code+""+client_id+""+secret);
		if(!sign.equals(tocheck)){
			throw new AuthenticateException("签名错误！请用 code+clientid+secret 进行签名。");
		} 
		
		
		String mkey = this.makeKey(client_id, redirecturl, code);
		
		//只能使用一次
		String authenticationKey = this.redis.opsForValue().get(mkey);
		if(authenticationKey!=null)
			this.redis.delete(mkey);
		 
		AbstractUser u = SecueHelper.retrieveUser(authenticationKey, us, client_id, redis);
		if(u==null) {
			throw new AuthenticateException("AuthCode失效，用户不存在！") ;
		} 
		
		JsonObject j = new JsonObject();
		j.put(ACCESS_TOKEN, authenticationKey);
		j.put("expires_in", 3600);
		j.put("principal", u.toResponseJsonObject());
		
		return j;
	}
	
	//页面上用户名密码post登录　
	private void doLogin(HttpServletRequest request, HttpServletResponse resp) throws IOException {
		String u = request.getParameter(PARAM_USERNAME);
		String p = request.getParameter(PARAM_PASSWORD);
		String client_id = request.getParameter(SecueHelper.CLIENT);
		String redirecturl = request.getParameter(PARAM_REDIRECT_URL);
		
		if(S.isBlank(u)) {
			throw new AuthenticateException("必须有用户名！") ;
		}
		if(S.isBlank(client_id) || S.isBlank(redirecturl)) {
			throw new AuthenticateException("必须有client和redirecturl信息！") ;
		} 
		
		//调用接口，进行登录的后续检查
		if(this.procInf !=null )
			this.procInf.checkLogin(request);
		
		this.getClientIdSecret(client_id);
		
		AbstractUser uobj = us.loadUserByName(u);
		if(uobj ==null) {
			throw new AuthenticateException("用户不存在！") ;
		}
		
		if(us.checkUserPassword(uobj, p, request)) {
			
			String authkey =  ID.uuid();
			uobj.setToken(authkey); 
			uobj.setClient(client_id); 
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
			  
			String path = this.redirectPath+"?"+ PARAM_REDIRECT_URL+"="+URLEncoder.encode(redirecturl,"UTF-8")+"&"+SecueHelper.CLIENT+"="+client_id+"&"+PARAM_AUTHCODE+"="+code;
			resp.sendRedirect(request.getContextPath()+path);  //先跳到自己的一个path，写了cookie以后，再由那个页面负责真正的跳转
			resp.flushBuffer();
			
		}else { 
			resp.setStatus(405);
			throw new AuthenticateException("用户名或密码错误！");  
		}
	}
	
	//为实现oss，显示登录页面时判断是否直接跳转
	private void doShowLoginPage(HttpServletRequest request, HttpServletResponse resp) throws IOException {
		
		String client_id = request.getParameter(SecueHelper.CLIENT);
		String redirecturl = request.getParameter(PARAM_REDIRECT_URL); 
		 
		if(S.isBlank(client_id) || S.isBlank(redirecturl)) {
			throw new AuthenticateException("必须有client和redirecturl信息！");
		}  

		this.getClientIdSecret(client_id);
		
		Cookie[] cookies = request.getCookies();
		String authkey = null;
		if(cookies!=null) {
			for(Cookie c : cookies) {
				if(SecueHelper.HEADER_KEY.equals(c.getName())){
					authkey = c.getValue();
					break;
				}
			}
		}
		 
		AbstractUser user = SecueHelper.retrieveUser(authkey, us, client_id, redis);  
		 
		if(user!=null) {
		
			String code = ID.uuid();
			
			String codekey = this.makeKey(client_id, redirecturl, code);
			
			this.redis.opsForValue().set(codekey, authkey,3600,TimeUnit.SECONDS);  
			
			String client_url = makeRealRedirectUrl(redirecturl,code,client_id);
			
			//跳转
			resp.sendRedirect(client_url); 
			resp.flushBuffer();
			return;
		} else {  
			String contextPath = request.getContextPath();
			SecueHelper.writeResponse(resp, this.getLoginHtml(contextPath,client_id, redirecturl));
		} 
		
	}
	
	private String makeRealRedirectUrl(String redirecturl, String authcode, String client_id)  {
		try {
			return redirecturl+(redirecturl.indexOf("?")>0?"&":"?")+PARAM_AUTHCODE+"="+authcode+"&"+SecueHelper.CLIENT+"="+client_id+"&"+PARAM_REDIRECT_URL+"="+URLEncoder.encode(redirecturl,"UTF-8"); 
			 
		} catch (UnsupportedEncodingException e) { 
			e.printStackTrace();
			return null;
		}
	}
	
	
	private boolean isServerRequest(String path) {
		if(path.endsWith(this.serverRequestUrl)) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isLoginRequest(String path) {
		if(path.endsWith(this.loginSubmitUrl)) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isGetShowLoginPageRequest(String path) {
		if(path.endsWith(this.loginPageUrl)) {
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
	
	private boolean isCheckAccessToken(String path) {
		if(path.endsWith(this.checkAccessTokenUrl)) {
			return true;
		}else {
			return false;
		}
	}
	
	
	private String getLoginHtml(String  contextPath,String clientid, String redirect_url) {
		try {
			if(this.loginhtml==null) {
				//{{submiturl}}  {{client_id}}  {{redirect_url}}  {{client_id_name}}   {{redirect_url_name}}
				InputStream is = this.getClass().getResourceAsStream("/"+loginPageFile);
				this.loginhtml = S.readAndCloseInputStream(is, "UTF-8"); 
			}
			String content = this.loginhtml.replace("{{submiturl}}", contextPath+""+this.loginSubmitUrl)
			.replace("{{client_id}}", clientid)
			.replace("{{redirect_url}}", redirect_url);
			if(this.procInf!=null)
				content = this.procInf.transferHtml(content);
			return content;
		} catch (IOException e) { 
			e.printStackTrace();
			return "error in load login page!";
		}
		
	}
	
	
	
	
	public String makeKey(String client_id, String redirecturl,String code) {
		String secret = this.getClientIdSecret(client_id);
		return  AUTH_CODE_PRE+":"+MD5.encode(client_id+"-"+redirecturl+"-"+code+"-"+secret);
	}
	
	
	private String getClientIdSecret(String client_id) {
		String secret = this.loadAccessSecret.apply(client_id);
		if(S.isBlank(secret))
			throw new BusinessException("400","没有注册的clientid");
		return secret;
	}
	
	
	//================================================
	 

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
