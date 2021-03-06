/**
 * 
 */
package noo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import noo.event.ListenerPool;
import noo.exception.BaseExceptionHandler;
import noo.jdbc.JdbcCacheSvr;
import noo.jdbc.JdbcSvr;
import noo.jdbc.SQLHolder;
import noo.json.JsonObjectResolver;
import noo.rest.security.api.ApiRateLimitPool;
import noo.rest.security.processor.LoginInterceptor;
import noo.rest.security.processor.LogoutInterceptor;
import noo.rest.security.processor.unify.AuthCodeLoginInterceptor;
import noo.util.SpringContext;

/**
 * @author qujianjun troopson@163.com 2017年6月4日
 */

@Configuration 
public class Config {

	@Autowired(required = false) 
	private DataSource dataSource;

	@Bean
	public BaseExceptionHandler createExceptionHandler() {
		return new BaseExceptionHandler();
	}

	@Bean
	@Primary
	@ConditionalOnProperty(name="spring.datasource.url")
	public JdbcSvr createJdbcSvr() {
		return new JdbcSvr(this.dataSource);
	}
	
	@Bean 
	@ConditionalOnProperty(name="spring.datasource.url")
	public JdbcCacheSvr createJdbcCach() {
		return new JdbcCacheSvr();
	}
	

	@Bean
	public SQLHolder createSQLHolder() {
		return new SQLHolder();
	}

	@Bean
	public SpringContext context() {
		return new SpringContext();
	}

	
	@Bean
	public ListenerPool listenerPool() {
		return new ListenerPool();
	}

	
//	@Bean
//	public NooMvcConfigurer NooWebMvcConfigurer() {
//		return new NooMvcConfigurer();
//	}
	
	
	@Autowired(required=false)
	private Set<RequestMappingHandlerAdapter> adapters; 
	 
	@PostConstruct
    public void addArgumentResolvers() {
		if(adapters==null)
			return;
		for(RequestMappingHandlerAdapter adapter: adapters) {
			List<HandlerMethodArgumentResolver> ls = adapter.getArgumentResolvers();
			List<HandlerMethodArgumentResolver> rslvs = new ArrayList<>();
			rslvs.add(new JsonObjectResolver());
			rslvs.addAll(ls);
			adapter.setArgumentResolvers(rslvs);
		}
    }
	
	//====================security bean============================
	
	@Bean  
	public LogoutInterceptor logoutHandler() {
		return new LogoutInterceptor();
	}
	
	@Bean  
	public LoginInterceptor userPasswordLoginHandler() {
		return new LoginInterceptor();
	}
	
	@Bean  
	public AuthCodeLoginInterceptor AuthCodeLoginInterceptor() {
		return new AuthCodeLoginInterceptor();
	}
	
//	@Bean
//	public UniLoginInterceptor UniLoginInterceptor() {
//		return new UniLoginInterceptor();
//	}
	
	@Bean
	public ApiRateLimitPool ApiRateLimitPool() {
		return new ApiRateLimitPool();
	}
	 

}
