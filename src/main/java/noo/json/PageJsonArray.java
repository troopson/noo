/**
 * 
 */
package noo.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import noo.jdbc.PageQuery;

/**
 * @author qujianjun troopson@163.com 2018年5月18日
 */
@JsonSerialize(using = MyJsonConverter.class)
public class PageJsonArray extends JsonArray {

	private static final long serialVersionUID = 1L;
	
	// 一页显示的记录数
	private int numPerPage=50;
	// 记录总数
	private int totalRows=0;
	// 总页数
	private int totalPages=0;
	// 当前页码
	private int currentPage=1;
	// 起始行数
	private int startIndex=0;
	// 结束行数
	private int lastIndex=0;

	@SuppressWarnings("rawtypes")
	public PageJsonArray(List l) {
		super(l);
	}
	
	PageJsonArray() { 
	}

	public PageJsonArray(PageQuery pq) {
		super(pq.getResultList());
		this.numPerPage = pq.getNumPerPage();
		this.totalRows = pq.getTotalRows();
		this.totalPages = pq.getTotalPages();
		this.currentPage = pq.getCurrentPage();
		this.startIndex = pq.getStartIndex();
		this.lastIndex = pq.getLastIndex();
	}

	public JsonObject toJsonObject() {
		JsonObject jo = new JsonObject();
		jo.put("content", this.list);
		// "last":true,"totalPages":1,"totalElements":5,"size":10,"number":0,"sort":null,"first":true,"numberOfElements":5
		jo.put("totalPages", this.getTotalPages());
		jo.put("totalElements", this.getTotalRows());
		jo.put("size", this.getNumPerPage());
		jo.put("number", this.getCurrentPage());
		jo.put("numberOfElements", this.list.size());
		return jo;
	}

	@Override
	public String encode() {
		return this.toJsonObject().encode();
	}
	
	@Override
	public Object convertToJson() {
		Map<String,Object> jo = new HashMap<>();
		jo.put("content", this.list);
		// "last":true,"totalPages":1,"totalElements":5,"size":10,"number":0,"sort":null,"first":true,"numberOfElements":5
		jo.put("totalPages", this.getTotalPages());
		jo.put("totalElements", this.getTotalRows());
		jo.put("size", this.getNumPerPage());
		jo.put("number", this.getCurrentPage());
		jo.put("numberOfElements", this.list.size());
		return jo;
	}

	/**
	 * Encode this JSON object as buffer.
	 *
	 * @return the buffer encoding.
	 */
	@Override
	public byte[] toBuffer() {
		return this.toJsonObject().toBuffer();
	}

	/**
	 * Encode the JSON array prettily as a string
	 *
	 * @return the string encoding
	 */
	@Override
	public String encodePrettily() {
		return this.toJsonObject().encodePrettily();
	}

	/**
	 * Make a copy of the JSON array
	 *
	 * @return a copy
	 */
	@Override
	public JsonArray copy() {
		List<Object> copiedList = new ArrayList<>(this.list.size());
		for (Object val : list) {
			val = Json.checkAndCopy(val, true);
			copiedList.add(val);
		}
		PageJsonArray p = new PageJsonArray();
		p.list = copiedList;
		p.numPerPage = numPerPage;
		p.totalRows = totalRows;
		p.totalPages = totalPages;
		p.currentPage = currentPage;
		p.startIndex = startIndex;
		p.lastIndex = lastIndex;
		return p;
	}

	@Override
	public boolean equals(Object o) {
		if (super.equals(o)) {
			PageJsonArray p = (PageJsonArray) o;
			if (p.numPerPage == numPerPage && p.totalRows == totalRows && p.totalPages == totalPages
					&& p.currentPage == currentPage && p.startIndex == startIndex && p.lastIndex == lastIndex) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		String s = numPerPage + "" + totalRows + "" + totalPages + "" + currentPage + "" + startIndex + "" + lastIndex;
		return Objects.hash(list, s);
	}

	public int getNumPerPage() {
		return numPerPage;
	}

	public void setNumPerPage(int numPerPage) {
		this.numPerPage = numPerPage;
	}

	public int getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getLastIndex() {
		return lastIndex;
	}

	public void setLastIndex(int lastIndex) {
		this.lastIndex = lastIndex;
	}

}
