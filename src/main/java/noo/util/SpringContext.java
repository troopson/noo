/**
 * 
 */
package noo.util;

import java.util.Map;

/**
 * @author qujianjun   troopson@163.com
 * 2017年8月24日 
 */
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class SpringContext implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if(SpringContext.applicationContext == null) {
            SpringContext.applicationContext = applicationContext;
        } 
      
    }
    
    //获取某个属性值
    public static String getProperty(String key) {
    	 return applicationContext.getEnvironment().getProperty(key);
    }

    //获取applicationContext
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    //通过name获取 Bean.
    public static Object getBean(String name){
        return getApplicationContext().getBean(name);
    }

    //通过class获取Bean.
    public static <T> T getBean(Class<T> clazz){
        return getApplicationContext().getBean(clazz);
    }

    //通过name,以及Clazz返回指定的Bean
    public static <T> T getBean(String name,Class<T> clazz){
        return getApplicationContext().getBean(name, clazz);
    }
    
    public static <T> Map<String,T> getBeansOfType(Class<T> clazs){
 	   return getApplicationContext().getBeansOfType(clazs);
    }

}
