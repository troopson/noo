/**
 * 
 */
package noo;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import noo.exception.BaseExceptionHandler;
import noo.jdbc.JdbcSvr;
import noo.jdbc.SQLHolder;
import noo.json.NooMvcConfigurer;
import noo.util.SpringContext;
import noo.web.NController;
import noo.web.NRemote;

/**
 * @author qujianjun troopson@163.com 2017年6月4日
 */

@Configuration
@EnableWebMvc
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
	public NController controller() {
		return new NController();
	}

	@Bean
	public NRemote nremote() {
		return new NRemote();
	}

	@Bean(name = "noo_rest")
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	@Bean
	public NooMvcConfigurer NooWebMvcConfigurer() {
		return new NooMvcConfigurer();
	}
 

}
