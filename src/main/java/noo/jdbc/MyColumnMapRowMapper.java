package noo.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;

import noo.json.JsonObject;
 
public class MyColumnMapRowMapper implements RowMapper<JsonObject> {

	@Override
	public JsonObject mapRow(ResultSet rs, int rowNum) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		Map<String, Object> mapOfColValues = createColumnMap(columnCount);
		for (int i = 1; i <= columnCount; i++) {
			String key = getColumnKey(JdbcUtils.lookupColumnName(rsmd, i));
			Object obj = getColumnValue(rs, i);
			mapOfColValues.put(key.toLowerCase(), obj);
		}
		return new JsonObject(mapOfColValues);
	}

	/**
	 * Create a Map instance to be used as column map.
	 * <p>By default, a linked case-insensitive Map will be created.
	 * @param columnCount the column count, to be used as initial
	 * capacity for the Map
	 * @return the new Map instance
	 * @see org.springframework.util.LinkedCaseInsensitiveMap
	 */
	protected Map<String, Object> createColumnMap(int columnCount) {
		return new LinkedCaseInsensitiveMap<Object>(columnCount);
	}

	/**
	 * Determine the key to use for the given column in the column Map.
	 * @param columnName the column name as returned by the ResultSet
	 * @return the column key to use
	 * @see java.sql.ResultSetMetaData#getColumnName
	 */
	protected String getColumnKey(String columnName) {
		return columnName;
	}

	/**
	 * Retrieve a JDBC object value for the specified column.
	 * <p>The default implementation uses the {@code getObject} method.
	 * Additionally, this implementation includes a "hack" to get around Oracle
	 * returning a non standard object for their TIMESTAMP datatype.
	 * @param rs is the ResultSet holding the data
	 * @param index is the column index
	 * @return the Object returned
	 * @see org.springframework.jdbc.support.JdbcUtils#getResultSetValue
	 */
	protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
		return JdbcUtils.getResultSetValue(rs, index);
	}

}
