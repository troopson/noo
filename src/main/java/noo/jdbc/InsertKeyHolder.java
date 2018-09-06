/**
 * 
 */
package noo.jdbc;

import org.springframework.jdbc.support.GeneratedKeyHolder;

/**
 * @author qujianjun   troopson@163.com
 * 2018年9月6日 
 */
public class InsertKeyHolder extends GeneratedKeyHolder {

	private int insert_count=0;

	public int getInsert_count() {
		return insert_count;
	}

	public void setInsert_count(int insert_count) {
		this.insert_count = insert_count;
	}
	
	
	
	
}
