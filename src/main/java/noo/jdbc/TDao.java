/**
 * 
 */
package noo.jdbc;

import java.util.List;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;

import noo.json.JsonArray;
import noo.json.JsonObject;
import noo.json.PageJsonArray;
import noo.util.S;

/**
 * @author qujianjun   troopson@163.com
 * 2016年5月7日 
 */

public abstract class TDao {

	@Autowired
	protected JdbcSvr jdbc;

	@Autowired
	private SQLHolder sqlholder;

	private String tableName=null;
	
	public String getTableByClassName() {
		if(this.tableName==null) {
			String s = this.getClass().getSimpleName();
			s = s.replaceAll("(?i)_?DAO$", "");
			this.tableName=  s;
		}
		return this.tableName;
	}
	
	protected String sqltext(String sqlid) {
		return sqlholder.getSQL(sqlid);
	}
	
	
	
	public JsonObject getById(Object id){
		String pk = this.jdbc.getSinglePK(this.getTableByClassName());
		return jdbc.get(this.getTableByClassName(), pk, id);		
	}
	
	public JsonObject getByField(String field, String value){		
		return jdbc.get(this.getTableByClassName(), field, value);
	}
	
	public int insertRow(JsonObject vs){
		return jdbc.insertRow(this.getTableByClassName(), vs);				
	}
	    
	public int insertRow(String fields,JsonObject vs){
		return jdbc.insertRow(this.getTableByClassName(),fields, vs);	
	}
	    
	    
	public int insertRow(String fields,Object[] values){
		return jdbc.insertRow(false,this.getTableByClassName(), S.splitWithComma(fields), values);
	}
	    
    public int replaceRow(String fields, Object[] values) {
    	return jdbc.insertRow(true, this.getTableByClassName(),S.splitWithComma(fields), values);
    }
    
    public int updateRow(String setFields,String conditionFields,  Object[] values){
	    return jdbc.updateRow(this.getTableByClassName(), setFields, conditionFields, values);
	}
    

	public int updateRowById(Object idvalue, JsonObject values) {
		String pk = this.jdbc.getSinglePK(this.getTableByClassName());
		Object[] vs=new Object[values.size()+1];
		String[] setFields=new String[values.size()];
		int i=0;
		for (Entry<String, Object> item : values) {
			vs[i]=item.getValue();
			setFields[i]=item.getKey();
			i=i+1;
		}
		vs[vs.length-1]=idvalue;
		return jdbc.updateRow(this.getTableByClassName(), setFields, new String[] {pk}, vs);		
	}
	 
    public int deleteRow(String condition, Object[] params){
	    return jdbc.deleteRow(this.getTableByClassName(), S.splitWithComma(condition), params);
	}
    
    public int deleteById(Object value) {
    	String pk = this.jdbc.getSinglePK(this.getTableByClassName());
    	return jdbc.deleteRow(this.getTableByClassName(), new String[] {pk}, new Object[] {value});
    }
	 
	 
    @SuppressWarnings("rawtypes")
	public int[] insertAll(List rows) {
    	return this.jdbc.insertAll(this.getTableByClassName(), rows);
    }
    
    public JsonArray findAll() {
    	return this.jdbc.qry("select * from "+this.getTableByClassName());
    }
    
    public JsonArray findBy(String whereOrder, Object[] param) {
    	return this.jdbc.qry("select * from "+this.getTableByClassName()+" where "+whereOrder, param);
    }

    public PageJsonArray findByPage(String whereOrder, Object[] param, int pageNo, int pageSize) {
    	return this.jdbc.qryByPage("select * from "+this.getTableByClassName()+" where "+whereOrder, param, pageNo, pageSize);
    }
    
    //=========================================================================
    
    public PageJsonArray queryByPage(String sql, Object[] param, int pageNo, int pageSize) {
    	return this.jdbc.qryByPage(sql, param, pageNo, pageSize);
    }    
    public PageJsonArray queryByPage2(String sql, JsonObject param, int pageNo, int pageSize) {
    	param.put(JdbcSvr.PageNo, pageNo);
    	param.put(JdbcSvr.PageSize, pageSize);
    	return this.jdbc.qryByPage(sql, param);
    }
    
    public JsonArray query(String sql, Object...param) {
    	return this.jdbc.qry(sql, param);
    }    
    public JsonArray queryByNameParam(String sql, JsonObject param) {
    	return this.jdbc.qry(sql, param);
    }
    
    public String queryString(String sql,Object...p) {
    	return this.jdbc.qryString(sql, p);
    }     
    public String queryStringByNameParam(String sql,JsonObject p) {
    	return this.jdbc.qryString(sql, p);
    }
    
    public Integer queryInt(String sql, Object...p) {
    	return this.jdbc.qryInt(sql, p);
    }
    public Integer queryIntByNameParam(String sql, JsonObject p) {
    	return this.jdbc.qryInt(sql, p);
    }
    
    
    
    
	 
	
}
