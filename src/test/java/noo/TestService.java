/**
 * 
 */
package noo;

import org.junit.Test;

import noo.jdbc.TDao;
import noo.web.NRemote;

/**
 * @author qujianjun   troopson@163.com
 * 2018年5月7日 
 */

public class TestService {

	
	@Test
	public void testGetTableName() {
		TestDao b= new TestDao();
		System.out.println(b.tableName());
		
	}
	
	@Test
	public void testMakeNooUrl() {
		NRemote ns= new NRemote();
		String s = ns.makeNRemoteUrl("test", "com.bean.TestService.testMakeNooUrl");
		System.out.println(s);
	}
	
	
}

class TestDao extends TDao{
	
}
