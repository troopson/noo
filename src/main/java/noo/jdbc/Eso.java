/**
 * 
 */
package noo.jdbc;

import java.util.LinkedHashMap;

/**
 * @author qujianjun   troopson@163.com
 * Sep 5, 2020 
 * 可执行的sql对象
 */
public class Eso {

	private String _sql;
	private LinkedHashMap<String,Object> _params;
	
	public Eso() {
	}
	
	public Eso(String sql, LinkedHashMap<String,Object> param) {
		this._sql = sql;
		this._params=param;
	}
	
	public void setSQL(String sql) {
		this._sql = sql;
	}
	
	public void addParam(String pn, Object pv) {
		if(this._params==null)
			this._params = new LinkedHashMap<>();
		this._params.put(pn, pv);
	}
	
	public String sql() {
		return this._sql;
	}
	
	public Object[] pvs() {
		if(this._params==null)
			return null;
		return this._params.values().toArray();
	}
	
	public String[] pns() {
		if(this._params==null)
			return null;
		return this._params.keySet().toArray(new String[] {});
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(this._sql);
		if(this._params!=null) {
			sb.append("\r\n");
			this._params.forEach((k,v)->{
				sb.append(k+":"+v.toString()).append(" ; ");
			});
		}
		return sb.toString();
	}
	

	
}
