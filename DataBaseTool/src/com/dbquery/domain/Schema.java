package com.dbquery.domain;

import java.util.List;

/**
 * @author jrout
 *
 */
public class Schema {
	private String name;
	private List<Table> tables;
	public Schema() {
		// TODO Auto-generated constructor stub
	}
	public Schema(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Table> getTables() {
		return tables;
	}
	public void setTables(List<Table> tables) {
		this.tables = tables;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return name+" "+tables;
	}
}
