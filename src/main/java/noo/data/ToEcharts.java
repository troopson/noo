/**
 * 
 */
package noo.data;

import noo.json.JsonArray;
import noo.json.JsonObject;
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
 *    category:["key1","key2","key3"]
 *    s1:[10,20,30,40],
 *    s2:[11,22,33],
 *    s3:[32,43,11],
 *    size:3
 * }
 *  
 */
public class ToEcharts {
	
	//类目名称
	public static final String CATAGORY ="category";
	//序列数量
	public static final String SERIES_SIZE ="size";
	//数据刻度值的名称
	public static final String CATAGORY_KEY="key"; 
	//数据序列的前缀
	public static final String SERIES_PREFIX ="s"; 
	
	//将JsonArray数据转换为Echart比较方便使用的数据格式
	public static JsonObject transfer(JsonArray jary) {
		if(jary==null || jary.isEmpty())
			return null;
		
		Integer[] series_size = new Integer[1]; 
		JsonArray category = new JsonArray(); 
		JsonObject result = new JsonObject();
		jary.forEachJsonObject(c->{
			String key = c.getString(CATAGORY_KEY);
			if(S.isBlank(key))
				return;
			category.add(key);
			if(series_size[0]==null) {
				series_size[0] = find_series_size(c);  
			}
			for(int i=1; i <= series_size[0]; i++ ) {
				String name = SERIES_PREFIX+i;
				Object s1 = c.getValue(SERIES_PREFIX+i);
				JsonArray js = result.getJsonArray(name);
				if(js == null) {
					js = new JsonArray();
					result.put(name, js);
				} 
				js.add(s1==null? 0 :s1);
			} 
		});
		result.put(CATAGORY, category);
		result.put(SERIES_SIZE, series_size[0]);
		return result;
	}
	
	
	private static int find_series_size(JsonObject j) { 
		int size = j.size();
		for(int i=1 ; i< size; i++) {
			if(j.containsKey(SERIES_PREFIX+i)) {
				continue;
			}else {
				return i-1;
			}
		}
		return size-1;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JsonArray jay = new JsonArray();
		for(int i=0; i<10; i++) {
			JsonObject jo = new JsonObject();
			jo.put("key", "020"+i);
			jo.put("s1", Math.round(10*Math.random()));
			jo.put("s2", Math.round(20*Math.random()));
			jo.put("s3", Math.round(50*Math.random()));
			jay.add(jo);
		}
		System.out.println(jay.encodePrettily());
		JsonObject jo = transfer(jay);
		System.out.println(jo.encodePrettily());

	}

}
