/**
 * 
 */
package noo.rest.security.processor.unify;

/**
 * @author qujianjun   troopson@163.com
 * Jul 28, 2020 
 */
public interface UnifiyLoginDefinition {

	
	public String getSystemRedirectUrl(String client);
	
	public boolean isUnifyLoginUrl(String path);
	
	public boolean isUnifyLogoutUrl(String path);
	
	
}
