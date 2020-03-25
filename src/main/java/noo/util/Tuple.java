/**
 * 
 */
package noo.util;

/**
 * @author qujianjun   troopson@163.com
 * Mar 16, 2020 
 * 
 * 一个Tuple对象，可以持有变量，在lambda表达式中，可以用来修改外部变量
 * 
 */
public class Tuple { 
	
	public Object _1 =null;
	public Object _2 =null;
	public Object _3 =null; 
	
	public Tuple() { 
	}
	
	public Tuple(Object a ) {
		this._1=a; 
	}
	
	public Tuple(Object a, Object b ) {
		this._1=a;
		this._2=b; 
	}
	
	public Tuple(Object a, Object b, Object c) {
		this._1=a;
		this._2=b;
		this._3=c; 
	}
	
	/**
	 * 
	 * @param i ，变量顺序号，从1开始， 1代表第一个变量，2代表第二个，3代表第三个
	 * @return
	 */
	public Object get(int i) {
		switch (i) {
		case 1:
			return _1; 
		case 2:
			return _2; 
		case 3:
			return _3; 
		default:
			throw new IllegalArgumentException("bad index number");
		} 
	}
	
	public String str(int i) {
		Object o = this.get(i);
		return o==null? null : o.toString(); 
	}
	 
	public int getInt(int i, int def) {
		Object s = this.get(i);
		if(s==null)
			return def;
		else if(s instanceof Number)
			return ((Number)s).intValue();
		else
			return Integer.parseInt(s.toString());
	}
	
	public boolean isTrue(int i) {
		Object s = this.get(i);
		if(s==null)
			return false;
		else if(s instanceof Boolean)
			return ((Boolean)s).booleanValue();
		else
			return "true".equalsIgnoreCase(s.toString())?true:false;
	} 

}
