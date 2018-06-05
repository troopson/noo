/**
 * 
 */
package noo.web;

import javax.annotation.Resource;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import noo.json.JsonArray;
import noo.json.JsonObject;

/**
 * @author qujianjun   troopson@163.com
 * 2018年5月28日 
 */



public class NRemote { 
	
	@Resource(name = "noo_rest")
	private RestTemplate rest;
	 
	public String getString(String sn,String beanMethod,JsonObject jsonobject) {
		HttpEntity<MultiValueMap<String, String>>  m = buildHttpEntity(jsonobject);
		return rest.postForObject(this.makeNRemoteUrl(sn, beanMethod), m, String.class);
	}
	
	public Number getNumber(String sn,String beanMethod,JsonObject jsonobject) {
		HttpEntity<MultiValueMap<String, String>>  m = buildHttpEntity(jsonobject);
		return rest.postForObject(this.makeNRemoteUrl(sn, beanMethod), m, Number.class);
	}
	
	public JsonObject getJsonObject(String sn,String beanMethod,JsonObject jsonobject) {
		HttpEntity<MultiValueMap<String, String>>  m = buildHttpEntity(jsonobject);
		return rest.postForObject(this.makeNRemoteUrl(sn, beanMethod), m, JsonObject.class);
	}
	
	public JsonArray getJsonArray(String sn,String beanMethod,JsonObject jsonobject) {
		HttpEntity<MultiValueMap<String, String>>  m = buildHttpEntity(jsonobject); 
		return rest.postForObject(this.makeNRemoteUrl(sn, beanMethod), m, JsonArray.class);
	}
	
	private HttpEntity<MultiValueMap<String, String>> buildHttpEntity(JsonObject jsonobject) {
		HttpHeaders headers = new HttpHeaders();  
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);  
  
        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();  
        jsonobject.forEach(entry->{
        	map.add(entry.getKey(), entry.getValue()==null?"":entry.getValue().toString());
        });
        
        return new HttpEntity<MultiValueMap<String, String>>(map,null);  
	}
	
	public String makeNRemoteUrl(String sn, String beanMethod) {
		int pos = beanMethod.lastIndexOf(".");
		String bn = beanMethod.substring(0, pos)+"/"+beanMethod.substring(pos+1);
		return "http://"+sn+"/nooremote/"+bn;
	}
	

	
	
}
