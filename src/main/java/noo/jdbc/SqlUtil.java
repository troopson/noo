/**
 * 
 */
package noo.jdbc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.LinkedCaseInsensitiveMap;

import noo.util.S;
 

/**
 * @author 瞿建军  2016年10月17日
 */
public class SqlUtil {

	public static String convertChn(String tableAlias, String field, String chnfields) {
		// CONVERT(deviceModelId USING gbk)

		if (S.isBlank(chnfields)) {
			return field;
		}
		chnfields = chnfields + ",";
		if (chnfields.indexOf(field + ",") == -1) {
			return field;
		}

		if (tableAlias != null) {
			return "CONVERT(" + tableAlias + "." + field + " USING gbk)";
		} else {
			return "CONVERT(" + field + " USING gbk)";
		}

	}

	private static final Pattern SQL_INJECTION_REG = Pattern
			.compile("select |update |delete |drop |grant |create | and | or |exec ");

	private static final Pattern A_Z0_9 =Pattern.compile("[a-zA-Z_0-9]*");
	
	public static boolean isInjection(String param) {
		if (S.isBlank(param)) {
			return false;
		}

		// 全字母，数字的，可以超过10个字符，否则不允许
		if (!A_Z0_9.matcher(param).matches() && param.length() > 10) {
			return true;
		}

		Matcher m = SQL_INJECTION_REG.matcher(param);
		return m.find();
	}
	
	//============================================

	public final static Pattern PARAM_REG = Pattern.compile("\\{[^\\{\\}]*[=|like|in|>|<]\\W*[:|#][a-zA-Z0-9_ ]*\\}",Pattern.CASE_INSENSITIVE);
	
	public final static Pattern CLEAN_AND1_1 = Pattern.compile(" and[ ]+1=1", Pattern.CASE_INSENSITIVE);
	
	public final static Pattern UPDATE_REG =  Pattern.compile("^(update|delete) ", Pattern.CASE_INSENSITIVE);
	
	public static String replaceParam(String sql, String with) {
		Matcher m = PARAM_REG.matcher(sql); 
		return cleanAND1_1(m.replaceAll(with));
	}
	 
