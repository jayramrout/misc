package com.jsonviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;

/**
 * @author Jayram Rout
 *
 */
class TabbedPaneController {
	JPanel jPanelMain = null;
	JTabbedPane jTabbedPane;
	JPanel defaultTreePanel = null;
	FileTransferHandler fileTransferHandler;

	boolean noTabs = true;

	String fileSeparator;

	/**
	 * @param jTabbedPane
	 * @param jPanelMain
	 */
	public TabbedPaneController(JTabbedPane jTabbedPane, JPanel jPanelMain) {
		this.jTabbedPane = jTabbedPane;
		this.jPanelMain = jPanelMain;
		fileTransferHandler = new FileTransferHandler(this);
		fileSeparator = System.getProperty("file.separator");
		// The split method in the String class uses
		// regular expressions to define the text used for
		// the split. The forward slash "\" is a special
		// character and must be escaped. Some look and feels,
		// such as Microsoft Windows, use the forward slash to
		// delimit the path.
		if ("\\".equals(fileSeparator)) {
			fileSeparator = "\\\\";
		}
		init();
	}

	/**
	 * @param filename
	 * @return
	 */
	public void addTab(String filename) {
		if (noTabs) {
			//jPanelMain.remove(defaultTreePanel);
			jPanelMain.add(jTabbedPane, BorderLayout.CENTER);
			noTabs = false;
		}
		String[] str = filename.split(fileSeparator);
		makeJTreePane(str[str.length - 1], filename);
	}

	/**
	 * Remove all tabs and their components, then put the default file area
	 * back.
	 */
	public void clearAll() {
		if (noTabs == false) {
			jTabbedPane.removeAll();
			jPanelMain.remove(jTabbedPane);
			initializeTab = true;
			Helper.fileList.clear();
		}
		init();
	}
	boolean initializeTab = true;
	private void init() {
		noTabs = true;
		if (initializeTab) {
			initializeTab = false;
			TreeView tv = new TreeView();
			JTree treePane = tv.getTreeView(getClass().getResourceAsStream("/StoreJSON.txt"));
			
			treePane.setTransferHandler(fileTransferHandler);
			JScrollPane treeScrollPane = new JScrollPane(treePane);

			final JTextArea instructionTextArea = new JTextArea();
			instructionTextArea.setForeground(Color.blue);
			try {
				instructionTextArea.read(new InputStreamReader(
	                    getClass().getResourceAsStream("/ReadMe.txt")),null);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
			JScrollPane consoleScrollPane = new JScrollPane(instructionTextArea);
			
			JSplitPane lowerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, consoleScrollPane);
			lowerSplitPane.setDividerLocation(600);
			
			jTabbedPane.addTab("Store", null, (Component) lowerSplitPane, JSONConstants.DEFAULT);
			jTabbedPane.setSelectedComponent((Component) lowerSplitPane);
			jPanelMain.add(jTabbedPane, BorderLayout.CENTER);
			jPanelMain.repaint();
		}
	}
	
	/**
	 * This init was without split
	 */
	/*private void init() {
		noTabs = true;
		if (initializeTab) {
			initializeTab = false;
			TreeView tv = new TreeView();
			JTree treePane = tv.getTreeView(getClass().getResourceAsStream("/StoreJSON.txt"));
			
			treePane.setTransferHandler(fileTransferHandler);
			JScrollPane fileScrollPane = new JScrollPane(treePane);
			
			jTabbedPane.addTab("Store", null, (Component) fileScrollPane, JSONConstants.DEFAULT);
			jTabbedPane.setSelectedComponent((Component) fileScrollPane);
//			defaultTreePanel = new JPanel(new BorderLayout(), false);
//			defaultTreePanel.add(fileScrollPane, BorderLayout.CENTER);
			jPanelMain.add(jTabbedPane, BorderLayout.CENTER);
			jPanelMain.repaint();
		}
		
	}*/

	/**
	 * @param name
	 * @param fileName
	 */
	protected void makeJTreePane(String name, String fileName) {
		FileInputStream is = null;
		try {
			is = new FileInputStream(new File(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		TreeView tv = new TreeView();
		JTree treePane = tv.getTreeView(is);
		treePane.setTransferHandler(fileTransferHandler);

		JScrollPane fileScrollPane = new JScrollPane(treePane);
		jTabbedPane.addTab(name, null, (Component) fileScrollPane, fileName);
		jTabbedPane.setSelectedComponent((Component) fileScrollPane);
		
		initTabComponent();
	}
	
	private void initTabComponent() {
		jTabbedPane.setTabComponentAt(jTabbedPane.getSelectedIndex(), new ButtonTabComponent(this));
	}
}