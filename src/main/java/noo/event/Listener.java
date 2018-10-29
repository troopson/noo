/**
 * 
 */
package noo.event;

/**
 * @author 瞿建军       Email: troopson@163.com
 * 2016年10月10日
 * 
 * 继承这个的类，然后加上Component的注解，就可以通过Event的trigger方法进行调用
 */

public interface Listener {
	
	public abstract String[] on();
	 
	public abstract void invoke(Event e); 

}
