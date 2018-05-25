/**
 * 
 */
package noo.exception;

import org.springframework.http.HttpStatus;

/**
* @author  瞿建军      
* 
* 创建时间： 2016年6月20日  上午6:50:44
* 
*/

public class SessionTimeoutException extends BaseException {


	public SessionTimeoutException() {
		super(HttpStatus.REQUEST_TIMEOUT+"", "Session超时");
	}
  
	private static final long serialVersionUID = 1L;

}
