package noo.event.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import noo.rest.security.SecurityFilter;
 

/**
 * @author qujianjun troopson@163.com 2018年8月21日
 */
@Configuration
public class Config {
 

	@Autowired
	private UserService as;

	@Autowired
	private StringRedisTemplate redis;
	

	@Bean
	public FilterRegistrationBean<SecurityFilter> testFilterRegistration() {

		FilterRegistrationBean<SecurityFilter> registration = new FilterRegistrationBean<>();
		SecurityFilter sf = new SecurityFilter(); 
		sf.setSecuritySetting(as);
		sf.setRedis(redis);
		registration.setFilter(sf); 
		registration.addUrlPatterns("/*");
		registration.setOrder(1);
		return registration;
	}

}