	@SuppressWarnings({"rawtypes", "unchecked" })
	public static String processParam(String sql, Map param) {
		Matcher m = PARAM_REG.matcher(sql);
		StringBuffer newsql = new StringBuffer();
		Map<String,Object> t = new LinkedCaseInsensitiveMap<Object>();
		if (param != null) {
			t.putAll(param);
		}
		boolean isupdate= UPDATE_REG.matcher(sql).find(); 
		while (m.find()) {
			String s = m.group();
			int lst = s.lastIndexOf(":");
			boolean ignore=true;
			if(lst==-1) {
				lst =  s.lastIndexOf("#");
				ignore=false;
			}
			String paramName = s.substring(lst + 1, s.length() - 1).trim();
			//System.out.println("["+paramName+"]");
			Object v = t.get(paramName);
			if (v != null && !"".equals(v) ) {
				//先判断是否有in关键词，如果有，将List值展开，并替换原有的变量字符串，展开后的值放到param中
				int inpos= s.toLowerCase().indexOf(" in ");
				if(inpos!=-1) {
					String headin = s.substring(1,inpos+4);
					Iterable<Object> colv = (Iterable<Object>)param.get(paramName); 
					int i=0;
					StringBuilder inReplace= new StringBuilder(headin); 
					for(Object ech : colv) {
						String in_name = paramName+"_"+i;
						if(i==0) {
							inReplace.append(" (:"+in_name);
						}else {
							inReplace.append( " ,:"+in_name);
						}
						i=i+1;
						param.put(in_name, ech); 
					}
					if (i>0)
						m.appendReplacement(newsql, inReplace.append(" )").toString());
					else {
						appendReplacement(newsql, ignore, m,isupdate);
					}
				}else {
					//此处转换为spring nametemplate的格式 file=:param
					String re = s.substring(1, s.length() - 1);
					if(!ignore) {
						re = re.replace('#', ':');
					}
					m.appendReplacement(newsql, re);
					
					//如果like类型的变量，没有%_符号，那么自动在右端加上%号
					if(s.toLowerCase().indexOf(" like ")!=-1 &&  !S.containChar(v.toString(), "%_")){					
						param.put(paramName, v+"%" ); 
					}  
				}				
				// param.put(paramName, v); //spring会放到自己的map中，避免大小写的问题
			} else {
				appendReplacement(newsql, ignore, m,isupdate);
			}
		}
		m.appendTail(newsql); 
		//replace simple
		String rtnsql = newsql.toString();
//		if(param!=null) {
//			for(Object k: param.keySet()) {
//				Object v = param.get(k);
//				if(k instanceof String) {
//					String key = (String)k; 
//					if( v instanceof Number || (v!=null && v instanceof String && Pattern.matches("[a-zA-Z0-9_-]*", (String)v))) {
//						String val = v==null? "" : v.toString();
//						rtnsql = rtnsql.replace("{"+key+"}", val);
//					}
//				}			
//			}
//		}
		//rtnsql = rtnsql.replaceAll("\\{[a-zA-Z0-9=:_-]*\\}", "1=1"); 
		return cleanAND1_1(rtnsql);
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Eso buildESO(String sql, Map param) {
		
		Matcher m = PARAM_REG.matcher(sql);
		StringBuffer newsql = new StringBuffer();
		Map<String,Object> t = new LinkedCaseInsensitiveMap<Object>();
		if (param != null) {
			t.putAll(param);
		}
		boolean isupdate= UPDATE_REG.matcher(sql).find();
		Eso eso = new Eso();
		while (m.find()) {
			String s = m.group();
			int lst = s.lastIndexOf(":");
			boolean ignore=true;
			if(lst==-1) {
				lst =  s.lastIndexOf("#");
				ignore=false;
			}
			String paramName = s.substring(lst + 1, s.length() - 1).trim();
			//System.out.println("["+paramName+"]");
			Object v = t.get(paramName);
			if (v != null && !"".equals(v) ) {
				//先判断是否有in关键词，如果有，将List值展开，并替换原有的变量字符串，展开后的值放到param中
				int inpos= s.toLowerCase().indexOf(" in ");
				if(inpos!=-1) {
					String headin = s.substring(1,inpos+4);
					Iterable<Object> colv = (Iterable<Object>)param.get(paramName); 
					int i=0;
					StringBuilder inReplace= new StringBuilder(headin); 
					for(Object ech : colv) {
						String in_name = paramName+"_"+i; 
						if(i==0) {
							inReplace.append(" (?");
						}else {
							inReplace.append( " ,?");
						}
						i=i+1; 
						eso.addParam(in_name, ech);
					}
					if (i>0)
						m.appendReplacement(newsql, inReplace.append(" )").toString());
					else {
						appendReplacement(newsql, ignore, m,isupdate);
					}
				}else { 
					String re = s.substring(1, lst);
					//re = re.replace(s, "?");
					
					m.appendReplacement(newsql, re+"?");
					//如果like类型的变量，没有%_符号，那么自动在右端加上%号
					if(s.toLowerCase().indexOf(" like ")!=-1 &&  !S.containChar(v.toString(), "%_")){	 
						eso.addParam(paramName, v+"%");
					}else {
						eso.addParam(paramName, v);
					} 
				}				
				// param.put(paramName, v); //spring会放到自己的map中，避免大小写的问题
			} else {
				appendReplacement(newsql, ignore, m,isupdate);
			}
		}
		m.appendTail(newsql); 
		//replace simple
		String rtnsql = newsql.toString();
//		if(param!=null) {
//			for(Object k: param.keySet()) {
//				Object v = param.get(k);
//				if(k instanceof String) {
//					String key = (String)k; 
//					if( v instanceof Number || (v!=null && v instanceof String && Pattern.matches("[a-zA-Z0-9_-]*", (String)v))) {
//						String val = v==null? "" : v.toString();
//						rtnsql = rtnsql.replace("{"+key+"}", val);
//					}
//				}			
//			}
//		}
		//rtnsql = rtnsql.replaceAll("\\{[a-zA-Z0-9=:_-]*\\}", "1=1"); 
		eso.setSQL(cleanAND1_1(rtnsql));
		return eso;
	}
	
	private static String cleanAND1_1(String sql) {
		return CLEAN_AND1_1.matcher(sql).replaceAll(" ");
	}
	
	private static void appendReplacement(StringBuffer newsql,boolean ignore, Matcher m, boolean isupdate) {
		if(isupdate || !ignore) {
			m.appendReplacement(newsql, "1=2");
		}else {				
			m.appendReplacement(newsql, "1=1"); 
		}
	}
	
	public static String toStaticSQL(String sql, Map<String,?> params) {
		if(params.isEmpty()) {
			return sql;
		}
		for(String key: params.keySet()) {
			Object v = params.get(key);
			if(v instanceof Number) {
				sql=sql.replace(":"+key+" ", v+" ");
			} else {
				sql=sql.replace(":"+key+" ", "'"+v+"' ");
			}
		}
		return sql;
	}
	 
	public static String parseEvent(String sql, Function<String[], String> f) {

		Matcher m = PARAM_REG.matcher(sql);
		StringBuffer newsql = new StringBuffer(); 

		boolean isupdate= UPDATE_REG.matcher(sql).find();
		while (m.find()) {
			String s = m.group();
			int lst = s.lastIndexOf(":");
			boolean ignore=true;
			if(lst==-1) {
				lst =  s.lastIndexOf("#");
				ignore=false;
			}
			String paramName = s.substring(lst + 1, s.length() - 1).trim();
			String fieldAndOprt=s.substring(1,lst);
			//System.out.println("["+paramName+"]");

		    String oprt = "=";
		    String expr = s.toLowerCase();
		    if(expr.contains(" not in "))
		    	oprt = " not in ";
		    else if(expr.contains(" not like "))
		    	oprt = " not like ";
		    else if(expr.contains(" in ")) 
				oprt = "in";
			else if(expr.contains(" like "))
				oprt = "like";
			
			String replaceMent = f.apply(new String[] {oprt, paramName});
			if(S.isBlank(replaceMent))
				appendReplacement(newsql, ignore, m, isupdate);
			else
				m.appendReplacement(newsql, fieldAndOprt+replaceMent); 
		}
		m.appendTail(newsql);  
		String rtnsql = newsql.toString();  
		return cleanAND1_1(rtnsql);
	}
	
	 
	public static void main(String[] args) {
		String sql ="select a from t where {p=#p } and {c like :c} and {kk=:vc} and {f=:tt} and {t not in :kk}";
		String sql2 ="delete a from t where {p=#p } and {c like :c} and {kk=:vc} and {f=:tt} and {t not in :kk}";
		String sql3 ="update t set a=c where {p=#p } and {c like :c} and {kk=:vc} and {f=:tt} and {t not in :kk}";
		Map<String,Object> m = new HashMap<>();
		m.put("p", "23");
		m.put("c", "cv");
		
		List<String> v = Arrays.asList("incc","indd","inee");
		m.put("kk", v);
		
		String s = processParam(sql,m);
		System.out.println(s);
		System.out.println(toStaticSQL(s,m));
		System.out.println(m);
		
		String s2 = processParam(sql2,m);
		System.out.println(s2);
		
		String s3 = processParam(sql3,m);
		System.out.println(s3);
		
		
		Eso e = buildESO(sql3,m);
		System.out.println(e);
		
		
	}

}
