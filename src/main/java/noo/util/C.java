package noo.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections4.map.CaseInsensitiveMap;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
* @author  瞿建军      
* 
* 创建时间： 2016年6月12日  下午5:43:43
* 
*/
@SuppressWarnings("rawtypes")
public final class C {
	
		
	public static String uid(){
		return UUID.randomUUID().toString();
	}
	
	public static String join( Collection o,String token){
		return S.joinString(o, token);
	}
	
	
	@SuppressWarnings("unchecked")
	public static List<String> split(String s,String regx){
		List<String> a=new ArrayList<String>();
		return (List<String>)doSplit(a,s,regx);
	}
	
	@SuppressWarnings("unchecked")
	public static Set<String> split2Set(String s,String regx){
		Set<String> a=new HashSet<String>();
		return (Set<String>)doSplit(a,s,regx);
	}
	
	@SuppressWarnings("unchecked")
	private static Collection doSplit(Collection a,String s,String regx){
		if(S.isBlank(s) || regx==null)
			return null;
		if(regx.equals("")){
			a.add(s);
			return a;
		}		
		String[] ss=s.split(regx);
		for(String as: ss){
			if(S.isNotBlank(as))
				a.add(as);
		}
		return a;
	}
	
	public static String[] toArray(List<String> list) {
		if (list == null || list.isEmpty())
			return null;
		int size = list.size();
		String[] result = new String[size];
		for (int i = 0; i < size; i++)
			result[i] = list.get(i);
		return result;

	}
	
	public static Map<String,Object> lowCaseKey(Map<String,Object> dest){
		if(dest==null || dest.isEmpty())
			return dest;
		Map<String,Object> m=new HashMap<String,Object>();
		for(String key: dest.keySet())
			m.put(key.toLowerCase(), dest.get(key));
		return m;
	}
	
	/*
	 * if(pos<0)
			    this.formSubmit.add((SubmitListener) o);
			else{
				int size=this.formSubmit.size();
				if(pos>size)
					pos=size;
				this.formSubmit.add(pos,(SubmitListener) o);
			}
	 */
	
	@SuppressWarnings("unchecked")
	public static void addAt(List l,Object o,int pos){
		if(l==null) return;
		if(pos<0)
			l.add(o);
		else{
			int size=l.size();
			if(pos>size)
				pos=size;
			l.add(pos,o);
		}
	}
	
	
	
	/**
	 * 判断一个map是否是空map，也就是里面每个key的值都是空的，或者空字符
	 * @param dest
	 * @return
	 */
	public static boolean isBlankMap(Map dest){
		if(dest==null || dest.isEmpty())
			return true;		
		for(Object v: dest.values()){
			if(v!=null && S.isNotBlank(v.toString()))
				return false;
		}
		
		return true;
	}
	
	
	
	public static String getString(Map map,String key){
		if(map==null)
			return null;
		if(S.isBlank(key))
			return null;
		Object v=map.get(key);
		return v==null?null:v.toString();		
	}
	
	public static double getNumber(Map m, String key){
		if(m==null)
			return 0;
		Object o=m.get(key);
		if(o==null || S.isBlank(o.toString()))
			return 0;
		else if(o instanceof Number)
			return ((Number)o).doubleValue();
		else
			return Double.parseDouble(o.toString());
	}
	
	public static BigDecimal getBigDecimal(Map m, String key){
		if(m==null)
			return null;
		Object o=m.get(key);
		if(o==null || S.isBlank(o.toString()))
			return null;
		else if(o instanceof BigDecimal)
			return (BigDecimal)o;
		else{
			try{
				return new BigDecimal(o.toString());
			}catch(NumberFormatException ne){
				return null;
			}
		}
	}
	
	public static int getInt(Map m,String key){
		if(m==null)
			return 0;
		Object o=m.get(key);
		if(o==null || S.isBlank(o.toString()))
			return 0;
		else if(o instanceof Integer)
			return (Integer)o;
		else if(o instanceof Number)
			return ((Number)o).intValue();
		else
			return Integer.parseInt(o.toString());
	}
	

	
	public static Date getDate(Map m,String key){
		if(m==null)
			return null;
		Object o=m.get(key);
		if(o==null || S.isBlank(o.toString()))
			return null;
		else if(o instanceof Date)
			return (Date)o;
		else 
			return D.toDate(o.toString().trim());
	}
	
	public static String getDateString(Map m,String key){
		if(m==null)
			return null;
		Object o=m.get(key);
		if(o==null)
			return null;
		else if(o instanceof Date)
			return D.strD((Date)o);
		else 
			return o.toString();
	}
	
	public static List getList(Map m,String key){
		if(m==null || key==null)
			return null;
		Object o=m.get(key);
		if(o==null)
			return null;		
		else if(o instanceof List)
			return (List)o;
		throw new IllegalArgumentException("试图从Bus中获取List,但是"+key+"不是一个List类型");
	}
	
	public static Map getMap(Map m, String key){
		if(m==null || key==null)
			return null;
		Object o=m.get(key);
		if(o==null)
			return null;		
		else if(o instanceof Map)
			return (Map)o;
		throw new IllegalArgumentException("试图从Bus中获取Map,但是"+key+"不是一个Map类型");
	}
	
	
	public static boolean getBoolean(Map m,String key,boolean defaultValue){
		if(m==null)
			return defaultValue;
		Object o=m.get(key);
		if(o==null)
			return defaultValue;
		String so=o.toString().toLowerCase();
		if("n".equals(so) || "0".equals(so) || "false".equals(so) || "".equals(so))
			return false;
		else
			return true;
	}
	
