/**
 * 
 */
package noo.exception;

/**
* @author  瞿建军      
* 
* 创建时间： 2016年6月13日  下午2:37:46
* 
*/
public class BaseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private String code=null;
	
	public BaseException(String code,String msg){
		super(msg);
		this.code=code;
	}
	
	@Override
	public String toString(){
		return "{\"code\":\""+this.code+"\", \"msg\":\""+this.getMessage()+"\" }";
	}
	
	public static String unknowException(String code, Exception e){
		return "{\"code\":\""+code+"\", \"msg\":\""+e.getMessage()+"\" }";
	}
	
}
