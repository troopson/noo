/**
 * 
 */
package noo.data;

import noo.json.JsonArray;
import noo.json.JsonObject;

/**
 * @author qujianjun   troopson@163.com
 * 数据处理接口
 * Nov 1, 2019 
 */
public interface IProcessUnit {
	 
	
	public void before(JsonArray ja);
	
	public void processRow(JsonObject j);
	
	public void end(JsonArray ja);
	
	//处理标志行
	public default void processFlagRow(JsonObject fl) {
		return;
	}
	

}
