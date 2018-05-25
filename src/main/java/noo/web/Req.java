/**
 * 
 */
package noo.web;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import noo.exception.NullParamException;
import noo.json.JsonObject;
import noo.util.S;

/**
* @author  瞿建军      
* 
* 创建时间： 2016年6月14日  下午4:20:30
* 
*/
public class Req {

	public static JsonObject params(){
		return params(null);
	}
	
	public static JsonObject params(String require){
		HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		Map<String,String[]> m=req.getParameterMap(); 
		JsonObject j = new JsonObject();
	
		if(m!=null && !m.isEmpty()){
			for(String key : m.keySet()){				
				String[] o=m.get(key);
				if(o!=null){
					String s=o[0];
					j.put(key, s);
				}
			}
		}
		 
		if(S.isNotBlank(require)) {
			String[] required = require.split(",");
			if(!j.containsAll(required))
			   throw new NullParamException(require);
		}
		
		return j;
		
	}
	
	
	
	
	 /**
	 * 获取客户端非代理的真实IP地址
	 */
	public static String getClientIP(HttpServletRequest request) {
	
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip))
			try {
				ip = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
			}
		return ip;
	}
	
	
	
	
	
}
