/**
 * 
 */
package noo.data;

import java.util.ArrayList;
import java.util.List;

import noo.json.JsonArray;

/**
 * @author qujianjun   troopson@163.com
 * Nov 1, 2019 
 */
public class DataSet {
	 
	
	private List<IProcessUnit> unit = new ArrayList<>(); 
	
	
	public void addUnit(IProcessUnit u) {
		this.unit.add(u);
	}
	
	
	public void transform(JsonArray ja) {
		if(unit.isEmpty())
			return;
		
		unit.forEach(u->u.before(ja));
		
		ja.forEachJsonObject(jo->{
			
			int i = jo.getInteger(RowTag.TAG_ROW_CUE, -1);
			if(i!=-1) {  //说明不是数据行
				unit.forEach(u->u.processFlagRow(jo)); 
			}else {
				unit.forEach(u->u.processRow(jo)); 
			}
		});
		
		unit.forEach(u->u.end(ja)); 
		
	}

}
