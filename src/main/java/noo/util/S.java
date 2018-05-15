/**
 * 
 */
package noo.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 瞿建军  Email: troopson@163.com 
 */
public class S {

	public static String getString(Map<String, ?> row, String key) {
		Object o = row.get(key);
		if (o == null)
			return null;
		return o.toString();
	}

	public static String nowatime() {
		Date d = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(c.getTime());
	}

	public static String today() {
		Date d = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
	}

	public static String date2str(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
	}

	public static String time2str(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(c.getTime());
	}

	public static Date toDate(String yyyyMMdd) {
		if (yyyyMMdd == null || yyyyMMdd.length() == 0)
			return null;
		return java.sql.Date.valueOf(yyyyMMdd.trim());
	}

	public static Date max(Date date1, Date date2) {
		if (date1 == null)
			return date2;
		if (date2 == null)
			return date1;
		return (date1.getTime() > date2.getTime()) ? date1 : date2;
	}

	public static int dayGap(Date date1, Date date2) {
		long diffmillis = date1.getTime() - date2.getTime();
		long between_days = diffmillis / (1000 * 3600 * 24);
		if (diffmillis > 0)
			return Integer.parseInt(String.valueOf(between_days)) + 1;
		else
			return Integer.parseInt(String.valueOf(between_days));
	}

	public static boolean isNotBlank(String s) {
		return !isBlank(s);
	}

	public static boolean isBlank(String s) {
		if (s == null || s.length() == 0)
			return true;
		else
			return false;
	}

	public static Long getLong(Map<String, ?> m, String key) {
		return Long.parseLong("" + m.get(key));
	}

	public static String filterEmoji(String source) {
		if (source != null && source.length() > 0) {
			return source.replaceAll("[\ud800\udc00-\udbff\udfff\ud800-\udfff]", "");
		} else {
			return source;
		}
	}
	
	public static Set<String> split(String s, String splitor){
		Set<String> r = new HashSet<>();
		if(S.isBlank(s))
			return null;
		String[] ss = s.split(splitor);
		for(String a: ss){
			r.add(a);
		}
		return r;
	}
	
	public static String[] splitWithComma(String field){
	    	return field.split("[,;]");
	}
	
	//=========================================================
	
	public static String readAndCloseInputStream(InputStream is, String charset) throws IOException {
		DataInputStream dis = null;
		BufferedReader br = null;
		if (charset == null)
			charset = "UTF-8";
		try {
			StringBuilder sb = new StringBuilder();
			dis = new DataInputStream(is);
			br = new BufferedReader(new InputStreamReader(dis, charset));
			String record = null;
			if ((record = br.readLine()) != null)
				sb.append(record);

			while ((record = br.readLine()) != null)
				sb.append(record).append("\n\r");

			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			return null;
		} finally {
			if (is != null)
				is.close();
			if (dis != null)
				dis.close();
			if (br != null)
				br.close();
		}
	}

	public static String readFile(File f, String charset) throws IOException {
		if (f == null || !f.isFile())
			return null;

		FileInputStream is = new FileInputStream(f);
		return readAndCloseInputStream(is, charset);

	}

