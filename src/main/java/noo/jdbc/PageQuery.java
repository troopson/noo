package noo.jdbc;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * @author 瞿建军
 * 
 *         创建时间： 2016年6月12日 下午5:43:43
 * 
 */
@SuppressWarnings("rawtypes")
public class PageQuery implements Serializable {

	private static final long serialVersionUID = -4739566072600281491L;

	// 一页显示的记录数
	private int numPerPage;
	// 记录总数
	private int totalRows;
	// 总页数
	private int totalPages;
	// 当前页码
	private int currentPage;
	// 起始行数
	private int startIndex;
	// 结束行数
	private int lastIndex;
	// 结果集存放List
	private List resultList;

	public PageQuery(String sql) {
	}

	/**
	 * 分页构造函数
	 * 
	 * @param sql
	 *            根据传入的sql语句得到一些基本分页信息
	 * @param args
	 *            参数
	 * @param currentPage
	 *            当前页
	 * @param numPerPage
	 *            每页记录数
	 * @param jTemplate
	 *            JdbcTemplate实例
	 */
	public PageQuery(String sql, Object[] args, int currentPage, int numPerPage, JdbcTemplate jTemplate) {
		if (jTemplate == null) {
			throw new IllegalArgumentException("PageInation.jTemplate is null,please initial it first. ");
		} else if (sql == null || sql.equals("")) {
			throw new IllegalArgumentException("PageInation.sql is null,please initial it first. ");
		}

		// 计算总记录数
		StringBuffer totalSQL = new StringBuffer(" SELECT count(*) FROM ( ");
		totalSQL.append(sql);
		totalSQL.append(" ) totalTable ");

		// 总记录数
		setTotalRows(jTemplate.queryForObject(totalSQL.toString(), args, Integer.class));

		// 设置每页显示记录数
		setNumPerPage(numPerPage);

		// 计算总页数
		setTotalPages();

		// 设置要显示的页数
		setCurrentPage(currentPage);

		// 计算起始行数
		setStartIndex();
		// 计算结束行数
		setLastIndex();

		// System.out.println("lastIndex="+lastIndex);

		// 构造oracle数据库的分页语句
		/**
		 * StringBuffer paginationSQL = new StringBuffer(" SELECT * FROM ( ");
		 * paginationSQL.append(" SELECT temp.* ,ROWNUM num FROM ( ");
		 * paginationSQL.append(sql); paginationSQL.append(" ) temp where ROWNUM <= " +
		 * lastIndex); paginationSQL.append(" ) WHERE num > " + startIndex);
		 */

		// 装入结果集
		setResultList(jTemplate.queryForList(getMySQLPageSQL(sql, startIndex, numPerPage), args));
	}

	// 提供一个可以执行命名参数的方法
	public PageQuery(String sql, Map<String, ?> args, int currentPage, int numPerPage,
			NamedParameterJdbcTemplate jTemplate) {
		if (jTemplate == null) {
			throw new IllegalArgumentException("PageInation.jTemplate is null,please initial it first. ");
		} else if (sql == null || sql.equals("")) {
			throw new IllegalArgumentException("PageInation.sql is null,please initial it first. ");
		}

		// 计算总记录数
		StringBuffer totalSQL = new StringBuffer(" SELECT count(*) FROM ( ");
		totalSQL.append(sql);
		totalSQL.append(" ) totalTable ");

		// 总记录数
		setTotalRows(jTemplate.queryForObject(totalSQL.toString(), args, Integer.class));

		// 设置每页显示记录数
		setNumPerPage(numPerPage);

		// 计算总页数
		setTotalPages();

		// 设置要显示的页数
		setCurrentPage(currentPage);

		// 计算起始行数
		setStartIndex();
		// 计算结束行数
		setLastIndex();

		// System.out.println("lastIndex="+lastIndex);

		// 构造oracle数据库的分页语句
		/**
		 * StringBuffer paginationSQL = new StringBuffer(" SELECT * FROM ( ");
		 * paginationSQL.append(" SELECT temp.* ,ROWNUM num FROM ( ");
		 * paginationSQL.append(sql); paginationSQL.append(" ) temp where ROWNUM <= " +
		 * lastIndex); paginationSQL.append(" ) WHERE num > " + startIndex);
		 */

		// 装入结果集
		setResultList(jTemplate.queryForList(getMySQLPageSQL(sql, startIndex, numPerPage), args));
	}

	/**
	 * 构造MySQL数据分页SQL
	 * 
	 * @param queryString
	 * @param startIndex
	 * @param pageSize
	 * @return
	 */
	public String getMySQLPageSQL(String queryString, Integer startIndex, Integer pageSize) {
		String result = "";
		if (null != startIndex && null != pageSize) {
			result = queryString + " limit " + startIndex + "," + pageSize;
		} else if (null != startIndex && null == pageSize) {
			result = queryString + " limit " + startIndex;
		} else {
			result = queryString;
		}
		return result;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		// System.out.println("---------------"+this.totalPages);
		if (currentPage < 1) {
			currentPage = 1;
		}
		if (currentPage > this.totalPages) {
			currentPage = this.totalPages;
		}
		this.currentPage = currentPage;
	}

	public int getNumPerPage() {
		return numPerPage;
	}

	public void setNumPerPage(int numPerPage) {
		this.numPerPage = numPerPage;
	}

	public List getResultList() {
		return resultList;
	}


	public void setResultList(List resultList) {
		this.resultList = resultList;
	}

	public int getTotalPages() {
		return totalPages;
	}

	// 计算总页数
	public void setTotalPages() {
		if (totalRows % numPerPage == 0) {
			this.totalPages = totalRows / numPerPage;
		} else {
			this.totalPages = (totalRows / numPerPage) + 1;
		}
	}

	public int getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex() {
		this.startIndex = (currentPage - 1) * numPerPage < 0 ? 0 : (currentPage - 1) * numPerPage;
	}

	public int getLastIndex() {
		return lastIndex;
	}

	// 计算结束时候的索引
	public void setLastIndex() {
		if (totalRows < numPerPage) {
			this.lastIndex = totalRows;
		} else if ((totalRows % numPerPage == 0) || (totalRows % numPerPage != 0 && currentPage < totalPages)) {
			this.lastIndex = currentPage * numPerPage;
		} else if (totalRows % numPerPage != 0 && currentPage == totalPages) {// 最后一页
			this.lastIndex = totalRows;
		}
	}

}