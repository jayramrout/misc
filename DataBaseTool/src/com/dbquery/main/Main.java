package com.dbquery.main;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.dbquery.components.DBQueryComponent;


/**
 * @author jayram rout
 *
 */
public class Main {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
//				UIManager.put("swing.boldMetal", Boolean.FALSE);
				try {
				UIManager.setLookAndFeel(
			            UIManager.getSystemLookAndFeelClassName());
				}catch(Exception exp) {
					exp.printStackTrace();
				} 
				new DBQueryComponent().showQueryPanel();
			}
		});
	}
}