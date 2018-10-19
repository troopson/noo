/**
 * 
 */
package noo.event.security;

import java.io.Serializable;

/*
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
*/
import noo.json.JsonObject;
import noo.rest.security.AbstractUser;

/**
 * @author qujianjun troopson@163.com 2018年8月21日
 */
public class User extends AbstractUser implements Serializable {

	private static final long serialVersionUID = 1L;

	private int userid;
	private String code;
	private String name;

	public JsonObject toPrincipal() {
		JsonObject jo = new JsonObject();
		jo.put("userid", this.userid);
		jo.put("code", this.code);
		jo.put("name", this.name);
		
		return jo;
	}
	
	public int getUserid() {
		return userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

 
	@Override
	public JsonObject toResponseJsonObject() { 
		return this.toPrincipal();
	}

	@Override
	public JsonObject toJsonObject() { 
		return this.toPrincipal();
	}

	
	

}
