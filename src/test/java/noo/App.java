/**
 * 
 */
package noo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
 

import noo.rest.security.processor.AccessCodeInterceptor;
import noo.rest.security.processor.OAuth2Interceptor;

/**
 * @author qujianjun   troopson@163.com
 * 2018年5月14日 
 */
@SpringBootApplication   
@Configuration 
public class App {
    public static void main(String[] args) {
    	ConfigurableApplicationContext context = SpringApplication.run(App.class,args); 
    	String s = context.getEnvironment().getProperty("spring.application.name");
    	System.out.println("========="+s+"=============");
    	
    }
    
    
	
	@Bean  
	public OAuth2Interceptor oauth2Handler() {
		OAuth2Interceptor  i = new OAuth2Interceptor();
		i.setLoadAccessSecret(clientid->"ok");
		return i;
	}
	
	@Bean  
	public AccessCodeInterceptor accessCodeHandler() {
		AccessCodeInterceptor  i = new AccessCodeInterceptor();
		i.setSecret_load(key->key);
		i.setUseOnce(true);
		return i;
	}
    
}

 