	public static boolean contains(String[] array,String s){
		if(array==null || s==null)
			return false;
		for(String e: array){
			if(s.equals(e))
				return true;
		}
		return false;
	}
	
	public static boolean containAll(String[] fs,Map<String,Object> r){
				
		for(String s: fs){
			Object o=r.get(s);
			if(o==null || "".equals(o))
				return false;
		}
		
		return true;
		
	}
	
	public static boolean containsIgnoreCase(String[] array,String s){
		if(array==null || s==null)
			return false;
		for(String e: array){
			if(s.equalsIgnoreCase(e))
				return true;
		}
		return false;
	}
	
	
	public static boolean isArrayElementAllEquals(Object[] a,Object[] b){
		if(a==null && b==null)
			return true;
		if(a==null || b==null)
			return false;
		if(a.length!=b.length)
			return false;
		for(int i=0;i<a.length;i++){
			Object aa=a[i];
			Object bb=b[i];
			if( (aa==null && bb==null) || (aa!=null &&  aa.equals(bb)) )
				continue;
			else
				return false;
		}
		return true;
	}
	
	public static boolean isEmpty(Collection c){
		if(c==null || c.isEmpty())
			return true;
		return false;
	}
	
	public static boolean isNotEmpty(Collection c){
		if(c!=null && !c.isEmpty())
			return true;
		return false;
	}
	
	
	public static boolean isNullArray(Object[] a){
		if(a==null || a.length==0)
			return true;
		for(Object o: a){
			if(o!=null)
				return false;
		}
		return true;
	}
	
	public static  Object[] joinArray(Object[] a,Object[] b){
		  if(a==null || a.length==0)
			  return b;
		  if(b==null || b.length==0)
			  return a;
		  Object[] newValues=new Object[a.length+b.length];
          System.arraycopy(a,0,newValues,0,a.length);
          System.arraycopy(b,0,newValues,a.length,b.length);
          return newValues;
	}
	
	public static String printArray(Object[] a){
		if(a==null)
			return "";
		StringBuilder sb=new StringBuilder("[");
		for(Object o: a){
			sb.append(o==null?"null":o.toString()).append(",");
		}
		sb.append("]");
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static Collection subCollection(Collection c,int size){
		if(c==null || c.size()<size)
			return c;
		int i=0;
		Collection result=new ArrayList();
		for(java.util.Iterator ite=c.iterator();ite.hasNext();){
			result.add(ite.next());
			i=i+1;
			if(i>size)
				break;
		}
		return result;
	}
	

	public static Object[] getColFromListMap(List<Map> m, String colName){
		if(m==null || m.isEmpty() || S.isBlank(colName))
			return null;
		Object [] result=new Object[m.size()];
		for(int i=0;i<m.size();i++){
			Map row=m.get(i);
			if(row==null)
				result[i]=null;
			else
				result[i]=row.get(colName);
		}
		return result;		
	}
	

	public static List<String> getStrColFromListMap(List<Map> m, String colName){
		if(m==null || m.isEmpty() || S.isBlank(colName))
			return null;
		List<String> list=new ArrayList<String>();		
		for(int i=0;i<m.size();i++){
			Map row=m.get(i);
			if(row==null)
				continue;
			String v=(String) row.get(colName);
			if(S.isBlank(v))
				continue;
			list.add(v);
		}
		return list;		
	}
	
	
	public static Map<String, String> buildAttributeMap(String attStr) {
		Map<String, String> result = new CaseInsensitiveMap<>();
		if (S.isNotBlank(attStr)) {
			attStr = attStr.trim();
			String[] tv = attStr.split(";");
			for (String atv : tv) {
				String[] pair = atv.split(":");
				if (pair.length == 2)
					result.put(pair[0].trim(), pair[1].trim());
			}
		}
		return result;
	}
	
	//==========================================================

	 // 直接写入一个对象  
    public static String toJson(Object obj) {  
        ObjectMapper mapper = new ObjectMapper();  
       
        try {  
           return  mapper.writeValueAsString(obj);  
        } catch (JsonGenerationException e) {  
            e.printStackTrace();  
        } catch (JsonMappingException e) {  
            e.printStackTrace();  
        } catch (JsonProcessingException e) {
			e.printStackTrace();
		}
        return null;
    }  
   
    
    // 直接将一个json转化为对象  
    public static <T> T fromJson(String s,Class<T> c) {  
    	ObjectMapper mapper = new ObjectMapper();  
  
        try {  
            return mapper.readValue(s, c); 
        } catch (JsonParseException e) {  
            e.printStackTrace();  
        } catch (JsonMappingException e) {  
            e.printStackTrace();  
        } catch (IOException e) {
			e.printStackTrace();
		} 
        return null;
    }  
    
    
    public static Map fromJson(String s) { 
    	return fromJson(s, Map.class);
    }
  
    //======================================================
    public static void waitToQuit(){
		Scanner reader=new Scanner(System.in);
		String s=reader.nextLine();
		while(!s.equalsIgnoreCase("quit") && !s.equalsIgnoreCase("exit")){
			s=reader.nextLine();
		}
		reader.close();
    }
	
}
