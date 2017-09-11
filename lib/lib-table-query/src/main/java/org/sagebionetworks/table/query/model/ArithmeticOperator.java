package org.sagebionetworks.table.query.model;

/**
 * The four arithmetic operators.
 *
 */
public enum ArithmeticOperator {

	ASTERISK("*"),
	SOLIDUS("/"),
	PLUS_SIGN("+"),
	MINUS_SIGN("-");
	
	ArithmeticOperator(String sql){
		this.sql = sql;
	}
	String sql;
	
	public String toSQL(){
		return this.sql;
	}
}