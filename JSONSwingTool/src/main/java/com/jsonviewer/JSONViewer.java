package com.jsonviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

/**
 * @author Jayram Rout
 *
 */
public class JSONViewer extends JFrame implements ActionListener {
	// private static final Logger log = Logger.getLogger(JSONViewer.class);

	TabbedPaneController tabbedPaneController;
	DefaultMutableTreeNode root_defaultMutableTreeNode;
	private DefaultTreeModel m_model;
	JTree m_tree;
	JButton clearTabs;
	// private JTextField m_searchText;
	JComboBox m_searchText;
	private JButton queryButton;
	String[] patternExamples = {"currentBudgetData.agElMisc.agReason", "store"};

	public JSONViewer() {
		init();
		setIcon();
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
		// searchPanel.setBorder(BorderFactory.createEtchedBorder());

		clearTabs = new JButton("Clear Tabs");
		clearTabs.addActionListener(this);
		final SortedComboBoxModel model = new SortedComboBoxModel(new String[]{"currentBudgetData.agElMisc.agReason"});
		m_searchText = new JComboBox(model);
		m_searchText.setEditable(true);
		// m_searchText.addActionListener(this);

		// m_searchText = new JTextField(70);
		Font font = new Font("Courier", Font.PLAIN, 13);
		m_searchText.setFont(font);
		m_searchText.setForeground(Color.BLUE);

		queryButton = new JButton("Query");

		searchPanel.add(clearTabs, BorderLayout.WEST);
		searchPanel.add(m_searchText, BorderLayout.CENTER);
		searchPanel.add(queryButton, BorderLayout.EAST);

		final JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem clearItem = new JMenuItem("Clear Text");
		popupMenu.add(clearItem);

		final JTextArea consoleTextArea = new JTextArea();

		consoleTextArea.setComponentPopupMenu(popupMenu);
		clearItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				consoleTextArea.setText("");
			}
		});

		consoleTextArea.setForeground(Color.RED);
		// consoleTextArea.setEditable(false);
		JScrollPane consoleScrollPane = new JScrollPane(consoleTextArea);

		PrintStream printStream = new PrintStream(new CustomOutputStream(consoleTextArea));

		System.setOut(printStream);
		System.setErr(printStream);

		JPanel outputPanel = new JPanel();
		outputPanel.setLayout(new BorderLayout());
		outputPanel.add(consoleScrollPane, BorderLayout.CENTER);

		queryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (model.getIndexOf(m_searchText.getSelectedItem().toString().trim()) == -1) {
					model.addElement(m_searchText.getSelectedItem().toString().trim());
				}
				queryJSON(m_searchText.getSelectedItem().toString());
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

		setTitle("Welcome " + hostname + " JSON Query");

		setLocationRelativeTo(null);
		setVisible(true);
	}
	/**
	 * 
	 */
	private void setIcon() {
		Image img = null;
		try {
			img = ImageIO.read(getClass().getResource("/json.png"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		setIconImage(img);
	}
	/**
	 * Query JSON String
	 * 
	 * @param nodeStr
	 */
	private void queryJSON(String nodeStr) {
		int selectedIndex = tabbedPaneController.jTabbedPane.getSelectedIndex();
		String fileName = tabbedPaneController.jTabbedPane.getToolTipTextAt(selectedIndex);
		StringBuilder jsonBuilder = new StringBuilder();

		Scanner scanner = null;
		File file = null;
		FileReader in = null;
		BufferedReader br = null;
		try {
			if (fileName == JSONConstants.DEFAULT) {
				file = new File(fileName);
				br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/StoreJSON.txt")));
			} else {
				file = new File(fileName);
				in = new FileReader(file);
				br = new BufferedReader(in);
			}
			try {
				String line;
				while ((line = br.readLine()) != null) {
					jsonBuilder.append(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if (scanner != null)
					scanner.close();

				if (in != null) {
					in.close();
				}
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String[] queryString = nodeStr.split(",");
		System.out.println("== " + file.getName() + " ==");
		for (String query : queryString) {
			if (query != null && query.length() > 0) {
				System.out.println("\t" + query + ":");
				Object object = readJsonPath(jsonBuilder.toString(), query);// JsonPath.read(json,
																			// query);
				if (object != null) {
					if (object instanceof List) {
						List<Object> objList = (List<Object>) object;
						for (Object obj : objList) {
							System.out.println("\t\t" + obj);
						}
					} else {
						System.out.println("\t\t" + object);
					}
				} else {
					System.out.println("\t\t" + object);
				}
			} else {
				System.out.println("\t\tEnter a Query String");
			}
		}
	}
	/**
	 * 
	 * @param json
	 * @param query
	 * @return
	 */
	public Object readJsonPath(String json, String query) {

		Object obj = null;
		try {
			// log.debug(json);
			obj = JsonPath.read(json, query);
		} catch (PathNotFoundException pnfe) {
			System.err.println("\t\tPath Not Found : " + query);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == clearTabs) {
			tabbedPaneController.clearAll();
		}
	}
}