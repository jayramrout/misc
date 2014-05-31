package com.dbquery.domain;

/**
 * @author jrout
 *
 */

public class Column {
	private String name;
	private String displaySize;
	private String typeName;
	public Column() {
		// TODO Auto-generated constructor stub
	}
	public Column(String name , String displaySize, String typeName) {
		this.name = name;
		this.displaySize = displaySize;
		this.typeName = typeName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDisplaySize() {
		return displaySize;
	}
	public void setDisplaySize(String displaySize) {
		this.displaySize = displaySize;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String toString() {
		return name+" (" + displaySize +") "+ typeName;
	}
}
