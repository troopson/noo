/**
 * 
 */
package noo.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import noo.json.JsonArray;
import noo.json.JsonObject;
import noo.json.PageJsonArray;
import noo.util.C;
import noo.util.S;

/**
 * @author 瞿建军
 * 
 *         创建时间： 2016年6月12日 下午5:43:43
 * 
 */

//@ConditionalOnProperty(name = "spring.datasource.url")
@SuppressWarnings("rawtypes")
public class JdbcSvr {

	public static final Logger log = LoggerFactory.getLogger(JdbcSvr.class);

	public static final String PRIMARY_KEY = "uuid";

	// ====================================================
	// SPRING JDBC模板接口
	protected JdbcTemplate jdbcTemplate;
	protected NamedParameterJdbcTemplate named;
	
	

	@Autowired
	public JdbcSvr(DataSource dataSource) {
		log.info("Create JdbcSvr, Inject dataSource "+dataSource.hashCode());
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.named = new NamedParameterJdbcTemplate(this.jdbcTemplate);
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public NamedParameterJdbcTemplate getNamedTemplate() {
		return this.named;
	}

	// =========================insert, update, delete==========================

	public InsertKeyHolder insertRowWithGenkey(String table, JsonObject vs) {
		int size = vs.size();
		String[] keys = new String[size];
		Object[] vals = new Object[size];
		int i = 0;
		for (String key : vs.getMap().keySet()) {
			Object v = vs.getValue(key);
			keys[i] = key;
			vals[i] = v;
			i = i + 1;
		}
		return this.insertRow(table, keys, vals); 
	}
	
	public int insertRow(String table, JsonObject vs) {
		return this.insertRowWithGenkey(table, vs).getInsert_count();
	}
	

	public int insertRow(String table, String fields, JsonObject vs) {
		String[] keys = S.splitWithComma(fields);
		int size = keys.length;
		Object[] vals = new Object[size];

		for (int i = 0; i < size; i++) {
			String key = keys[i].trim();
			Object v = vs.getValue(key);
			if (v == null && PRIMARY_KEY.equalsIgnoreCase(key)) {
				v = C.uid();
			}
			vals[i] = v;
		}
		return this.insertRow(false, table, keys, vals);
	}

	public int insertRow(String table, String fields, Object[] values) {
		return this.insertRow(false, table, S.splitWithComma(fields), values);
	}

	public int replaceRow(String table, String fields, Object[] values) {
		return this.insertRow(true, table, S.splitWithComma(fields), values);
	}

	 
	public int insertRow(boolean replace, String table, String[] fields, Object[] values) {

		StringBuilder sql = buildInsertSQL(replace, table, fields, values); 
		 
		
		return this.getJdbcTemplate().update(sql.toString(), values);

	}
	
	public InsertKeyHolder insertRow(String table, String[] fields, Object[] values) {
		StringBuilder sql = buildInsertSQL(false, table, fields, values); 
		
		InsertKeyHolder kh = new InsertKeyHolder();
		int i = this.getJdbcTemplate().update(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement stat = con.prepareStatement(sql.toString(),Statement.RETURN_GENERATED_KEYS);  
				ArgumentPreparedStatementSetter as = new ArgumentPreparedStatementSetter(values);
				as.setValues(stat);
				return stat;
			}
			
		}, kh);
		kh.setInsert_count(i);
		
