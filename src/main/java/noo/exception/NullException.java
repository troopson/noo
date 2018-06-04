/**
 * 
 */
package noo.exception;

import org.springframework.http.HttpStatus;

/**
 * @author qujianjun   troopson@163.com
 * 2018年5月24日 
 */
public class NullException  extends BusinessException {


		public NullException(String paramName) {
			super(HttpStatus.BAD_REQUEST+"",  paramName+"不能为空！");
		}
	  
		private static final long serialVersionUID = 1L;

	
}
