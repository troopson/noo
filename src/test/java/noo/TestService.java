/**
 * 
 */
package noo;

import org.junit.Test;

import noo.jdbc.TDao;

/**
 * @author qujianjun   troopson@163.com
 * 2018年5月7日 
 */

public class TestService {

	
	@Test
	public void testGetTableName() {
		TestDao b= new TestDao();
		System.out.println(b.getTableByClassName());
		
	}
	
	
}

class TestDao extends TDao{
	
}
