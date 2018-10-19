/**
 * 
 */
package noo;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import noo.event.ListenerPool;
import noo.exception.BaseExceptionHandler;
import noo.jdbc.JdbcSvr;
import noo.jdbc.SQLHolder;
import noo.json.JsonObjectResolver;
import noo.rest.security.processor.LogoutInterceptor;
import noo.rest.security.processor.LoginInterceptor;
import noo.util.SpringContext;
import noo.web.NRemote;
import noo.web.NooStub;

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
	@ConditionalOnMissingBean(type="org.springframework.web.client.RestTemplate")
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	@Bean
	public NooStub controller() {
		return new NooStub();
	}

	@Bean
	public NRemote nremote() {
		return new NRemote();
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
	private RequestMappingHandlerAdapter adapter; 
	 
	@PostConstruct
    public void addArgumentResolvers() {
		List<HandlerMethodArgumentResolver> ls = adapter.getArgumentResolvers();
		List<HandlerMethodArgumentResolver> rslvs = new ArrayList<>();
		rslvs.add(new JsonObjectResolver());
		rslvs.addAll(ls);
		adapter.setArgumentResolvers(rslvs);
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
	
 

}
