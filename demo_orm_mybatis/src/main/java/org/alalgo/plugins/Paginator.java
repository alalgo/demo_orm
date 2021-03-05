package org.alalgo.plugins;

import org.apache.ibatis.session.RowBounds;

public class Paginator extends RowBounds {
	private int pageNum = 0;
	private int perPage = Integer.MAX_VALUE;
	private Long count;
	public Paginator() {
		super();
	}
	public Paginator(int offset, int limit) {
		super();
		this.pageNum = offset;
		this.perPage = limit;
	}
	public Long getCount() {
		return count;
	}
	public void setCount(Long count) {
		this.count = count;
	}
	
	@Override
	public int getOffset() {
		return this.pageNum;
	}
	@Override
	public int getLimit() {
		return this.perPage;
	}
	
}
