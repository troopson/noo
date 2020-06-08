/**
 * 
 */
package noo.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import noo.json.JsonArray;
import noo.json.JsonObject;
import noo.util.BigDecimalUtil;
import noo.util.S;

/**
 * @author qujianjun   troopson@163.com
 * Feb 29, 2020 
 * 
 * 将一个JsonArray转换为方便Echart显示的数据对象。
 * 
 * 要求JsonArray中，每个JsonObject中，有一个key的字段，用来标记分类，所有序列按照s1，s2，s3的名称显示
 * 
 * 返回的数据对象格式是：
 * {
 *    系列1:[{"分类1":10},{"分类2":20},{"分类3":30}],
 *    系列2:[{"分类1":11},{"分类2":22},{"分类3":31}],
 *    系列3:[{"分类1":7},{"分类2":12},{"分类3":34}] 
 * }
 *  
 *  
 *  在写sql语句的时候，要求必须有一个name字段，如果不带有series字段，那么会用列名作为序列的名称
 *  如果带了series字段，会用series字段的值作为序列名称，这种情况下，一般只会选择一个值字段
 *  
 *  示例一：
 *  select project_name as name, count(*) as 名单总数, count(distinct mobile) as 电话总数 from dwd.hd_detail_st
 *  数据转换后的格式：
    {
      "名单总数":[{"国考":10},{"省考":20},{"教师招聘":30}],
      "电话总数":[{"国考":8},{"省考":10},{"教师招聘":20}] 
    }
 *   
 *  示例二：
 *  select cast(createtime as date) as name, project_name as series, count(*) md_num from dwd.hd_detail_st order by cast(createtime as date)
 *  数据转换后的格式：
    {
      "国考":[{"2020-02-01":10},{"2020-02-02":20},{"2020-02-03":30}],
      "省考":[{"2020-02-01":8},{"2020-02-02":10},{"2020-02-03":20}], 
      "教师招聘":[{"2020-02-01":8},{"2020-02-02":10},{"2020-02-03":20}]
    } 
 *   
 *   示例三：
 *  select count(price_1000_num) 1000_num,count(price_2000_num) 2000_num,count(price_3000_num) 3000_num from dwd.hd_detail_st order by cast(createtime as date)
 *  数据转换后的格式：
    {
      "pie":[{"1000_num":10},{"2000_num":20},{"3000_num":30}] 
    } 
 *   
 *   
 *   
 */
public class ToEcharts {
  
	//数据刻度值的名称
	public static final String CATAGORY_KEY="name";  //数据刻度值的名称
	//系列名称
	public static final String SERIES_KEY="series";  
	
	
	//将JsonArray数据转换为Echart比较方便使用的数据格式
	public static JsonObject transfer(JsonArray jary) {
		if(jary==null || jary.isEmpty())
			return null;
		
		return parseSeriesData(jary); 
	}
	 
	
	 
	private static JsonObject parseSeriesData(JsonArray jary) {  
		JsonObject data = new JsonObject(); 
		
		jary.forEachJsonObject(c->{    
			String key = c.getString(CATAGORY_KEY); //看看有没有name字段，分类字段
			if(S.isBlank(key)) { //没有name字段，直接不处理，所以这里只处理模式一和模式二的sql语句
				return; 
			}
			String series_name = c.getString(SERIES_KEY); //有SERIES_KEY是示例二的模式，没有是示例一的模式
			c.forEach(e->{
				String k = e.getKey();
				Object v = e.getValue();
				if(CATAGORY_KEY.equals(k) || SERIES_KEY.equals(k))
					return; 
				String sname = series_name==null? k: series_name;  
				JsonArray j = data.getJsonArray(sname);
				if(j==null) {
					j = new JsonArray();
					data.put(sname, j);
				}
				JsonObject one = new JsonObject();
				if(v instanceof BigDecimal) {
					one.put(key,BigDecimalUtil.round((BigDecimal)v, 2));
				}else
					one.put(key,v);
				j.add(one);
			});
		});
		
		
		
		if(data.isEmpty() && jary.size()==1 ) {
			JsonObject first = jary.getJsonObject(0);
			if(!first.containsKey("name")) { //如果没有name字段，是示例三的模式
				JsonArray pieData = makePieData(first);
				if(pieData!=null)
					data.put("data", pieData);
			}
		} 
		return data;
	}
	
	
	private static JsonArray makePieData(JsonObject j) {
		List<Object[]> ls = new ArrayList<>();
		j.forEach(c->{
			String key = c.getKey();
			Object v = c.getValue();
			if(v==null || !(v instanceof Number) )
				return; 
			ls.add(new Object[] {key,(Number)v});
		});
		 
		Comparator<Object[]> cmp  = (o1,o2) -> {
			double n1 = ((Number)o1[1]).doubleValue();
			double n2 = ((Number)o2[1]).doubleValue();
			if(n1>n2)
				return -1;
			else
				return 1; 
		}; 
		
		ls.sort(cmp);
		
		JsonArray jary = new JsonArray();
		ls.forEach(c->{
			String key = (String)c[0];
			Number v = (Number)c[1];
			JsonObject one = new JsonObject();
			if(v instanceof BigDecimal) {
				one.put(key,BigDecimalUtil.round((BigDecimal)v, 2));
			}else
				one.put(key,v); 
			jary.add(one);
		});
		return jary;
	}
	 
	
	
 
//	public static void main(String[] args) {
//		JsonArray jay = new JsonArray();
//		for(int i=0; i<10; i++) {
//			JsonObject jo = new JsonObject(); 
//			jo.put("name", "020"+i);
//			jo.put("s1", Math.round(10*Math.random()));
//			jo.put("s2", Math.round(20*Math.random()));
//			jo.put("s3", Math.round(50*Math.random()));
//			jay.add(jo);
//		}
//		//System.out.println(jay.encodePrettily());
//		JsonObject jo = transfer(jay);
//		System.out.println(jo.encodePrettily());
//		
//		System.out.println("--------------------------");
//		JsonArray pie = new JsonArray();
//		JsonObject item = new JsonObject();  
//		item.put("s1", Math.round(10*Math.random()));
//		item.put("s2", Math.round(20*Math.random()));
//		item.put("s3", Math.round(50*Math.random()));
//		pie.add(item);
//		JsonObject jo1 = transfer(pie); 
//		System.out.println(jo1.encodePrettily());
//		
//
//	}

}
