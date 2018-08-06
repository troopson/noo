/**
 * 
 */
package noo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author qujianjun   troopson@163.com
 * 2018年5月14日 
 */
@SpringBootApplication  
@EnableEurekaClient
@EnableDiscoveryClient
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class,args);
    }
}

 