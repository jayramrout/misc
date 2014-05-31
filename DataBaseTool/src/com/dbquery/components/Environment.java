package com.dbquery.components;

public enum Environment {
	Integ("Integ"), System("System"), Unit("Unit"), Other("Other");

	String env;
	public String getValue() {
		return env;
	}
	private Environment(String env) {
		this.env = env;
	}
}