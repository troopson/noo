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
 * 实现增加合计行的功能
 * @author qujianjun   troopson@163.com
 * Nov 4, 2019 
 */
public class SumCountUnit implements IProcessUnit {

	
	public static BigDecimal add(BigDecimal b, Object o) {
		
		BigDecimal a = BigDecimalUtil.getBigDecimal(o);
		if(a==null)
			return b;
		return b.add(a);
	}
	
	//---------------------------------------
	
	private JsonObject srow;
	private Map<String,BigDecimal> Svals;
	private Map<String,Set<String>> Cvals;
	private int scale=2;
	
	public SumCountUnit(String sumfields, String countfields) {
		if( S.isBlank(sumfields) && S.isBlank(countfields) )
			throw new NullPointerException("SumCountUnit: sumfield and countfield can not be null at same time.");
		
		if(sumfields!=null) { 
		    Svals= new HashMap<>();
		    S.split(sumfields, ",").forEach(s->Svals.put(s, new BigDecimal(0)));
		}
		if(countfields!=null) { 
		    Cvals= new HashMap<>();
			S.split(countfields, ",").forEach(c->Cvals.put(c, new HashSet<>()));
		}
		
	}
	
	public void setScale(int i) {
		this.scale = i;
	}
	
	public void setSumRow(JsonObject sumrow) {
		this.srow = sumrow;
	}
	
	
	@Override
	public void before(JsonArray ja) { 
		if(this.srow==null)
			this.srow = new JsonObject(); 
	}

	@Override
	public void processRow(JsonObject j) {
		if(Svals!=null) 
			Svals.forEach((k,v)->{ 
				Svals.put(k,add(v,j.getValue(k))); 
			});
		if(Cvals!=null)
			Cvals.forEach((k,v)->{
				String t = j.getString(k);
				if(t!=null)
					v.add(t);
			}); 
	}

	@Override
	public void end(JsonArray ja) {
		if(Svals!=null) 
			Svals.forEach((k,v)->srow.put(k, BigDecimalUtil.round(v,scale)));
		if(Cvals!=null)
			Cvals.forEach((k,v)->srow.put(k, v.size()));
		
		ja.add(srow);
		 
	} 
	

}
