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

	private String table_name=null;
	
	public String tableName() {
		if(this.table_name==null) {
			String s = this.getClass().getSimpleName();
			s = s.replaceAll("(?i)_?DAO$", "");
			this.table_name=  s;
		}
		return this.table_name;
	}
	
	public String sqltext(String sqlid) {
		return sqlholder.getSQL(sqlid);
	}
	
	
	
	public JsonObject getById(Object id){
		String pk = this.jdbc.getSinglePK(this.tableName());
		return jdbc.get(this.tableName(), pk, id);		
	}
	
	public JsonObject getByField(String field, String value){		
		return jdbc.get(this.tableName(), field, value);
	}
	
	public JsonObject getWith(String where, Object[] params) {
		JsonArray r = this.findBy(where, params);
		if(r.isEmpty()) {
			return null;
		}
		return r.getJsonObject(0);
	}
	
	
	public InsertKeyHolder insertRow(JsonObject vs){
		return jdbc.insertRowWithGenkey(this.tableName(), vs);	
	}
	    
	public int insertRow(String fields,JsonObject vs){
		return jdbc.insertRow(this.tableName(),fields, vs);	
	}
	    
	    
	public int insertRow(String fields,Object[] values){
		return jdbc.insertRow(false,this.tableName(), S.splitWithComma(fields), values);
	}
	    
    public int replaceRow(String fields, Object[] values) {
    	return jdbc.insertRow(true, this.tableName(),S.splitWithComma(fields), values);
    }
    
    public int updateRow(String setFields,String conditionFields,  Object[] values){
	    return jdbc.updateRow(this.tableName(), setFields, conditionFields, values);
	}
    

	public int updateRowById(Object idvalue, JsonObject values) {
		String pk = this.jdbc.getSinglePK(this.tableName());
		Object[] vs=new Object[values.size()+1];
		String[] setFields=new String[values.size()];
		int i=0;
		for (Entry<String, Object> item : values) {
			vs[i]=item.getValue();
			setFields[i]=item.getKey();
			i=i+1;
		}
		vs[vs.length-1]=idvalue;
		return jdbc.updateRow(this.tableName(), setFields, new String[] {pk}, vs);		
	}
	 
    public int deleteRow(String condition, Object[] params){
	    return jdbc.deleteRow(this.tableName(), S.splitWithComma(condition), params);
	}
    
    public int deleteById(Object value) {
    	String pk = this.jdbc.getSinglePK(this.tableName());
    	return jdbc.deleteRow(this.tableName(), new String[] {pk}, new Object[] {value});
    }
	 
	 
    @SuppressWarnings("rawtypes")
	public int[] insertAll(List rows) {
    	return this.jdbc.insertAll(this.tableName(), rows);
    }
    
    public JsonArray findAll() {
    	return this.jdbc.qry("select * from "+this.tableName());
    }
    
    public JsonArray findAll(String fields) {
    	return this.jdbc.qry("select "+fields+" from "+this.tableName());
    }
    
    public JsonArray findBy(String whereOrder, Object[] param) {
    	return this.jdbc.qry("select * from "+this.tableName()+" where "+whereOrder, param);
    }
    
    public JsonArray findBy(String fields,String whereOrder, Object[] param) {
    	return this.jdbc.qry("select "+fields+" from "+this.tableName()+" where "+whereOrder, param);
    }

    public PageJsonArray findByPage(String whereOrder, Object[] param, int pageNo, int pageSize) {
    	return this.jdbc.qryByPage("select * from "+this.tableName()+" where "+whereOrder, param, pageNo, pageSize);
    }
    
    public PageJsonArray findByPage(String fields, String whereOrder, Object[] param, int pageNo, int pageSize) {
    	return this.jdbc.qryByPage("select "+fields+" from "+this.tableName()+" where "+whereOrder, param, pageNo, pageSize);
    }
    
    //=========================================================================
    
    public PageJsonArray queryByPage(String sql, Object[] param, int pageNo, int pageSize) {
    	return this.jdbc.qryByPage(sql, param, pageNo, pageSize);
    }    
    public PageJsonArray queryByPageNameParam(String sql, JsonObject param, int pageNo, int pageSize) {
    	param.put(JdbcSvr.PAGE_NO, pageNo);
    	param.put(JdbcSvr.PAGE_SIZE, pageSize);
    	return this.jdbc.qryByPage(sql, param);
    }
    public PageJsonArray queryByPage(String sql, JsonObject param) { 
    	return this.jdbc.qryByPage(sql, param);
    }
    
    public JsonArray query(String sql, Object...param) {
    	return this.jdbc.qry(sql, param);
    }    
    public JsonArray queryByNameParam(String sql, JsonObject param) {
    	return this.jdbc.qry(sql, param);
    }
    
    public JsonObject queryOne(String sql, JsonObject p) {
    	return this.jdbc.qryOneRow(sql, p);
    }
    public JsonObject queryOneRow(String sql, Object...param) {
    	JsonArray ja =this.jdbc.qry(sql, param);
    	if(ja.isEmpty()) {
			return null;
		} else {
			return ja.getJsonObject(0);
		}
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
    
    public boolean hasRow(String sql, Object...p) {
    	return this.jdbc.hasRow(sql, p);
    }
    
    
    
    
	 
	
}
