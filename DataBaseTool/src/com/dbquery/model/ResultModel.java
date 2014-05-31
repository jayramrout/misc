package com.dbquery.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;

import com.dbquery.domain.Column;
import com.dbquery.domain.DataBase;
import com.dbquery.domain.Schema;
import com.dbquery.domain.Table;
import com.ibm.db2.jcc.DB2DataSource;

/**
 * @author Jayram Rout
 * 
 */
public class ResultModel {
	private PreparedStatement pstmt = null;
	private ResultSet rset = null;
	private Connection conn = null;
	private BufferedReader br = null;
	public String[] columnNames;
	public Object[][] data;
	StringBuilder script = new StringBuilder();
	Scanner scanner = null;
	private static String ALL_OPER = "delete update select describe desc insert with";
	private static String DESC_OPER = "describe desc";
	private static String SELECT_DESC_OPER = "select describe desc";
	private static String DEL_UPDATE_INSERT_OPER = "delete update insert";
	Boolean deleteUpdateOperation = Boolean.FALSE;
	private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");
	// public static int i = 0;

	public Boolean isDeleteUpdateOperation() {
		return deleteUpdateOperation;
	}
	/**
	 * This Constructor is only for testing.
	 */
	public ResultModel() {
		columnNames = new String[]{"First Name", "Last Name", "Sport", "# of Years", "Vegetarian"};
		data = new Object[][]{{"Jayram", "Rout", "Snowboarding", new Integer(5), new Boolean(false)},
				{"Roma", "Rout", "Rowing", new Integer(3), new Boolean(true)}, {"Sue", "Black", "Knitting", new Integer(2), new Boolean(false)},
				{"Jane", "White", "Speed reading", new Integer(20), new Boolean(true)}};
	}

	public ResultModel(String queryText, String environment, Properties prop) throws Exception {
		List<String> columnNamesList = new ArrayList<String>();
		List<String> entityDetails = null;
		List rowDetails = new ArrayList();
		long startTime;
		try {
			scanner = new Scanner(queryText);
			String line = null;
			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				if (line != null && !(line.trim().startsWith("--")) && line.trim().length() > 0) {
					script.append(line);
					script.append(LINE_SEPARATOR);
				}
			}
			String query = script.toString().trim();
			query = query.replaceAll("\r\n", "\n");
			System.out.println(query);
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			String operation = query.substring(0, query.indexOf(" ")).toLowerCase();
			if (!ALL_OPER.contains(operation)) {
				System.out.println("Unsupported SQL Operation");
				throw new RuntimeException("Unsupported SQL Operation");
			}
			long startTimeToGetConnection = System.currentTimeMillis();
//			conn = DriverManager.getConnection(prop.getProperty("dburl_" + environment).trim(), prop.getProperty("dbuname_" + environment).trim(),
//					prop.getProperty("dbpassword_" + environment).trim());
			
			
			/*DB2DataSource ds = new DB2DataSource();
            ds.setUser("dac709");
            ds.setPassword("");
            ds.setCurrentSchema("ANIC01TC");
            ds.setServerName("172.16.32.30");
            ds.setPortNumber(446);
            ds.setDatabaseName("DSNA");
            ds.setDriverType(4);*/
            
//			conn = ds.getConnection();
			conn =		DriverManager.getConnection(prop.getProperty("dburl_" + environment).trim(), prop.getProperty("dac_uname").trim(),
					prop.getProperty("dac_password").trim());
			
			
            
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			long endTimeToGetConnection = System.currentTimeMillis();
			System.out.println("Got Connected to DB in ...." + (endTimeToGetConnection - startTimeToGetConnection) / 1000 + " Seconds");

			startTime = System.currentTimeMillis();
			if (SELECT_DESC_OPER.contains(operation)) {
				if (DESC_OPER.contains(operation)) {
					final String tableName = query.substring(query.indexOf(" "));
					query = "select * from " + tableName.replace(";", "") + " fetch first 1 rows only;";
					columnNamesList.add("Field");
					columnNamesList.add("Size");
					columnNamesList.add("Datatype");
				}
				System.out.println("Query Executed : " + query);
				pstmt = conn.prepareStatement(query);
				pstmt.setEscapeProcessing(true);
				pstmt.setQueryTimeout(60);
				rset = pstmt.executeQuery();
				long endTime = System.currentTimeMillis();
				System.out.println("Total Time For Executing the Query in Seconds :" + (endTime - startTime) / 1000);

				if (rset != null) {
					ResultSetMetaData metaData = rset.getMetaData();

					int cc = metaData.getColumnCount();

					if (DESC_OPER.contains(operation)) {
						for (int i = 0; i < cc; i++) {
							entityDetails = new ArrayList<String>();
							entityDetails.add(metaData.getColumnName(i + 1));
							entityDetails.add(String.valueOf(metaData.getColumnDisplaySize(i + 1)));
							entityDetails.add(metaData.getColumnTypeName(i + 1));

							rowDetails.add(entityDetails.toArray());
						}
					} else {
						for (int i = 1; i <= cc; i++) {
							columnNamesList.add(metaData.getColumnName(i));
						}

						while (rset.next()) {
							entityDetails = new ArrayList();
							for (int i = 1; i <= cc; i++) {
								if (rset != null && rset.getObject(i) != null) {
									
									String dataValue = String.valueOf(rset.getObject(i)).trim();
									if (rset.getObject(i) instanceof Clob) {
										InputStream in = rset.getAsciiStream(i);
										StringWriter w = new StringWriter();
										IOUtils.copy(in, w);
										String clobAsString = w.toString();
										entityDetails.add(dataValue.startsWith("<HTML>") ? " "+ clobAsString : clobAsString);
									} else {
										entityDetails.add(dataValue.startsWith("<HTML>") ? " "+dataValue : dataValue);
									}
								} else {
									entityDetails.add("NULL");
								}
							}
							rowDetails.add(entityDetails.toArray());
						}
					}
				} else {
					System.out.println("No Records Found");
				}
			} else if (DEL_UPDATE_INSERT_OPER.contains(operation)) {
				deleteUpdateOperation = Boolean.TRUE;

				entityDetails = new ArrayList();
				pstmt = conn.prepareStatement(query);
				pstmt.setQueryTimeout(60);
				int rows = pstmt.executeUpdate();
				columnNamesList.add("Rows " + operation + " Count ");
				entityDetails.add(rows + "");
				rowDetails.add(entityDetails.toArray());
				System.out.println("Rows affected in DDL : " + rows);
				System.out.println("DDL Statement Complete : Query : " + query);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw e;
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			closeConnections();
		}
		int colSize = columnNamesList.size();

		columnNames = new String[colSize];
		for (int i = 0; i < colSize; i++) {
			columnNames[i] = (String) columnNamesList.get(i);
		}

		data = new Object[rowDetails.size()][columnNamesList.size()];
		for (int i = 0; i < rowDetails.size(); i++) {
			data[i] = (Object[]) rowDetails.get(i);
		}
	}
	
