package com.jsonviewer;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_END;
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
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
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
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
	static Integer ignoreArray[] = {VK_A, VK_ESCAPE, VK_UP, VK_DOWN, 10, VK_HOME, VK_SHIFT, VK_CONTROL, VK_LEFT, VK_RIGHT, VK_END};
	public static List<Integer> ignoreKeyCodes = new ArrayList<Integer>();

	static {
		ignoreKeyCodes = Arrays.asList(ignoreArray);
	}
	public JSONViewer() {
		init();
		setIcon();
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
		// searchPanel.setBorder(BorderFactory.createEtchedBorder());

		clearTabs = new JButton("Clear Tabs");
		clearTabs.addActionListener(this);
		jComboBoxQuery = new JComboBox();

		jComboBoxQuery.setEditable(true);
		jComboBoxQuery.getEditor().getEditorComponent()
				.addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
//						System.out.println(" Key Char ="+e.getKeyChar() + " Key Code = "+e.getKeyCode());
						int tempCaretPosition = 0;
						ComboBoxEditor editor = jComboBoxQuery.getEditor();
						JTextField textField = (JTextField )editor.getEditorComponent();
						tempCaretPosition = textField.getCaretPosition();
						if(ignoreKeyCodes.contains(e.getKeyCode())){
							// DO NOTHING
						}else {
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

		/*final JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem clearItem = new JMenuItem("Clear Text");
		JMenuItem copyItem = new JMenuItem("Copy");
		JMenuItem cutItem = new JMenuItem("Cut");

		popupMenu.add(clearItem);
		popupMenu.add(copyItem);
		popupMenu.add(cutItem);*/
		
		
		final RSyntaxTextArea consoleTextArea = new RSyntaxTextArea(20, 60);
		consoleTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
		consoleTextArea.setCodeFoldingEnabled(true);
		RTextScrollPane jsonTextScrollPane = new RTextScrollPane(consoleTextArea);
		
		PrintStream printStream = new PrintStream(new CustomOutputStream(
				consoleTextArea));

		System.setOut(printStream);
		System.setErr(printStream);

		JPanel outputPanel = new JPanel();
		outputPanel.setLayout(new BorderLayout());
		outputPanel.add(jsonTextScrollPane, BorderLayout.CENTER);

		queryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final String queryString = jComboBoxQuery.getSelectedItem()
						.toString().replaceAll("\\s", "");

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
	 * Creates the menubar and adds the action listener to it.
	 */
	private DefaultListModel philosophers;
	private JList list;

	public void createMenuBar(){
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);
		
		JMenuItem openItem = new JMenuItem("Open");
		openItem.setMnemonic(KeyEvent.VK_O);
		openItem.setAccelerator(KeyStroke.getKeyStroke('O', CTRL_DOWN_MASK));

		JMenuItem quitM = new JMenuItem("Quit", KeyEvent.VK_Q);

		fileMenu.add(openItem);
		fileMenu.add(quitM);
		
		JMenu optionMenu = new JMenu("Option");
		optionMenu.setMnemonic(KeyEvent.VK_O);
		JMenuItem openResource = new JMenuItem("Open Resource", KeyEvent.VK_O);
		openResource.setAccelerator( 
			    KeyStroke.getKeyStroke(
			       KeyEvent.VK_R, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
		
		optionMenu.add(openResource);
		menuBar.add(optionMenu);
//		private final DefaultListModel philosophers;
//		private final JList list;

		openResource.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				philosophers = new DefaultListModel();
				philosophers.addElement("Socrates");
				philosophers.addElement("Plato");
				philosophers.addElement("Aristotle");
				philosophers.addElement("St. Thomas Aquinas");
				philosophers.addElement("Soren Kierkegaard");
				philosophers.addElement("Immanuel Kant");
				philosophers.addElement("Friedrich Nietzsche");
				philosophers.addElement("Hannah Arendt");
				list = new JList(philosophers);
				list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				
				JPanel searchPanelResource = new JPanel(new BorderLayout());
				JTextArea  resourceQuery = new JTextArea("Query in Progress...");
				searchPanelResource.add(resourceQuery, BorderLayout.CENTER);
				searchPanelResource.setBorder(BorderFactory.createLineBorder(Color.black));
				
				
				JPanel jPanelLower = new JPanel(new BorderLayout());
				jPanelLower.setSize(new Dimension(200, 200));
				jPanelLower.add(list,BorderLayout.CENTER);
				jPanelLower.setBorder(BorderFactory.createLineBorder(Color.black));
				
				final JDialog dlg = new JDialog(JSONViewer.this, "Resource Dialog", true);
				dlg.add(BorderLayout.NORTH, searchPanelResource);
				dlg.add(BorderLayout.CENTER, jPanelLower);
				
				dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dlg.setSize(400, 300);
				dlg.setLocationRelativeTo(JSONViewer.this);
				dlg.setVisible(true);
				
				/*final Thread t1 = new Thread(new Runnable() {
					public void run() {
						dlg.setVisible(true);
					}
				});
				t1.start();*/

				
			}
		});
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				FileDialog fd = new FileDialog(JSONViewer.this, "Select File", FileDialog.LOAD);
				fd.show();
				String filePath = null;
				if (fd.getFile() != null) {
					filePath = fd.getDirectory() + fd.getFile();
					setTitle(fd.getFile());
					try {
						tabbedPaneController.addTab(filePath);
					} catch (Exception e) {
						e.printStackTrace();
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
		String fileName = tabbedPaneController.jTabbedPane
				.getToolTipTextAt(selectedIndex);
		String[] queryString = nodeStr.split(",");
		System.out.println("== " + fileName.substring(fileName.lastIndexOf("\\")+1) + " ==");
		for (String query : queryString) {
			if (query != null && query.length() > 0) {
				System.out.println("\t\"" + query + "\":");
				Object object = readJsonPath(new Helper().getJSONString(fileName), query);
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