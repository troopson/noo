/**
 * 
 */
package noo.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
* @author  瞿建军      
* 
* 创建时间： 2016年6月12日  下午5:43:43
* 
*/
public class BigDceimalUtil {
	public static BigDecimal HUNDRED = new BigDecimal("100");
	/**
	 * 
	 * @param divisor  除数
	 * @param dividend 被除数
	 * @param newScale 保留小数位
	 * @return dividend/divisor
	 */
	
	public static BigDecimal getPercentage(BigDecimal divisor,BigDecimal dividend,int newScale){
		return dividend.divide(divisor,newScale, BigDecimal.ROUND_HALF_UP).setScale(newScale, BigDecimal.ROUND_HALF_UP);
	}
	/**
	 * 
	 * @param divisor  除数
	 * @param dividend 被除数
	 * @param newScale 保留小数位
	 * @return (dividend/divisor)*100
	 */
	public static BigDecimal getPercentage100(BigDecimal divisor,BigDecimal dividend,int newScale){
		if (divisor.compareTo(BigDecimal.ZERO)<1) {
			return BigDecimal.ZERO;
		}
		BigDecimal rs = dividend.divide(divisor,newScale, BigDecimal.ROUND_HALF_UP).setScale(newScale, BigDecimal.ROUND_HALF_UP).multiply(HUNDRED);
		return rs.setScale(2, BigDecimal.ROUND_HALF_UP);
	}
	
	
	public static BigDecimal sum(List<Map<String,Object>> l, String field){
		BigDecimal s=new BigDecimal(0);
		for(Map<String,Object> m: l){
			Object o=m.get(field);
			if(o==null) {
				continue;
			}
			try{
				BigDecimal d=new BigDecimal(o.toString());
				s=s.add(d);
			}catch(NumberFormatException ne){
				m.remove(field);
			}
			
		}
		return s;
	}
	
	
	

}
