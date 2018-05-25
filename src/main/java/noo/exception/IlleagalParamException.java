/**
 * 
 */
package noo.exception;

import org.springframework.http.HttpStatus;

/**
 * @author qujianjun troopson@163.com 2018年5月24日
 */
public class IlleagalParamException extends BaseException {

	public IlleagalParamException(Exception e) {
		super(HttpStatus.BAD_REQUEST + "", e.getMessage());
	}

	public IlleagalParamException(String paramName, String value) {
		super(HttpStatus.BAD_REQUEST + "", "非法参数：" + paramName + "=" + value);
	}

	private static final long serialVersionUID = 1L;

}
