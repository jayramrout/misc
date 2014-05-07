package com.jsonviewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;

/**
 * @author Jayram Rout
 *
 */
class TabbedPaneController {
	JPanel jPanelMain = null;
	JTabbedPane jTabbedPane;
	JPanel emptyFilePanel = null;
	FileTransferHandler fileTransferHandler;

	boolean noFiles = true;

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
		if (noFiles) {
			jPanelMain.remove(emptyFilePanel);
			jPanelMain.add(jTabbedPane, BorderLayout.CENTER);
			noFiles = false;
		}
		String[] str = filename.split(fileSeparator);
		makeJTreePane(str[str.length - 1], filename);
	}

	/**
	 * Remove all tabs and their components, then put the default file area
	 * back.
	 */
	public void clearAll() {
		if (noFiles == false) {
			jTabbedPane.removeAll();
			jPanelMain.remove(jTabbedPane);
		}
		init();
	}

	private void init() {
		noFiles = true;
		if (emptyFilePanel == null) {
			TreeView tv = new TreeView();
			JTree treePane = tv.getTreeView(getClass().getResourceAsStream("/StoreJSON.txt"));
			
			treePane.setTransferHandler(fileTransferHandler);
			JScrollPane fileScrollPane = new JScrollPane(treePane);
			emptyFilePanel = new JPanel(new BorderLayout(), false);
			emptyFilePanel.add(fileScrollPane, BorderLayout.CENTER);
		}
		jPanelMain.add(emptyFilePanel, BorderLayout.CENTER);
		jPanelMain.repaint();
	}

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
		jTabbedPane.setTabComponentAt(jTabbedPane.getSelectedIndex(), new ButtonTabComponent(jTabbedPane, this));
	}
}