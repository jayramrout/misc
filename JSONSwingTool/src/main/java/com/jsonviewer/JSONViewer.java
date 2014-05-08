package com.jsonviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.jayway.jsonpath.JsonPath;

/**
 * @author Jayram Rout
 *
 */
public class JSONViewer extends JFrame implements ActionListener{

	TabbedPaneController tabbedPaneController;
	DefaultMutableTreeNode root_defaultMutableTreeNode;
	private DefaultTreeModel m_model;
	private PrintStream standardOut;
	JTree m_tree;
	JButton clearTabs;
	private JTextField m_searchText;
	private JButton queryButton;

	public JSONViewer() {
		init();
	}
	public void init() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JTabbedPane jTabbedPane = new JTabbedPane();
		jTabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		JPanel jPanelMain = new JPanel(new BorderLayout());

		tabbedPaneController = new TabbedPaneController(jTabbedPane, jPanelMain);

		JPanel jPanelLower = new JPanel(new BorderLayout());
		jPanelLower.setSize(new Dimension(200, 200));

		JPanel searchPanel = new JPanel(new BorderLayout());
		searchPanel.setBorder(BorderFactory.createEtchedBorder());

		clearTabs = new JButton("Clear Tabs");
		clearTabs.addActionListener(this);

		m_searchText = new JTextField(70);
		Font font = new Font("Courier", Font.PLAIN, 13);
		m_searchText.setFont(font);
		m_searchText.setForeground(Color.BLUE);
		
		queryButton = new JButton("Query");
		 
		searchPanel.add(clearTabs , BorderLayout.WEST);
		searchPanel.add(m_searchText,BorderLayout.CENTER);
		searchPanel.add(queryButton,BorderLayout.EAST);

		final JTextArea consoleTextArea = new JTextArea();
//		consoleTextArea.setEditable(false);
		JScrollPane consoleScrollPane = new JScrollPane(consoleTextArea);

		PrintStream printStream = new PrintStream(new CustomOutputStream(consoleTextArea));
		
		// keeps reference of standard output stream
		standardOut = System.out;
		System.setOut(printStream);
		System.setErr(printStream);

		JPanel outputPanel = new JPanel();
		outputPanel.setLayout(new BorderLayout());
		outputPanel.add(consoleScrollPane, BorderLayout.CENTER);
		
		try {
			consoleTextArea.read(new InputStreamReader(
                    getClass().getResourceAsStream("/ReadMe.txt")),null);
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		queryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//consoleTextArea.setText("");
				queryJSON(m_searchText.getText());

				// DefaultMutableTreeNode node =
				// searchNode(m_searchText.getText());
				/*
				 * if (node != null) { TreeNode[] nodes =
				 * m_model.getPathToRoot(node); TreePath path = new
				 * TreePath(nodes); m_tree.scrollPathToVisible(path);
				 * m_tree.setSelectionPath(path); } else {
				 * System.out.println("Node with string " +
				 * m_searchText.getText() + " not found"); }
				 */
			}
		});
		getRootPane().setDefaultButton(queryButton);		
		JSplitPane lowerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, searchPanel, outputPanel);
		lowerSplitPane.setDividerSize(0);
		jPanelLower.add(lowerSplitPane);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jPanelMain, jPanelLower);
		splitPane.setDividerSize(2);
		splitPane.setDividerLocation(500);
		add(splitPane);

		Dimension d = new Dimension(950, 800);
		Container container = getContentPane();
		container.setPreferredSize(d);
		pack();
//		setResizable(false);
		setVisible(true);

		String hostname = null;
		try {
			InetAddress addr;
			addr = InetAddress.getLocalHost();
			hostname = addr.getHostName();
			if (hostname != null && hostname.length() >= 1) {
				hostname = hostname.substring(5, hostname.length() - 1);
				hostname += " to ";
			}
		} catch (Exception exp) {
			exp.printStackTrace();
			hostname = "to ";
		}

		setTitle("Welcome " + hostname + "New Heights JSON Query");

		setLocationRelativeTo(null);
		setVisible(true);
		
		Image img = null;
		try {
			img = ImageIO.read(getClass().getResource("/json.png"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		setIconImage( img );
	}

	/**
	 * 
	 * @param nodeStr
	 * @return
	 */
	/*public DefaultMutableTreeNode searchNode(String nodeStr) {
		DefaultMutableTreeNode node = null;

		int selectedIndex = tabbedPaneController.jTabbedPane.getSelectedIndex();

		String fileName = tabbedPaneController.jTabbedPane.getToolTipTextAt(selectedIndex);
		String json = null;
		try {
			json = new Scanner(new File(fileName)).useDelimiter("\\Z").next();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		System.out.println(JsonPath.read(json, nodeStr));

		
		 * JScrollPane fileScrollPane =
		 * (JScrollPane)tabbedPaneController.jTabbedPane.getSelectedComponent();
		 * m_tree = (JTree)fileScrollPane.getViewport().getView(); m_model =
		 * (DefaultTreeModel)m_tree.getModel(); root_defaultMutableTreeNode =
		 * (DefaultMutableTreeNode)m_model.getRoot();
		 * 
		 * Enumeration e =
		 * root_defaultMutableTreeNode.breadthFirstEnumeration(); while
		 * (e.hasMoreElements()) { node = (DefaultMutableTreeNode)
		 * e.nextElement(); if (nodeStr.equals(node.getUserObject().toString()))
		 * { return node; } }
		 
		return null;
	}*/
	/**
	 * Query JSON String
	 * @param nodeStr
	 */
	private void queryJSON(String nodeStr) {
		int selectedIndex = tabbedPaneController.jTabbedPane.getSelectedIndex();
		String fileName = tabbedPaneController.jTabbedPane.getToolTipTextAt(selectedIndex);
		String json = null;
		Scanner scanner = null;
		File file = null;
		try {
			file = new File(fileName);
			scanner = new Scanner(file).useDelimiter("\\Z");
			json = scanner.next();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} finally {
			if (scanner != null)
				scanner.close();
		}
		String []queryString = nodeStr.split(",");
		
//		long start_time = System.currentTimeMillis();
		System.out.println("================FileName :"+ file.getName()+"===========================");
		for(String query : queryString) {
			System.out.println("---"+query+"---");
			Object object = JsonPath.read(json, query);
			if(object instanceof List){
				List<Object> objList = (List<Object>)object;
				for(Object obj : objList){
					System.out.println(obj);
				}
			}else{
				System.out.println(object);
			}
//			System.out.println(JsonPath.read(json, query));	
		}
//		long end_time = System.currentTimeMillis();
//		System.out.println("Total Time = " + (end_time-start_time));
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == clearTabs) {
			tabbedPaneController.clearAll();
		}
	}
}