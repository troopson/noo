/**
 * 
 */
package noo.data;

import noo.json.JsonArray;
import noo.json.JsonObject;

/**
 * @author qujianjun   troopson@163.com
 * Jan 2, 2020 
 */
public class RowIdxUnit implements IProcessUnit {
	
	public static final String IDX_ROWKEY="_row_idx";
	
	
	private int idx = 1;

	@Override
	public void before(JsonArray ja) { 
		
	}

	@Override
	public void processRow(JsonObject j) { 
		j.put(IDX_ROWKEY, idx);
		idx = idx+1;
	}

	@Override
	public void end(JsonArray ja) {
		
	}

}
