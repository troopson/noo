/**
 * 
 */
package noo.json;

import java.util.Map;

import javax.servlet.http.HttpServletRequest; 

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import noo.util.S;

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

		JsonObject result = null;
		HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
		String method = servletRequest.getMethod();
	    if(!HttpMethod.GET.matches(method) && !HttpMethod.HEAD.matches(method)) {
              
             String jsonBody = S.readAndCloseInputStream(servletRequest.getInputStream(), "UTF-8"); 
             if(S.isNotBlank(jsonBody)) {
            	//jsonBody = jsonBody.replace("<script>", "");
				result = new JsonObject(jsonBody);
			}
			 
		 }
		 if(result==null) {
			result = new JsonObject();
		}
		
		for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
			if (entry.getValue().length > 0) {
				result.put(entry.getKey(), entry.getValue()[0]);
			}
		} 
		
		return result;
	}

}