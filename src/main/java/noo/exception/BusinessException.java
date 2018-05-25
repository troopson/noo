/**
 * 
 */
package noo.exception;


/**
* @author  瞿建军      
* 
* 
* 创建时间： 2016年6月20日  上午6:50:44
* 
*/
public class BusinessException extends BaseException {

	private static final long serialVersionUID = 1L;
 
	
	public BusinessException(String code, String msg) {
		super(code, msg);
	}
	
	public BusinessException(int code, String msg) {
		super(code+"", msg);
	}

	


}
