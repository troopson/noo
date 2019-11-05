/**
 * 
 */
package noo.data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import noo.json.JsonArray;
import noo.json.JsonObject;
import noo.util.BigDecimalUtil;
import noo.util.S;

/**
 * @author qujianjun   troopson@163.com
 * Oct 24, 2019 
 */
public class SumCount {
	
	public static JsonArray add(JsonArray result, JsonObject srow, String sumfields) {
		return add(result, srow, sumfields,null,2);
	}
	
	
	
	public static JsonArray add(JsonArray result, JsonObject srow, String sumfields, String countfields, int scale) {
		if(result==null || (S.isBlank(sumfields) && S.isBlank(countfields)) )
			return result; 
		
		final JsonObject sumRow= srow==null? new JsonObject(): srow;
		
		Map<String,BigDecimal> Svals= new HashMap<>();
		Map<String,Set<String>> Cvals= new HashMap<>();
		if(sumfields!=null) 
			S.split(sumfields, ",").forEach(s->Svals.put(s, new BigDecimal(0)));
		
		if(countfields!=null)
			S.split(countfields, ",").forEach(c->Cvals.put(c, new HashSet<>()));
		
		result.forEachJsonObject(c->{
			int i = c.getInteger(RowTag.TAG_ROW, -1);
			if(i!=-1)  //说明不是数据行
				return;
			
			if(sumfields!=null) 
				Svals.forEach((k,v)->{ 
					Svals.put(k,add(v,c.getValue(k))); 
				});
			if(countfields!=null)
				Cvals.forEach((k,v)->{
					String t = c.getString(k);
					if(t!=null)
						v.add(t);
				});
		});
		if(sumfields!=null) 
			Svals.forEach((k,v)->sumRow.put(k, BigDecimalUtil.round(v,scale)));
		if(countfields!=null)
			Cvals.forEach((k,v)->sumRow.put(k, v.size()));
		
		result.add(sumRow);
		
		return result;
		
	}
	
	public static BigDecimal add(BigDecimal b, Object o) {
	
		BigDecimal a = BigDecimalUtil.getBigDecimal(o);
		if(a==null)
			return b;
		return b.add(a);
	}

}
