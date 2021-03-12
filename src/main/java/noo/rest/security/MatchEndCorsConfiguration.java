/**
 * 
 */
package noo.rest.security;

import java.util.List;

import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;

/**
 * @author qujianjun   troopson@163.com
 * 2021年3月11日 
 */
public class MatchEndCorsConfiguration extends CorsConfiguration {

	@Override
	public String checkOrigin(@Nullable String requestOrigin) {
		if (!StringUtils.hasText(requestOrigin)) {
			return null;
		}
		List<String> allowedOrigins = this.getAllowedOrigins();
		if (ObjectUtils.isEmpty(allowedOrigins)) {
			return null;
		}

		if (allowedOrigins.contains(ALL)) {
			if (this.getAllowCredentials() != Boolean.TRUE) {
				return ALL;
			}
			else {
				return requestOrigin;
			}
		}
		//去掉端口号
		int pos = requestOrigin.indexOf(":");
		if(pos!=-1)
			requestOrigin = requestOrigin.substring(0,pos);
		for (String allowedOrigin : allowedOrigins) { 
			if (requestOrigin.endsWith(allowedOrigin)) {
				return requestOrigin;
			}
		}

		return null;
	}
	
}
