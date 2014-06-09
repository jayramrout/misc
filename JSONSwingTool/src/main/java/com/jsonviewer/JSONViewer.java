package com.jsonviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
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
import javax.swing.text.JTextComponent;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

/**
 * @author Jayram Rout
 *
 */
public class JSONViewer extends JFrame implements ActionListener {
	// private static final Logger log = Logger.getLogger(JSONViewer.class);

	private TabbedPaneController tabbedPaneController;
	private JButton clearTabs;
	private JComboBox jComboBoxQuery;
	private JButton queryButton;
	public static Set<String> treeSet = new TreeSet<String>();

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
		jComboBoxQuery = new JComboBox();

		jComboBoxQuery.setEditable(true);
		jComboBoxQuery.getEditor().getEditorComponent()
				.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						if (e.getKeyCode() != 38 && e.getKeyCode() != 40
								&& e.getKeyCode() != 10 && e.getKeyCode() != 36
								&& e.getKeyCode() != 16 && e.getKeyCode() != 17
								&& e.getKeyCode() != 37 && e.getKeyCode() != 39
								&& e.getKeyCode() != 35) {
							String a = jComboBoxQuery.getEditor().getItem().toString();
							jComboBoxQuery.removeAllItems();

							int counter = 0;
							jComboBoxQuery.addItem("");
							for (String keys : treeSet) {
								if (keys.toLowerCase().contains(a.toLowerCase())) {
									jComboBoxQuery.addItem(keys);
									counter++;
								}
							}
							jComboBoxQuery.getEditor().setItem(new String(a));
							((JTextComponent)e.getSource()).setCaretPosition(a.length());
							
							jComboBoxQuery.hidePopup();
							if (counter != 0) {
								jComboBoxQuery.showPopup();
							}
						}
					}
				});
		Font font = new Font("Courier", Font.PLAIN, 13);
		jComboBoxQuery.setFont(font);
		jComboBoxQuery.setForeground(Color.BLUE);

		queryButton = new JButton("Query");

		searchPanel.add(clearTabs, BorderLayout.WEST);
		searchPanel.add(jComboBoxQuery, BorderLayout.CENTER);
		searchPanel.add(queryButton, BorderLayout.EAST);

		final JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem clearItem = new JMenuItem("Clear Text");
		JMenuItem copyItem = new JMenuItem("Copy");
		JMenuItem cutItem = new JMenuItem("Cut");

		popupMenu.add(clearItem);
		popupMenu.add(copyItem);
		popupMenu.add(cutItem);
		final JTextArea consoleTextArea = new JTextArea();

		// m_searchText.setComponentPopupMenu(popupMenu);
		consoleTextArea.setComponentPopupMenu(popupMenu);

		clearItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				consoleTextArea.setText("");
			}
		});

		copyItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				consoleTextArea.copy();
			}
		});

		cutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				consoleTextArea.cut();
			}
		});

		consoleTextArea.setForeground(Color.RED);
		// consoleTextArea.setEditable(false);
		JScrollPane consoleScrollPane = new JScrollPane(consoleTextArea);

		PrintStream printStream = new PrintStream(new CustomOutputStream(
				consoleTextArea));

		System.setOut(printStream);
		System.setErr(printStream);

		JPanel outputPanel = new JPanel();
		outputPanel.setLayout(new BorderLayout());
		outputPanel.add(consoleScrollPane, BorderLayout.CENTER);

		queryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final String queryString = jComboBoxQuery.getSelectedItem()
						.toString().replaceAll("\\s", "");

				/*
				 * if (model.getIndexOf(queryString) == -1) {
				 * model.addElement(queryString); }
				 */
				if (!treeSet.contains(queryString)) {
					treeSet.add(queryString);
				}
				queryJSON(queryString);
			}
		});
		getRootPane().setDefaultButton(queryButton);
		JSplitPane lowerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				searchPanel, outputPanel);
		lowerSplitPane.setDividerSize(0);
		jPanelLower.add(lowerSplitPane);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				jPanelMain, jPanelLower);
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
		String fileName = tabbedPaneController.jTabbedPane
				.getToolTipTextAt(selectedIndex);
		String[] queryString = nodeStr.split(",");
		System.out.println("== " + fileName.substring(fileName.lastIndexOf("\\")+1) + " ==");
		for (String query : queryString) {
			if (query != null && query.length() > 0) {
				System.out.println("\t" + query + ":");
				Object object = readJsonPath(new Helper().getJSONString(fileName), query);// JsonPath.read(json,
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