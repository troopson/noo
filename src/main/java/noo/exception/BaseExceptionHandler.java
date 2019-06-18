/**
 * 
 */
package noo.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

/**
* @author  瞿建军      
* 
* 创建时间： 2016年6月13日  下午3:12:45
* 
*/
@ControllerAdvice
public class BaseExceptionHandler {

	@Autowired(required=false)
	private ExceptionProcessor processor;
 
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String onLogicException(Exception exception, WebRequest request) {
        return ((BusinessException)exception).toString();
    }
    
    
   
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public String onUnAuthrizedException(Exception exception, WebRequest request) {
        return ((AccessDeniedException)exception).toString();
    }
    
    @ExceptionHandler(AuthenticateException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public String onAuthenticateException(Exception exception, WebRequest request) {
        return ((AuthenticateException)exception).toString();
    }
        
   
    @ExceptionHandler(SessionTimeoutException.class)
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    @ResponseBody
    public String onSessionTimeoutException(Exception exception, WebRequest request) {
        return ((SessionTimeoutException)exception).toString();
    }
    
    
    
    
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String onRuntimeException(Exception exception, WebRequest request) {
    	exception.printStackTrace();  
    	if(processor!=null)
    		processor.process(exception);
        return BaseException.unknowException(HttpStatus.BAD_REQUEST+"",exception);
    }
    
    @ExceptionHandler(org.springframework.jdbc.UncategorizedSQLException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String onUncategorizedSQLException(Exception exception, WebRequest request) {
    	exception.printStackTrace();
    	if(processor!=null)
    		processor.process(exception);
    	UncategorizedSQLException u = (UncategorizedSQLException)exception;
        return BaseException.unknowException(HttpStatus.BAD_REQUEST+"",u.getSQLException());
    }
    
    
    
  
    @ExceptionHandler(org.springframework.web.multipart.MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String onMultipartException(Exception exception, WebRequest request) {
    	exception.printStackTrace();
    	if(processor!=null)
    		processor.process(exception);
        return new BaseException("file.upload.failed",exception.getMessage()).toString();
    }
    
    
}