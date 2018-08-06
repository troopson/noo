/**
 * 
 */
package noo;

import org.springframework.stereotype.Repository;

import noo.jdbc.TDao;

/**
 * @author qujianjun   troopson@163.com
 * 2018年5月14日 
 */
@Repository
public class BookDao extends TDao {

	public static String replace(String s) {
		return s.replaceAll("(?i)_?DAO$", "");		 
	}
	
	public static void main(String[] args) {
		
		String a ="testDao";
		String b ="a_dao";
		String c ="a_Dao";
		
		System.out.println(replace(a));
		System.out.println(replace(b));
		System.out.println(replace(c));
		
	}
	
}
