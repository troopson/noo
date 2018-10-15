/**
 * 
 */
package noo.web;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import noo.event.Event;
import noo.exception.IlleagalParamException;
import noo.json.JsonObject;
import noo.util.Req;
import noo.util.SpringContext;

/**
 * @author 瞿建军 troopson@163.com 2018年5月27日
 */
@ResponseBody
@RequestMapping("/"+NRemote.NOO_REMOTE_NAME+"/*")
public class NooStub {

	public static final Log log = LogFactory.getLog(NooStub.class);
	
	@RequestMapping("event/{name}")
	public boolean remote_event(@PathVariable(required = true) String name) {
		try {
			
			JsonObject param = Req.params();

			Event e = new Event(name,this);
			e.trigger(param);
			log.info("execute remote event "+name+", published OK!");
			return true;

		} catch (RuntimeException e) {
			e.printStackTrace();
			throw new IlleagalParamException(e);
		}

	}
	
	
	@RequestMapping("{beanName}/{method}")
	public Object getResult(@PathVariable(required = true) String beanName, @PathVariable(required = true) String method) {
		try {
			
			JsonObject param = Req.params();

			Object o = SpringContext.getBean(beanName);
			
			Method m = ReflectionUtils.findMethod(o.getClass(), method, JsonObject.class);
			if(m!=null) {
				return ReflectionUtils.invokeMethod(m, o, param);
			}				
			
			m = ReflectionUtils.findMethod(o.getClass(), method, Map.class);
			if(m!=null) {
				return ReflectionUtils.invokeMethod(m, o, param.getMap());
			} else {
				throw new IlleagalParamException("没有找到"+beanName+"/"+method);
			}

		} catch (RuntimeException e) {
			e.printStackTrace();
			throw new IlleagalParamException(e);
		}

	}
	
	


}
