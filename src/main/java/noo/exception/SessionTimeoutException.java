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
		super(ExpCode.SESSION_TIMEOUT, "当前登录信息已失效，请重新登陆！");
	}
	
	public SessionTimeoutException(String msg) {
		super(ExpCode.SESSION_TIMEOUT, msg);
	}
	
	public SessionTimeoutException(String code,String msg) {
		super(code, msg);
	}
	
  
	private static final long serialVersionUID = 1L;

}
