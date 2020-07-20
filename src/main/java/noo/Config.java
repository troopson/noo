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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import noo.event.ListenerPool;
import noo.exception.BaseExceptionHandler;
import noo.jdbc.JdbcSvr;
import noo.jdbc.SQLHolder;
import noo.json.JsonObjectResolver;
import noo.rest.security.api.ApiRateLimitPool;
import noo.rest.security.processor.AuthCodeLoginInterceptor;
import noo.rest.security.processor.LoginInterceptor;
import noo.rest.security.processor.LogoutInterceptor;
import noo.util.SpringContext;

/**
 * @author qujianjun troopson@163.com 2017年6月4日
 */

@Configuration 
public class Config {

	@Autowired
	private DataSource dataSource;

	@Bean
	public BaseExceptionHandler createExceptionHandler() {
		return new BaseExceptionHandler();
	}

	@Bean
	@Primary
	public JdbcSvr createJdbcSvr() {
		return new JdbcSvr(this.dataSource);
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
	
	
	@Autowired
	private Set<RequestMappingHandlerAdapter> adapters; 
	 
	@PostConstruct
    public void addArgumentResolvers() {
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
	
	@Bean
	public ApiRateLimitPool ApiRateLimitPool() {
		return new ApiRateLimitPool();
	}
	 

}
