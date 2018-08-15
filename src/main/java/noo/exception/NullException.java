/**
 * 
 */
package noo.exception;

import org.springframework.http.HttpStatus;

/**
 * @author qujianjun troopson@163.com 2018年5月24日
 */
public class NullException extends BusinessException {

	public NullException(int code, String message) {
		super(code, message);
	}

	public NullException(String paramName) {
		super(HttpStatus.BAD_REQUEST + "", "Param [" + paramName + "] can not be null！");
	}

	private static final long serialVersionUID = 1L;

}
