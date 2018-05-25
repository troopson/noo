/**
 * 
 */
package noo.exception;

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
class BaseExceptionHandler {

   
    
    /**
     * Handle exceptions thrown by handlers.
     */
    @ExceptionHandler(value = BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String onLogicException(Exception exception, WebRequest request) {
        return ((BusinessException)exception).toString();
    }
    
    
    /**
     * Handle exceptions thrown by handlers.
     */
    @ExceptionHandler(value = UnAuthrizedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public String onUnAuthrizedException(Exception exception, WebRequest request) {
        return ((UnAuthrizedException)exception).toString();
    }
    
    
    
    /**
     * Handle exceptions thrown by handlers.
     */
    @ExceptionHandler(value = SessionTimeoutException.class)
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    @ResponseBody
    public String onSessionTimeoutException(Exception exception, WebRequest request) {
        return ((SessionTimeoutException)exception).toString();
    }
    
    
    
    /**
     * Handle exceptions thrown by handlers.
     */
    @ExceptionHandler(value = RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String onRuntimeException(Exception exception, WebRequest request) {
    	exception.printStackTrace();    	
        return BaseException.unknowException(HttpStatus.BAD_REQUEST+"",exception);
    }
    
    @ExceptionHandler(value = org.springframework.jdbc.UncategorizedSQLException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String onUncategorizedSQLException(Exception exception, WebRequest request) {
    	exception.printStackTrace();
    	UncategorizedSQLException u = (UncategorizedSQLException)exception;
        return BaseException.unknowException(HttpStatus.BAD_REQUEST+"",u.getSQLException());
    }
    
    
    
    
    /**
     * Handle exceptions thrown by handlers.
     */
    @ExceptionHandler(value = org.springframework.web.multipart.MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String onMultipartException(Exception exception, WebRequest request) {
    	exception.printStackTrace();
        return new BaseException("file.upload.maxsize","上传的文件超过了最大限制。").toString();
    }
    
    
}