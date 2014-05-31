package com.dbquery.domain;

import java.util.List;

/**
 * @author jrout
 *
 */
public class DataBase {
	private String name;
	private List<Schema> schemas;
	public DataBase() {
		// TODO Auto-generated constructor stub
	}
	public DataBase(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Schema> getSchemas() {
		return schemas;
	}
	public void setSchemas(List<Schema> schemas) {
		this.schemas = schemas;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return schemas+"";
	}
}