		return kh;
		
	}

	private StringBuilder buildInsertSQL(boolean replace, String table, String[] fields, Object[] values) {
		StringBuilder sql = null;
		if (replace) {
			sql = new StringBuilder("replace into ");
		} else {
			sql = new StringBuilder("insert into ");
		}

		sql.append(table).append(" (");

		StringBuilder valpiece = new StringBuilder(" values(");
		int length = fields.length, vallength = values.length;
		if (length != vallength) {
			throw new IllegalArgumentException("字段数量和参数数量不一致！");
		}

		for (int i = 0; i < length; i++) {
			String afield = fields[i];
			if (i != 0) {
				sql.append(",");
				valpiece.append(",");
			}
			sql.append(afield);
			valpiece.append("?");
		}

		sql.append(") ").append(valpiece).append(")");
		log.debug(sql.toString()+"   "+C.printArray(values));
		return sql;
	}

	public int updateRow(String table, String setFields,String conditionFields,  Object[] values) {
		return this.updateRow(table, S.splitWithComma(setFields), conditionFields.split(","), values);
	}
	

	// update xt_role set code='s' ,name='d' where uuid='a'
	public int updateRow(String table, String[] setFields, String[] conditionFields,  Object[] values) {

		StringBuilder sql = new StringBuilder("update ").append(table).append(" set ");
		int condlength = conditionFields.length;
		int fieldslength = setFields.length;

		// 添加 set

		for (int j = 0; j < fieldslength; j++) {
			String afield = setFields[j];
			if (j != 0) {
				sql.append(",");
			}
			sql.append(afield).append("= ?");
		}
		sql.append(" where ");

		for (int i = 0; i < condlength; i++) {
			String afield = conditionFields[i];
			if (i != 0) {
				sql.append(" and ");
			}
			sql.append(afield).append("=?");
		}

		log.info(sql.toString() + "  " + C.printArray(values));
		return this.getJdbcTemplate().update(sql.toString(), values);

	}

	public int deleteRow(String table, String condition, Object[] params) {
		return this.deleteRow(table, S.splitWithComma(condition), params);
	}

	// delete from xt_role where uuid='a' and code='b'
	public int deleteRow(String table, String[] condition, Object[] params) {

		StringBuilder sql = new StringBuilder("delete from ").append(table).append(" where ");
		int condlength = condition.length;

		for (int i = 0; i < condlength; i++) {
			String afield = condition[i];
			if (i != 0) {
				sql.append(" and ");
			}
			sql.append(afield).append("=?");
		}
		log.debug(sql.toString() + "  " + C.printArray(params));
		return this.getJdbcTemplate().update(sql.toString(), params);

	}

	public JsonObject get(String table, Object uuid) {
		return this.get(table, "uuid", uuid);
	}

	@SuppressWarnings("unchecked")
	public JsonObject get(String table, String pkfield, Object uuid) {
		List l = this.getJdbcTemplate().queryForList("select * from " + table + " where " + pkfield + "=?",
				new Object[] { uuid });
		if (l == null || l.isEmpty()) {
			return null;
		}
		return new JsonObject((Map<String, Object>) l.get(0));
	}

	public int execute(String sql, Object... obj) {
		return this.getJdbcTemplate().update(sql, obj);

	}

	// ===============================batch======================================

	@SuppressWarnings("unchecked")
	public int[] insertAll(String table, List rows) {
		String tableFields = this.allField(table);
		return this.batchInsert(table, tableFields, rows);
	}

	@SuppressWarnings("unchecked")
	public int[] batchInsert(String table, String fields, List<Map> params) {
		if (S.isBlank(fields)) {
			throw new NullPointerException();
		}

		if (params == null || params.isEmpty()) {
			return null;
		}

		List<Object[]> pv = new ArrayList<>();
		String[] fs = S.splitWithComma(fields);
		int length = fs.length;

		String sql = this.createBatchInsertSql(table, fs);

		for (Map<String, Object> row : params) {
			Object[] onerow = new Object[length];
			for (int i = 0; i < length; i++) {
				String key = fs[i].trim();
				Object value = row.get(key);
				if (value == null && PRIMARY_KEY.equalsIgnoreCase(key)) {
					value = C.uid();
				}

				onerow[i] = value;
			}
			pv.add(onerow);
		}
		log.debug(sql);
		return this.getJdbcTemplate().batchUpdate(sql, pv);
	}


	private String createBatchInsertSql(String table, String[] fields) {
		StringBuilder sql = new StringBuilder("insert into ").append(table).append(" (");
		StringBuilder valpiece = new StringBuilder(" values(");

		for (int i = 0; i < fields.length; i++) {
			String afield = fields[i];
			if (i != 0) {
				sql.append(",");
				valpiece.append(",");
			}
			sql.append(afield);
			valpiece.append("?");
		}

		sql.append(") ").append(valpiece).append(")");
		// log.debug(sql.toString());
		return sql.toString();
	}

	// ============================select===========================================

	public void forEach(String sql, JsonObject param, Consumer<JsonObject> c) {
		String newsql = SqlUtil.processParam(sql, param==null?null:param.getMap());
		this.getNamedTemplate().query(newsql, param==null?null:param.getMap(), new RowCallbackHandler() {

			private MyColumnMapRowMapper rm= new MyColumnMapRowMapper();
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				JsonObject j = rm.mapRow(rs, 0);
				c.accept(j);
			}
			
		} );
	}
	
	public JsonArray qry(String sql) {
		return new JsonArray(this.getJdbcTemplate().query(sql, new MyColumnMapRowMapper()));
	}

	public JsonArray qry(String sql, Object... p) {
		if (p == null || p.length == 0) {
			return new JsonArray(this.getJdbcTemplate().query(sql, new MyColumnMapRowMapper()));
		} else {
			return new JsonArray(this.getJdbcTemplate().query(sql, p, new MyColumnMapRowMapper()));
		}
	}

	public JsonArray qry(String sql, JsonObject param) {		
		String newsql = SqlUtil.processParam(sql, param==null?null:param.getMap());
		log.debug(newsql+"  "+param.encode());
		return new JsonArray(this.getNamedTemplate().query(newsql, param==null?null:param.getMap(), new MyColumnMapRowMapper()));
	}

	public JsonObject qryOneRow(String sql, JsonObject param) {

		JsonArray lm = this.qry(sql, param);
		if (lm == null || lm.isEmpty()) {
			return null;
		}
		return lm.getJsonObject(0);
	}
	
	public JsonObject qryOne(String sql, Object...param) {

		JsonArray lm = this.qry(sql, param);
		if (lm == null || lm.isEmpty()) {
			return null;
		}
		return lm.getJsonObject(0);
	} 
	

	public String qryString(String sql, Object... p) {
		try {
			return this.getJdbcTemplate().queryForObject(sql, p, String.class);
		} catch (IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}

	public String qryString(String sql, JsonObject param) {

		JsonObject m = this.qryOneRow(sql, param);
		if (m == null || m.isEmpty()) {
			return null;
		}
		return (String)m.iterator().next().getValue();
	}

	public Number qryNumber(String sql, JsonObject param) {

		JsonObject m = this.qryOneRow(sql, param);
		if (m == null || m.isEmpty()) {
			return -1;
		}
		return (Number) m.iterator().next().getValue();
	}

	public Integer qryInt(String sql, Object... p) {
		try {
			return this.getJdbcTemplate().queryForObject(sql, p, Integer.class);
		} catch (IncorrectResultSizeDataAccessException e) {
			return -1;
		}
	}

	public boolean hasRow(String sql, Object... p) {

		List l = this.getJdbcTemplate().queryForList(sql, p);
		if (l == null || l.isEmpty()) {
			return false;
		}
		return true;
	}

	// ==============page===支持两种变量名称的获取==========================
    
	public static final String PAGE_NO = "pageno";
	public static final String PAGE_SIZE = "pagesize";

	public static final String OFFSET = "offset";
	public static final String LIMIT = "limit";
	
	
	public static int[] getPageSizePageNo(JsonObject param) {
		Integer pageSize = param.getInteger(PAGE_SIZE);
		if (pageSize==null) {
			pageSize = param.getInteger(LIMIT);
		}
		if (pageSize == null) {
			pageSize = 50;
		}
		
		Integer pageNo = param.getInteger(PAGE_NO);
		if (pageNo == null) {
			Integer offset = param.getInteger(OFFSET);
			if(offset!=null) {
				pageNo = 1 + offset / pageSize;
			}
		}

		if (pageNo == null || pageNo < 1 ) {
			pageNo = 1;
		}
		
		return new int[] {pageSize, pageNo};
	}
	
	//==========================================================
	 
	public PageJsonArray qryByPage(String sql, JsonObject param) {
		return this.qryByPage(sql, param, true);
	}

	public PageJsonArray qryByPage(String sql, JsonObject param, boolean isQueryTotal) {

		int[] pageSizeNo = getPageSizePageNo(param);
		
		Integer pageSize = pageSizeNo[0]; 
		Integer pageNo = pageSizeNo[1];

		String newsql = SqlUtil.processParam(sql, param.getMap());
		JdbcSvr.log.debug(newsql+"   "+param.encode());
		PageQuery page = new PageQuery(newsql.toString(), param.getMap(), pageNo, pageSize, this.getNamedTemplate(),isQueryTotal);
		page.getResultList();
		return new PageJsonArray(page);
	}

	public PageJsonArray qryByPage(String sql, Object[] params, int pageNo, int pageSize) {
		PageQuery page = new PageQuery(sql, params, pageNo, pageSize, this.getJdbcTemplate(),true);
		page.getResultList();
		return new PageJsonArray(page);
	}
	
	public JsonObject qryMoreRowStartFrom(String sql, JsonObject param, String byField) {
		int[] pageSizeNo = getPageSizePageNo(param);
		
		Integer pageSize = pageSizeNo[0];   
		
		if(sql.toLowerCase().indexOf(" limit ") == -1)
			sql = sql +" limit "+pageSize;
		JsonArray jary = this.qry(sql, param);
		return this.doReturnMore(jary, pageSize, byField); 
	}
	
	public JsonObject qryMoreRowStartFrom(String sql, Object[] params, int pageSize, String byField) {
		if(sql.toLowerCase().indexOf(" limit ") == -1)
			sql = sql +" limit "+pageSize;
		JsonArray jary = this.qry(sql, params);
		return this.doReturnMore(jary, pageSize, byField);
	}
	
	private JsonObject doReturnMore(JsonArray jary, int pageSize, String byField) { 
		JsonObject result = new JsonObject();
		if(jary==null || jary.isEmpty()) {
			jary= jary==null? new JsonArray(): jary;
		    result.put("is_end", 0);
		}else {
			int size =  jary.size();
		    JsonObject jo = jary.getJsonObject(size-1); 
		    result.put("maxid", jo.getValue(byField));
		    if(size < pageSize)
				result.put("is_end", 0);
		}
		result.put("content", jary); 
		return result;
	}

	// ================================meta=============================

	private static Map<String, String> tableInfo = new HashMap<>();
	private static Map<String, Set<String>> tablepks = new HashMap<>();
 
    public String allField(String table) {
		String t = table.toLowerCase();
		if (tableInfo.containsKey(t)) {
			return tableInfo.get(t);
		}
		final List<String> colname = new ArrayList<>();
		String sql= "select * from " + table + " where 1=2";
		getSQLMeta(sql, j->{
			colname.add(j.getString("columnName"));
		});
		String fs = C.join(colname, ",");
		tableInfo.put(t, fs);
		return fs;

	}

	@SuppressWarnings("unchecked")
	public void getSQLMeta(String sql, Consumer<JsonObject> c) { 
		
		 this.getJdbcTemplate().query(sql, new ResultSetExtractor() {
			@Override
			public Object extractData(ResultSet rs) throws SQLException, DataAccessException {

				ResultSetMetaData meta = rs.getMetaData();
				int colnum = meta.getColumnCount();
				for (int i = 0; i < colnum; i++) {
					JsonObject j = new JsonObject();
					j.put("columnName", meta.getColumnName(i + 1));
					j.put("columnType", meta.getColumnType(i + 1));
					j.put("columnDisplaySize", meta.getColumnDisplaySize(i + 1));
					c.accept(j);
				} 
				return null;
			} 
		}); 
	}

	String getSinglePK(String tableName) {
		Set<String> s = getPkNames(tableName);
		if(s==null || s.isEmpty()) {
			return null;
		}
		return s.iterator().next();
	}
	
	public Set<String> getPkNames(String tableName) {
		final String lowerTable = tableName.toLowerCase();
		if(tablepks.containsKey(lowerTable)) {
			return tablepks.get(lowerTable);
		}
		
		Set<String> pks = this.getJdbcTemplate().execute(new ConnectionCallback<Set<String>>() {

			@Override
			public Set<String> doInConnection(Connection con) throws SQLException, DataAccessException {

				try {
					ResultSet rs = con.getMetaData().getPrimaryKeys(con.getCatalog(), null, lowerTable);
					Set<String> pks = new HashSet<>();
					while (rs.next()) {
						if (pks == null) {
							pks = new HashSet<String>();
						}
						String s = rs.getString("COLUMN_NAME");
						pks.add(s);
					}
					return pks;
				} catch (SQLException e) {
					return null;
				}
			}

		});
		if(pks!=null) {
			tablepks.put(lowerTable, pks);
		}
		
		return pks;
	}

	// ===================================================================

	public String appendOrderby(String sql, String tableAlias, String orderby, String asc, String defaultFiled,
			String chnfields) {
		if (S.isNotBlank(orderby) && !SqlUtil.isInjection(orderby)) {

			if (orderby.indexOf("(") > 0) {
				sql = sql + "order by " + orderby;
			} else {
				sql = sql + "order by " + SqlUtil.convertChn(tableAlias, orderby, chnfields);
			}

			if (S.isNotBlank(asc) && !SqlUtil.isInjection(asc)) {
				sql = sql + " " + asc;
			}
		} else if (S.isNotBlank(defaultFiled)) {
			if (defaultFiled.indexOf("(") > 0) {
				sql = sql + "order by " + defaultFiled;
			} else {
				sql = sql + "order by " + SqlUtil.convertChn(tableAlias, orderby, defaultFiled);
			}

			if (S.isNotBlank(asc) && !SqlUtil.isInjection(asc)) {
				sql = sql + " " + asc;
			}
		}
		return sql;
	}

	public String appendOrderby(String sql, String tableAlias, String orderby, String asc, String defaultFiled) {
		return appendOrderby(sql, tableAlias, orderby, asc, defaultFiled, null);
	}
	
	//=============================================

}
