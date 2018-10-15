/**
 * 
 */
package noo.exception;

/**
* @author  瞿建军      
* 
* 创建时间： 2016年6月20日  上午6:50:44
* 
*/

public class SessionTimeoutException extends BaseException {


	public SessionTimeoutException() {
		super(ExpCode.SESSION_TIMEOUT, "Session超时");
	}
  
	private static final long serialVersionUID = 1L;

}
