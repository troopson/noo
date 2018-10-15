/**
 * 
 */
package noo.exception;

/**
* @author  瞿建军      
* 
* 创建时间： 2016年6月20日  上午6:54:03
* 
*/

public class AccessDeniedException extends BaseException {

 
	private static final long serialVersionUID = 1L;

	public AccessDeniedException(String msg) {
		super(ExpCode.AUTHORIZE, msg);
	}

}
