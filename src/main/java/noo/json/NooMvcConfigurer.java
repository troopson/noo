/**
 * 
 */
package noo.json;

import java.util.List;

import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author qujianjun   troopson@163.com
 * 2018年6月12日 
 */
public class NooMvcConfigurer implements WebMvcConfigurer {
	 
	@Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new JsonObjectResolver());
    }
	
}