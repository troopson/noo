/**
 * 
 */
package noo.web;

import java.lang.reflect.Method;

import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import noo.SpringContext;
import noo.exception.IlleagalParamException;
import noo.json.JsonObject;

/**
 * @author 瞿建军 troopson@163.com 2018年3月27日
 */
@RestController
@RequestMapping("/noo/*")
public class CommonController {

	@RequestMapping("{beanName}/{method}")
	public Object getResult(@PathVariable(required = true) String beanName, @PathVariable(required = true) String method) {
		try {

			JsonObject param = Req.params();

			Object o = SpringContext.getBean(beanName);

			Method m = ReflectionUtils.findMethod(o.getClass(), method, JsonObject.class);
			return ReflectionUtils.invokeMethod(m, o, param);

		} catch (RuntimeException e) {
			throw new IlleagalParamException(e);
		}

	}

}
