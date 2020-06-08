package noo.util;

import java.util.HashMap;
import java.util.Map;

import net.dongliu.requests.Requests;
import noo.json.JsonObject;

/**
 * 报警工具类
 * @author qujianjun   troopson@163.com
 * Jun 8, 2020
 */
 
public class A {
 

	public static final String DINGDING_ROBOT="alert.dingding";
	public static String dingding_url;
	
	public static void dingding(String msg) {
		if(dingding_url==null) {
			String res =SpringContext.getProperty(DINGDING_ROBOT);
			dingding_url = (res==null)?"None":res;
		}
		if("None".equals(dingding_url))
			return;
		
		JsonObject param = new JsonObject();
		param.put("msgtype", "text");
		JsonObject message = new JsonObject();
		message.put("content", msg);
		param.put("text", message);
		Map<String, String> header = new HashMap<>();
		header.put("Content-Type", "application/json"); 
	
		try {
			Requests.post(dingding_url).headers(header).body(param.encode()).send();
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
	}

}
