package com.dbquery.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import com.dbquery.components.DBQueryComponent;
import com.dbquery.components.Environment;
import com.dbquery.constants.DBContants;

/**
 * @author jrout
 *
 */
public class DBQueryUtil { 
	/**
	 * *
	 * @param environment
	 * @return
	 */
	public static String getShortName(String environment) {
		String shortValue = "";
		if (Environment.Unit.getValue().equals(environment))
			shortValue = "U";
		else if (Environment.Integ.getValue().equals(environment))
			shortValue = "I";
		else if (Environment.System.getValue().equals(environment))
			shortValue = "S";
		return "(" + shortValue + ")";
	}
	
	/**
	 * 
	 * @param table
	 * @param file
	 */
	public static void toExcelFile(JTable table, File file) throws Exception {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Data");

		int rownum = 0;

		TableModel model = table.getModel();
		int cellnum = 0;
		HSSFRow row = sheet.createRow(rownum++);

		HSSFFont font = wb.createFont();
		font.setFontHeightInPoints((short) 10);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); // Setting Bold font

		HSSFCellStyle boldStyle = wb.createCellStyle();
		boldStyle.setFont(font); // Attaching the font to the Style

		for (int i = 0; i < model.getColumnCount(); i++) {
			Cell cell = row.createCell(cellnum++);
			cell.setCellValue((String) model.getColumnName(i));
			cell.setCellStyle(boldStyle); // Applying Style to the Cell.
		}

		for (int i = 0; i < model.getRowCount(); i++) {
			cellnum = 0;
			row = sheet.createRow(rownum++);
			for (int j = 0; j < model.getColumnCount(); j++) {
				Cell cell = row.createCell(cellnum++);
				/*
				 * if(obj instanceof Date) cell.setCellValue((Date)obj); else
				 * if(obj instanceof Boolean) cell.setCellValue((Boolean)obj);
				 * else if(obj instanceof String)
				 * cell.setCellValue((String)obj); else if(obj instanceof
				 * Double) cell.setCellValue((Double)obj);
				 */
				cell.setCellValue((String) model.getValueAt(i, j));
			}
		}
		try {
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.close();
			System.out.println("Excel written successfully..");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks UserName Password
	 * 
	 * @return
	 */
	public static boolean isUserNamePasswordCorrect(Properties prop) {

		Connection conn = null;
		try {
			if(prop.getProperty("dac_uname").trim().equals("") || prop.getProperty("dac_password").equals("")){
				System.out.println("UserName Or Password is not been entered.. Please enter !!!");
				return Boolean.FALSE;
			}
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			conn = DriverManager.getConnection(prop.getProperty("dburl_Integ").trim(), prop.getProperty("dac_uname").trim(), prop.getProperty("dac_password")
					.trim());
			System.out.println("UserName Password Authorization is Successfull");
			return Boolean.TRUE;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return Boolean.FALSE;
		} catch (SQLException e) {
			System.out.println("UserName Password Authorization Failed...");
			return Boolean.FALSE;
		} catch(Exception exp){
			exp.printStackTrace();
			return Boolean.FALSE;
		}finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}
	
	/**
	 * 
	 */
	public static void showUserNamePwdOption(Properties prop) {
		
		String loginMessage = "Login";
	    JLabel userNamelabel = new JLabel("Enter DAC UserName:");
	    JTextField userNameField = new JTextField(prop.getProperty("dac_uname"), 10);
	    JLabel passwordlabel = new JLabel("Enter DAC Password:");
	    JPasswordField passField = new JPasswordField(10);

	    Object[] array = { userNamelabel, userNameField, passwordlabel, passField };

	    boolean usernamePwdEntered = false;
	    for (int i = 0; i < 3; i++) {
	      passField.setText("");
	      if (i > 0) {
	        loginMessage = "UserName or PWD is Wrong Login Again!!";
	      }
	      int res = JOptionPane.showConfirmDialog(null, array, loginMessage, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

	      if (res == JOptionPane.OK_OPTION) {
	        String username = userNameField.getText();
	        char[] password = passField.getPassword();
	        String pwd = new String(password);
	        if ((username != null) && (!"".equals(username.trim())) && (pwd != null) && (!"".equals(pwd.trim()))) {
	          prop.setProperty("dac_uname", username.trim());
	          prop.setProperty("dac_password", pwd);
	          if (isUserNamePasswordCorrect(prop))
	          {
	            usernamePwdEntered = true;
	            break;
	          }
	        } 
	      } else if (res == JOptionPane.CANCEL_OPTION || res == JOptionPane.CLOSED_OPTION) {
	        System.exit(0);
	      }
	    }
	    if (!usernamePwdEntered) {
	      JOptionPane.showMessageDialog(null, new String[] { "Failed to Enter DAC UserName Password . Try again Later" }, "Error", JOptionPane.ERROR_MESSAGE);
	      System.exit(0);
	    }
	}
	
	/**
	 * 
	 * @param prop
	 */
	public static void authenticateUserAndLoadProperties(Properties prop) {
		FileWriter fw = null;
		InputStream inputStream = null;
		FileInputStream fis = null;
		String userDir = System.getProperty("user.dir");
		String filePathOne = userDir + DBContants.CONFIG_FILE_NAME;
		String currentFilePathInUse = "";
		try {
			File fileOne = new File(filePathOne);

			if (fileOne.exists()) {
				fis = new FileInputStream(fileOne);
				currentFilePathInUse = fileOne.getAbsolutePath();
				prop.load(fis);
				if (!DBQueryUtil.isUserNamePasswordCorrect(prop)) {
					DBQueryUtil.showUserNamePwdOption(prop);
					prop.store(new FileOutputStream(currentFilePathInUse), "jdbc:db2://<IPADDRESS>:<PORT>/DSNA:currentSchema=<SchemaName>;");
				}
				
				inputStream = DBQueryComponent.class.getClassLoader().getResourceAsStream("db2ErrorCode.properties");
				prop.load(inputStream);

			}else {
				inputStream = DBQueryComponent.class.getClassLoader().getResourceAsStream("config.properties");
				fw = new FileWriter(fileOne);
				int c = inputStream.read();
				while (c != -1) {
					fw.write(c);
					c = inputStream.read();
				}
				fw.flush();
				JOptionPane.showMessageDialog(null, new String[]{"Edit config.properties file under the current location and then run the application again"},
						"Result", JOptionPane.WARNING_MESSAGE);
				System.exit(0);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null)
					inputStream.close();
				if (fw != null)
					fw.close();
			} catch (IOException ioe) {
			}
		}
	}
	/**
	 * 
	 * @return
	 */
	public static String getInitialMessage(){
		return "This Tool Support : SELECT UPDATE DELETE DESCRIBE : Operations\n"
				+ "Choose the environment(default is Integ) , disable/enable QueryName Popup(This is used to give a name to the query you execute)\n"
				+ "Opens a saved file to load in the editor\n"
				+ "Tips:\n"
				+ "\tSelect the SQL and press Alt+R or F5 or the GREEN button on the top left to execute the Query.\n"
				+ "\tTo Save the table in excel , Right click and choose Export To Excel\n"
				+ "\tYou can add the table in the CUSTOM schema by adding an entry in the config.properties . Add table name entry for custom_tables separated by comma.\n"
				+ "\tTriple Click on a cell to see the value in a popup Editor\n";
	}
}
