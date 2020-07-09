/**
 * 
 */
package noo.rest.security.delegate;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.http.HttpHeaders;

import noo.json.JsonObject;
import noo.json.JsonObjectResolver;

/**
 * @author qujianjun troopson@163.com Jul 9, 2020
 */
public class DelegateHttpServletRequest extends HttpServletRequestWrapper {

	private JsonObject params = null;

	public DelegateHttpServletRequest(HttpServletRequest request) {
		super(request);
		params = parseRequestJsonParam((HttpServletRequest) this.getRequest());
	}

	@Override
	public String getParameter(String paramName) {
		String value = this.getRequest().getParameter(paramName);
		if (value == null && params != null) {
			value = params.getString(paramName);
		}
		return value;
	}

	// content-type: application/json
	public static JsonObject parseRequestJsonParam(HttpServletRequest request) {
		String content_type = request.getHeader(HttpHeaders.CONTENT_TYPE);
		if ("application/json".equalsIgnoreCase(content_type)) {
			try {
				return JsonObjectResolver.parseAsJson(request);
			} catch (IOException e) { 
			}
		}
		return null;

	}

}
