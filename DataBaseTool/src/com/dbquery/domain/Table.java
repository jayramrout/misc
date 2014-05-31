package com.dbquery.domain;

import java.util.List;

/**
 * @author jrout
 *
 */
public class Table {
	private String name;
	private List<Column> columns;
	public Table() {
		// TODO Auto-generated constructor stub
	}
	public Table(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Column> getColumns() {
		return columns;
	}
	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return name+"\n"+ columns;
	}
}
