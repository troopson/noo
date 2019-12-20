/**
 * 
 */
package noo.exception;

import noo.json.JsonObject;

/**
* @author  瞿建军      
* 
* 创建时间： 2016年6月13日  下午2:37:46
* 
*/
public class BaseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	protected String code=null;
	
	public BaseException(String code,String msg){
		super(msg);
		this.code=code;
	}
	
	@Override
	public String toString(){
		JsonObject j = new JsonObject();
		j.put("code", this.code);
		j.put("message", this.getMessage());
		return j.encode();
	}
	
	public static String unknowException(String code, Exception e){
		JsonObject j = new JsonObject();
		j.put("code", code);
		j.put("message", e.getMessage());
		return j.encode(); 
	}
	
}