	/**
	 * 
	 */
	public void closeConnections() {
		try {
			if (br != null)
				br.close();
			if(rset != null) 
				rset.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				conn.close();
			System.out.println("Connection Closed...");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param queryText
	 * @param environment
	 * @param prop
	 */
	public void generateJSON() {

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		Connection conn = null;
		BufferedReader br = null;
		long startTime;
		ObjectMapper mapper = new ObjectMapper();
		try {

			File file = new File(System.getProperty("user.dir") + "/resources/NHSchemas.json");

			String query = "SELECT TBCREATOR as SCHEMA , TBNAME , NAME as COLUMN , COLTYPE , LENGTH from sysibm.syscolumns where tbcreator like 'ANI%' and (tbname like 'T0%' or tbname like 'TF%' or tbname like 'TC%' or tbname like 'TX%')";
			Class.forName("com.ibm.db2.jcc.DB2Driver");

			long startTimeToGetConnection = System.currentTimeMillis();
			conn = DriverManager.getConnection("jdbc:db2://172.16.32.30:446/DSNA:currentSchema=ANIC01SS;", "ITDB203", "ITDB203X");
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			long endTimeToGetConnection = System.currentTimeMillis();
			System.out.println("Got Connected to DB in ...." + (endTimeToGetConnection - startTimeToGetConnection) / 1000 + " Seconds");

			startTime = System.currentTimeMillis();
			System.out.println("Query Executed : " + query);

			pstmt = conn.prepareStatement(query);
			pstmt.setQueryTimeout(60);
			resultSet = pstmt.executeQuery();

			long endTime = System.currentTimeMillis();
			System.out.println("Total Time For Executing the Query in Seconds :" + (endTime - startTime) / 1000);

			DataBase dataBase = new DataBase("NewHeights");
			List<Schema> schemaList = new ArrayList<Schema>();
			List<Column> columnList = null;
			List<Table> tableList = null;

			List<String> uniqueStringList = new ArrayList<String>();
			Schema schema = null;
			Table table = null;
			if (resultSet != null) {
				while (resultSet.next()) {
					String schemaName = resultSet.getString("SCHEMA").trim();
					if (!uniqueStringList.contains(schemaName)) {
						uniqueStringList.add(schemaName);
						tableList = new ArrayList<Table>();
						schema = new Schema(resultSet.getString("SCHEMA").trim());
						schema.setTables(tableList);
						schemaList.add(schema);
					}
					String schema_table_name = schemaName + resultSet.getString("TBNAME").trim();
					if (!uniqueStringList.contains(schema_table_name)) {
						uniqueStringList.add(schema_table_name);
						columnList = new ArrayList<Column>();
						table = new Table(resultSet.getString("TBNAME").trim());
						table.setColumns(columnList);
						tableList.add(table);
						
					}
					String schema_table_column_name = schema_table_name + resultSet.getString("COLUMN").trim();
					if (!uniqueStringList.contains(schema_table_column_name)) {
						uniqueStringList.add(schema_table_column_name);
						Column column = new Column(resultSet.getString("COLUMN").trim(), resultSet.getString("LENGTH").trim(), resultSet.getString("COLTYPE")
								.trim());
						columnList.add(column);
					}
				}
				dataBase.setSchemas(schemaList);
			}
			mapper.writeValue(file, dataBase);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
				System.out.println("Connection Closed and Results Displayed...");
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("ResultModel.generateJSON() Completed");

	}

	public static void main(String[] args) {
		ResultModel model = new ResultModel();
//		model.generateJSON();
	}
}