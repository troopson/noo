/**
 * 
 */
package noo.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;


/**
* @author  瞿建军      
* 
* 创建时间： 2016年6月12日  下午5:43:43
* 
*/
public class BigDecimalUtil {
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
	
	
	public static BigDecimal getBigDecimal(Object o) {
		if (o == null)
			return null;
		else if (o instanceof BigDecimal)
			return (BigDecimal) o;
		else if (o instanceof Number) {
			return new BigDecimal(o.toString());
		}
		else if (o instanceof String) {
			String ss = ((String) o).trim();
			if("".equals(ss))
				return null;
			return new BigDecimal(ss);
		} 
		throw new IllegalArgumentException("非法的数字类型");
	}
	
	
	/**
	 * 
	 * 提供精确的小数位四舍五入处理
	 * 
	 * @param v
	 *            需要四舍五入的数字
	 * 
	 * @param scale
	 *            小数点后保留几位
	 * 
	 * @param round_mode
	 *            指定的舍入模式
	 * 
	 * @return 四舍五入后的结果
	 * 
	 */

	public static double round(double v, int scale, int round_mode)

	{

		if (scale < 0)

		{

			throw new IllegalArgumentException("The scale must be a positive integer or zero");

		}

		BigDecimal b = new BigDecimal(Double.toString(v));

		return b.setScale(scale, round_mode).doubleValue();

	}

	/**
	 * 
	 * 提供精确的小数位四舍五入处理,舍入模式采用ROUND_HALF_EVEN
	 * 
	 * @param v
	 *            需要四舍五入的数字
	 * 
	 * @param scale
	 *            小数点后保留几位
	 * 
	 * @return 四舍五入后的结果，以字符串格式返回
	 * 
	 */

	public static String round(String v, int scale)

	{

		return round(v, scale, BigDecimal.ROUND_HALF_EVEN);

	}

	/**
	 * 
	 * 提供精确的小数位四舍五入处理
	 * 
	 * @param v
	 *            需要四舍五入的数字
	 * 
	 * @param scale
	 *            小数点后保留几位
	 * 
	 * @param round_mode
	 *            指定的舍入模式
	 * 
	 * @return 四舍五入后的结果，以字符串格式返回
	 * 
	 */

	public static String round(String v, int scale, int round_mode)

	{

		if (scale < 0)

		{

			throw new IllegalArgumentException("The scale must be a positive integer or zero");

		}

		BigDecimal b = new BigDecimal(v);

		return b.setScale(scale, round_mode).toString();

	}

	public static double round(BigDecimal value, int bit) {
		if (value == null || (value.doubleValue() > -0.000000001 && value.doubleValue() < 0.000000001))
			return 0;
		return value.divide(new BigDecimal("1"), bit, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static BigDecimal roundToBigDecimal(BigDecimal value, int bit) {
		if (value == null || (value.doubleValue() > -0.000000001 && value.doubleValue() < 0.000000001))
			return new BigDecimal(0.0);
		return value.divide(new BigDecimal("1"), bit, BigDecimal.ROUND_HALF_UP);
	}
	
	
	/**
	 * 将BigDecimal后面的0截去
	 * @param value
	 * @return
	 */
	public static String toPlainString(BigDecimal value){
		if(value==null)
			return null;
	    String temp=value.toPlainString();
	    int pos=temp.indexOf(".");
	    BigDecimal tempN=null;
	    if(pos==-1)
	    	tempN=value; 
	    else
	        tempN= new BigDecimal(temp.substring(0,pos)+"."+StringUtils.stripEnd(temp.substring(pos+1), "0"));
	    return tempN.toPlainString();
	    
	}
	
	

}
