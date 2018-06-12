/**
 * 
 */
package noo.json;

import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @author qujianjun troopson@163.com 2018年6月12日
 */
public final class JsonObjectResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter methodParameter) {
		return methodParameter.getParameterType().equals(JsonObject.class);
	}

	@Override
	public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
			NativeWebRequest webRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {

		Map<String, String[]> parameterMap = webRequest.getParameterMap();

		JsonObject result = new JsonObject();
		for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
			if (entry.getValue().length > 0) {
				result.put(entry.getKey(), entry.getValue()[0]);
			}
		}
		return result;
	}

}