package com.jrout.jsonviewer;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_END;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_HOME;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_SHIFT;
import static java.awt.event.KeyEvent.VK_UP;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.text.JTextComponent;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

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
	static Integer ignoreArray[] = {VK_A, VK_ESCAPE, VK_UP, VK_DOWN, VK_ENTER, VK_HOME, VK_SHIFT, VK_CONTROL, VK_LEFT,
			VK_RIGHT, VK_END};
	public static List<Integer> ignoreKeyCodes = new ArrayList<Integer>();
	private String rootFolderPath = null;
	static {
		ignoreKeyCodes = Arrays.asList(ignoreArray);
	}

	public JSONViewer() {
		init();
		setIcon();
		// rootFolderToSearch();
	}

	public void rootFolderToSearch() {
		JLabel jsonRootFolderLabel = new JLabel("Enter Root Folder for Searching the json File:");
		JTextField jsonRootFolderField = new JTextField(10);
		Object[] array = {jsonRootFolderLabel, jsonRootFolderField};
		int res = JOptionPane.showConfirmDialog(null, array, "", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (res == JOptionPane.OK_OPTION) {
			rootFolderPath = jsonRootFolderField.getText();
			if (!new File(rootFolderPath).isDirectory()) {
				JOptionPane.showMessageDialog(null, new String[]{"Root Folder is not correct Add again"}, "Error",
						JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
	}

	public void init() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		createMenuBar();
		JTabbedPane jTabbedPane = new JTabbedPane();
		jTabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		JPanel jPanelMain = new JPanel(new BorderLayout());

		tabbedPaneController = new TabbedPaneController(jTabbedPane, jPanelMain);

		JPanel jPanelLower = new JPanel(new BorderLayout());
		jPanelLower.setSize(new Dimension(200, 200));

		JPanel searchPanel = new JPanel(new BorderLayout());

		clearTabs = new JButton("Clear Tabs");
		clearTabs.addActionListener(this);
		jComboBoxQuery = new JComboBox();

		jComboBoxQuery.setEditable(true);
		jComboBoxQuery.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				// System.out.println(" Key Char ="+e.getKeyChar() +
				// " Key Code = "+e.getKeyCode());
				int tempCaretPosition = 0;
				ComboBoxEditor editor = jComboBoxQuery.getEditor();
				JTextField textField = (JTextField) editor.getEditorComponent();
				tempCaretPosition = textField.getCaretPosition();
				if (ignoreKeyCodes.contains(e.getKeyCode())) {
					// DO NOTHING
				} else {
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
					((JTextComponent) e.getSource()).setCaretPosition(a.length());

					jComboBoxQuery.hidePopup();
					if (counter != 0) {
						jComboBoxQuery.showPopup();
					}
					textField.setCaretPosition(tempCaretPosition);
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

		final RSyntaxTextArea consoleTextArea = new RSyntaxTextArea(20, 60);
		consoleTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
		consoleTextArea.setCodeFoldingEnabled(true);
		RTextScrollPane jsonTextScrollPane = new RTextScrollPane(consoleTextArea);

		PrintStream printStream = new PrintStream(new CustomOutputStream(consoleTextArea));

		System.setOut(printStream);
		System.setErr(printStream);

		JPanel outputPanel = new JPanel();
		outputPanel.setLayout(new BorderLayout());
		outputPanel.add(jsonTextScrollPane, BorderLayout.CENTER);

		queryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jComboBoxQuery.getSelectedItem() != null) {
					final String queryString = jComboBoxQuery.getSelectedItem().toString().replaceAll("\\s", "");
					if (!treeSet.contains(queryString)) {
						treeSet.add(queryString);
					}
					queryJSON(queryString);
				}
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

		setTitle("Welcome to JSON Query");
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/**
	 * Creates the menubar and adds the action listener to it.
	 */
	private DefaultListModel filesModel;
	private JList jList;

	public void createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(helpMenu);

		JMenuItem aboutItem = new JMenuItem("About");
		aboutItem.setMnemonic(KeyEvent.VK_A);
		helpMenu.add(aboutItem);

		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JOptionPane.showMessageDialog(null,
						"Copyrights \u00a9 2016 By Jayram Rout. \n Email : jayram.rout@7chapters.info",
						"About Json Tool", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		JMenuItem openItem = new JMenuItem("Open");
		openItem.setMnemonic(KeyEvent.VK_O);
		openItem.setAccelerator(KeyStroke.getKeyStroke('O', CTRL_DOWN_MASK));

		JMenuItem quitM = new JMenuItem("Quit", KeyEvent.VK_Q);

		fileMenu.add(openItem);
		fileMenu.add(quitM);

		JMenu optionMenu = new JMenu("Option");
		optionMenu.setMnemonic(KeyEvent.VK_O);
		JMenuItem openResource = new JMenuItem("Open Resource", KeyEvent.VK_O);
		openResource.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));

		optionMenu.add(openResource);

		openResource.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				filesModel = new DefaultListModel();
				jList = new JList(filesModel);
				jList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

				JPanel searchPanelResource = new JPanel(new BorderLayout());
				final JTextArea resourceQuery = new JTextArea();
				searchPanelResource.add(resourceQuery, BorderLayout.CENTER);
				searchPanelResource.setBorder(BorderFactory.createLineBorder(Color.black));
				resourceQuery.addKeyListener(new KeyListener() {

					@Override
					public void keyTyped(KeyEvent paramKeyEvent) {
						filesModel.removeAllElements();

						Path dir = FileSystems.getDefault().getPath(rootFolderPath);
						List<String> fileList = new ArrayList<String>();
						List<String> fileNames = Helper.getFileNames(fileList, dir, resourceQuery.getText());
						for (String filePath : fileNames) {
							filesModel.addElement(filePath);
						}
					}
					@Override
					public void keyReleased(KeyEvent paramKeyEvent) {
					}
					@Override
					public void keyPressed(KeyEvent paramKeyEvent) {
					}
				});

				JPanel jPanelLower = new JPanel(new BorderLayout());
				jPanelLower.setSize(new Dimension(200, 200));
				jPanelLower.add(jList, BorderLayout.CENTER);
				jPanelLower.setBorder(BorderFactory.createLineBorder(Color.black));

				final JDialog dlg = new JDialog(JSONViewer.this, "Resource Dialog", true);
				dlg.add(BorderLayout.NORTH, searchPanelResource);
				dlg.add(BorderLayout.CENTER, jPanelLower);

				dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dlg.setSize(400, 300);
				dlg.setLocationRelativeTo(JSONViewer.this);
				dlg.setVisible(true);

			}
		});
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				FileDialog fd = new FileDialog(JSONViewer.this, "Select File", FileDialog.LOAD);
				fd.setMultipleMode(true);
				fd.show();
				String filePath = null;
				if (fd.getFiles() != null) {
					for (File file : fd.getFiles()) {
						filePath = file.getAbsolutePath();
						setTitle(file.getName());
						try {
							tabbedPaneController.addTab(filePath);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		});

		quitM.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
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
		String[] queryString = nodeStr.split(",");
		System.out.println("== " + fileName.substring(fileName.lastIndexOf("\\") + 1) + " ==");
		for (String query : queryString) {
			if (query != null && query.length() > 0) {
				System.out.println("\t\"" + query + "\":");
				Object object = readJsonPath(new Helper().getJSONString(fileName), query);
				if (object != null) {
					if (object instanceof List) {
						List<Object> objList = (List<Object>) object;
						int idx = 0;
						for (Object obj : objList) {
							System.out.println("\t\t" + "[" + idx++ + "]" + obj);
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