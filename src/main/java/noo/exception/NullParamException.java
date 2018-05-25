/**
 * 
 */
package noo.exception;

import org.springframework.http.HttpStatus;

/**
 * @author qujianjun   troopson@163.com
 * 2018年5月24日 
 */
public class NullParamException  extends BaseException {


		public NullParamException(String paramName) {
			super(HttpStatus.BAD_REQUEST+"", "参数"+paramName+"不能为空！");
		}
	  
		private static final long serialVersionUID = 1L;

	
}
