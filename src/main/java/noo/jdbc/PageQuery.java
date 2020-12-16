package noo.jdbc;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import noo.jdbc.JdbcSvr.DBType;

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
	
	private transient boolean isQueryTotalCount = true;



	/**
	 * 分页构造函数
	 *   
	 */
	public PageQuery(String sql, Object[] args, int currentPage, int numPerPage, JdbcSvr svr, boolean query_total) {
		if (svr == null) {
			throw new IllegalArgumentException("PageInation.jTemplate is null,please initial it first. ");
		} else if (sql == null || sql.equals("")) {
			throw new IllegalArgumentException("PageInation.sql is null,please initial it first. ");
		}

		numPerPage = numPerPage>2000? 2000: numPerPage;
		
		this.isQueryTotalCount = query_total;
		
		if(this.isQueryTotalCount) {
			// 计算总记录数
			StringBuffer totalSQL = new StringBuffer(" SELECT count(*) FROM ( ");
			totalSQL.append(sql);
			totalSQL.append(" ) totalTable ");
	
			// 总记录数
			setTotalRows(svr.qryInt(totalSQL.toString(), args));
			//setTotalRows(svr.getJdbcTemplate().queryForObject(totalSQL.toString(), args, Integer.class));
		}else {
			this.setTotalRows(0);
		}

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

		// 装入结果集
	
		setResultList(svr.jdbcTemplate.queryForList(getPageSQL(svr.dbtype(),sql, startIndex, numPerPage), args));
	}

	// 提供一个可以执行命名参数的方法
	public PageQuery(String sql, Map<String, ?> args, int currentPage, int numPerPage, JdbcSvr svr, boolean query_total) {
		if (svr == null) {
			throw new IllegalArgumentException("PageInation.jTemplate is null,please initial it first. ");
		} else if (sql == null || sql.equals("")) {
			throw new IllegalArgumentException("PageInation.sql is null,please initial it first. ");
		}
		
		numPerPage = numPerPage>2000? 2000: numPerPage;
		
		this.isQueryTotalCount = query_total;
		if(this.isQueryTotalCount) {
			// 计算总记录数
			StringBuffer totalSQL = new StringBuffer(" SELECT count(*) FROM ( ");
			totalSQL.append(sql);
			totalSQL.append(" ) totalTable ");
	
			// 总记录数
			setTotalRows(svr.named.queryForObject(totalSQL.toString(), args, Integer.class));
		}else {
			setTotalRows(0);
		}

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

		// 装入结果集
		setResultList(svr.named.queryForList(getPageSQL(svr.dbtype(),sql, startIndex, numPerPage), args));
	}

	
	public String getPageSQL(DBType dbtype,String queryString, Integer startIndex, Integer pageSize) {
		if(dbtype==DBType.MYSQL)
			return this.getMySQLPageSQL(queryString, startIndex, pageSize);
		else if(dbtype==DBType.POSTGRES)
			return this.getPostgresPageSQL(queryString, startIndex, pageSize);
		else if(dbtype==DBType.ORACLE)
			return this.getOraclePageSQL(queryString, startIndex, pageSize);
		else
			return this.getMySQLPageSQL(queryString, startIndex, pageSize);
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
		return queryString + " limit " + startIndex + "," + pageSize; 
	}
	
	/**
	 * 构造PostgreSQL数据分页SQL
	 * 
	 * @param queryString
	 * @param startIndex
	 * @param pageSize
	 * @return
	 */
	public String getPostgresPageSQL(String queryString, Integer startIndex, Integer pageSize) {
		return queryString + " limit " + pageSize + " offset " + startIndex; 
	}
	
	/**
	 * 构造Oracle数据分页SQL
	 * 
	 * @param queryString
	 * @param startIndex
	 * @param pageSize
	 * @return
	 *  
	 */
	public String getOraclePageSQL(String queryString, Integer startIndex, Integer pageSize) { 
		return "SELECT * FROM ( SELECT t.* ,ROWNUM num FROM ( "+queryString+" ) t where ROWNUM <= "+(startIndex + pageSize)+" ) WHERE num > "+startIndex; 
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		// System.out.println("---------------"+this.totalPages);
		if (currentPage < 1) {
			currentPage = 1;
		}
		if (this.isQueryTotalCount && currentPage > this.totalPages) {
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
		if(!this.isQueryTotalCount) {  //如果不查询总条数，直接计算LastIndex
			this.lastIndex = currentPage * numPerPage;
		}else if (totalRows < numPerPage) {
			this.lastIndex = totalRows;
		} else if ((totalRows % numPerPage == 0) || (totalRows % numPerPage != 0 && currentPage < totalPages)) {
			this.lastIndex = currentPage * numPerPage;
		} else if (totalRows % numPerPage != 0 && currentPage == totalPages) {// 最后一页
			this.lastIndex = totalRows;
		}
	}
 

}