	public static void writeToFile(File file, String text) {
		Writer writer = null;
		FileOutputStream os=null;
		try {
			os=new FileOutputStream(file);
			writer = new OutputStreamWriter(os, "UTF-8");
			writer.write(text);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(os!=null)
				try {
				os.close();
				} catch (IOException e) {
				}
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
				}
		}
	}
	
	public static String trace(Throwable e){
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        e.printStackTrace(writer);
        StringBuffer buffer = stringWriter.getBuffer();
        return buffer.toString();	    
	}
	
	//---------------------------------------------------
	
	public static boolean isContainChinese(String str) {
		if (isBlank(str))
			return false;
		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return true;
		}
		return false;
	}
	
	public static boolean containChar(String str, String regx){
		if (isBlank(str))
			return false;
		Pattern p = Pattern.compile("["+regx+"]");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return true;
		}
		return false;
		
	}

	

	public static boolean equals(String a, String b) {
		if (a == null && b == null)
			return true;
		if (a == null && b != null)
			return false;
		return a.equals(b);
	}

	public static boolean equalsIgnoreCase(String a, String b) {
		if (a == null && b == null)
			return true;
		if (a == null && b != null)
			return false;
		return a.equalsIgnoreCase(b);
	}


	public static String insert(String src, int pos, String clause) {
		if (isBlank(clause))
			return src;
		if (src.length() < pos)
			return src + clause;
		else if (pos < 0)
			return clause + src;
		else {
			return src.substring(0, pos) + clause + src.substring(pos);
		}

	}

	/**
	 * 将字符串里面的空字符（空格，换行）进行压缩
	 * @param s
	 * @return
	 */
	public static String reasonClean(String s) {
		if (s == null || s.length() == 0)
			return null;
		return s.trim().replaceAll("\\s", " ").replaceAll("\\s{2,}", " ");
	}

	/**
	 * 清除空格换行字符和注释
	 */
	public static String clearNote(String s) {
		if (isBlank(s))
			return s;
		return s.replaceAll("\r|\n", " ").replaceAll("\\s{2,}|/\\*([^/*])*\\*/", " ").trim();
	}

	/**
	 * 对字符串里面的双引号进行html的转义
	 * @param s
	 * @return
	 */
	public static String toHtmlQuote(String s) {
		if (s == null || s.length() == 0)
			return s;
		if (s.indexOf("\"") != -1)
			return s.replaceAll("\"", "&#34;");
		return s;

	}

	/**
	 * 对一个数组中的各个项，拼接成一个字符串,并忽略中间的null项目
	 * @param o
	 * @return
	 */
	public static String join(Object[] o, char token) {
		if (o == null || o.length == 0)
			return "";
		StringBuilder sb = null;
		for (Object obj : o) {
			if (obj == null || "".equals(obj))
				continue;
			if (sb == null)
				sb = new StringBuilder(obj.toString());
			else {
				sb.append(token).append(obj.toString());
			}
		}
		return sb == null ? "" : sb.toString();
	}

	/**
	 * 对一个数组中的各个项，拼接成一个字符串,并忽略中间的null项目
	 * @param o
	 * @return
	 */
	public static String joinString(String[] o, String token) {
		if (o == null || o.length == 0)
			return "";
		StringBuilder sb = null;
		for (String obj : o) {
			if (obj == null || "".equals(obj))
				continue;
			if (sb == null)
				sb = new StringBuilder(obj);
			else {
				if (token != null)
					sb.append(token);
				sb.append(obj);
			}
		}
		return sb == null ? "" : sb.toString();
	}

	/**
	 * 对一个数组中的各个项，拼接成一个字符串,并忽略中间的null项目
	 * @param o
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String joinString(Collection o, String token) {
		if (o == null || o.size() == 0)
			return "";
		StringBuilder sb = null;
		for (Object one : o) {
			if (one == null)
				continue;
			String obj = one.toString();
			if ("".equals(obj))
				continue;
			if (sb == null)
				sb = new StringBuilder(obj);
			else {
				if (token != null)
					sb.append(token);
				sb.append(obj);
			}
		}
		return sb == null ? "" : sb.toString();
	}

	/**
	 * 合并两个属性串 类似： a;b,c;   d;a;c,dfd
	 * 
	 * @param s
	 * @return  a;b;c;d;dfd
	 */
	public static String joinSplitString(String... s) {
		Set<String> r = new HashSet<String>();
		if (s != null) {
			for (String one : s) {
				if (isNotBlank(one)) {
					String[] as = one.split(";");
					for (String a : as)
						r.add(a);
				}
			}
		}
		if (!r.isEmpty()) {
			StringBuilder sb = null;
			for (String a : r) {
				if (sb == null)
					sb = new StringBuilder(a);
				else
					sb.append(";").append(a);
			}
			return sb.toString();
		} else
			return null;
	}

	/**
	 * 
	      * 方法功能描述:从String获取List类型数据,
	      * String 的格式要求是a,b,c...的格式
	      * 
	      * @param     参数
	      * @return    返回
	      * @exception 异常描述
	 */
	public static List<String> getList(String s) {

		if (isBlank(s))
			return null;

		List<String> list = new ArrayList<String>();

		String[] v = s.split(",");
		for (String item : v) {
			if (isBlank(item)) {
				continue;
			}
			list.add(item);
		}
		return list;
	}

	/**
	 * 
	      * 方法功能描述:从String获取Map类型数据,
	      * String的格式要求是A=a1;B=b1;C=c1...的格式
	      * 
	      * @param     参数
	      * @return    返回
	      * @exception 异常描述
	 */
	public static Map<String, String> getMap(String s) {
		if (isBlank(s))
			return null;

		Map<String, String> m = new HashMap<String, String>();

		String[] v = s.split(";");
		for (String item : v) {
			if (isBlank(item))
				continue;
			String[] pair = item.split("=");
			if (pair.length != 2)
				return null;//直接返回null,在接收处抛出异常
			String cKey = pair[0].trim();
			String cValue = pair[1].trim();
			m.put(cKey, cValue);
		}
		return m;
	}

	/**
	 * 
	      * 方法功能描述:从String获取List<Map<String, String>>类型数据,
	      * String的格式要求是{A=a1;B=b1;C=c1},{A=a2;B=b2;C=c2}...的格式
	      * 
	      * @param     参数
	      * @return    返回
	      * @exception 异常描述
	 */
	public static List<Map<String, String>> getListMap(String s) {
		if (isBlank(s))
			return null;

		List<Map<String, String>> list = new ArrayList<Map<String, String>>();

		String[] v = s.split(",");
		for (String item : v) {
			if (isBlank(item)) {
				continue;
			}

			item = item.trim();// 获取到{A=a1;B=b1;C=c1}
			if (!item.startsWith("{") || !item.endsWith("}")) {
				return null;//直接返回null,在接收处抛出异常
			}

			item = item.substring(1, item.length() - 1);//获取到A=a1;B=b1;C=c1
			Map<String, String> m = getMap(item);
			if (m == null) {
				return null;// 直接返回null,在接收处抛出异常
			}
			list.add(m);
		}
		return list;
	}

	//===========================================================

	
	/**
	 * 半角转全角
	 * 
	 * @param input
	 *            String.
	 * @return 全角字符串.
	 */
	public static String ToSBC(String input) {
		char c[] = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == ' ') {
				c[i] = '\u3000';
			} else if (c[i] < '\177') {
				c[i] = (char) (c[i] + 65248);

			}
		}
		return new String(c);
	}

	/**
	 * 全角转半角
	 * 
	 * @param input
	 *            String.
	 * @return 半角字符串
	 */
	public static String ToDBC(String input) {

		char c[] = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
//			if (c[i] == '\u3000') {
			if (c[i] == '\u3000' || c[i] == 160) {// 增加对&nbsp;的处理  modified by liuxinxing 20100203 
				c[i] = ' ';
			} else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
				c[i] = (char) (c[i] - 65248);

			}
		}
		String returnString = new String(c);

		return returnString;
	}
	public static String isNull(String str) {		
		if(str==null || "".equals(str.trim()) || "undefined".equals(str)){			
			return null;		
		}		
		return str.trim();
	}
	public static boolean isEmpty(String str) {		
		if(str==null || "".equals(str.trim()) || "undefined".equals(str)){			
			return true;		
		}		
		return false;
	}
	
	public static boolean isNotEmpty(String str) {			
		return !isEmpty(str);
	}

	public static String nullConvertSpace(String str){
		if(str == null||"null".equalsIgnoreCase(str.trim())){
			str = "";
		}
		return str;
	}
	
	public static String spaceConvertNull(String str){
		if(str == null || "".equals(str.trim()) || "null".equalsIgnoreCase(str.trim())){
			str = null;
		}
		return str;
	}
	
	public static BigDecimal convertBigDecimal(String str){
		if(str == null || "".equals(str.trim())){
			str = null;
		}
		if(str!=null) {
			BigDecimal bg=new BigDecimal(str);
			return bg;
		} else {
			return null;
		}
		
	}
	public static Integer convertInteger(String str){
		if(str == null || "".equals(str.trim())){
			str = null;
		}
		if(str!=null) {
			try {
				return Integer.parseInt(str);
			} catch(Exception e) {
				return 0;
			}
		} else {
			return 0;
		}
		
	}
	
	public static long convertLong(String str){
		if(str == null || "".equals(str.trim())){
			str = null;
		}
		if(str!=null) {
			try {
				return Long.parseLong(str);
			} catch(Exception e) {
				return 0;
			}
		} else {
			return 0;
		}
		
	}
	public static int pageStringConvertInt(String pageStr){
		return (null == pageStr || "".equals(pageStr)) ? 1 : Integer.parseInt(pageStr);
	}
	
	public static String replaceBlank(String str){
		
	   Pattern p = Pattern.compile("\\s*|\t|\r|\n");
	   Matcher m = p.matcher(str);
	   String after = m.replaceAll("");
	   return after;
	   
	}
	
	/**
	 * 把字符串转成utf8编码，保证中文文件名不会乱码
	 * 
	 * @param s
	 * @return
	 */
	public static String toUtf8String(String s) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c >= 0 && c <= 255) {
				sb.append(c);
			} else {
				byte[] b;
				try {
					b = Character.toString(c).getBytes("UTF-8");
				} catch (Exception ex) {
					b = new byte[0];
				}
				for (int j = 0; j < b.length; j++) {
					int k = b[j];
					if (k < 0)
						k += 256;
					sb.append("%" + Integer.toHexString(k).toUpperCase());
				}
			}
		}
		return sb.toString();
	}
	
	public static String subReplace(String org,int start,int end,String reg){
		if (isEmpty(org)) {
			return org;
		}
		int len = org.length();
		if (start>len) {
			start = len;
		}
		if (start>len) {
			start = len;
		}
		if (end>len) {
			end = len;
		}
		if (start>end) {
			start = end;
		}
		return org.substring(start,end) + reg;
	};
	
}
