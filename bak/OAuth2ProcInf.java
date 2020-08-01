/**
 * 
 */
package noo.rest.security.processor;

import javax.servlet.http.HttpServletRequest;

/**
 * @author qujianjun   troopson@163.com
 * 2018年11月16日 
 */
public interface OAuth2ProcInf {

	public boolean checkLogin(HttpServletRequest resq);
	
	public String transferHtml(String html);
	
}
