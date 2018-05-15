/**
 * 
 */
package noo.jdbc;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.map.CaseInsensitiveMap;

import noo.util.S;
 

/**
 * @author 瞿建军  2016年10月17日
 */
public class SqlUtil {

	public static String convertChn(String tableAlias, String field, String chnfields) {
		// CONVERT(deviceModelId USING gbk)

		if (S.isBlank(chnfields))
			return field;
		chnfields = chnfields + ",";
		if (chnfields.indexOf(field + ",") == -1) // 不包含字段
			return field;

		if (tableAlias != null)
			return "CONVERT(" + tableAlias + "." + field + " USING gbk)";
		else
			return "CONVERT(" + field + " USING gbk)";

	}

	private static final Pattern SQL_Injection_reg = Pattern
			.compile("select |update |delete |drop |grant |create | and | or |exec ");

	public static boolean isInjection(String param) {
		if (S.isBlank(param))
			return false;

		// 全字母，数字的，可以超过10个字符，否则不允许
		if (!param.matches("[a-zA-Z_0-9]*") && param.length() > 10)
			return true;

		Matcher m = SQL_Injection_reg.matcher(param);
		return m.find();
	}
	
	//============================================

	private final static Pattern ParamReg = Pattern.compile("\\{[^\\{\\}]*[=|like]\\W*[:|#][a-zA-Z0-9_ ]*\\}");
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String processParam(String sql, Map param) {
		if(param==null || param.isEmpty())
		      return sql;

		Matcher m = ParamReg.matcher(sql);
		StringBuffer newsql = new StringBuffer();
		Map<String,Object> t = new CaseInsensitiveMap<>();
		if (param != null)
			t.putAll(param);

		while (m.find()) {
			String s = m.group();
			int lst = s.lastIndexOf(":");
			boolean ignore=true;
			if(lst==-1) {
				lst =  s.lastIndexOf("#");
				ignore=false;
			}
			String paramName = s.substring(lst + 1, s.length() - 1);
			// System.out.println(paramName);
			Object v = t.get(paramName);
			if (v != null && !"".equals(v)) {
				//此处转换为spring nametemplate的格式 file=:param
				String re = s.substring(1, s.length() - 1);
				if(!ignore)
					re = re.replace('#', ':');
				m.appendReplacement(newsql, re);
				
				//如果like类型的变量，没有%_符号，那么自动在两端加上%号
				if(s.toLowerCase().indexOf(" like ")!=-1 && 
				  !S.containChar(v.toString(), "%_")){					
					  param.put(paramName, "%"+v+"%" );
					
				}
				
				// param.put(paramName, v); //spring会放到自己的map中，避免大小写的问题
			} else {
				if(!ignore) {
					m.appendReplacement(newsql, "1=2");
				}else {				
					m.appendReplacement(newsql, "");
	
					String temp = newsql.toString().trim().toLowerCase();
	
					if (temp.endsWith("where") || temp.endsWith("and") || temp.endsWith("or") || temp.endsWith("("))
						newsql.append(" 1=1 ");
				}
			}
		}
		m.appendTail(newsql);
		// System.out.println(newsql.toString());
		//replace simple
		String rtnsql = newsql.toString();
		for(Object k: param.keySet()) {
			Object v = param.get(k);
			if(k instanceof String && v instanceof String) {
				String key = (String)k;
				String val = (String)v;
				if(Pattern.matches("[a-zA-Z0-9_-]*", val)) {
					rtnsql = rtnsql.replace("{"+key+"}", val);
				}
			}			
		}
		
		//rtnsql = rtnsql.replaceAll("\\{[a-zA-Z0-9=:_-]*\\}", "1=1");
		return rtnsql;
		
	}
	
	public static String toStaticSQL(String sql, Map<String,?> params) {
		if(params.isEmpty())
			return sql;
		for(String key: params.keySet()) {
			Object v = params.get(key);
			if(v instanceof Number)
				sql=sql.replace(":"+key, v+"");
			else
			    sql=sql.replace(":"+key, "'"+v+"'");
		}
		return sql;
	}
	
	 
	public static void main(String[] args) {
		String sql ="select a from t where {p=#p} and {c=:c} and {kk=:vc} and {f=:tt}";
		Map<String,Object> m = new HashMap<>();
		m.put("p", "23");
		m.put("c", "cv");
		String s = processParam(sql,m);
		System.out.println(s);
		System.out.println(toStaticSQL(s,m));
		
	}

}
